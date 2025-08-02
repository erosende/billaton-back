package dev.erosende.secadapter.repository.db;

import dev.erosende.billaton.application.domain.model.IdentificationTypeDto;
import dev.erosende.billaton.application.domain.ports.secondary.db.IdentificationTypesRepository;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class IdentificationTypesRepositoryImpl implements IdentificationTypesRepository {

  private static final String FIND_ALL_IDENTIFICATION_TYPES_SQL = """
      SELECT identification_type_id, name
      FROM identification_type
      """;

  private final NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public List<IdentificationTypeDto> findAllIdentificationTypes() {
    return jdbcTemplate.query(FIND_ALL_IDENTIFICATION_TYPES_SQL, new BeanPropertyRowMapper<>(IdentificationTypeDto.class));
  }

}
