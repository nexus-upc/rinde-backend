package com.nexus.rinde.iam.domain.services;

import com.nexus.rinde.iam.domain.model.commands.DefinePasswordCommand;
import com.nexus.rinde.iam.domain.model.commands.RequestPasswordResetCommand;
import com.nexus.rinde.iam.domain.model.commands.SignInCommand;
import com.nexus.rinde.iam.domain.model.valueobjects.AuthenticationResult;

/** Casos de uso de acceso: iniciar sesión, solicitar la recuperación y definir la contraseña. */
public interface AuthenticationCommandService {

  AuthenticationResult handle(SignInCommand command);

  void handle(RequestPasswordResetCommand command);

  void handle(DefinePasswordCommand command);
}
