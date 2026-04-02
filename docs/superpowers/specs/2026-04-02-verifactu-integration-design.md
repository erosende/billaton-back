# VeriFactu Integration Design

**Date:** 2026-04-02
**Status:** Approved
**Deadline:** Mandatory from 2027-01-01 (Impuesto sobre Sociedades) / 2027-07-01 (autónomos)

## Context

Billaton-back needs to comply with Spain's VeriFactu regulation (Royal Decree 1007/2023, Order HAC/1177/2024). The regulation requires all invoicing software to maintain an immutable hash-chained record of every invoice and — in VERI*FACTU mode — send those records to the AEAT in real time.

**Chosen mode:** VERI\*FACTU (automatic real-time submission to AEAT). No XML digital signature required per record; the hash chain replaces it.

**Certificate:** PFX file (FNMT personal autónomo certificate). Used for mutual TLS on the AEAT SOAP endpoint.

**Invoice types in scope:** F1 (normal full invoice), R1 and R4 (rectificative invoices). F2/F3 out of scope for now.

---

## Database Changes

### `document` — new columns
```sql
tipo_factura           VARCHAR(2)   NOT NULL DEFAULT 'F1'
descripcion_operacion  VARCHAR(500) NOT NULL DEFAULT ''
factura_rectificada_id INTEGER      NULL REFERENCES document(document_id)
tipo_rectificativa     VARCHAR(1)   NULL  -- 'S' sustitutiva | 'I' incremental
```

### New table `invoice_series` — automatic sequential numbering
```sql
series_id   SERIAL PRIMARY KEY
issuer_id   INTEGER NOT NULL REFERENCES participant(participant_id)
prefix      VARCHAR(10) NOT NULL   -- 'F' invoices, 'R' rectificatives
year        INTEGER NOT NULL
last_number INTEGER NOT NULL DEFAULT 0
UNIQUE (issuer_id, prefix, year)
```
Number format: `<prefix>-<zero-padded-3-digits>-<year>` e.g. `F-001-2025`.

### New table `verifactu_record` — submission queue + chain
```sql
verifactu_record_id     SERIAL PRIMARY KEY
document_id             INTEGER NOT NULL REFERENCES document(document_id)
issuer_nif              VARCHAR(9) NOT NULL
tipo_registro           VARCHAR(10) NOT NULL   -- 'ALTA' | 'ANULACION'
num_serie_factura       VARCHAR(60) NOT NULL
fecha_expedicion        DATE NOT NULL
tipo_factura            VARCHAR(2) NOT NULL
importe_total           DECIMAL(12,2) NOT NULL
cuota_total             DECIMAL(12,2) NOT NULL
huella                  VARCHAR(64) NOT NULL   -- SHA-256 of this record (uppercase hex)
huella_anterior         VARCHAR(64) NULL       -- huella of previous record, NULL for first ever
fecha_hora_gen_registro TIMESTAMPTZ NOT NULL
xml_enviado             TEXT NULL              -- full SOAP XML sent (audit trail)
status                  VARCHAR(20) NOT NULL DEFAULT 'PENDING'  -- PENDING|SENT|ERROR|ANNULLED
csv_aeat                VARCHAR(16) NULL       -- AEAT's Código Seguro de Verificación
error_message           TEXT NULL
retry_count             INTEGER NOT NULL DEFAULT 0
created_at              TIMESTAMPTZ NOT NULL DEFAULT now()
sent_at                 TIMESTAMPTZ NULL
user_id                 UUID NOT NULL
```

### `issuer_config` — new column
```sql
clave_regimen VARCHAR(2) NOT NULL DEFAULT '01'  -- 01=Régimen general IVA
```

---

## Architecture

Follows existing hexagonal architecture. New components per module:

### billaton-application (domain)

**Primary ports (use case interfaces):**
- `InvoiceNumberingUseCase` — `generateNextCode(issuerId, prefix, year) → String`
- `VerifactuUseCase` — `getStatus(documentId, userId)`, `retrySubmission(documentId, userId)`

