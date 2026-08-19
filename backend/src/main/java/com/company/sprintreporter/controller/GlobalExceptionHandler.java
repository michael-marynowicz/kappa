package com.company.sprintreporter.controller;

import com.company.sprintreporter.application.dto.ApiErrorResponseDto;
import com.company.sprintreporter.service.exception.BusinessException;
import com.company.sprintreporter.service.exception.IssueNotFoundException;
import com.company.sprintreporter.service.exception.JiraException;
import com.company.sprintreporter.service.exception.JiraAuthenticationException;
import com.company.sprintreporter.service.exception.JiraNotConnectedException;
import com.company.sprintreporter.service.exception.NoDashboardSelectedException;
import com.company.sprintreporter.service.exception.JiraPermissionException;
import com.company.sprintreporter.service.exception.JiraApiException;
import com.company.sprintreporter.service.exception.JiraConnectionException;
import com.company.sprintreporter.service.exception.JiraCaptchaRequiredException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Centralized exception handling for all REST controllers.
 * Maps domain/application exceptions to consistent HTTP error responses.
 *
 * Adding a new exception type here is all that's needed —
 * no changes to controllers or services required.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IssueNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDto> handleIssueNotFound(IssueNotFoundException ex) {
        log.warn("Issue not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponseDto.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .error("Not Found")
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponseDto> handleBusinessException(BusinessException ex) {
        log.warn("Business error: {}", ex.getMessage());
        HttpStatus status = ex.getStatus();
        return ResponseEntity.status(status)
                .body(ApiErrorResponseDto.builder()
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponseDto> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponseDto.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("Bad Request")
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDto> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();

        log.warn("Validation failed: {}", fieldErrors);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiErrorResponseDto.builder()
                        .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .error("Validation Failed")
                        .message("One or more fields are invalid")
                        .details(fieldErrors)
                        .build());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponseDto> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponseDto.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("Bad Request")
                        .message("Invalid data for database constraints (role, uniqueness, or numeric range).")
                        .build());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponseDto> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        log.warn("Response status exception: {} - {}", status, ex.getReason());
        return ResponseEntity.status(status)
                .body(ApiErrorResponseDto.builder()
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .message(ex.getReason())
                        .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponseDto> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiErrorResponseDto.builder()
                        .status(HttpStatus.FORBIDDEN.value())
                        .error("Forbidden")
                        .message("You do not have permission to access this resource.")
                        .build());
    }

    @ExceptionHandler(NoDashboardSelectedException.class)
    public ResponseEntity<ApiErrorResponseDto> handleNoDashboard(NoDashboardSelectedException ex) {
        log.warn("No active dashboard: {}", ex.getMessage());
        return ResponseEntity.status(ex.getHttpStatus())
                .body(ApiErrorResponseDto.builder()
                        .status(ex.getHttpStatus().value())
                        .error("No Dashboard Selected")
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(JiraNotConnectedException.class)
    public ResponseEntity<ApiErrorResponseDto> handleJiraNotConnected(JiraNotConnectedException ex) {
        log.warn("Jira not connected for user: {}", ex.getMessage());
        return ResponseEntity.status(ex.getHttpStatus())
                .body(ApiErrorResponseDto.builder()
                        .status(ex.getHttpStatus().value())
                        .error("Jira Not Connected")
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(JiraAuthenticationException.class)
    public ResponseEntity<ApiErrorResponseDto> handleJiraAuth(JiraAuthenticationException ex) {
        log.warn("Jira authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(ex.getHttpStatus())
                .body(ApiErrorResponseDto.builder()
                        .status(ex.getHttpStatus().value())
                        .error("Jira Authentication Failed")
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(JiraCaptchaRequiredException.class)
    public ResponseEntity<ApiErrorResponseDto> handleJiraCaptchaRequired(JiraCaptchaRequiredException ex) {
        log.warn("Jira CAPTCHA lock detected: {}", ex.getMessage());
        return ResponseEntity.status(ex.getHttpStatus())
                .body(ApiErrorResponseDto.builder()
                        .status(ex.getHttpStatus().value())
                        .error("Jira Captcha Required")
                        .errorCode("JIRA_CAPTCHA_REQUIRED")
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(JiraPermissionException.class)
    public ResponseEntity<ApiErrorResponseDto> handleJiraPermission(JiraPermissionException ex) {
        log.warn("Jira permission denied: {}", ex.getMessage());
        return ResponseEntity.status(ex.getHttpStatus())
                .body(ApiErrorResponseDto.builder()
                        .status(ex.getHttpStatus().value())
                        .error("Jira Permission Denied")
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(JiraConnectionException.class)
    public ResponseEntity<ApiErrorResponseDto> handleJiraConnection(JiraConnectionException ex) {
        log.error("Jira connection failed: {}", ex.getMessage(), ex);
        return ResponseEntity.status(ex.getHttpStatus())
                .body(ApiErrorResponseDto.builder()
                        .status(ex.getHttpStatus().value())
                        .error("Jira Connection Failed")
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(JiraApiException.class)
    public ResponseEntity<ApiErrorResponseDto> handleJiraApi(JiraApiException ex) {
        log.error("Jira API error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(ex.getHttpStatus())
                .body(ApiErrorResponseDto.builder()
                        .status(ex.getHttpStatus().value())
                        .error("Jira API Error")
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(JiraException.class)
    public ResponseEntity<ApiErrorResponseDto> handleJira(JiraException ex) {
        log.error("Jira error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(ex.getHttpStatus())
                .body(ApiErrorResponseDto.builder()
                        .status(ex.getHttpStatus().value())
                        .error("Jira Error")
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(org.springframework.web.reactive.function.client.WebClientResponseException.class)
    public ResponseEntity<ApiErrorResponseDto> handleWebClientResponse(org.springframework.web.reactive.function.client.WebClientResponseException ex) {
        int statusCode = ex.getStatusCode().value();
        String message = switch(statusCode) {
            case 401 -> "Jira authentication failed: invalid credentials or expired token. " +
                       "Please verify your PAT/API token and base URL.";
            case 403 -> "Jira permission denied: your account lacks required permissions. " +
                        "Verify the token has access to the project.";
            case 404 -> "Jira API endpoint not found: verify base URL is correct (e.g. https://jira.company.com).";
            case 500, 502, 503 -> "Jira server error: " + ex.getStatusText() + ". Please try again later.";
            default -> "Jira API error: HTTP " + statusCode + " - " + ex.getStatusText();
        };
        
        log.error("Jira API error ({}): {}", statusCode, ex.getStatusText(), ex);
        
        HttpStatus httpStatus = HttpStatus.valueOf(statusCode < 500 ? statusCode : HttpStatus.BAD_GATEWAY.value());
        return ResponseEntity.status(httpStatus)
                .body(ApiErrorResponseDto.builder()
                        .status(httpStatus.value())
                        .error(httpStatus.getReasonPhrase())
                        .message(message)
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponseDto> handleUnexpected(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponseDto.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error("Internal Server Error")
                        .message("An unexpected error occurred. Please try again later.")
                        .build());
    }
}
