package dev.erosende.secadapter.repository.db;

import dev.erosende.billaton.application.domain.model.ParticipantDto;
import dev.erosende.billaton.application.domain.ports.secondary.db.ParticipantsRepository;
import dev.erosende.secadapter.mapper.ParticipantRowMapper;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ParticipantsRepositoryImpl implements ParticipantsRepository {

  private static final String WILDCARD_TEMPLATE = "%{0}%";

  private static final String FIND_PARTICIPANTS_SQL = """
      SELECT p.participant_id, p.identification_number, p.name, p.surnames, p.email, p.phone_number,
             pt.name AS participant_type, it.name AS identification_type, it.identification_type_id,
             a.address_id, a.address_line_one, a.address_line_two, a.postal_code, a.city, a.province
      FROM participant p
      INNER JOIN address a ON a.address_id = p.address_id
      INNER JOIN identification_type it ON it.identification_type_id = p.identification_type_id
      INNER JOIN participant_type pt ON pt.participant_type_id = p.participant_type_id
      WHERE p.participant_type_id = :participantType
        AND p.user_id = :userId
        AND (
          COALESCE(:searchTerm, '') = ''
          OR p.name LIKE :searchTerm
          OR p.surnames LIKE :searchTerm
          OR p.identification_number LIKE :searchTerm
        )
        AND p.historical IS FALSE
      ORDER BY p.name, p.surnames
      """;

  private static final String FIND_PARTICIPANT_SQL = """
      SELECT p.participant_id, p.identification_number, p.name, p.surnames, p.email, p.phone_number,
             pt.name AS participant_type, it.name AS identification_type, it.identification_type_id,
             a.address_id, a.address_line_one, a.address_line_two, a.postal_code, a.city, a.province
      FROM participant p
      INNER JOIN address a ON a.address_id = p.address_id
      INNER JOIN identification_type it ON it.identification_type_id = p.identification_type_id
      INNER JOIN participant_type pt ON pt.participant_type_id = p.participant_type_id
      WHERE p.participant_id = :participantId
      """;

  private static final String SAVE_PARTICIPANT_SQL = """
      INSERT INTO participant (
        identification_number,
        name,
        surnames,
        email,
        phone_number,
        address_id,
        identification_type_id,
        participant_type_id,
        user_id
      )
      VALUES (
        :identificationNumber,
        :name,
        :surnames,
        :email,
        :phoneNumber,
        :addressId,
        :identificationTypeId,
        :participantTypeId,
        :userId
      )
      """;

  private static final String UPDATE_PARTICIPANT_SQL = """
      UPDATE participant
      SET
        identification_number = :identificationNumber,
        name = :name,
        surnames = :surnames,
        email = :email,
        phone_number = :phoneNumber,
        identification_type_id = :identificationTypeId
      WHERE participant_id = :participantId
      """;

  private static final String DELETE_PARTICIPANT_SQL = """
      DELETE FROM participant
      WHERE participant_id = :participantId AND user_id = :userId AND participant_type_id = 2-- participant type 2 is Recipient
      """;

  private static final String SOFT_DELETE_PARTICIPANT_SQL = """
      UPDATE participant
      SET historical = true
      WHERE participant_id = :participantId AND user_id = :userId AND participant_type_id = 2 -- participant type 2 is Recipient
      """;

  private final NamedParameterJdbcTemplate jdbcTemplate;
  private final ParticipantRowMapper participantRowMapper;

  @Override
  public List<ParticipantDto> findParticipants(String userId, int participantType, String searchTerm) {
    SqlParameterSource params = new MapSqlParameterSource()
        .addValue("participantType", participantType)
        .addValue("searchTerm", getWildcard(searchTerm))
        .addValue("userId", UUID.fromString(userId));
    return jdbcTemplate.query(FIND_PARTICIPANTS_SQL, params, participantRowMapper);
  }

  @Override
  public Optional<ParticipantDto> findParticipantById(Integer participantId) {
    Optional<ParticipantDto> result;
    try {
      SqlParameterSource params = new MapSqlParameterSource("participantId", participantId);
      result = Optional.ofNullable(jdbcTemplate.queryForObject(
          FIND_PARTICIPANT_SQL,
          params,
          participantRowMapper
      ));
    } catch (EmptyResultDataAccessException erdae) {
      result = Optional.empty();
    }
    return result;
  }

  @Override
  public Integer saveParticipant(String userId, ParticipantDto participant) {
    SqlParameterSource params = new MapSqlParameterSource()
        .addValue("identificationNumber", participant.getIdentificationNumber())
        .addValue("name", participant.getName())
        .addValue("surnames", participant.getSurnames())
        .addValue("email", participant.getEmail())
        .addValue("phoneNumber", participant.getPhoneNumber())
        .addValue("identificationTypeId", participant.getIdentificationTypeId())
        .addValue("addressId", participant.getAddress().getAddressId())
        .addValue("participantTypeId", participant.getParticipantTypeId())
        .addValue("userId", UUID.fromString(userId));

    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(SAVE_PARTICIPANT_SQL, params, keyHolder, new String[]{"participant_id"});

    return keyHolder.getKey().intValue();
  }

  @Override
  public void updateParticipant(ParticipantDto participant) {
    SqlParameterSource params = new MapSqlParameterSource()
        .addValue("participantId", participant.getParticipantId())
        .addValue("identificationNumber", participant.getIdentificationNumber())
        .addValue("name", participant.getName())
        .addValue("surnames", participant.getSurnames())
        .addValue("email", participant.getEmail())
        .addValue("phoneNumber", participant.getPhoneNumber())
        .addValue("identificationTypeId", participant.getIdentificationTypeId());
    jdbcTemplate.update(UPDATE_PARTICIPANT_SQL, params);
  }

  @Override
  public void deleteParticipant(String userId, Integer participantId) {
    SqlParameterSource params = new MapSqlParameterSource()
        .addValue("participantId", participantId)
        .addValue("userId", UUID.fromString(userId));
    jdbcTemplate.update(DELETE_PARTICIPANT_SQL, params);
  }

  @Override
  public void softDeleteParticipant(String userId, Integer participantId) {
    SqlParameterSource params = new MapSqlParameterSource()
        .addValue("participantId", participantId)
        .addValue("userId", UUID.fromString(userId));
    jdbcTemplate.update(SOFT_DELETE_PARTICIPANT_SQL, params);
  }

  private String getWildcard(String searchTerm) {
    return searchTerm == null ? "" : MessageFormat.format(WILDCARD_TEMPLATE, searchTerm);
  }

}
