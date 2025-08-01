package dev.erosende.secadapter.repository.db;

import dev.erosende.billaton.application.domain.model.ParticipantDto;
import dev.erosende.billaton.application.domain.ports.secondary.db.ParticipantsRepository;
import dev.erosende.secadapter.mapper.ParticipantRowMapper;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

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
      SELECT p.participantId, p.identificationNumber, p.name, p.surnames, p.email, p.phoneNumber,
             pt.name AS participantType, it.name AS identificationType, it.identificationTypeId,
             a.addressId, a.addressLineOne, a.addressLineTwo, a.postalCode, a.city, a.province
      FROM Participant p
      INNER JOIN Address a ON a.addressId = p.addressId
      INNER JOIN IdentificationType it ON it.identificationTypeId = p.identificationTypeId
      INNER JOIN ParticipantType pt ON pt.participantTypeId = p.participantTypeId
      WHERE p.participantTypeId = :participantType
        AND (
          :searchTerm IS NULL
          OR p.name LIKE :searchTerm
          OR p.surnames LIKE :searchTerm
          OR p.identificationNumber LIKE :searchTerm
        )
      ORDER BY p.name, p.surnames ASC
      """;

  private static final String FIND_PARTICIPANT_SQL = """
      SELECT p.participantId, p.identificationNumber, p.name, p.surnames, p.email, p.phoneNumber,
             pt.name AS participantType, it.name AS identificationType, it.identificationTypeId,
             a.addressId, a.addressLineOne, a.addressLineTwo, a.postalCode, a.city, a.province
      FROM Participant p
      INNER JOIN Address a ON a.addressId = p.addressId
      INNER JOIN IdentificationType it ON it.identificationTypeId = p.identificationTypeId
      INNER JOIN ParticipantType pt ON pt.participantTypeId = p.participantTypeId
      WHERE p.participantId = :participantId
      """;

  private static final String SAVE_PARTICIPANT_SQL = """
      INSERT INTO Participant (
        identificationNumber,
        name,
        surnames,
        email,
        phoneNumber,
        addressId,
        identificationTypeId,
        participantTypeId
      )
      VALUES (
        :identificationNumber,
        :name,
        :surnames,
        :email,
        :phoneNumber,
        :addressId,
        :identificationTypeId,
        :participantTypeId
      )
      """;

  private static final String UPDATE_PARTICIPANT_SQL = """
      UPDATE Participant
      SET
        identificationNumber = :identificationNumber,
        name = :name,
        surnames = :surnames,
        email = :email,
        phoneNumber = :phoneNumber,
        identificationTypeId = :identificationTypeId
      WHERE participantId = :participantId
      """;

  private static final String DELETE_PARTICIPANT_SQL = """
      DELETE p
      FROM Participant p
      WHERE p.participantId = :participantId AND p.participantTypeId = 2 -- participant type 2 is Recipient
      """;

  private final NamedParameterJdbcTemplate jdbcTemplate;
  private final ParticipantRowMapper participantRowMapper;

  @Override
  public List<ParticipantDto> findParticipants(int participantType, String searchTerm) {
    SqlParameterSource params = new MapSqlParameterSource()
        .addValue("participantType", participantType)
        .addValue("searchTerm", getWildcard(searchTerm));
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
  public Integer saveParticipant(ParticipantDto participant) {
    SqlParameterSource params = new MapSqlParameterSource()
        .addValue("identificationNumber", participant.getIdentificationNumber())
        .addValue("name", participant.getName())
        .addValue("surnames", participant.getSurnames())
        .addValue("email", participant.getEmail())
        .addValue("phoneNumber", participant.getPhoneNumber())
        .addValue("identificationTypeId", participant.getIdentificationTypeId())
        .addValue("addressId", participant.getAddress().getAddressId())
        .addValue("participantTypeId", participant.getParticipantTypeId());

    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(SAVE_PARTICIPANT_SQL, params, keyHolder);

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
  public void deleteParticipant(Integer participantId) {
    SqlParameterSource params = new MapSqlParameterSource("participantId", participantId);
    jdbcTemplate.update(DELETE_PARTICIPANT_SQL, params);
  }

  private String getWildcard(String searchTerm) {
    return searchTerm == null ? null : MessageFormat.format(WILDCARD_TEMPLATE, searchTerm);
  }

}
