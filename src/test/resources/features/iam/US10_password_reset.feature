# language: es
Característica: US10 Recuperar la contraseña
  Como usuario que olvidó su contraseña
  Quiero recibir un enlace para definir una contraseña nueva
  Para volver a ingresar a RINDE sin pedir ayuda

  Antecedentes:
    Dado que existe una empresa verificada con RUC "20123456789" y administrador "ana@andes.pe"

  Escenario: Solicitar la recuperación con un correo registrado
    Cuando "ana@andes.pe" solicita recuperar su contraseña
    Entonces la respuesta tiene código 202
    Y se publica el evento "PasswordResetRequested" para "ana@andes.pe"

  Escenario: Solicitar la recuperación con un correo desconocido no revela nada
    Cuando "nadie@andes.pe" solicita recuperar su contraseña
    Entonces la respuesta tiene código 202
    Y no se publica el evento "PasswordResetRequested"

  Escenario: Definir una contraseña nueva con el enlace de recuperación
    Dado "ana@andes.pe" solicita recuperar su contraseña
    Cuando defino la contraseña "Nueva-Clave-2026" con el enlace de recuperación recibido
    Entonces la respuesta tiene código 204
    Y "ana@andes.pe" puede iniciar sesión con la contraseña "Nueva-Clave-2026"
    Y "ana@andes.pe" no puede iniciar sesión con la contraseña "Clave-Segura-2026"

  Escenario: Un enlace de recuperación ya usado es rechazado
    Dado "ana@andes.pe" solicita recuperar su contraseña
    Y que cambié mi contraseña a "Nueva-Clave-2026" con el enlace de recuperación recibido
    Cuando defino la contraseña "Otra-Clave-2026" con el enlace de recuperación recibido
    Entonces la respuesta tiene código 400
    Y el mensaje de error es "El enlace no es válido, venció o ya fue utilizado."

  Escenario: Un enlace de recuperación vencido es rechazado
    Dado "ana@andes.pe" solicita recuperar su contraseña
    Y que el enlace recibido venció
    Cuando defino la contraseña "Nueva-Clave-2026" con el enlace de recuperación recibido
    Entonces la respuesta tiene código 400
    Y el mensaje de error es "El enlace no es válido, venció o ya fue utilizado."
    Y "ana@andes.pe" puede iniciar sesión con la contraseña "Clave-Segura-2026"

  Escenario: Una contraseña demasiado corta es rechazada
    Dado "ana@andes.pe" solicita recuperar su contraseña
    Cuando defino la contraseña "corta" con el enlace de recuperación recibido
    Entonces la respuesta tiene código 400
