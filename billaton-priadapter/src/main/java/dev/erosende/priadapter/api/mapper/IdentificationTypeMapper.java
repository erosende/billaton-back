package dev.erosende.priadapter.api.mapper;

import dev.erosende.billaton.application.domain.model.IdentificationTypeDto;
import dev.erosende.priadapter.api.model.response.IdentificationTypeResponseDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(componentModel = "spring",
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface IdentificationTypeMapper {

  IdentificationTypeResponseDto toIdentificationTypeResponseDto(IdentificationTypeDto src);

  List<IdentificationTypeResponseDto> toIdentificationTypeResponseDto(List<IdentificationTypeDto> src);

}
