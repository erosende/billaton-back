package dev.erosende.secadapter.mapper;

import dev.erosende.billaton.application.domain.model.AddressDto;
import dev.erosende.billaton.application.domain.model.ParticipantDto;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class ParticipantRowMapper implements RowMapper<ParticipantDto> {

  @Override
  public ParticipantDto mapRow(ResultSet rs, int rowNum) throws SQLException {
    AddressDto address = AddressDto.builder()
        .addressId(rs.getInt("address_id"))
        .addressLineOne(rs.getString("address_line_one"))
        .addressLineTwo(rs.getString("address_line_two"))
        .postalCode(rs.getString("postal_code"))
        .city(rs.getString("city"))
        .province(rs.getString("province"))
        .build();

    return ParticipantDto.builder()
        .participantId(rs.getInt("participant_id"))
        .identificationNumber(rs.getString("identification_number"))
        .identificationType(rs.getString("identification_type"))
        .identificationTypeId(rs.getInt("identification_type_id"))
        .name(rs.getString("name"))
        .surnames(rs.getString("surnames"))
        .email(rs.getString("email"))
        .phoneNumber(rs.getString("phone_number"))
        .participantType(rs.getString("participant_type"))
        .address(address)
        .build();
  }

}
