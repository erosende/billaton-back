package dev.erosende.priadapter.api.adapter;

import dev.erosende.billaton.application.domain.model.ConceptDto;
import dev.erosende.billaton.application.domain.model.DocumentDto;
import dev.erosende.billaton.application.domain.model.DocumentFileDto;
import dev.erosende.billaton.application.domain.model.generic.Page;
import dev.erosende.billaton.application.domain.model.generic.PagingParams;
import dev.erosende.billaton.application.domain.ports.primary.DocumentsUseCase;
import dev.erosende.billaton.application.domain.util.PagingUtils;
import dev.erosende.priadapter.api.constant.ResponseMessage;
import dev.erosende.priadapter.api.mapper.ConceptMapper;
import dev.erosende.priadapter.api.mapper.DocumentMapper;
import dev.erosende.priadapter.api.model.request.ConceptRequestDto;
import dev.erosende.priadapter.api.model.request.DocumentRequestDto;
import dev.erosende.priadapter.api.model.response.ConceptResponseDto;
import dev.erosende.priadapter.api.model.response.DocumentResponseDto;
import dev.erosende.priadapter.api.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Documents Controller")
@RestController("DocumentsController")
@RequestMapping("/billaton/documents")
@RequiredArgsConstructor
public class DocumentController {

  private final DocumentsUseCase documentsUseCase;
  private final DocumentMapper documentMapper;
  private final ConceptMapper conceptMapper;

