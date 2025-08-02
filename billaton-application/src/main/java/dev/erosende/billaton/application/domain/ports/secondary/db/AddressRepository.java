package dev.erosende.billaton.application.domain.ports.secondary.db;

import dev.erosende.billaton.application.domain.model.AddressDto;

public interface AddressRepository {

  Integer saveAddress(AddressDto address);

  void updateAddress(AddressDto address);

  void deleteAddress(Integer addressId);

}