**Secondary ports (repository interfaces):**
- `VerifactuRepository` — `save`, `findPendingOrError`, `findLastByIssuerNif`, `updateStatus`, `updateSent`
- `InvoiceSeriesRepository` — `getNextNumber(issuerId, prefix, year)` with row-level lock

**Secondary port (outbound external):**
- `VerifactuSoapPort` — `send(VerifactuRecordDto) → VerifactuSoapResponseDto`

**Domain services:**
- `HashChainService` — builds the AEAT concatenation string and computes SHA-256
- `VerifactuRecordBuilder` — constructs `VerifactuRecordDto` from a fully-loaded `DocumentDto`
- `QrCodeService` — builds the AEAT ValidarQR URL and generates a `BufferedImage` via ZXing
- `VerifactuSubmissionService` — scheduled worker orchestration (see flow below)

### billaton-secadapter (secondary adapters)
- `VerifactuRepositoryImpl` — JDBC on `verifactu_record`
- `InvoiceSeriesRepositoryImpl` — JDBC with `SELECT ... FOR UPDATE` on `invoice_series`
- `VerifactuSoapClient` — Apache CXF SOAP client; loads PFX via `SslConfig`; implements `VerifactuSoapPort`
- `SslConfig` — reads `VERIFACTU_CERTIFICATE_PATH` + `VERIFACTU_CERTIFICATE_PASSWORD`, builds `SSLContext`

### billaton-priadapter (primary adapters)
Two new endpoints on `DocumentController`:
- `GET  /billaton/documents/{id}/verifactu` → `VerifactuStatusResponseDto` (status, csv, error, sentAt)
- `POST /billaton/documents/{id}/verifactu/retry` → `BaseResponse<Void>` (resets status to PENDING)

### billaton-init (bootstrap)
- `VerifactuSchedulerConfig` — `@Scheduled(fixedDelay = 120_000)` calls `VerifactuSubmissionService.processQueue()`

---

## Core Flows

### Flow 1 — Invoice creation (single transaction)
1. Acquire `pg_advisory_xact_lock(hashCode(issuerNif))` — serializes all invoice creation for the same issuer, preventing concurrent hash chain corruption
2. `InvoiceSeriesRepositoryImpl.getNextNumber(issuerId, prefix, year)` — `SELECT FOR UPDATE` + increment
3. Persist `document` row (with `tipo_factura`, `descripcion_operacion`, auto-generated `documentCode`)
4. `HashChainService`: fetch last `huella` for this `issuer_nif` → compute new huella
5. Persist `verifactu_record` with `status=PENDING`
6. Return `documentId` to client — AEAT submission is async

All steps within one `@Transactional` boundary. The advisory lock is transaction-scoped and released automatically on commit/rollback. Rollback on any failure leaves no orphan records.

### Flow 2 — Scheduled worker (every 2 minutes)
1. `SELECT` records where `status IN ('PENDING','ERROR') AND retry_count < 5` ordered by `created_at ASC`
2. Group by `issuer_nif`; acquire `pg_advisory_xact_lock(hashCode(nif))` per group — guarantees serial processing per issuer, prevents hash chain corruption under concurrent requests
3. For each record:
   - Build SOAP XML (`RegistroAlta` or `RegistroAnulacion`)
   - Send via `VerifactuSoapClient` (mutual TLS with PFX)
   - On AEAT success → `status=SENT`, `csv_aeat`, `sent_at`, store `xml_enviado`
   - On AEAT error / network failure → `status=ERROR`, `error_message`, `retry_count++`
4. Advisory lock released at transaction end

After 5 failed retries the record stays in `ERROR` and requires manual retry via the API endpoint.

### Flow 3 — Rectificative invoice (R1/R4)
Request body additions:
```json
{
  "tipoFactura": "R1",
  "tipoRectificativa": "S",
  "facturaRectificadaId": 42,
  "descripcionOperacion": "Corrección de error material en factura F-001-2025"
}
```
- `documentCode` generated from series `R` (e.g. `R-001-2025`)
- `VerifactuRecordBuilder` reads `factura_rectificada_id` and populates `FacturasRectificadas` in the XML

