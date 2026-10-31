package com.nexus.rinde.shared.interfaces.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nexus.rinde.shared.domain.exceptions.AuthenticationFailedException;
import com.nexus.rinde.shared.domain.exceptions.BusinessRuleException;
import com.nexus.rinde.shared.domain.exceptions.ConflictException;
import com.nexus.rinde.shared.domain.exceptions.ForbiddenOperationException;
import com.nexus.rinde.shared.domain.exceptions.ResourceNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new ThrowingController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .addFilters(new CorrelationIdFilter())
            .build();
  }

  @Test
  void notFoundReturns404WithTraceId() throws Exception {
    mockMvc
        .perform(get("/test/not-found").header(CorrelationIdFilter.HEADER, "abc-123"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.detail").value("no existe"))
        .andExpect(jsonPath("$.traceId").value("abc-123"));
  }

  @Test
  void businessRuleReturns400() throws Exception {
    mockMvc.perform(get("/test/business-rule")).andExpect(status().isBadRequest());
  }

  @Test
  void conflictReturns409() throws Exception {
    mockMvc.perform(get("/test/conflict")).andExpect(status().isConflict());
  }

  @Test
  void forbiddenReturns403() throws Exception {
    mockMvc.perform(get("/test/forbidden")).andExpect(status().isForbidden());
  }

  @Test
  void authenticationFailedReturns401() throws Exception {
    mockMvc.perform(get("/test/unauthorized")).andExpect(status().isUnauthorized());
  }

  @Test
  void accessDeniedReturns403() throws Exception {
    mockMvc.perform(get("/test/access-denied")).andExpect(status().isForbidden());
  }

  @Test
  void unexpectedErrorReturns500WithoutInternalDetails() throws Exception {
    mockMvc
        .perform(get("/test/boom"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.detail").value("Ocurrió un error inesperado."))
        .andExpect(jsonPath("$.traceId").exists());
  }

  @Test
  void invalidBodyReturns400WithFieldErrors() throws Exception {
    mockMvc
        .perform(post("/test/validated").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.name").exists())
        .andExpect(jsonPath("$.traceId").exists());
  }

  @Test
  void unsupportedMethodKeepsProblemFormatWithTraceId() throws Exception {
    mockMvc
        .perform(post("/test/not-found"))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.traceId").exists());
  }

  @RestController
  static class ThrowingController {

    @GetMapping("/test/not-found")
    void notFound() {
      throw new ResourceNotFoundException("no existe");
    }

    @GetMapping("/test/business-rule")
    void businessRule() {
      throw new BusinessRuleException("regla");
    }

    @GetMapping("/test/conflict")
    void conflict() {
      throw new ConflictException("conflicto");
    }

    @GetMapping("/test/forbidden")
    void forbidden() {
      throw new ForbiddenOperationException("prohibido");
    }

    @GetMapping("/test/unauthorized")
    void unauthorized() {
      throw new AuthenticationFailedException("credenciales");
    }

    @GetMapping("/test/access-denied")
    void accessDenied() {
      throw new AccessDeniedException("denegado");
    }

    @GetMapping("/test/boom")
    void boom() {
      throw new IllegalStateException("detalle interno");
    }

    @PostMapping("/test/validated")
    void validated(@Valid @RequestBody Body body) {}
  }

  record Body(@NotBlank String name) {}
}
