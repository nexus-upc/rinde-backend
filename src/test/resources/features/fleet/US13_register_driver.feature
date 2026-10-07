# language: es
Característica: US13 Registrar conductores y verificar vigencia de licencia
  Como administrador o responsable de operaciones
  Quiero registrar a los conductores de mi flota y controlar la vigencia de sus licencias
  Para asegurar que solo conductores habilitados ejecuten las operaciones de transporte

  Antecedentes:
    Dado que existe una empresa verificada con RUC "20123456789" y administrador "ana@andes.pe"
    Y que el administrador "ana@andes.pe" inició sesión

  Escenario: Registrar conductor exitosamente con licencia vigente y habilitarlo
    Cuando el administrador registra el conductor "Juan Perez" con documento "DNI" "10203040", licencia "Q10203040" categoría "A-IIIc" y vencimiento "2028-12-31"
    Entonces la respuesta tiene código 201
    Y la respuesta incluye el campo "licenseNumber" con el valor "Q10203040"
    Y el conductor queda con el estado "ENABLED"

  Escenario: Registrar conductor con licencia vencida no queda habilitado
    Cuando el administrador registra el conductor "Pedro Vencido" con documento "DNI" "55667788", licencia "Q55667788" categoría "A-IIb" y vencimiento "2020-01-01"
    Entonces la respuesta tiene código 201
    Y el conductor queda con el estado "DISABLED"

  Escenario: Rechazar registro por documento duplicado en la empresa
    Dado que el administrador registró el conductor "Juan Perez" con documento "DNI" "10203040", licencia "Q10203040" categoría "A-IIIc" y vencimiento "2028-12-31"
    Cuando el administrador registra el conductor "Otro Perez" con documento "DNI" "10203040", licencia "Q99999999" categoría "A-IIb" y vencimiento "2028-12-31"
    Entonces la respuesta tiene código 409
    Y el mensaje de error es "El documento ya se encuentra registrado en la empresa."

  Escenario: Rechazar registro por licencia duplicada en la empresa
    Dado que el administrador registró el conductor "Juan Perez" con documento "DNI" "10203040", licencia "Q10203040" categoría "A-IIIc" y vencimiento "2028-12-31"
    Cuando el administrador registra el conductor "Segundo Perez" con documento "DNI" "88776655", licencia "Q10203040" categoría "A-IIIc" y vencimiento "2028-12-31"
    Entonces la respuesta tiene código 409
    Y el mensaje de error es "La licencia ya se encuentra registrada en la empresa."

  Escenario: Listar conductores solo de la empresa autenticada
    Dado que el administrador registró el conductor "Juan Perez" con documento "DNI" "10203040", licencia "Q10203040" categoría "A-IIIc" y vencimiento "2028-12-31"
    Y que existe una empresa verificada con RUC "20987654321" y administrador "otro@otra.pe"
    Y que el administrador "otro@otra.pe" inició sesión
    Y que el administrador registró el conductor "Carlos Otra" con documento "DNI" "77889900", licencia "Q77889900" categoría "A-IIIb" y vencimiento "2028-12-31"
    Y que el administrador "ana@andes.pe" inició sesión
    Cuando el administrador lista los conductores de su empresa
    Entonces la respuesta tiene código 200
    Y la lista contiene 1 conductores
    Y la lista incluye el conductor con documento "10203040"
    Y la lista no incluye el conductor con documento "77889900"

  Escenario: Un conductor no puede registrar otros conductores
    Dado que el administrador invitó a "Luis Quispe" con el correo "luis@andes.pe" y el rol "DRIVER"
    Y que "luis@andes.pe" definió la contraseña "Clave-De-Luis-2026" con su enlace de invitación
    Cuando el conductor "luis@andes.pe" intenta registrar un conductor con licencia "Q11223344"
    Entonces la respuesta tiene código 403
