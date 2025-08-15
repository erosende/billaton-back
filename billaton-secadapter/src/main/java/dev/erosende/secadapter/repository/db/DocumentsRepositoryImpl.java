package dev.erosende.secadapter.repository.db;

import dev.erosende.billaton.application.domain.model.DocumentBackupDto;
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

import java.util.*;

@Repository
@RequiredArgsConstructor
public class DocumentsRepositoryImpl implements DocumentsRepository {

  private static final Map<String, String> FIELD_MAPPINGS = new HashMap<>() {{
    put("documentId", "d.document_id");
    put("documentTypeId", "d.document_type_id");
    put("documentDate", "d.document_date");
    put("recipientId", "d.recipient_id");
    put("historical", "d.historical");
    put("userId", "d.user_id");
  }};

  public static final String WHERE_KEYWORD = " WHERE ";
  private static final String GROUP_BY_DOCUMENT_ID = "";
  private static final String WHERE_CLAUSE_DOCUMENT_ID = " WHERE d.document_id = :documentId ";

  private static final String FIND_DOCUMENT_BASE_SQL = """
      SELECT DISTINCT
             d.document_id, d.document_type_id, d.document_code, d.document_date, d.resource_path,
             d.issuer_id, d.recipient_id, dt.name AS document_type,
             SUM(c.amount * c.price_per_unit * (1 + (ic.vat / 100))) OVER (PARTITION BY d.document_id) AS total_amount,
             CONCAT(r.name, ' ', r.surnames) AS recipient_name,
             CONCAT(i.name, ' ', i.surnames) AS issuer_name
      FROM document d
      INNER JOIN document_type dt ON dt.document_type_id = d.document_type_id
      INNER JOIN issuer_config ic ON ic.issuer_id = d.issuer_id
      INNER JOIN participant r ON r.participant_id = d.recipient_id
      INNER JOIN participant i ON i.participant_id = d.issuer_id
      LEFT JOIN concept c ON c.document_id = d.document_id
      """;

  private static final String FIND_DOCUMENTS_FOR_BACKUP_SQL = """
      SELECT d.document_id, d.document_code, dt.name AS documentType, d.document_date, d.resource_path,
        replace(concat(pr.name, ' ', pr.surnames), ' ', '_') AS recipientFullName,
        replace(concat(pi.name, ' ', pi.surnames), ' ', '_') AS issuerFullName
      FROM "document" d
      INNER JOIN document_type dt ON dt.document_type_id = d.document_type_id
      INNER JOIN participant pr ON pr.participant_id = d.recipient_id
      INNER JOIN participant pi ON pi.participant_id = d.issuer_id
      WHERE d.backed_up = FALSE
        AND d.historical = FALSE
        AND d.resource_path IS NOT NULL
      """;

  private static final String SAVE_DOCUMENT_SQL = """
          INSERT INTO document (
            document_type_id,
            document_date,
            document_code,
            issuer_id,
            recipient_id,
            user_id
          ) VALUES (
            :documentTypeId,
            :documentDate,
            :documentCode,
            :issuerId,
            :recipientId,
            :userId
          )
      """;

  private static final String UPDATE_DOCUMENT_SQL = """
          UPDATE document
          SET
            document_type_id = :documentTypeId,
            document_date = :documentDate,
            document_code = :documentCode,
            issuer_id = :issuerId,
            recipient_id = :recipientId
          WHERE document_id = :documentId
      """;

  private static final String UPDATE_RESOURCE_PATH_SQL = """
      UPDATE document
      SET resource_path = :resourcePath
      WHERE document_id = :documentId
      """;

  private static final String UPDATE_BACKUP_STATUS_SQL = """
      UPDATE document
      SET backed_up = :backedUp
      WHERE document_id = :documentId
      """;

  private static final String DELETE_DOCUMENT_LOGICALLY_SQL = """
      UPDATE document
      SET historical = true
      WHERE document_id = :documentId AND user_id = :userId
      """;

  private static final String SOFT_DELETE_DOCUMENT_BY_RECIPIENT_SQL = """
      UPDATE document
      SET historical = true
      WHERE recipient_id = :recipientId AND user_id = :userId
      """;

  private static final String SOFT_DELETE_DOCUMENT_BY_ISSUER_SQL = """
      UPDATE document
      SET historical = true
      WHERE issuer_id = :issuerId AND user_id = :userId
      """;

  private static final String COUNT_DOCUMENTS_BASE_SQL = """
      SELECT COUNT(*)
      FROM document d
      """;

  private final NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public Page<DocumentDto> findDocuments(String userId, PagingParams pagingParams) {
    StringBuilder queryBuilder = new StringBuilder(FIND_DOCUMENT_BASE_SQL);
    StringBuilder countQueryBuilder = new StringBuilder(COUNT_DOCUMENTS_BASE_SQL);
    MapSqlParameterSource params = new MapSqlParameterSource();

    //Add filters
    PagingUtils.addFilterToPagingParams(pagingParams, "userId", UUID.fromString(userId));
    PagingUtils.addFilterToPagingParams(pagingParams, "historical", Boolean.FALSE);
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
  public List<DocumentBackupDto> findDocumentsForBackup() {
    return jdbcTemplate.query(FIND_DOCUMENTS_FOR_BACKUP_SQL, new BeanPropertyRowMapper<>(DocumentBackupDto.class));
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
  public Integer saveDocument(String userId, DocumentDto document) {
    SqlParameterSource params = new MapSqlParameterSource()
        .addValue("documentTypeId", document.getDocumentTypeId())
        .addValue("documentCode", document.getDocumentCode())
        .addValue("documentDate", document.getDocumentDate())
        .addValue("issuerId", document.getIssuerId())
        .addValue("recipientId", document.getRecipientId())
        .addValue("userId", UUID.fromString(userId));
    KeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(SAVE_DOCUMENT_SQL, params, keyHolder, new String[]{"document_id"});
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
  public void updateDocumentBackupStatus(Integer documentId, boolean backedUp) {
    SqlParameterSource params = new MapSqlParameterSource()
        .addValue("documentId", documentId)
        .addValue("backedUp", backedUp);
    jdbcTemplate.update(UPDATE_BACKUP_STATUS_SQL, params);
  }

  @Override
  public void deleteDocumentLogically(String userId, Integer documentId) {
    SqlParameterSource params = new MapSqlParameterSource()
        .addValue("documentId", documentId)
        .addValue("userId", UUID.fromString(userId));
    jdbcTemplate.update(DELETE_DOCUMENT_LOGICALLY_SQL, params);
  }

  @Override
  public int softDeleteDocumentByRecipient(String userId, Integer recipientId) {
    SqlParameterSource params = new MapSqlParameterSource()
        .addValue("recipientId", recipientId)
        .addValue("userId", UUID.fromString(userId));
    return jdbcTemplate.update(SOFT_DELETE_DOCUMENT_BY_RECIPIENT_SQL, params);
  }

  @Override
  public int softDeleteDocumentByIssuer(String userId, Integer issuerId) {
    SqlParameterSource params = new MapSqlParameterSource()
        .addValue("issuerId", issuerId)
        .addValue("userId", UUID.fromString(userId));
    return jdbcTemplate.update(SOFT_DELETE_DOCUMENT_BY_ISSUER_SQL, params);
  }

}
