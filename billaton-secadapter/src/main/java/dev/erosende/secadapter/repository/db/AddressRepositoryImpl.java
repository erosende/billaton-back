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
      INSERT INTO address (
        address_line_one,
        address_line_two,
        postal_code,
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
      UPDATE address
      SET
        address_line_one = :addressLineOne,
        address_line_two = :addressLineTwo,
        postal_code = :postalCode,
        city = :city,
        province = :province
      WHERE address_id = :addressId
      """;

  private static final String DELETE_ADDRESS_SQL = """
      DELETE FROM address
      WHERE address_id = :addressId
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
    jdbcTemplate.update(SAVE_ADDRESS_SQL, params, keyHolder, new String[]{"address_id"});

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
  public void deleteAddress(Integer addressId) {
    SqlParameterSource params = new MapSqlParameterSource("addressId", addressId);
    jdbcTemplate.update(DELETE_ADDRESS_SQL, params);
  }

}
