package dev.erosende.priadapter.api.adapter;

import dev.erosende.billaton.application.domain.enums.ParticipantType;
import dev.erosende.billaton.application.domain.exception.ResourceNotFoundException;
import dev.erosende.billaton.application.domain.model.IssuerConfigDto;
import dev.erosende.billaton.application.domain.model.ParticipantDto;
import dev.erosende.billaton.application.domain.ports.primary.ParticipantsUseCase;
import dev.erosende.priadapter.api.constant.ResponseMessage;
import dev.erosende.priadapter.api.mapper.IssuerConfigMapper;
import dev.erosende.priadapter.api.mapper.ParticipantMapper;
import dev.erosende.priadapter.api.model.request.IssuerConfigRequestDto;
import dev.erosende.priadapter.api.model.request.ParticipantRequestDto;
import dev.erosende.priadapter.api.model.response.IssuerConfigResponseDto;
import dev.erosende.priadapter.api.model.response.ParticipantResponseDto;
import dev.erosende.priadapter.api.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Participants Controller")
@RestController("ParticipantsController")
@RequestMapping("/billaton/participants")
@RequiredArgsConstructor
public class ParticipantController {

  private final ParticipantsUseCase participantsUseCase;
  private final ParticipantMapper participantMapper;
  private final IssuerConfigMapper issuerConfigMapper;

