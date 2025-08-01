package dev.erosende.secadapter.repository.db;

import dev.erosende.billaton.application.domain.model.AddressDto;
import dev.erosende.billaton.application.domain.ports.secondary.db.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AddressRepositoryImpl implements AddressRepository {

  private static final String SAVE_ADDRESS_SQL = """
      INSERT INTO Address (
        addressLineOne,
        addressLineTwo,
        postalCode,
        city,
        province
      ) VALUES (
        :addressLineOne,
        :addressLineTwo,
        :postalCode,
        :city,
        :province
      )
      """;

  private static final String UPDATE_ADDRESS_SQL = """
      UPDATE Address
      SET
        addressLineOne = :addressLineOne,
        addressLineTwo = :addressLineTwo,
        postalCode = :postalCode,
        city = :city,
        province = :province
      WHERE addressId = :addressId
      """;

  private static final String DELETE_ADDRESS_SQL = """
      DELETE a
      FROM Address a
      INNER JOIN Participant p ON a.addressId = p.addressId
      WHERE p.participantId = :participantId AND p.participantTypeId = 2 -- participant type 2 is Recipient
      """;

  private final NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public Integer saveAddress(AddressDto address) {
    SqlParameterSource params = new MapSqlParameterSource()
        .addValue("addressLineOne", address.getAddressLineOne())
        .addValue("addressLineTwo", address.getAddressLineTwo())
        .addValue("postalCode", address.getPostalCode())
        .addValue("city", address.getCity())
        .addValue("province", address.getProvince());

    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(SAVE_ADDRESS_SQL, params, keyHolder);

    return keyHolder.getKey().intValue();
  }

  @Override
  public void updateAddress(AddressDto address) {
    SqlParameterSource params = new MapSqlParameterSource()
        .addValue("addressId", address.getAddressId())
        .addValue("addressLineOne", address.getAddressLineOne())
        .addValue("addressLineTwo", address.getAddressLineTwo())
        .addValue("postalCode", address.getPostalCode())
        .addValue("city", address.getCity())
        .addValue("province", address.getProvince());
    jdbcTemplate.update(UPDATE_ADDRESS_SQL, params);
  }

  @Override
  public void deleteAddress(Integer participantId) {
    SqlParameterSource params = new MapSqlParameterSource("participantId", participantId);
    jdbcTemplate.update(DELETE_ADDRESS_SQL, params);
  }

}
