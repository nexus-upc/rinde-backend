# language: es
Característica: US22 Sincronizar gastos registrados sin conexión (Offline Sync)
  Como conductor
  Quiero enviar un lote de gastos registrados durante el viaje sin conexión
  Para sincronizarlos con el sistema en cuanto recupere conectividad a internet

  Antecedentes:
    Dado que existe una empresa verificada con RUC "20123456789" y administrador "ana@andes.pe"
    Y que el administrador "ana@andes.pe" inició sesión
    Y que el administrador invitó a "Luis Quispe" con el correo "luis@andes.pe" y el rol "DRIVER"
    Y que "luis@andes.pe" definió la contraseña "Clave-De-Luis-2026" con su enlace de invitación
    Y que existe el vehículo "camión"
    Y que el administrador programa el viaje "viaje" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-20"
    Y que el administrador asigna el viaje "viaje" al vehículo "camión" y al conductor "luis@andes.pe"
    Y que el conductor "luis@andes.pe" inició sesión
    Y que el conductor inicia el viaje "viaje"

  Escenario: Sincronizar con éxito un lote de gastos capturados offline
    Cuando el conductor sincroniza un lote con 2 gastos para el viaje "viaje"
    Entonces la respuesta tiene código 200
    Y la respuesta incluye el campo "totalReceived" con el valor "2"
    Y la respuesta incluye el campo "synchronizedCount" con el valor "2"

  Escenario: Sincronización idempotente de lote con gastos ya existentes
    Dado que el conductor registró un gasto "combustible" de tipo "FUEL" por el monto "100.00" con clave "KEY-OFFLINE-01" en el viaje "viaje"
    Cuando el conductor sincroniza un lote que incluye el gasto con clave "KEY-OFFLINE-01" y uno nuevo con clave "KEY-OFFLINE-02" para el viaje "viaje"
    Entonces la respuesta tiene código 200
    Y la respuesta incluye el campo "totalReceived" con el valor "2"
    Y la respuesta incluye el campo "synchronizedCount" con el valor "2"

  Escenario: Lote con un gasto de un viaje que no existe guarda solo el gasto válido
    Cuando el conductor sincroniza un lote con un gasto para el viaje "viaje" y otro para un viaje que no existe
    Entonces la respuesta tiene código 200
    Y la respuesta incluye el campo "synchronizedCount" con el valor "1"
    Y la respuesta incluye el campo "rejectedCount" con el valor "1"
    Y el elemento 0 de rechazados tiene el campo "code" con el valor "TRIP_NOT_FOUND"
    Y el elemento 0 de rechazados tiene el campo "reason" con el valor "El viaje no existe."
    Cuando el conductor consulta los gastos del viaje "viaje"
    Entonces la lista de gastos contiene 1 elementos

  Escenario: Sincronizar sin autenticación es rechazado
    Cuando una persona sin token sincroniza un lote de gastos para el viaje "viaje"
    Entonces la respuesta tiene código 401
