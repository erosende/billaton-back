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
        .addressId(rs.getInt("addressId"))
        .addressLineOne(rs.getString("addressLineOne"))
        .addressLineTwo(rs.getString("addressLineTwo"))
        .postalCode(rs.getString("postalCode"))
        .city(rs.getString("city"))
        .province(rs.getString("province"))
        .build();

    return ParticipantDto.builder()
        .participantId(rs.getInt("participantId"))
        .identificationNumber(rs.getString("identificationNumber"))
        .identificationType(rs.getString("identificationType"))
        .identificationTypeId(rs.getInt("identificationTypeId"))
        .name(rs.getString("name"))
        .surnames(rs.getString("surnames"))
        .email(rs.getString("email"))
        .phoneNumber(rs.getString("phoneNumber"))
        .participantType(rs.getString("participantType"))
        .address(address)
        .build();
  }

}
