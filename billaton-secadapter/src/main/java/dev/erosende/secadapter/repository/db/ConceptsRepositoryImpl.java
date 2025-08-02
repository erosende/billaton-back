package dev.erosende.secadapter.repository.db;

import dev.erosende.billaton.application.domain.model.ConceptDto;
import dev.erosende.billaton.application.domain.ports.secondary.db.ConceptsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ConceptsRepositoryImpl implements ConceptsRepository {

  private static final String FIND_CONCEPTS_SQL = """
      SELECT c.concept_id, c.description, c.amount, c.price_per_unit, c.document_id
      FROM concept c
      WHERE c.document_id = :documentId
      """;

  private static final String SAVE_CONCEPT_SQL = """
      INSERT INTO concept(description, amount, price_per_unit, document_id)
      VALUES (:description, :amount, :pricePerUnit, :documentId)
      """;

  private static final String UPDATE_CONCEPT_SQL = """
      UPDATE concept
      SET
        description = :description,
        amount = :amount,
        price_per_unit = :pricePerUnit
      WHERE concept_id = :conceptId AND document_id = :documentId
      """;

  private static final String DELETE_CONCEPTS_SQL = """
      DELETE FROM concept c
      WHERE c.document_id = :documentId AND c.concept_id = :conceptId
      """;

  private static final String DELETE_ALL_DOCUMENT_CONCEPTS_SQL = """
      DELETE FROM concept c
      WHERE c.document_id = :documentId
      """;

  private final NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public List<ConceptDto> findConcepts(Integer documentId) {
    SqlParameterSource params = new MapSqlParameterSource("documentId", documentId);
    return jdbcTemplate.query(FIND_CONCEPTS_SQL, params, new BeanPropertyRowMapper<>(ConceptDto.class));
  }

  @Override
  public Integer saveConcept(ConceptDto concept) {
    SqlParameterSource params = new MapSqlParameterSource()
        .addValue("description", concept.getDescription())
        .addValue("amount", concept.getAmount())
        .addValue("pricePerUnit", concept.getPricePerUnit())
        .addValue("documentId", concept.getDocumentId());
    KeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(SAVE_CONCEPT_SQL, params, keyHolder, new String[]{"concept_id"});
    return keyHolder.getKey().intValue();
  }

  @Override
  public void updateConcept(ConceptDto concept) {
    SqlParameterSource params = new MapSqlParameterSource()
        .addValue("documentId", concept.getDocumentId())
        .addValue("conceptId", concept.getConceptId())
        .addValue("description", concept.getDescription())
        .addValue("amount", concept.getAmount())
        .addValue("pricePerUnit", concept.getPricePerUnit());
    jdbcTemplate.update(UPDATE_CONCEPT_SQL, params);
  }

  @Override
  public void deleteConcept(Integer documentId, Integer conceptId) {
    SqlParameterSource params = new MapSqlParameterSource()
        .addValue("conceptId", conceptId)
        .addValue("documentId", documentId);
    jdbcTemplate.update(DELETE_CONCEPTS_SQL, params);
  }

  @Override
  public void deleteByDocumentId(Integer documentId) {
    SqlParameterSource params = new MapSqlParameterSource("documentId", documentId);
    jdbcTemplate.update(DELETE_ALL_DOCUMENT_CONCEPTS_SQL, params);
  }
}
