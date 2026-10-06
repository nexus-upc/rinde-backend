# language: es
Característica: US19 Consultar el detalle propio de un viaje
  Como conductor
  Quiero abrir el detalle de un viaje asignado a mí
  Para revisar su carga, ruta y estado

  Antecedentes:
    Dado que existe una empresa verificada con RUC "20123456789" y administrador "ana@andes.pe"
    Y que el administrador "ana@andes.pe" inició sesión
    Y que el administrador invitó a "Luis Quispe" con el correo "luis@andes.pe" y el rol "DRIVER"
    Y que "luis@andes.pe" definió la contraseña "Clave-De-Luis-2026" con su enlace de invitación
    Y que existe el vehículo "camión"

  Escenario: El conductor abre el detalle de su viaje
    Cuando el administrador programa el viaje "viaje" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-20"
    Y el administrador asigna el viaje "viaje" al vehículo "camión" y al conductor "luis@andes.pe"
    Cuando el usuario "luis@andes.pe" consulta el detalle del viaje "viaje"
    Entonces la respuesta tiene código 200
    Y la respuesta incluye el campo "origin" con el valor "Lima"
    Y la respuesta incluye el campo "destination" con el valor "Arequipa"
    Y la respuesta no incluye el campo "advance"
    Y la respuesta no incluye el campo "observations"

  Escenario: El conductor recibe 404 por un viaje ajeno
    Dado que el administrador invitó a "Marta Ríos" con el correo "marta@andes.pe" y el rol "DRIVER"
    Y que "marta@andes.pe" definió la contraseña "Clave-De-Marta-2026" con su enlace de invitación
    Cuando el administrador programa el viaje "viaje" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-20"
    Y el administrador asigna el viaje "viaje" al vehículo "camión" y al conductor "luis@andes.pe"
    Cuando el usuario "marta@andes.pe" consulta el detalle del viaje "viaje"
    Entonces la respuesta tiene código 404

  Escenario: Un administrador conserva acceso al detalle operativo
    Cuando el administrador programa el viaje "viaje" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-20"
    Cuando el administrador consulta el detalle del viaje "viaje"
    Entonces la respuesta tiene código 200
    Y el viaje queda en estado "SCHEDULED"
