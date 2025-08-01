package dev.erosende.priadapter.api.adapter;

import dev.erosende.billaton.application.domain.model.IdentificationTypeDto;
import dev.erosende.billaton.application.domain.ports.primary.IdentificationTypesUseCase;
import dev.erosende.priadapter.api.constant.ResponseMessage;
import dev.erosende.priadapter.api.mapper.IdentificationTypeMapper;
import dev.erosende.priadapter.api.model.response.IdentificationTypeResponseDto;
import dev.erosende.priadapter.api.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Identification types Controller")
@RestController("IdentificationTypesController")
@RequestMapping("/billaton/identification-types")
@RequiredArgsConstructor
public class IdentificationTypeController {

  private final IdentificationTypesUseCase identificationTypesUseCase;
  private final IdentificationTypeMapper identificationTypeMapper;

  @GetMapping
  @Operation(summary = "Returns a list of all identification types")
  @ApiResponse(responseCode = "200", description = ResponseMessage.SUCCESS_RETRIEVAL)
  @ApiResponse(responseCode = "500", description = "example: generic not found response")
  public ResponseEntity<BaseResponse<List<IdentificationTypeResponseDto>>> getIdentificationTypes() {
    log.info("Retrieving all identification types");
    ResponseEntity<BaseResponse<List<IdentificationTypeResponseDto>>> response;
    try {
      List<IdentificationTypeDto> useCaseResult = identificationTypesUseCase.getIdentificationTypes();
      log.info("Successfully retrieved {} identification types", useCaseResult.size());

      response = ResponseEntity.ok(BaseResponse.success(identificationTypeMapper.toIdentificationTypeResponseDto(useCaseResult)));
    } catch (Exception e) {
      response = new ResponseEntity<>(BaseResponse.error(e, log, LogLevel.ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return response;
  }

}
