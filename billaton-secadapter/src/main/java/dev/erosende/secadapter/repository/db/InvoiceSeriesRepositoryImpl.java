package dev.erosende.secadapter.repository.db;

import dev.erosende.billaton.application.domain.ports.secondary.db.InvoiceSeriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InvoiceSeriesRepositoryImpl implements InvoiceSeriesRepository {

    private static final String SELECT_FOR_UPDATE_SQL = """
            SELECT last_number FROM invoice_series
            WHERE issuer_id = :issuerId AND prefix = :prefix AND year = :year
            FOR UPDATE
            """;

    private static final String UPDATE_LAST_NUMBER_SQL = """
            UPDATE invoice_series SET last_number = :nextNumber
            WHERE issuer_id = :issuerId AND prefix = :prefix AND year = :year
            """;

    private static final String INSERT_SERIES_SQL = """
            INSERT INTO invoice_series (issuer_id, prefix, year, last_number)
            VALUES (:issuerId, :prefix, :year, 1)
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public String getNextDocumentCode(Integer issuerId, String prefix, int year) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("issuerId", issuerId)
                .addValue("prefix", prefix)
                .addValue("year", year);

        int nextNumber;
        try {
            Integer lastNumber = jdbcTemplate.queryForObject(SELECT_FOR_UPDATE_SQL, params, Integer.class);
            nextNumber = lastNumber + 1;
            params.addValue("nextNumber", nextNumber);
            jdbcTemplate.update(UPDATE_LAST_NUMBER_SQL, params);
        } catch (EmptyResultDataAccessException e) {
            jdbcTemplate.update(INSERT_SERIES_SQL, params);
            nextNumber = 1;
        }

        return String.format("%s-%03d-%d", prefix, nextNumber, year);
    }
}
