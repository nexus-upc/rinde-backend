# language: es
Característica: US15 Asignar vehículo y conductor
  Como administrador u operador
  Quiero asignar un vehículo y un conductor habilitado a un viaje
  Para preparar su ejecución sin cruces de agenda

  Antecedentes:
    Dado que existe una empresa verificada con RUC "20123456789" y administrador "ana@andes.pe"
    Y que el administrador "ana@andes.pe" inició sesión
    Y que el administrador invitó a "Luis Quispe" con el correo "luis@andes.pe" y el rol "DRIVER"
    Y que "luis@andes.pe" definió la contraseña "Clave-De-Luis-2026" con su enlace de invitación
    Y que existe el vehículo "camión"

  Escenario: Asignar recursos libres y publicar TripAssigned
    Cuando el administrador programa el viaje "viaje" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-20"
    Y el administrador asigna el viaje "viaje" al vehículo "camión" y al conductor "luis@andes.pe"
    Entonces la respuesta tiene código 200
    Y la respuesta incluye el campo "status" con el valor "ASSIGNED"
    Y la respuesta no incluye una alerta de mantenimiento
    Y se publica el evento "TripAssigned" para "luis@andes.pe"

  Escenario: Mostrar alerta si el mantenimiento está próximo
    Dado que Fleet informa mantenimiento próximo
    Cuando el administrador programa el viaje "viaje" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-20"
    Y el administrador asigna el viaje "viaje" al vehículo "camión" y al conductor "luis@andes.pe"
    Entonces la respuesta tiene código 200
    Y la respuesta incluye una alerta de mantenimiento "DUE_SOON"

  Escenario: Rechazar un conductor no habilitado
    Dado que Fleet informa que el conductor no está habilitado
    Cuando el administrador programa el viaje "viaje" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-20"
    Y el administrador asigna el viaje "viaje" al vehículo "camión" y al conductor "luis@andes.pe"
    Entonces la respuesta tiene código 409
    Y el mensaje de error es "El conductor no está habilitado para ser asignado."

  Escenario: El mantenimiento vencido requiere confirmación
    Dado que Fleet informa mantenimiento vencido
    Cuando el administrador programa el viaje "viaje" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-20"
    Y el administrador asigna el viaje "viaje" al vehículo "camión" y al conductor "luis@andes.pe"
    Entonces la respuesta tiene código 409
    Y el mensaje de error es "La unidad tiene el mantenimiento vencido. Confirme la asignación para continuar."

  Escenario: Guardar quién confirmó mantenimiento vencido
    Dado que Fleet informa mantenimiento vencido
    Cuando el administrador programa el viaje "viaje" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-20"
    Y el administrador confirma el mantenimiento vencido y asigna el viaje "viaje" al vehículo "camión" y al conductor "luis@andes.pe"
    Entonces la respuesta tiene código 200
    Cuando el administrador consulta el detalle del viaje "viaje"
    Entonces el detalle registra quién confirmó el mantenimiento vencido

  Escenario: Impedir reutilizar el vehículo en la misma fecha
    Cuando el administrador programa el viaje "primero" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-20"
    Y el administrador asigna el viaje "primero" al vehículo "camión" y al conductor "luis@andes.pe"
    Y el administrador programa el viaje "segundo" de "Lima" a "Cusco" con carga "Muebles" y salida "2026-10-20"
    Y el administrador asigna el viaje "segundo" al vehículo "camión" y al conductor "luis@andes.pe"
    Entonces la respuesta tiene código 409
    Y el mensaje de error contiene "vehículo"

  Escenario: Impedir reutilizar el conductor en la misma fecha
    Cuando el administrador programa el viaje "primero" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-20"
    Y el administrador asigna el viaje "primero" al vehículo "camión" y al conductor "luis@andes.pe"
    Y que existe el vehículo "otro camión"
    Y el administrador programa el viaje "segundo" de "Lima" a "Cusco" con carga "Muebles" y salida "2026-10-20"
    Y el administrador asigna el viaje "segundo" al vehículo "otro camión" y al conductor "luis@andes.pe"
    Entonces la respuesta tiene código 409
    Y el mensaje de error contiene "conductor"

  Escenario: Una asignación solo puede hacerse desde SCHEDULED
    Cuando el administrador programa el viaje "viaje" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-20"
    Y el administrador asigna el viaje "viaje" al vehículo "camión" y al conductor "luis@andes.pe"
    Y el administrador asigna el viaje "viaje" al vehículo "camión" y al conductor "luis@andes.pe"
    Entonces la respuesta tiene código 409
    Y el mensaje de error contiene "ASSIGNED"

  Escenario: Un conductor no puede asignar recursos
    Cuando el administrador programa el viaje "viaje" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-20"
    Cuando el conductor "luis@andes.pe" intenta asignar el viaje "viaje"
    Entonces la respuesta tiene código 403

  Escenario: Una empresa no puede asignar el viaje de otra empresa
    Cuando el administrador programa el viaje "viaje" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-20"
    Dado que existe una empresa verificada con RUC "20987654321" y administrador "otro@otra.pe"
    Y que el administrador "otro@otra.pe" inició sesión
    Cuando un administrador de otra empresa asigna el viaje "viaje"
    Entonces la respuesta tiene código 404
