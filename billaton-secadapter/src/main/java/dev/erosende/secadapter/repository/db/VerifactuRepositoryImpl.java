package dev.erosende.secadapter.repository.db;

import dev.erosende.billaton.application.domain.model.VerifactuRecordDto;
import dev.erosende.billaton.application.domain.ports.secondary.db.VerifactuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class VerifactuRepositoryImpl implements VerifactuRepository {

    private static final String SAVE_SQL = """
            INSERT INTO verifactu_record (
                document_id, issuer_nif, tipo_registro, num_serie_factura,
                fecha_expedicion, tipo_factura, importe_total, cuota_total,
                huella, huella_anterior, fecha_hora_gen_registro, status, user_id
            ) VALUES (
                :documentId, :issuerNif, :tipoRegistro, :numSerieFactura,
                :fechaExpedicion, :tipoFactura, :importeTotal, :cuotaTotal,
                :huella, :huellaAnterior, :fechaHoraGenRegistro, :status, :userId
            )
            """;

    private static final String FIND_BY_DOCUMENT_SQL = """
            SELECT * FROM verifactu_record WHERE document_id = :documentId
            ORDER BY created_at DESC LIMIT 1
            """;

    private static final String FIND_LAST_HUELLA_SQL = """
            SELECT huella FROM verifactu_record
            WHERE issuer_nif = :issuerNif
            ORDER BY created_at DESC LIMIT 1
            """;

    private static final String FIND_DISTINCT_NIFS_SQL = """
            SELECT DISTINCT issuer_nif FROM verifactu_record
            WHERE status IN ('PENDING', 'ERROR') AND retry_count < 5
            """;

    private static final String FIND_AND_LOCK_SQL = """
            SELECT pg_advisory_xact_lock(abs(hashtext(:issuerNif)))
            """;

    private static final String FIND_PENDING_BY_NIF_SQL = """
            SELECT * FROM verifactu_record
            WHERE issuer_nif = :issuerNif AND status IN ('PENDING', 'ERROR') AND retry_count < 5
            ORDER BY created_at ASC LIMIT 50
            """;

    private static final String MARK_SENT_SQL = """
            UPDATE verifactu_record
            SET status = 'SENT', csv_aeat = :csv, xml_enviado = :xmlEnviado, sent_at = now()
            WHERE verifactu_record_id = :id
            """;

    private static final String MARK_ERROR_SQL = """
            UPDATE verifactu_record
            SET status = 'ERROR', error_message = :errorMessage, retry_count = retry_count + 1
            WHERE verifactu_record_id = :id
            """;

    private static final String MARK_CANCELLED_SQL = """
            UPDATE verifactu_record SET status = 'CANCELLED' WHERE verifactu_record_id = :id
            """;

    private static final String RESET_RETRY_SQL = """
            UPDATE verifactu_record SET status = 'PENDING', retry_count = 0, error_message = NULL
            WHERE verifactu_record_id = :id
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Integer save(VerifactuRecordDto record) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("documentId", record.getDocumentId())
                .addValue("issuerNif", record.getIssuerNif())
                .addValue("tipoRegistro", record.getTipoRegistro())
                .addValue("numSerieFactura", record.getNumSerieFactura())
                .addValue("fechaExpedicion", record.getFechaExpedicion())
                .addValue("tipoFactura", record.getTipoFactura())
                .addValue("importeTotal", record.getImporteTotal())
                .addValue("cuotaTotal", record.getCuotaTotal())
                .addValue("huella", record.getHuella())
                .addValue("huellaAnterior", record.getHuellaAnterior())
                .addValue("fechaHoraGenRegistro", record.getFechaHoraGenRegistro())
                .addValue("status", record.getStatus())
                .addValue("userId", record.getUserId() != null ? UUID.fromString(record.getUserId()) : null);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(SAVE_SQL, params, keyHolder, new String[]{"verifactu_record_id"});
        return keyHolder.getKey().intValue();
    }

    @Override
    public Optional<VerifactuRecordDto> findByDocumentId(Integer documentId) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource("documentId", documentId);
            VerifactuRecordDto result = jdbcTemplate.queryForObject(FIND_BY_DOCUMENT_SQL, params, this::mapRow);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> findLastHuellaByIssuerNif(String issuerNif) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource("issuerNif", issuerNif);
            return Optional.ofNullable(jdbcTemplate.queryForObject(FIND_LAST_HUELLA_SQL, params, String.class));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<String> findDistinctIssuerNifsWithPendingRecords() {
        return jdbcTemplate.queryForList(FIND_DISTINCT_NIFS_SQL, new MapSqlParameterSource(), String.class);
    }

    @Override
    public List<VerifactuRecordDto> findAndLockPendingByIssuerNif(String issuerNif) {
        MapSqlParameterSource params = new MapSqlParameterSource("issuerNif", issuerNif);
        jdbcTemplate.queryForObject(FIND_AND_LOCK_SQL, params, Void.class);
        return jdbcTemplate.query(FIND_PENDING_BY_NIF_SQL, params, this::mapRow);
    }

    @Override
    public void markAsSent(Integer verifactuRecordId, String csv, String xmlEnviado) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", verifactuRecordId)
                .addValue("csv", csv)
                .addValue("xmlEnviado", xmlEnviado);
        jdbcTemplate.update(MARK_SENT_SQL, params);
    }

    @Override
    public void markAsError(Integer verifactuRecordId, String errorMessage) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", verifactuRecordId)
                .addValue("errorMessage", errorMessage);
        jdbcTemplate.update(MARK_ERROR_SQL, params);
    }

    @Override
    public void markAsCancelled(Integer verifactuRecordId) {
        jdbcTemplate.update(MARK_CANCELLED_SQL, new MapSqlParameterSource("id", verifactuRecordId));
    }

    @Override
    public void resetForRetry(Integer verifactuRecordId) {
        jdbcTemplate.update(RESET_RETRY_SQL, new MapSqlParameterSource("id", verifactuRecordId));
    }

    private VerifactuRecordDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        return VerifactuRecordDto.builder()
                .verifactuRecordId(rs.getInt("verifactu_record_id"))
                .documentId(rs.getInt("document_id"))
                .issuerNif(rs.getString("issuer_nif"))
                .tipoRegistro(rs.getString("tipo_registro"))
                .numSerieFactura(rs.getString("num_serie_factura"))
                .fechaExpedicion(rs.getDate("fecha_expedicion").toLocalDate())
                .tipoFactura(rs.getString("tipo_factura"))
                .importeTotal(rs.getBigDecimal("importe_total"))
                .cuotaTotal(rs.getBigDecimal("cuota_total"))
                .huella(rs.getString("huella"))
                .huellaAnterior(rs.getString("huella_anterior"))
                .fechaHoraGenRegistro(rs.getObject("fecha_hora_gen_registro", OffsetDateTime.class))
                .status(rs.getString("status"))
                .csvAeat(rs.getString("csv_aeat"))
                .errorMessage(rs.getString("error_message"))
                .retryCount(rs.getInt("retry_count"))
                .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                .sentAt(rs.getObject("sent_at", OffsetDateTime.class))
                .userId(rs.getString("user_id"))
                .build();
    }
}
