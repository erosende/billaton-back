package dev.erosende.priadapter.api.mapper;

import dev.erosende.billaton.application.domain.model.AddressDto;
import dev.erosende.priadapter.api.model.request.AddressRequestDto;
import dev.erosende.priadapter.api.model.response.AddressResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(componentModel = "spring",
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface AddressMapper {

  AddressResponseDto toAddressResponseDto(AddressDto src);

  AddressDto toAddressDto(AddressRequestDto src);

}
