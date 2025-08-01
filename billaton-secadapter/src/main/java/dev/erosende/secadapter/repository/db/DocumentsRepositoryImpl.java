package dev.erosende.secadapter.repository.db;

import dev.erosende.billaton.application.domain.model.DocumentDto;
import dev.erosende.billaton.application.domain.model.generic.Page;
import dev.erosende.billaton.application.domain.model.generic.PagingParams;
import dev.erosende.billaton.application.domain.ports.secondary.db.DocumentsRepository;
import dev.erosende.secadapter.utils.PagingUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DocumentsRepositoryImpl implements DocumentsRepository {

  private static final Map<String, String> FIELD_MAPPINGS = new HashMap<>() {{
    put("documentId", "d.documentId");
    put("documentTypeId", "d.documentTypeId");
    put("documentDate", "d.documentDate");
    put("recipientId", "d.recipientId");
  }};

  public static final String WHERE_KEYWORD = " WHERE ";
  private static final String GROUP_BY_DOCUMENT_ID = " GROUP BY d.documentId ";
  private static final String WHERE_CLAUSE_DOCUMENT_ID = " WHERE d.documentId = :documentId ";

  private static final String FIND_DOCUMENT_BASE_SQL = """
      SELECT d.documentId, d.documentTypeId, d.documentCode, d.documentDate, d.resourcePath,
             d.issuerId, d.recipientId, dt.name AS documentType,
             SUM(c.amount * c.pricePerUnit * (1 + (ic.vat / 100))) AS totalAmount,
             CONCAT(r.name, ' ', r.surnames) AS recipientName,
             CONCAT(i.name, ' ', i.surnames) AS issuerName
      FROM Document d
      INNER JOIN DocumentType dt ON dt.documentTypeId = d.documentTypeId
      INNER JOIN IssuerConfig ic ON ic.issuerId = d.issuerId
      INNER JOIN Participant r ON r.participantId = d.recipientId
      INNER JOIN Participant i ON i.participantId = d.issuerId
      LEFT JOIN Concept c ON c.documentId = d.documentId
      """;

  private static final String SAVE_DOCUMENT_SQL = """
      INSERT INTO Document (
        documentTypeId,
        documentDate,
        documentCode,
        issuerId,
        recipientId
      ) VALUES (
        :documentTypeId,
        :documentDate,
        :documentCode,
        :issuerId,
        :recipientId
      )
  """;

  private static final String UPDATE_DOCUMENT_SQL = """
      UPDATE Document
      SET
        documentTypeId = :documentTypeId,
        documentDate = :documentDate,
        documentCode = :documentCode,
        issuerId = :issuerId,
        recipientId = :recipientId
      WHERE documentId = :documentId
  """;

  private static final String UPDATE_RESOURCE_PATH_SQL = """
      UPDATE Document
      SET resourcePath = :resourcePath
      WHERE documentId = :documentId
      """;

  private static final String DELETE_DOCUMENT_SQL = """
      DELETE FROM Document
      WHERE documentId = :documentId
      """;

  private static final String SOFT_DELETE_PARTICIPANT_DOCUMENTS_SQL = """
      DELETE FROM Document d
      WHERE d.recipientId = :participantId
      """;

  private static final String COUNT_DOCUMENTS_BASE_SQL = """
      SELECT COUNT(*)
      FROM Document d
      """;
  
  private final NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public Page<DocumentDto> findDocuments(PagingParams pagingParams) {
    StringBuilder queryBuilder = new StringBuilder(FIND_DOCUMENT_BASE_SQL);
    StringBuilder countQueryBuilder = new StringBuilder(COUNT_DOCUMENTS_BASE_SQL);
    MapSqlParameterSource params = new MapSqlParameterSource();

    //Add filters
    String whereClause = PagingUtils.buildWhereClause(pagingParams.getFilters(), FIELD_MAPPINGS, params);
    if (StringUtils.isNotEmpty(whereClause)) {
      queryBuilder.append(WHERE_KEYWORD).append(whereClause);
      countQueryBuilder.append(WHERE_KEYWORD).append(whereClause);
    }

    // Add grouping by clause
    queryBuilder.append(GROUP_BY_DOCUMENT_ID);

    // Add sorting
    String orderByClause = PagingUtils.buildOrderByClause(pagingParams.getSort(), FIELD_MAPPINGS);
    if (StringUtils.isNotEmpty(orderByClause)) {
      queryBuilder.append(" ORDER BY ").append(orderByClause);
    }

    // Add pagination
    queryBuilder.append(" LIMIT :limit OFFSET :offset");
    params.addValue("limit", pagingParams.getSize());
    params.addValue("offset", pagingParams.getOffset());

    List<DocumentDto> queryResult = jdbcTemplate.query(queryBuilder.toString(), params, new BeanPropertyRowMapper<>(DocumentDto.class));
    Long totalElements = jdbcTemplate.queryForObject(countQueryBuilder.toString(), params, Long.class);

    return Page.of(queryResult, pagingParams.getPage(), pagingParams.getSize(), totalElements != null ? totalElements : 0L);
  }

  @Override
  public Optional<DocumentDto> findDocumentById(Integer documentId) {
    Optional<DocumentDto> result;
    try {
      SqlParameterSource params = new MapSqlParameterSource("documentId", documentId);
      StringBuilder queryBuilder = new StringBuilder(FIND_DOCUMENT_BASE_SQL)
          .append(WHERE_CLAUSE_DOCUMENT_ID);
      result = Optional.ofNullable(jdbcTemplate.queryForObject(
          queryBuilder.toString(),
          params,
          new BeanPropertyRowMapper<>(DocumentDto.class)
      ));
    } catch (EmptyResultDataAccessException erdae) {
      result = Optional.empty();
    }

    return result;
  }

  @Override
  public Integer saveDocument(DocumentDto document) {
    SqlParameterSource params = new MapSqlParameterSource()
        .addValue("documentTypeId", document.getDocumentTypeId())
        .addValue("documentCode", document.getDocumentCode())
        .addValue("documentDate", document.getDocumentDate())
        .addValue("issuerId", document.getIssuerId())
        .addValue("recipientId", document.getRecipientId());
    KeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(SAVE_DOCUMENT_SQL, params, keyHolder);
    return keyHolder.getKey().intValue();
  }

  @Override
  public void updateDocument(DocumentDto document) {
    SqlParameterSource params = new MapSqlParameterSource()
        .addValue("documentId", document.getDocumentId())
        .addValue("documentTypeId", document.getDocumentTypeId())
        .addValue("documentCode", document.getDocumentCode())
        .addValue("documentDate", document.getDocumentDate())
        .addValue("issuerId", document.getIssuerId())
        .addValue("recipientId", document.getRecipientId());
    jdbcTemplate.update(UPDATE_DOCUMENT_SQL, params);
  }

  @Override
  public void updateDocumentResourcePath(Integer documentId, String resourcePath) {
    SqlParameterSource params = new MapSqlParameterSource()
        .addValue("documentId", documentId)
        .addValue("resourcePath", resourcePath);
    jdbcTemplate.update(UPDATE_RESOURCE_PATH_SQL, params);
  }

  @Override
  public void deleteDocument(Integer documentId) {
    SqlParameterSource params = new MapSqlParameterSource("documentId", documentId);
    jdbcTemplate.update(DELETE_DOCUMENT_SQL, params);
  }

  @Override
  public void softDeleteParticipantDocuments(Integer participantId) {
    SqlParameterSource params = new MapSqlParameterSource("participantId", participantId);
    jdbcTemplate.update(SOFT_DELETE_PARTICIPANT_DOCUMENTS_SQL, params);
  }

}