  @GetMapping
  @Operation(summary = "Returns a list of all participants filtering by type and search term")
  @ApiResponse(responseCode = "200", description = ResponseMessage.SUCCESS_RETRIEVAL)
  @ApiResponse(responseCode = "500", description = "example: generic not found response")
  public ResponseEntity<BaseResponse<List<ParticipantResponseDto>>> getParticipants(@NotNull @RequestParam ParticipantType participantType,
                                                                                    @RequestParam(required = false) String searchTerm)
  {
    log.info("Retrieving participants with type {} and search term {}", participantType.getCode(), searchTerm);
    ResponseEntity<BaseResponse<List<ParticipantResponseDto>>> response;
    try {
      List<ParticipantDto> useCaseResult = participantsUseCase.getParticipants(participantType, searchTerm);
      log.info("Successfully retrieved {} participants", useCaseResult.size());

      response = ResponseEntity.ok(BaseResponse.success(participantMapper.toParticipantResponseDto(useCaseResult)));
    } catch (Exception e) {
      response = new ResponseEntity<>(BaseResponse.error(e, log, LogLevel.ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return response;
  }

  @PostMapping("/recipients")
  @Operation(summary = "Creates a new participant of type Recipient")
  @ApiResponse(responseCode = "201", description = ResponseMessage.SUCCESS_CREATION)
  @ApiResponse(responseCode = "500", description = "example: generic not found response")
  public ResponseEntity<BaseResponse<Integer>> createRecipientParticipant(@Valid @RequestBody ParticipantRequestDto participant) {
    log.info("Creating new participant of type Recipient");
    ResponseEntity<BaseResponse<Integer>> response;
    try {
      Integer useCaseResult = participantsUseCase.createRecipientParticipant(participantMapper.toParticipantDto(participant));
      log.info("Successfully created participant with ID: {}", useCaseResult);

      response = new ResponseEntity<>(BaseResponse.success(useCaseResult), HttpStatus.CREATED);
    } catch (Exception e) {
      response = new ResponseEntity<>(BaseResponse.error(e, log, LogLevel.ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return response;
  }

  @PutMapping("/recipients/{participantId}")
  @Operation(summary = "Updates an existent participant of type Recipient")
  @ApiResponse(responseCode = "200", description = ResponseMessage.SUCCESS_UPDATE)
  @ApiResponse(responseCode = "500", description = "example: generic not found response")
  public ResponseEntity<BaseResponse<Void>> updateRecipientParticipant(@PathVariable Integer participantId, @Valid @RequestBody ParticipantRequestDto participant) {
    log.info("Updating participant of type Recipient with ID: {}", participantId);
    ResponseEntity<BaseResponse<Void>> response;
    try {
      ParticipantDto mappedInput = participantMapper.toParticipantDto(participant, participantId);
      participantsUseCase.updateRecipientParticipant(mappedInput);
      log.info("Successfully updated participant with ID: {}", participantId);

      response = new ResponseEntity<>(BaseResponse.success(null), HttpStatus.OK);
    } catch (Exception e) {
      response = new ResponseEntity<>(BaseResponse.error(e, log, LogLevel.ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return response;
  }

  @GetMapping("/issuers/{participantId}/config")
  @Operation(summary = "Returns the config for the requested issuer")
  @ApiResponse(responseCode = "200", description = ResponseMessage.SUCCESS_RETRIEVAL)
  @ApiResponse(responseCode = "400", description = ResponseMessage.ERROR_NOT_FOUND)
  @ApiResponse(responseCode = "500", description = ResponseMessage.ERROR_INTERNAL)
  public ResponseEntity<BaseResponse<IssuerConfigResponseDto>> getIssuerConfig(@PathVariable Integer participantId) {
    log.info("Retrieving issuer's config for participant with ID {}", participantId);
    ResponseEntity<BaseResponse<IssuerConfigResponseDto>> response;
    try {
      IssuerConfigDto useCaseResult = participantsUseCase.getIssuerConfig(participantId);
      log.info("Successfully retrieved issuer's config");

      response = ResponseEntity.ok(BaseResponse.success(issuerConfigMapper.toIssuerConfigResponseDto(useCaseResult)));
    } catch (ResourceNotFoundException bre) {
      response = new ResponseEntity<>(BaseResponse.error(bre, log, LogLevel.WARN), HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      response = new ResponseEntity<>(BaseResponse.error(e, log, LogLevel.ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return response;
  }

  @PutMapping("/issuers/{participantId}/config/{configId}")
  @Operation(summary = "Updates an existent issuer config")
  @ApiResponse(responseCode = "200", description = ResponseMessage.SUCCESS_UPDATE)
  @ApiResponse(responseCode = "500", description = "example: generic not found response")
  public ResponseEntity<BaseResponse<Void>> updateIssuerConfig(@PathVariable Integer participantId,
                                                               @PathVariable Integer configId,
                                                               @Valid @RequestBody IssuerConfigRequestDto issuerConfig)
  {
    log.info("Updating issuer config for participant with ID : {}", participantId);
    ResponseEntity<BaseResponse<Void>> response;
    try {
      IssuerConfigDto mappedInput = issuerConfigMapper.toIssuerConfigDto(issuerConfig, participantId, configId);
      participantsUseCase.updateIssuerConfig(mappedInput);
      log.info("Successfully updated issuer's config for participant with ID: {}", participantId);

      response = new ResponseEntity<>(BaseResponse.success(null), HttpStatus.OK);
    } catch (Exception e) {
      response = new ResponseEntity<>(BaseResponse.error(e, log, LogLevel.ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return response;
  }

  @DeleteMapping("/recipients/{participantId}")
  @Operation(summary = "Deletes an existent client")
  @ApiResponse(responseCode = "200", description = ResponseMessage.SUCCESS_DELETION)
  @ApiResponse(responseCode = "500", description = ResponseMessage.ERROR_INTERNAL)
  public ResponseEntity<BaseResponse<Void>> deleteRecipient(@PathVariable Integer participantId) {
    log.info("Deleting recipient with ID: {}", participantId);
    ResponseEntity<BaseResponse<Void>> response;
    try {
      participantsUseCase.deleteRecipientParticipant(participantId);
      response = ResponseEntity.ok(BaseResponse.success(null));
    } catch (Exception e) {
      response = new ResponseEntity<>(BaseResponse.error(e, log, LogLevel.ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return response;
  }

}
