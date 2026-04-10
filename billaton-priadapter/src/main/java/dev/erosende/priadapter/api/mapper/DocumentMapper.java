package dev.erosende.priadapter.api.mapper;

import dev.erosende.billaton.application.domain.model.DocumentDto;
import dev.erosende.priadapter.api.model.request.DocumentRequestDto;
import dev.erosende.priadapter.api.model.response.DocumentResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(componentModel = "spring",
    nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface DocumentMapper {

  DocumentResponseDto toDocumentResponseDto(DocumentDto src);

  @Mapping(target = "documentCode", ignore = true)
  DocumentDto toDocumentDto(DocumentRequestDto src);

  @Mapping(target = "documentId", expression = "java(documentId)")
  @Mapping(target = "documentCode", ignore = true)
  DocumentDto toDocumentDto(DocumentRequestDto src, Integer documentId);

}
