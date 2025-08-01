package dev.erosende.priadapter.api.mapper;

import dev.erosende.billaton.application.domain.model.ConceptDto;
import dev.erosende.priadapter.api.model.request.ConceptRequestDto;
import dev.erosende.priadapter.api.model.response.ConceptResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
    nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface ConceptMapper {

  ConceptResponseDto toConceptResponseDto(ConceptDto src);

  List<ConceptResponseDto> toConceptResponseDto(List<ConceptDto> src);

  @Mapping(target = "documentId", expression = "java(documentId)")
  ConceptDto toConceptDto(ConceptRequestDto src, Integer documentId);

  @Mapping(target = "documentId", expression = "java(documentId)")
  @Mapping(target = "conceptId", expression = "java(conceptId)")
  ConceptDto toConceptDto(ConceptRequestDto src, Integer documentId, Integer conceptId);

}
