# language: es
Característica: US17 Consultar el detalle de un viaje
  Como usuario autorizado de una empresa
  Quiero consultar la información operativa de un viaje
  Para conocer su ruta, carga, asignación e historial

  Antecedentes:
    Dado que existe una empresa verificada con RUC "20123456789" y administrador "ana@andes.pe"
    Y que el administrador "ana@andes.pe" inició sesión
    Y que el administrador invitó a "Luis Quispe" con el correo "luis@andes.pe" y el rol "DRIVER"
    Y que "luis@andes.pe" definió la contraseña "Clave-De-Luis-2026" con su enlace de invitación
    Y que existe el vehículo "camión"

  Escenario: Consultar todos los datos operativos del viaje
    Cuando el administrador programa el viaje "viaje" de "Lima" a "Arequipa" con carga "Repuestos", peso "850.25" y salida "2026-10-20"
    Y que el viaje "viaje" ya fue asignado al vehículo "camión" y al conductor "luis@andes.pe"
    Cuando el administrador consulta el detalle del viaje "viaje"
    Entonces la respuesta tiene código 200
    Y el viaje queda en estado "ASSIGNED"
    Y el peso de la carga es "850.25"
    Y el detalle muestra la fecha de asignación
    Y el historial contiene los estados "SCHEDULED,ASSIGNED"
    Y la respuesta no incluye el campo "advance"
    Y la respuesta no incluye el campo "observations"

  Escenario: El detalle muestra el viaje liquidado al cerrarse su liquidación
    Cuando el administrador programa el viaje "viaje" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-20"
    Y que el viaje "viaje" ya fue asignado al vehículo "camión" y al conductor "luis@andes.pe"
    Y el conductor "luis@andes.pe" inicia el viaje "viaje"
    Y el conductor "luis@andes.pe" finaliza el viaje "viaje"
    Y que la liquidación del viaje "viaje" de la empresa con RUC "20123456789" fue cerrada
    Cuando el administrador consulta el detalle del viaje "viaje"
    Entonces la respuesta tiene código 200
    Y el viaje queda en estado "SETTLED"
    Y el historial contiene los estados "SCHEDULED,ASSIGNED,IN_ROUTE,FINISHED,SETTLED"

  Escenario: El conductor puede consultar su propio viaje
    Cuando el administrador programa el viaje "viaje" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-20"
    Y que el viaje "viaje" ya fue asignado al vehículo "camión" y al conductor "luis@andes.pe"
    Cuando el usuario "luis@andes.pe" consulta el detalle del viaje "viaje"
    Entonces la respuesta tiene código 200
    Y el viaje queda en estado "ASSIGNED"

  Escenario: El conductor no puede consultar el viaje de otro conductor
    Dado que el administrador invitó a "Marta Ríos" con el correo "marta@andes.pe" y el rol "DRIVER"
    Y que "marta@andes.pe" definió la contraseña "Clave-De-Marta-2026" con su enlace de invitación
    Cuando el administrador programa el viaje "viaje" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-20"
    Y que el viaje "viaje" ya fue asignado al vehículo "camión" y al conductor "luis@andes.pe"
    Cuando el usuario "marta@andes.pe" consulta el detalle del viaje "viaje"
    Entonces la respuesta tiene código 404

  Escenario: Una empresa no puede consultar el viaje de otra empresa
    Cuando el administrador programa el viaje "viaje" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-20"
    Dado que existe una empresa verificada con RUC "20987654321" y administrador "otro@otra.pe"
    Y que el administrador "otro@otra.pe" inició sesión
    Cuando un administrador de otra empresa consulta el viaje "viaje"
    Entonces la respuesta tiene código 404

  Escenario: Un viaje inexistente responde 404
    Cuando el administrador consulta un viaje que no existe
    Entonces la respuesta tiene código 404

  Escenario: Consultar el detalle requiere autenticación
    Cuando el administrador programa el viaje "viaje" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-20"
    Cuando una persona sin token consulta el detalle del viaje "viaje"
    Entonces la respuesta tiene código 401
