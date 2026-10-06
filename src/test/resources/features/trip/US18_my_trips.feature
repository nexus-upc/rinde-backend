# language: es
Característica: US18 Consultar mis viajes asignados
  Como conductor
  Quiero ver los viajes que debo ejecutar ordenados por fecha de salida
  Para organizar mi ruta de trabajo

  Antecedentes:
    Dado que existe una empresa verificada con RUC "20123456789" y administrador "ana@andes.pe"
    Y que el administrador "ana@andes.pe" inició sesión
    Y que el administrador invitó a "Luis Quispe" con el correo "luis@andes.pe" y el rol "DRIVER"
    Y que "luis@andes.pe" definió la contraseña "Clave-De-Luis-2026" con su enlace de invitación
    Y que existe el vehículo "camión uno"
    Y que existe el vehículo "camión dos"

  Escenario: Listar los viajes propios ordenados por fecha
    Cuando el administrador programa el viaje "más tarde" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-21"
    Y el administrador asigna el viaje "más tarde" al vehículo "camión uno" y al conductor "luis@andes.pe"
    Y el administrador programa el viaje "más temprano" de "Lima" a "Cusco" con carga "Muebles" y salida "2026-10-20"
    Y el administrador asigna el viaje "más temprano" al vehículo "camión dos" y al conductor "luis@andes.pe"
    Cuando el conductor "luis@andes.pe" consulta sus viajes asignados
    Entonces la respuesta tiene código 200
    Y la lista contiene 2 viajes
    Y el tablero devuelve los códigos en orden "TRP-000002,TRP-000001"

  Escenario: Un conductor sin asignaciones recibe una lista vacía
    Cuando el conductor "luis@andes.pe" consulta sus viajes asignados
    Entonces la respuesta tiene código 200
    Y la lista está vacía

  Escenario: El conductor solo ve los viajes de su empresa
    Cuando el administrador programa el viaje "viaje" de "Lima" a "Arequipa" con carga "Repuestos" y salida "2026-10-20"
    Y el administrador asigna el viaje "viaje" al vehículo "camión uno" y al conductor "luis@andes.pe"
    Dado que existe una empresa verificada con RUC "20987654321" y administrador "otro@otra.pe"
    Y que el administrador "otro@otra.pe" inició sesión
    Y que el administrador invitó a "Mario Soto" con el correo "mario@otra.pe" y el rol "DRIVER"
    Y que "mario@otra.pe" definió la contraseña "Clave-De-Mario-2026" con su enlace de invitación
    Cuando el conductor "mario@otra.pe" consulta sus viajes asignados
    Entonces la respuesta tiene código 200
    Y la lista está vacía

  Escenario: Un administrador no puede consultar la lista del conductor
    Cuando el administrador consulta sus viajes asignados
    Entonces la respuesta tiene código 403