### Flow 4 — Cancellation (soft delete)
`DELETE /billaton/documents/{id}` existing behavior unchanged, plus:
1. Mark `document.historical = true` (existing)
2. Insert `verifactu_record` with `tipo_registro=ANULACION`, `status=PENDING`
3. Worker processes it in the same queue as alta records

---

## Hash Chain Algorithm

Concatenation string for `RegistroAlta`:
```
IDEmisorFactura=<nif>&NumSerieFactura=<series>&FechaExpedicionFactura=<dd-mm-yyyy>&TipoFactura=<type>&CuotaTotal=<cuota>&ImporteTotal=<total>&Huella=<prev_huella>&FechaHoraHusoGenRegistro=<iso_datetime>
```

For `RegistroAnulacion` (fewer fields — no TipoFactura/CuotaTotal/ImporteTotal):
```
IDEmisorFactura=<nif>&NumSerieFactura=<series>&FechaExpedicionFactura=<dd-mm-yyyy>&Huella=<prev_huella>&FechaHoraHusoGenRegistro=<iso_datetime>
```

Rules:
- Encoding: UTF-8
- Algorithm: SHA-256
- Output: uppercase hex, 64 chars
- `FechaExpedicionFactura`: `dd-MM-yyyy` format (NOT ISO)
- `FechaHoraHusoGenRegistro`: ISO 8601 with timezone (e.g. `2025-04-15T10:30:00+02:00`)
- `Huella=` for the very first record of an issuer: empty string after the `=`
- Numeric values: no trailing decimal zeros (e.g. `1210.4` not `1210.40`)

---

## SOAP Endpoint

| Environment | URL |
|---|---|
| Pre-production | `https://prewww1.aeat.es/wlpl/TIKE-CONT/ws/SistemaFacturacion/VerifactuSOAP` |
| Production | `https://www1.agenciatributaria.gob.es/wlpl/TIKE-CONT/ws/SistemaFacturacion/VerifactuSOAP` |

Selected by `VERIFACTU_ENV=PRE|PRO`. Personal NIF certificate endpoints (not seal).

WSDL: `https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SistemaFacturacion.wsdl`

---

## PDF Changes

- Add dependency: `com.google.zxing:core` + `com.google.zxing:javase`
- `QrCodeService.generateQrImage(documentDto)` → `BufferedImage` (256×256 px)
  - URL: `https://www2.agenciatributaria.es/wlpl/TIKE-CONT/ValidarQR?nif=<nif>&numserie=<series>&fecha=<dd-mm-yyyy>&importe=<total>`
- Edit `crimson.jrxml` → recompile to `crimson.jasper`:
  - New image parameter `IMAGE_VERIFACTU_QR` (`java.awt.Image`) — top-right corner, min 30×30mm
  - New text `"VERI*FACTU"` — near QR
  - New text `"Verificable en la sede electrónica de la AEAT"` — below QR or footer
- `CrimsonReportMapper`: pass `IMAGE_VERIFACTU_QR` and QR URL to parameter map

QR is included even before AEAT confirmation (AEAT validates by NIF+serie+fecha+importe, not by CSV).

---

## New Environment Variables

```
VERIFACTU_CERTIFICATE_PATH       # absolute path to .pfx file on server
VERIFACTU_CERTIFICATE_PASSWORD   # PFX password
VERIFACTU_ENV                    # PRE or PRO
VERIFACTU_SISTEMA_ID             # 2-char code from AEAT software registration
VERIFACTU_SISTEMA_VERSION        # software version string, e.g. "1.0.0"
VERIFACTU_PRODUCTOR_NIF          # developer/producer NIF (autónomo NIF)
VERIFACTU_PRODUCTOR_NOMBRE       # developer/producer name
```

> `VERIFACTU_SISTEMA_ID` requires prior registration of the software at AEAT's sede electrónica. A placeholder value can be used in pre-production testing.

---

## Out of Scope

- F2 (simplified invoices / tickets)
- F3 (invoices replacing tickets)
- R2, R3, R5 rectificative types
- Non-VERI*FACTU mode (local-only chain with XAdES signature)
- TicketBAI (País Vasco / Navarra)
- Multi-issuer scenarios (single autónomo for now)
