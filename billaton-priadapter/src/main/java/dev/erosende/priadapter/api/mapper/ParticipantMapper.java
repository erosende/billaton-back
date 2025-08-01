package dev.erosende.priadapter.api.mapper;

import dev.erosende.billaton.application.domain.model.ParticipantDto;
import dev.erosende.priadapter.api.model.request.ParticipantRequestDto;
import dev.erosende.priadapter.api.model.response.ParticipantResponseDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
        uses = { AddressMapper.class })
public interface ParticipantMapper {

  ParticipantResponseDto toParticipantResponseDto(ParticipantDto src);

  List<ParticipantResponseDto> toParticipantResponseDto(List<ParticipantDto> src);

  ParticipantDto toParticipantDto(ParticipantRequestDto src);

  @Mapping(target = "participantId", expression = "java(participantId)")
  ParticipantDto toParticipantDto(ParticipantRequestDto src, Integer participantId);

}
