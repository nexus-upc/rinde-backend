# language: es
Característica: US34 Consultar estado de mantenimiento y elegibilidad de recursos
  Como administrador o responsable de operaciones
  Quiero consultar el estado técnico del vehículo y la habilitación del conductor
  Para anticipar mantenimientos y asegurar que solo recursos aptos se asignen a viajes

  Antecedentes:
    Dado que existe una empresa verificada con RUC "20123456789" y administrador "ana@andes.pe"
    Y que el administrador "ana@andes.pe" inició sesión
    Y que el administrador registró el vehículo "camion" con placa "ABC-123", marca "Volvo", modelo "FH540", año 2022 y capacidad "15000.00"

  Escenario: Consultar estado de mantenimiento de una unidad al día
    Cuando el administrador consulta el estado de salud del vehículo "camion"
    Entonces la respuesta tiene código 200
    Y el estado de mantenimiento es "UP_TO_DATE"
    Y el vehículo está disponible para viaje

  Escenario: Alerta de mantenimiento próximo a vencer
    Dado que se registra un mantenimiento "PREVENTIVO" para el vehículo "camion" con ejecución "2026-10-01", costo "450.00" y próximo mantenimiento "2026-10-10"
    Cuando el administrador consulta el estado de salud del vehículo "camion"
    Entonces la respuesta tiene código 200
    Y el estado de mantenimiento es "DUE_SOON"
    Y el vehículo está disponible para viaje

  Escenario: Alerta de mantenimiento vencido bloquea disponibilidad normal
    Dado que se registra un mantenimiento "CORRECTIVO" para el vehículo "camion" con ejecución "2026-08-01", costo "900.00" y próximo mantenimiento "2026-09-01"
    Cuando el administrador consulta el estado de salud del vehículo "camion"
    Entonces la respuesta tiene código 200
    Y el estado de mantenimiento es "OVERDUE"
    Y el vehículo no está disponible para viaje

  Escenario: Listar alertas de mantenimiento para unidades que requieren servicio
    Dado que se registra un mantenimiento "PREVENTIVO" para el vehículo "camion" con ejecución "2026-10-01", costo "450.00" y próximo mantenimiento "2026-10-10"
    Cuando el administrador consulta las alertas de mantenimiento
    Entonces la respuesta tiene código 200
    Y la lista de alertas contiene al menos 1 elemento
    Y la lista de alertas incluye la unidad con placa "ABC-123"

  Escenario: Consultar elegibilidad de conductor habilitado
    Dado que el administrador registró el conductor "Juan Perez" con documento "DNI" "10203040", licencia "Q10203040" categoría "A-IIIc" y vencimiento "2028-12-31"
    Cuando el administrador consulta la elegibilidad del conductor "Juan Perez"
    Entonces la respuesta tiene código 200
    Y el conductor queda habilitado

  Escenario: Consultar elegibilidad de conductor con licencia vencida
    Dado que el administrador registró el conductor "Pedro Vencido" con documento "DNI" "55667788", licencia "Q55667788" categoría "A-IIb" y vencimiento "2020-01-01"
    Cuando el administrador consulta la elegibilidad del conductor "Pedro Vencido"
    Entonces la respuesta tiene código 200
    Y el conductor no queda habilitado

  Escenario: Un usuario de otra empresa no puede consultar el estado técnico de una unidad
    Dado que existe una empresa verificada con RUC "20987654321" y administrador "otro@otra.pe"
    Y que el administrador "otro@otra.pe" inició sesión
    Cuando un administrador de otra empresa consulta el estado de salud del vehículo "camion"
    Entonces la respuesta tiene código 404
