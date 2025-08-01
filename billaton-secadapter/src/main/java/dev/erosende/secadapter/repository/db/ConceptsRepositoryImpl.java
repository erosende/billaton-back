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
      SELECT c.conceptId, c.description, c.amount, c.pricePerUnit, c.documentId
      FROM Concept c
      WHERE c.documentId = :documentId
      """;

  private static final String SAVE_CONCEPT_SQL = """
      INSERT INTO Concept(description, amount, pricePerUnit, documentId)
      VALUES (:description, :amount, :pricePerUnit, :documentId)
      """;

  private static final String UPDATE_CONCEPT_SQL = """
      UPDATE Concept
      SET
        description = :description,
        amount = :amount,
        pricePerUnit = :pricePerUnit
      WHERE conceptId = :conceptId AND documentId = :documentId
      """;

  private static final String DELETE_CONCEPTS_SQL = """
      DELETE FROM Concept c
      WHERE c.documentId = :documentId AND c.conceptId = :conceptId
      """;

  private static final String DELETE_ALL_DOCUMENT_CONCEPTS_SQL = """
      DELETE FROM Concept c
      WHERE c.documentId = :documentId
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

    jdbcTemplate.update(SAVE_CONCEPT_SQL, params, keyHolder);
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
