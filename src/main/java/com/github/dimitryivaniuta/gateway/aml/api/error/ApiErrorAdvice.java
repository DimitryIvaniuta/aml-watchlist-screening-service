package com.github.dimitryivaniuta.gateway.aml.api.error;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

/**
 * Global API error handler producing RFC7807 Problem Details responses.
 */
@RestControllerAdvice
public class ApiErrorAdvice {

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ProblemDetail> badRequest(IllegalArgumentException ex, HttpServletRequest req) {
    return problem(HttpStatus.BAD_REQUEST, "bad_request", ex.getMessage(), req, null);
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ProblemDetail> conflict(IllegalStateException ex, HttpServletRequest req) {
    return problem(HttpStatus.CONFLICT, "conflict", ex.getMessage(), req, null);
  }

  @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
  public ResponseEntity<ProblemDetail> validation(Exception ex, HttpServletRequest req) {
    return problem(HttpStatus.BAD_REQUEST, "validation_failed", "Validation failed", req, Map.of("details", ex.getMessage()));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ProblemDetail> forbidden(AccessDeniedException ex, HttpServletRequest req) {
    return problem(HttpStatus.FORBIDDEN, "forbidden", "Access denied", req, null);
  }

  @ExceptionHandler(ErrorResponseException.class)
  public ResponseEntity<ProblemDetail> errorResponse(ErrorResponseException ex, HttpServletRequest req) {
    ProblemDetail pd = ex.getBody();
    if (pd != null) {
      pd.setProperty("timestamp", Instant.now().toString());
      pd.setProperty("path", req.getRequestURI());
      return ResponseEntity.status(ex.getStatusCode()).body(pd);
    }
    return problem(HttpStatus.valueOf(ex.getStatusCode().value()), "error", ex.getMessage(), req, null);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> serverError(Exception ex, HttpServletRequest req) {
    return problem(HttpStatus.INTERNAL_SERVER_ERROR, "internal_error", "Unexpected error", req, Map.of("error", ex.getMessage()));
  }

  private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String code, String detail, HttpServletRequest req, Map<String, Object> extra) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
    pd.setTitle(code);
    pd.setProperty("code", code);
    pd.setProperty("timestamp", Instant.now().toString());
    pd.setProperty("path", req.getRequestURI());
    if (extra != null) extra.forEach(pd::setProperty);
    return ResponseEntity.status(status).body(pd);
  }
}
