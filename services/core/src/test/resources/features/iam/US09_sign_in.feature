# language: es
Característica: US09 Iniciar sesión
  Como usuario de una empresa en RINDE
  Quiero iniciar sesión con mi correo y mi contraseña
  Para acceder solo a los datos de mi empresa según mi rol

  Antecedentes:
    Dado que existe una empresa verificada con RUC "20123456789" y administrador "ana@andes.pe"

  Escenario: Iniciar sesión con credenciales correctas
    Cuando "ana@andes.pe" inicia sesión con la contraseña "Clave-Segura-2026"
    Entonces la respuesta tiene código 200
    Y el token de acceso indica el rol "ADMINISTRATOR", la empresa de RUC "20123456789" y el estado de empresa "ACTIVE"

  Escenario: Una contraseña incorrecta es rechazada
    Cuando "ana@andes.pe" inicia sesión con la contraseña "clave-equivocada"
    Entonces la respuesta tiene código 401
    Y el mensaje de error es "El correo o la contraseña son incorrectos."

  Escenario: Un correo no registrado recibe el mismo mensaje
    Cuando "nadie@andes.pe" inicia sesión con la contraseña "Clave-Segura-2026"
    Entonces la respuesta tiene código 401
    Y el mensaje de error es "El correo o la contraseña son incorrectos."

  Escenario: Una empresa pendiente de verificación no puede iniciar sesión
    Dado que registré la empresa con RUC "20987654321" y administrador "luis@pendiente.pe" sin confirmar el correo
    Cuando "luis@pendiente.pe" inicia sesión con la contraseña "Clave-Segura-2026"
    Entonces la respuesta tiene código 403
    Y el mensaje de error contiene "confirmó su correo"

  Escenario: Una empresa restringida puede iniciar sesión y el token lo indica
    Dado que la suscripción de la empresa con RUC "20123456789" fue suspendida
    Cuando "ana@andes.pe" inicia sesión con la contraseña "Clave-Segura-2026"
    Entonces la respuesta tiene código 200
    Y el token de acceso indica el rol "ADMINISTRATOR", la empresa de RUC "20123456789" y el estado de empresa "RESTRICTED"

  Escenario: Un usuario deshabilitado no puede iniciar sesión
    Dado que el administrador "ana@andes.pe" inició sesión
    Y que el administrador invitó a "Luis Quispe" con el correo "luis@andes.pe" y el rol "DRIVER"
    Y que "luis@andes.pe" definió la contraseña "Clave-De-Luis-2026" con su enlace de invitación
    Y que el administrador deshabilitó al usuario "luis@andes.pe"
    Cuando "luis@andes.pe" inicia sesión con la contraseña "Clave-De-Luis-2026"
    Entonces la respuesta tiene código 403
    Y el mensaje de error contiene "deshabilitado"
