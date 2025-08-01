package dev.erosende.billaton.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {

  private Integer addressId;
  private String addressLineOne;
  private String addressLineTwo;
  private String postalCode;
  private String city;
  private String province;

}
