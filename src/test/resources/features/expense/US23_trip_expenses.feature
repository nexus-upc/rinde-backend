# language: es
Característica: US23 Consultar el resumen de gastos de mi viaje
  Como conductor
  Quiero ver el listado y detalle de los gastos que he registrado en mi viaje
  Para conocer el estado de mi rendición antes del cierre

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

  Escenario: Consultar listado de gastos del viaje
    Dado que el conductor registró un gasto "combustible" de tipo "FUEL" por el monto "150.00" con clave "KEY-US23-01" en el viaje "viaje"
    Y que el conductor registró un gasto "peaje" de tipo "TOLL" por el monto "30.00" con clave "KEY-US23-02" en el viaje "viaje"
    Cuando el conductor consulta los gastos del viaje "viaje"
    Entonces la respuesta tiene código 200
    Y la lista de gastos contiene 2 elementos

  Escenario: Consultar detalle individual de un gasto
    Dado que el conductor registró un gasto "combustible" de tipo "FUEL" por el monto "150.00" con clave "KEY-US23-03" en el viaje "viaje"
    Cuando el conductor consulta el detalle del gasto "combustible"
    Entonces la respuesta tiene código 200
    Y la respuesta incluye el campo "category" con el valor "FUEL"
    Y la respuesta incluye el campo "amount" con el valor "150.0"

  Escenario: Un usuario de otra empresa no puede consultar gastos de este viaje
    Dado que el conductor registró un gasto "combustible" de tipo "FUEL" por el monto "150.00" con clave "KEY-US23-04" en el viaje "viaje"
    Y que existe una empresa verificada con RUC "20987654321" y administrador "otro@otra.pe"
    Y que el administrador "otro@otra.pe" inició sesión
    Cuando el administrador "otro@otra.pe" consulta el detalle del gasto "combustible"
    Entonces la respuesta tiene código 404
    Y el mensaje de error es "El gasto no existe."
