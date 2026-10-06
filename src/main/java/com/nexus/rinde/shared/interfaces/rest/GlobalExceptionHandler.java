package com.nexus.rinde.shared.interfaces.rest;

import com.nexus.rinde.shared.domain.exceptions.AuthenticationFailedException;
import com.nexus.rinde.shared.domain.exceptions.BusinessRuleException;
import com.nexus.rinde.shared.domain.exceptions.ConflictException;
import com.nexus.rinde.shared.domain.exceptions.ForbiddenOperationException;
import com.nexus.rinde.shared.domain.exceptions.ResourceNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Traduce las excepciones de dominio y de Spring MVC a respuestas RFC 7807 (ProblemDetail) con
 * traceId, para que todos los contextos respondan los errores con el mismo formato (CRN-06).
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException ex) {
    return respond(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(BusinessRuleException.class)
  public ResponseEntity<ProblemDetail> handleBusinessRule(BusinessRuleException ex) {
    return respond(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ProblemDetail> handleConflict(ConflictException ex) {
    return respond(HttpStatus.CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(ForbiddenOperationException.class)
  public ResponseEntity<ProblemDetail> handleForbidden(ForbiddenOperationException ex) {
    return respond(HttpStatus.FORBIDDEN, ex.getMessage());
  }

  @ExceptionHandler(AuthenticationFailedException.class)
  public ResponseEntity<ProblemDetail> handleAuthenticationFailed(
      AuthenticationFailedException ex) {
    return respond(HttpStatus.UNAUTHORIZED, ex.getMessage());
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex) {
    return respond(HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta operación.");
  }

  /**
   * Último recurso: no expone detalles internos, solo el traceId para ubicar el error en el log.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex) {
    log.error("Error inesperado", ex);
    return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado.");
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    Map<String, String> errors = new LinkedHashMap<>();
    for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
      errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
    }
    ProblemDetail problem =
        ProblemDetails.of(HttpStatus.BAD_REQUEST, "La solicitud tiene datos inválidos.");
    problem.setProperty("errors", errors);
    return ResponseEntity.badRequest().body(problem);
  }

  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      Exception ex,
      Object body,
      HttpHeaders headers,
      HttpStatusCode statusCode,
      WebRequest request) {
    ResponseEntity<Object> response =
        super.handleExceptionInternal(ex, body, headers, statusCode, request);
    if (response != null && response.getBody() instanceof ProblemDetail problem) {
      ProblemDetails.addTraceId(problem);
    }
    return response;
  }

  private ResponseEntity<ProblemDetail> respond(HttpStatus status, String detail) {
    return ResponseEntity.status(status).body(ProblemDetails.of(status, detail));
  }
}
