# language: es
Característica: US20 Iniciar y finalizar un viaje
  Como conductor asignado
  Quiero registrar el inicio y el fin del viaje
  Para conservar su estado y habilitar a los contextos posteriores

  Antecedentes:
    Dado que existe una empresa verificada con RUC "20123456789" y administrador "ana@andes.pe"
    Y que el administrador "ana@andes.pe" inició sesión
    Y que el administrador invitó a "Luis Quispe" con el correo "luis@andes.pe" y el rol "DRIVER"
    Y que "luis@andes.pe" definió la contraseña "Clave-De-Luis-2026" con su enlace de invitación
    Y que existe el vehículo "camión"

  Escenario: El conductor inicia y finaliza su viaje
    Cuando el administrador programa el viaje "viaje" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-20"
    Y el administrador asigna el viaje "viaje" al vehículo "camión" y al conductor "luis@andes.pe"
    Cuando el conductor "luis@andes.pe" inicia el viaje "viaje"
    Entonces la respuesta tiene código 200
    Y el viaje queda en estado "IN_ROUTE"
    Y se publica el evento "TripStarted" para "luis@andes.pe"
    Cuando el conductor "luis@andes.pe" finaliza el viaje "viaje"
    Entonces la respuesta tiene código 200
    Y el viaje queda en estado "FINISHED"
    Y la fecha de inicio y fin están registradas
    Y el historial contiene los estados "SCHEDULED,ASSIGNED,IN_ROUTE,FINISHED"
    Y se publica el evento "TripFinished" para "luis@andes.pe"

  Escenario: Finalizar antes de iniciar produce conflicto de estado
    Cuando el administrador programa el viaje "viaje" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-20"
    Y el administrador asigna el viaje "viaje" al vehículo "camión" y al conductor "luis@andes.pe"
    Cuando el conductor "luis@andes.pe" finaliza el viaje "viaje"
    Entonces la respuesta tiene código 409
    Y el mensaje de error contiene "ASSIGNED"

  Escenario: No se puede iniciar nuevamente un viaje finalizado
    Cuando el administrador programa el viaje "viaje" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-20"
    Y el administrador asigna el viaje "viaje" al vehículo "camión" y al conductor "luis@andes.pe"
    Y el conductor "luis@andes.pe" inicia el viaje "viaje"
    Y el conductor "luis@andes.pe" finaliza el viaje "viaje"
    Cuando el conductor "luis@andes.pe" inicia el viaje "viaje"
    Entonces la respuesta tiene código 409
    Y el mensaje de error contiene "FINISHED"

  Escenario: Un conductor distinto recibe 404
    Dado que el administrador invitó a "Marta Ríos" con el correo "marta@andes.pe" y el rol "DRIVER"
    Y que "marta@andes.pe" definió la contraseña "Clave-De-Marta-2026" con su enlace de invitación
    Cuando el administrador programa el viaje "viaje" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-20"
    Y el administrador asigna el viaje "viaje" al vehículo "camión" y al conductor "luis@andes.pe"
    Cuando el conductor "marta@andes.pe" intenta iniciar el viaje "viaje"
    Entonces la respuesta tiene código 404

  Escenario: Otra empresa recibe 404 al iniciar un viaje
    Cuando el administrador programa el viaje "viaje" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-20"
    Y el administrador asigna el viaje "viaje" al vehículo "camión" y al conductor "luis@andes.pe"
    Dado que existe una empresa verificada con RUC "20987654321" y administrador "otro@otra.pe"
    Y que el administrador "otro@otra.pe" inició sesión
    Y que el administrador invitó a "Mario Soto" con el correo "mario@otra.pe" y el rol "DRIVER"
    Y que "mario@otra.pe" definió la contraseña "Clave-De-Mario-2026" con su enlace de invitación
    Cuando el conductor "mario@otra.pe" intenta iniciar el viaje "viaje"
    Entonces la respuesta tiene código 404

  Escenario: Un administrador no puede iniciar el viaje
    Cuando el administrador programa el viaje "viaje" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-20"
    Cuando el administrador intenta iniciar el viaje "viaje"
    Entonces la respuesta tiene código 403
