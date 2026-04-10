-- docs/sql/verifactu_schema.sql
-- VeriFactu schema changes — run against PostgreSQL

-- 1. New columns on document table
ALTER TABLE document ADD COLUMN tipo_factura VARCHAR(2) NOT NULL DEFAULT 'F1';
ALTER TABLE document ADD COLUMN descripcion_operacion VARCHAR(500) NOT NULL DEFAULT '';
ALTER TABLE document ADD COLUMN factura_rectificada_id INTEGER REFERENCES document(document_id);
ALTER TABLE document ADD COLUMN tipo_rectificativa VARCHAR(1);

-- 2. Invoice series for automatic sequential numbering
CREATE TABLE invoice_series (
    series_id SERIAL PRIMARY KEY,
    issuer_id INTEGER NOT NULL REFERENCES participant(participant_id),
    prefix VARCHAR(10) NOT NULL,
    year INTEGER NOT NULL,
    last_number INTEGER NOT NULL DEFAULT 0,
    UNIQUE (issuer_id, prefix, year)
);

-- 3. VeriFactu record queue + hash chain
CREATE TABLE verifactu_record (
    verifactu_record_id SERIAL PRIMARY KEY,
    document_id INTEGER NOT NULL REFERENCES document(document_id),
    issuer_nif VARCHAR(9) NOT NULL,
    tipo_registro VARCHAR(10) NOT NULL,
    num_serie_factura VARCHAR(60) NOT NULL,
    fecha_expedicion DATE NOT NULL,
    tipo_factura VARCHAR(2) NOT NULL,
    importe_total DECIMAL(12,2) NOT NULL,
    cuota_total DECIMAL(12,2) NOT NULL,
    huella VARCHAR(64) NOT NULL,
    huella_anterior VARCHAR(64),
    fecha_hora_gen_registro TIMESTAMPTZ NOT NULL,
    xml_enviado TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    csv_aeat VARCHAR(16),
    error_message TEXT,
    retry_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    sent_at TIMESTAMPTZ,
    user_id UUID NOT NULL
);

CREATE INDEX idx_verifactu_record_status ON verifactu_record(status) WHERE status IN ('PENDING', 'ERROR');
CREATE INDEX idx_verifactu_record_issuer_nif ON verifactu_record(issuer_nif, created_at);

-- 4. New column on issuer_config
ALTER TABLE issuer_config ADD COLUMN clave_regimen VARCHAR(2) NOT NULL DEFAULT '01';
