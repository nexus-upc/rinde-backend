# language: es
Característica: US40 Controlar el estado de la suscripción
  Como administrador de una empresa
  Quiero conocer el estado y la vigencia de mi suscripción
  Para anticipar el vencimiento y las restricciones aplicables

  Antecedentes:
    Dado que existe una empresa verificada con RUC "20123456789" y administrador "ana@andes.pe"
    Y que el administrador "ana@andes.pe" inició sesión

  Escenario: Consultar el estado y la vigencia de la empresa
    Dado que existe una suscripción activa de prueba con vencimiento "2026-10-13T00:00:00Z"
    Cuando el administrador consulta la suscripción de la empresa
    Entonces la respuesta tiene código 200
    Y la suscripción consultada tiene el estado "ACTIVE"
    Y la suscripción consultada vence en "2026-10-13T00:00:00Z"

  Escenario: Una empresa no puede consultar la suscripción de otra empresa
    Dado que existe el plan activo de prueba "Plan-Consulta" con precio mensual "37.50" y límite 5
    Y que el administrador crea una suscripción pendiente con el plan "Plan-Consulta"
    Y que existe una empresa verificada con RUC "20987654321" y administrador "otro@otra.pe"
    Y que el administrador "otro@otra.pe" inició sesión
    Cuando un administrador de otra empresa consulta la suscripción creada
    Entonces la respuesta tiene código 404

  Escenario: Consultar una suscripción sin token requiere autenticación
    Dado que existe una suscripción activa de prueba con vencimiento "2026-10-13T00:00:00Z"
    Cuando una persona sin token consulta la suscripción de la empresa
    Entonces la respuesta tiene código 401

  Escenario: Avisar cinco días antes del vencimiento
    Dado que la suscripción activa de la empresa vence en cinco días
    Cuando el proceso diario de suscripciones se ejecuta dos veces
    Y el administrador consulta la suscripción de la empresa
    Entonces la respuesta tiene código 200
    Y la consulta muestra el aviso de renovación y el estado "EXPIRING"
    Y se registra una sola notificación de vencimiento para el administrador

  Escenario: Aplicar restricciones después de tres días sin pago y permitir terminar el viaje en ruta
    Dado que el administrador invitó a "Luis Quispe" con el correo "luis@andes.pe" y el rol "DRIVER"
    Y que "luis@andes.pe" definió la contraseña "Clave-De-Luis-2026" con su enlace de invitación
    Y que el administrador registró el vehículo "camión" con placa "US40-1001", marca "Volvo", modelo "FH", año 2022 y capacidad "5000"
    Y que el administrador programa el viaje "viaje" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-20"
    Y que el administrador asigna el viaje "viaje" al vehículo "camión" y al conductor "luis@andes.pe"
    Y que el conductor "luis@andes.pe" inicia el viaje "viaje"
    Y que existe una suscripción activa vencida hace más de tres días
    Cuando el proceso diario de suscripciones avanza los estados
    Y el administrador intenta programar un viaje nuevo y registrar otro vehículo
    Entonces la respuesta tiene código 403
    Y se impiden ambos registros porque la empresa debe regularizar la suscripción
    Cuando el conductor finaliza el viaje en ruta y registra un gasto
    Entonces la respuesta tiene código 201
    Y se permiten finalizar el viaje y registrar el gasto

  Escenario: Impedir superar el límite de unidades y sugerir un plan mayor
    Dado que existe el plan activo de prueba "Plan-Us40-Límite" con precio mensual "89.00" y límite 2
    Y que la empresa tiene 2 vehículos registrados
    Cuando el administrador selecciona el plan "Plan-Us40-Límite"
    Entonces la respuesta tiene código 201
    Cuando el administrador inicia el checkout simulado de la suscripción creada
    Entonces la respuesta tiene código 201
    Cuando la pasarela confirma el último pago simulado
    Entonces la respuesta tiene código 200
    Cuando el administrador registra el vehículo "vehículo adicional" con placa "US40-1003", marca "Volvo", modelo "FH", año 2022 y capacidad "5000"
    Entonces la respuesta tiene código 409
    Y el mensaje de error contiene "plan con mayor capacidad"
