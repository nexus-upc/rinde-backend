# language: es
Característica: US17 Consultar resumen consolidado de un viaje
  Como administrador u operador de la empresa
  Quiero ver el resumen de un viaje con sus gastos y liquidación
  Para tener una visión integral desde el dashboard

  Antecedentes:
    Dado que existe una empresa verificada con RUC "20123456789" y administrador "ana@andes.pe"
    Y que el administrador "ana@andes.pe" inició sesión
    Y que el administrador invitó a "Luis Quispe" con el correo "luis@andes.pe" y el rol "DRIVER"
    Y que "luis@andes.pe" definió la contraseña "Clave-De-Luis-2026" con su enlace de invitación
    Y que existe el vehículo "camión"
    Y que el administrador programa el viaje "viaje" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-20"
    Y que el administrador asigna el viaje "viaje" al vehículo "camión" y al conductor "luis@andes.pe"

  Escenario: Resumen de un viaje asignado
    Cuando el administrador consulta el resumen del viaje "viaje"
    Entonces la respuesta tiene código 200
    Y la respuesta incluye el campo "status" con el valor "ASSIGNED"
    Y la respuesta incluye el campo "origin" con el valor "Lima"
    Y la respuesta incluye el campo "destination" con el valor "Arequipa"

  Escenario: Resumen de un viaje finalizado con liquidación
    Dado que el conductor "luis@andes.pe" inició sesión
    Y que el conductor inicia el viaje "viaje"
    Y que el conductor "luis@andes.pe" finaliza el viaje "viaje"
    Cuando el administrador consulta el resumen del viaje "viaje"
    Entonces la respuesta tiene código 200
    Y la respuesta incluye el campo "status" con el valor "FINISHED"

  Escenario: Viaje no encontrado devuelve 404
    Cuando el administrador consulta el resumen de un viaje inexistente
    Entonces la respuesta tiene código 404

  Escenario: Consultar resumen requiere autenticación
    Cuando una persona sin token consulta el resumen del viaje "viaje"
    Entonces la respuesta tiene código 401
