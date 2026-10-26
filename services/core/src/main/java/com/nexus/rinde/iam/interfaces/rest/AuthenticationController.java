package com.nexus.rinde.iam.interfaces.rest;

import com.nexus.rinde.iam.domain.services.AuthenticationCommandService;
import com.nexus.rinde.iam.interfaces.rest.resources.AuthenticationResource;
import com.nexus.rinde.iam.interfaces.rest.resources.DefinePasswordResource;
import com.nexus.rinde.iam.interfaces.rest.resources.PasswordResetRequestResource;
import com.nexus.rinde.iam.interfaces.rest.resources.SignInResource;
import com.nexus.rinde.iam.interfaces.rest.transform.AuthenticationResourceFromResultAssembler;
import com.nexus.rinde.iam.interfaces.rest.transform.DefinePasswordCommandFromResourceAssembler;
import com.nexus.rinde.iam.interfaces.rest.transform.PasswordResetCommandFromResourceAssembler;
import com.nexus.rinde.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Acceso a RINDE: inicio de sesión (US09) y recuperación de contraseña (US10, US11). Rutas
 * públicas.
 */
@RestController
@RequestMapping(value = "/api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Inicio de sesión y contraseñas (US09, US10, US11)")
@SecurityRequirements
public class AuthenticationController {

  private final AuthenticationCommandService authenticationCommandService;

  public AuthenticationController(AuthenticationCommandService authenticationCommandService) {
    this.authenticationCommandService = authenticationCommandService;
  }

  @PostMapping("/sign-in")
  @Operation(
      summary = "Iniciar sesión y obtener el token",
      description =
          "Devuelve un token JWT con el usuario, su rol, su empresa y el estado de la empresa."
              + " Una empresa pendiente de verificación no puede iniciar sesión.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Sesión iniciada"),
    @ApiResponse(
        responseCode = "401",
        description = "Correo o contraseña incorrectos",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Empresa pendiente de verificación o usuario deshabilitado",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class)))
  })
  public ResponseEntity<AuthenticationResource> signIn(
      @Valid @RequestBody SignInResource resource) {
    var result =
        authenticationCommandService.handle(SignInCommandFromResourceAssembler.toCommand(resource));
    return ResponseEntity.ok(AuthenticationResourceFromResultAssembler.toResource(result));
  }

  @PostMapping("/password-reset")
  @Operation(
      summary = "Solicitar la recuperación de contraseña",
      description =
          "Siempre responde 202, exista o no el correo, para no revelar quién está registrado."
              + " El enlace de recuperación (vigente 1 hora) se registra en el log del servidor.")
  @ApiResponses({
    @ApiResponse(responseCode = "202", description = "Solicitud recibida"),
    @ApiResponse(
        responseCode = "400",
        description = "Falta el correo",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class)))
  })
  public ResponseEntity<Void> requestPasswordReset(
      @Valid @RequestBody PasswordResetRequestResource resource) {
    authenticationCommandService.handle(
        PasswordResetCommandFromResourceAssembler.toCommand(resource));
    return ResponseEntity.accepted().build();
  }

  @PostMapping("/password")
  @Operation(
      summary = "Definir la contraseña con un enlace de acceso",
      description =
          "Con un enlace de invitación activa al usuario; con un enlace de recuperación solo"
              + " cambia la contraseña. El enlace queda usado.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Contraseña definida"),
    @ApiResponse(
        responseCode = "400",
        description = "Enlace inválido, vencido o ya usado, o contraseña inválida",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class)))
  })
  public ResponseEntity<Void> definePassword(@Valid @RequestBody DefinePasswordResource resource) {
    authenticationCommandService.handle(
        DefinePasswordCommandFromResourceAssembler.toCommand(resource));
    return ResponseEntity.noContent().build();
  }
}
