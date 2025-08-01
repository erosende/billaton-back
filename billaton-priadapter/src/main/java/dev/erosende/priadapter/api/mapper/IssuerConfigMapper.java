package dev.erosende.priadapter.api.mapper;

import dev.erosende.billaton.application.domain.model.IssuerConfigDto;
import dev.erosende.priadapter.api.model.request.IssuerConfigRequestDto;
import dev.erosende.priadapter.api.model.response.IssuerConfigResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(componentModel = "spring",
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface IssuerConfigMapper {

  IssuerConfigResponseDto toIssuerConfigResponseDto(IssuerConfigDto src);

  @Mapping(target = "issuerId", expression = "java(participantId)")
  @Mapping(target = "issuerConfigId", expression = "java(configId)")
  IssuerConfigDto toIssuerConfigDto(IssuerConfigRequestDto src, Integer participantId, Integer configId);

}