  @GetMapping
  @Operation(summary = "Returns a page with documents")
  @ApiResponse(responseCode = "200", description = ResponseMessage.SUCCESS_RETRIEVAL)
  @ApiResponse(responseCode = "500", description = ResponseMessage.ERROR_INTERNAL)
  public ResponseEntity<BaseResponse<Page<DocumentResponseDto>>> getDocuments(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
      @RequestParam(required = false) List<String> sort,
      @RequestParam(required = false) Integer documentTypeId,
      @RequestParam(required = false) LocalDate dateFrom,
      @RequestParam(required = false) LocalDate dateTo,
      @RequestParam(required = false) Integer recipientId)
  {
    ResponseEntity<BaseResponse<Page<DocumentResponseDto>>> response;
    try {
      log.info("Retrieving document's page {} | size = {}", page, size);
      PagingParams pagingParams = PagingParams.builder()
          .page(page)
          .size(size)
          .sort(PagingUtils.parseSortCriteria(sort))
          .filters(PagingUtils.buildDocumentFilters(documentTypeId, dateFrom, dateTo, recipientId))
          .build();

      Page<DocumentDto> useCaseResult = documentsUseCase.getDocuments(pagingParams);
      Page<DocumentResponseDto> responseData = useCaseResult.map(documentMapper::toDocumentResponseDto);

      response = ResponseEntity.ok(BaseResponse.success(responseData));
    } catch (Exception e) {
      response = new ResponseEntity<>(BaseResponse.error(e, log, LogLevel.ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return response;
  }

  @GetMapping("/{documentId}/concepts")
  @Operation(summary = "Returns a list of all the concepts attached to a document")
  @ApiResponse(responseCode = "200", description = ResponseMessage.SUCCESS_RETRIEVAL)
  @ApiResponse(responseCode = "500", description = ResponseMessage.ERROR_INTERNAL)
  public ResponseEntity<BaseResponse<List<ConceptResponseDto>>> getConcepts(@PathVariable Integer documentId) {
    ResponseEntity<BaseResponse<List<ConceptResponseDto>>> response;
    try {
      log.info("Retrieving document concepts for document with ID {}", documentId);
      List<ConceptDto> useCaseResult = documentsUseCase.getConcepts(documentId);

      log.info("Successfully retrieved {} concepts", useCaseResult.size());
      response = ResponseEntity.ok(BaseResponse.success(conceptMapper.toConceptResponseDto(useCaseResult)));
    } catch (Exception e) {
      response = new ResponseEntity<>(BaseResponse.error(e, log, LogLevel.ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return response;
  }

  @PostMapping
  @Operation(summary = "Creates a document")
  @ApiResponse(responseCode = "200", description = ResponseMessage.SUCCESS_CREATION)
  @ApiResponse(responseCode = "500", description = ResponseMessage.ERROR_INTERNAL)
  public ResponseEntity<BaseResponse<Integer>> createDocument(@RequestBody DocumentRequestDto document) {
    ResponseEntity<BaseResponse<Integer>> response;
    try {
      log.info("Creating new document");
      Integer useCaseResult = documentsUseCase.createDocument(documentMapper.toDocumentDto(document));

      log.info("Successfully created new document with ID: {}", useCaseResult);
      response = ResponseEntity.ok(BaseResponse.success(useCaseResult));
    } catch (Exception e) {
      response = new ResponseEntity<>(BaseResponse.error(e, log, LogLevel.ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return response;
  }

  @PostMapping("/{documentId}/concepts")
  @Operation(summary = "Creates a concept")
  @ApiResponse(responseCode = "200", description = ResponseMessage.SUCCESS_CREATION)
  @ApiResponse(responseCode = "500", description = ResponseMessage.ERROR_INTERNAL)
  public ResponseEntity<BaseResponse<Integer>> createConcept(@PathVariable Integer documentId, @RequestBody ConceptRequestDto concept) {
    ResponseEntity<BaseResponse<Integer>> response;
    try {
      log.info("Creating new concept for document with ID {}", documentId);
      ConceptDto mappedInput = conceptMapper.toConceptDto(concept, documentId);
      Integer useCaseResult = documentsUseCase.createConcept(mappedInput);

      log.info("Successfully created new concept with ID: {}", useCaseResult);
      response = ResponseEntity.ok(BaseResponse.success(useCaseResult));
    } catch (Exception e) {
      response = new ResponseEntity<>(BaseResponse.error(e, log, LogLevel.ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return response;
  }

  @PutMapping("/{documentId}")
  @Operation(summary = "Updates a document")
  @ApiResponse(responseCode = "200", description = ResponseMessage.SUCCESS_UPDATE)
  @ApiResponse(responseCode = "500", description = ResponseMessage.ERROR_INTERNAL)
  public ResponseEntity<BaseResponse<Void>> updateDocument(@PathVariable Integer documentId, @RequestBody DocumentRequestDto document) {
    ResponseEntity<BaseResponse<Void>> response;
    try {
      log.info("Updating document with ID {}", documentId);
      documentsUseCase.updateDocument(documentMapper.toDocumentDto(document, documentId));

      log.info("Successfully updated document with ID {}", documentId);
      response = ResponseEntity.ok(BaseResponse.success(null));
    } catch (Exception e) {
      response = new ResponseEntity<>(BaseResponse.error(e, log, LogLevel.ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return response;
  }

  @PutMapping("/{documentId}/concepts/{conceptId}")
  @Operation(summary = "Updates a concept")
  @ApiResponse(responseCode = "200", description = ResponseMessage.SUCCESS_UPDATE)
  @ApiResponse(responseCode = "500", description = ResponseMessage.ERROR_INTERNAL)
  public ResponseEntity<BaseResponse<Void>> updateConcept(@PathVariable Integer documentId,
                                                          @PathVariable Integer conceptId,
                                                          @RequestBody ConceptRequestDto concept) {
    ResponseEntity<BaseResponse<Void>> response;
    try {
      log.info("Updating concept with ID {}", conceptId);
      ConceptDto mappedInput = conceptMapper.toConceptDto(concept, documentId, conceptId);
      documentsUseCase.updateConcept(mappedInput);

      log.info("Successfully updated concept with ID {}", conceptId);
      response = ResponseEntity.ok(BaseResponse.success(null));
    } catch (Exception e) {
      response = new ResponseEntity<>(BaseResponse.error(e, log, LogLevel.ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return response;
  }

  @DeleteMapping("/{documentId}")
  @Operation(summary = "Deletes a document")
  @ApiResponse(responseCode = "200", description = ResponseMessage.SUCCESS_DELETION)
  @ApiResponse(responseCode = "500", description = ResponseMessage.ERROR_INTERNAL)
  public ResponseEntity<BaseResponse<Void>> deleteDocument(@PathVariable Integer documentId) {
    ResponseEntity<BaseResponse<Void>> response;
    try {
      log.info("Deleting document with ID {}", documentId);
      documentsUseCase.deleteDocument(documentId);

      log.info("Successfully deleted document with ID {}", documentId);
      response = ResponseEntity.ok(BaseResponse.success(null));
    } catch (Exception e) {
      response = new ResponseEntity<>(BaseResponse.error(e, log, LogLevel.ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return response;
  }

  @DeleteMapping("/{documentId}/concepts/{conceptId}")
  @Operation(summary = "Deletes a concept")
  @ApiResponse(responseCode = "200", description = ResponseMessage.SUCCESS_DELETION)
  @ApiResponse(responseCode = "500", description = ResponseMessage.ERROR_INTERNAL)
  public ResponseEntity<BaseResponse<Void>> deleteConcept(@PathVariable Integer documentId, @PathVariable Integer conceptId) {
    ResponseEntity<BaseResponse<Void>> response;
    try {
      log.info("Deleting concept with ID {}", conceptId);
      documentsUseCase.deleteConcept(documentId, conceptId);

      log.info("Successfully deleted concept with ID {}", conceptId);
      response = ResponseEntity.ok(BaseResponse.success(null));
    } catch (Exception e) {
      response = new ResponseEntity<>(BaseResponse.error(e, log, LogLevel.ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return response;
  }

  @GetMapping("/{documentId}/pdf/download")
  @Operation(summary = "Returns a document as a PDF")
  @ApiResponse(responseCode = "200", description = ResponseMessage.SUCCESS_RETRIEVAL)
  @ApiResponse(responseCode = "500", description = ResponseMessage.ERROR_INTERNAL)
  public ResponseEntity<?> getDocumentAsPdf(@PathVariable Integer documentId) {
    ResponseEntity<?> response;
    try {
      log.info("Retrieving pdf for document with ID {}", documentId);
      DocumentFileDto useCaseResult = documentsUseCase.downloadDocumentAsPdf(documentId);

      response = buildDocumentFileResponseEntity(documentId, useCaseResult);
    } catch (Exception e) {
      response = new ResponseEntity<>(BaseResponse.error(e, log, LogLevel.ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return response;
  }

  @PostMapping("/{documentId}/pdf/generate")
  @Operation(summary = "Generates a document and uploads it to the cloud")
  @ApiResponse(responseCode = "200", description = ResponseMessage.SUCCESS_OPERATION)
  @ApiResponse(responseCode = "500", description = ResponseMessage.ERROR_INTERNAL)
  public ResponseEntity<?> generateDocument(@PathVariable Integer documentId) {
    ResponseEntity<?> response;
    try {
      log.info("Generating pdf for document with ID {}", documentId);
      DocumentFileDto useCaseResult = documentsUseCase.generateDocumentAsPdf(documentId);

      response = buildDocumentFileResponseEntity(documentId, useCaseResult);
    } catch (Exception e) {
      response = new ResponseEntity<>(BaseResponse.error(e, log, LogLevel.ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return response;
  }

  private ResponseEntity<?> buildDocumentFileResponseEntity(@PathVariable Integer documentId, DocumentFileDto documentFile) {
    ResponseEntity<?> response;
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentLength(documentFile.getContent().length);

    headers.setContentDispositionFormData("inline", documentFile.getFileName() );

    headers.setCacheControl(CacheControl.noCache());
    headers.setPragma("no-cache");
    headers.setExpires(0);

    log.info("Successfully generated pdf for document with ID {}", documentId);
    response = ResponseEntity.ok()
        .headers(headers)
        .body(documentFile.getContent());
    return response;
  }

}
