# language: es
Característica: US37 Consultar estado de flota y métricas operativas
  Como administrador u operador de la empresa
  Quiero ver el estado de mi flota y las métricas de operación
  Para tomar decisiones informadas desde el dashboard

  Antecedentes:
    Dado que existe una empresa verificada con RUC "20123456789" y administrador "ana@andes.pe"
    Y que el administrador "ana@andes.pe" inició sesión
    Y que el administrador invitó a "Luis Quispe" con el correo "luis@andes.pe" y el rol "DRIVER"
    Y que "luis@andes.pe" definió la contraseña "Clave-De-Luis-2026" con su enlace de invitación
    Y que el administrador registró el vehículo "camión" con placa "XYZ-001", marca "Volvo", modelo "FH16", año 2023 y capacidad "20000"
    Y que el administrador registró el conductor "Luis Quispe" con documento "DNI" "12345678", licencia "Q12345678" categoría "AIIIB" y vencimiento "2027-12-31"

  Escenario: Estado de flota con un vehículo y un conductor
    Cuando el administrador consulta el estado de la flota
    Entonces la respuesta tiene código 200
    Y la respuesta incluye el campo "totalVehicles" con el valor "1"
    Y la respuesta incluye el campo "totalDrivers" con el valor "1"

  Escenario: Métricas operativas sin viajes
    Cuando el administrador consulta las métricas operativas
    Entonces la respuesta tiene código 200
    Y la respuesta incluye el campo "totalTrips" con el valor "0"
    Y la respuesta incluye el campo "totalExpenses" con el valor "0"

  Escenario: Métricas operativas con un viaje programado
    Dado que el administrador programa el viaje "viaje" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-20"
    Cuando el administrador consulta las métricas operativas
    Entonces la respuesta tiene código 200
    Y la respuesta incluye el campo "totalTrips" con el valor "1"
    Y la respuesta incluye el campo "scheduledTrips" con el valor "1"

  Escenario: Estado de flota requiere autenticación
    Cuando una persona sin token consulta el estado de la flota
    Entonces la respuesta tiene código 401

  Escenario: Métricas requieren autenticación
    Cuando una persona sin token consulta las métricas operativas
    Entonces la respuesta tiene código 401
