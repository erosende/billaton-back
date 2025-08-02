package dev.erosende.secadapter.repository.db;

import dev.erosende.billaton.application.domain.model.IssuerConfigDto;
import dev.erosende.billaton.application.domain.ports.secondary.db.IssuerConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class IssuerConfigRepositoryImpl implements IssuerConfigRepository {

  private static final String FIND_ISSUER_CONFIG_SQL = """
      SELECT
          issuer_config_id,
          issuer_id,
          vat,
          payment_account_number,
          logo_path
      FROM issuer_config
      WHERE issuer_id = :participantId
      """;

  private static final String UPDATE_ISSUER_CONFIG_SQL = """
      UPDATE issuer_config
      SET
        vat = :vat,
        payment_account_number = :paymentAccountNumber
      WHERE issuer_config_id = :issuerConfigId AND issuer_id = :issuerId
      """;

  private final NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public Optional<IssuerConfigDto> findIssuerConfig(Integer participantId) {
    Optional<IssuerConfigDto> result;
    try {
      SqlParameterSource params = new MapSqlParameterSource("participantId", participantId);
      result = Optional.ofNullable(jdbcTemplate.queryForObject(
          FIND_ISSUER_CONFIG_SQL,
          params,
          new BeanPropertyRowMapper<>(IssuerConfigDto.class)
      ));
    } catch (EmptyResultDataAccessException erdae) {
      result = Optional.empty();
    }

    return result;
  }

  @Override
  public Integer updateIssuerConfig(IssuerConfigDto issuerConfig) {
    SqlParameterSource params = new MapSqlParameterSource()
        .addValue("issuerConfigId", issuerConfig.getIssuerConfigId())
        .addValue("issuerId", issuerConfig.getIssuerId())
        .addValue("vat", issuerConfig.getVat())
        .addValue("paymentAccountNumber", issuerConfig.getPaymentAccountNumber());
    return jdbcTemplate.update(UPDATE_ISSUER_CONFIG_SQL, params);
  }

}
