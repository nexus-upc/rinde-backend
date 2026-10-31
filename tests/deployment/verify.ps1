#requires -Version 7.2
[CmdletBinding()]
param([switch]$Build)

$ErrorActionPreference = 'Stop'
$taskRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '../..')).Path
$taskPreviousLocation = (Get-Location).Path
$taskProject = 'rinde-verification-' + [guid]::NewGuid().ToString('N').Substring(0, 10)
$taskEnvFile = [IO.Path]::GetTempFileName()
$taskStarted = $false
function Invoke-Compose {
 & docker compose --env-file $taskEnvFile -p $taskProject -f (Join-Path $taskRoot 'compose.yaml') @args
}
try {
 Set-Location -LiteralPath $taskRoot
 $taskEnvText = [IO.File]::ReadAllText((Join-Path $taskRoot 'infra/.env.example'))
 foreach ($taskKey in @('POSTGRES_PASSWORD','RABBITMQ_PASSWORD','JWT_SECRET','RINDE_PLATFORM_JWT_SECRET','RINDE_PLATFORM_BOOTSTRAP_PASSWORD','RINDE_BILLING_SIMULATION_WEBHOOK_SECRET')) {
  $taskValue = [Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(32)).ToLowerInvariant()
  $taskEnvText = [regex]::Replace($taskEnvText, '(?m)^' + $taskKey + '=.*$', $taskKey + '=' + $taskValue)
 }
 foreach ($taskKey in @('CORE_PORT','IAM_PORT','TRIP_PORT','GATEWAY_PORT')) {
  $taskEnvText = [regex]::Replace($taskEnvText, '(?m)^' + $taskKey + '=.*$', $taskKey + '=0')
 }
 [IO.File]::WriteAllText($taskEnvFile, $taskEnvText, [Text.UTF8Encoding]::new($false))
 if ($Build) {
  Invoke-Compose build
  if ($LASTEXITCODE -ne 0) { throw 'Falló la construcción de imágenes.' }
 }
 $taskOwnerPassword = ([regex]::Match($taskEnvText,'(?m)^RINDE_PLATFORM_BOOTSTRAP_PASSWORD=(.*)$')).Groups[1].Value.Trim()
 $taskEnvText=$taskEnvText.Replace('RINDE_PLATFORM_BOOTSTRAP_ENABLED=false','RINDE_PLATFORM_BOOTSTRAP_ENABLED=true').Replace('RINDE_PLATFORM_BOOTSTRAP_NAME=','RINDE_PLATFORM_BOOTSTRAP_NAME=Prueba de plataforma').Replace('RINDE_PLATFORM_BOOTSTRAP_EMAIL=','RINDE_PLATFORM_BOOTSTRAP_EMAIL=owner@example.invalid').Replace('RINDE_BILLING_SIMULATION_ENABLED=false','RINDE_BILLING_SIMULATION_ENABLED=true')
 [IO.File]::WriteAllText($taskEnvFile,$taskEnvText,[Text.UTF8Encoding]::new($false))
 $taskStarted = $true
 Invoke-Compose up -d --no-build
 if ($LASTEXITCODE -ne 0) { throw 'No se pudo arrancar el entorno de prueba.' }
 New-Item -ItemType Directory -Path (Join-Path $PSScriptRoot 'target') -Force | Out-Null
$taskChecks = [Collections.Generic.List[object]]::new()
function Read-Json($Response) {
 $taskText = if ($Response.Content -is [byte[]]) { [Text.Encoding]::UTF8.GetString($Response.Content) } else { [string]$Response.Content }
 return $taskText | ConvertFrom-Json -Depth 20
}
function Check-Http {
 param([string]$Method, [string]$Uri, [int]$Expected, $Body = $null, [hashtable]$Headers = @{})
 $taskRequest = @{Method=$Method;Uri=$Uri;Headers=$Headers;SkipHttpErrorCheck=$true;TimeoutSec=12}
 if ($null -ne $Body) { $taskRequest.Body = $Body | ConvertTo-Json -Depth 6; $taskRequest.ContentType = 'application/json' }
 $taskResponse = Invoke-WebRequest @taskRequest
 if ([int]$taskResponse.StatusCode -ne $Expected) { throw ('HTTP inesperado: ' + $Method + ' ' + $Uri + ' ' + $taskResponse.StatusCode) }
 $taskChecks.Add(@{method=$Method;path=([uri]$Uri).AbsolutePath;status=[int]$taskResponse.StatusCode})
 return $taskResponse
}
foreach ($taskEndpoint in @(@{Name='core';Port=8080}, @{Name='iam';Port=8081}, @{Name='trip';Port=8082}, @{Name='gateway';Port=8000})) {
 $taskAddress = Invoke-Compose port $taskEndpoint.Name $taskEndpoint.Port
 if ($LASTEXITCODE -ne 0) { throw 'No se pudo consultar el puerto publicado.' }
 $taskPort = [int](($taskAddress.Trim() -split ':')[-1])
 $taskHealthy = $false
 $taskDeadline = [DateTime]::UtcNow.AddSeconds(90)
 do {
  try {
   $taskHealth = Invoke-WebRequest -Uri ('http://localhost:' + $taskPort + '/actuator/health') -SkipHttpErrorCheck -TimeoutSec 5
   $taskHealthy = [int]$taskHealth.StatusCode -eq 200 -and ((Read-Json $taskHealth).status -eq 'UP')
  } catch { $taskHealthy = $false }
  if (-not $taskHealthy) { Start-Sleep -Seconds 2 }
 } while (-not $taskHealthy -and [DateTime]::UtcNow -lt $taskDeadline)
 if (-not $taskHealthy) { throw ('No arrancó el servicio del puerto ' + $taskPort) }
 $taskChecks.Add(@{servicePort=$taskPort;health='UP'})
}
$taskBase = 'http://' + (Invoke-Compose port gateway 8000).Trim()
foreach ($taskDocs in @('iam','monolith','trip')) {
 $taskDoc = Check-Http GET ($taskBase + '/v3/api-docs/' + $taskDocs) 200
 if (-not (Read-Json $taskDoc).openapi) { throw 'OpenAPI inválido.' }
}
$null = Check-Http GET ($taskBase + '/api/internal/v1/tenants/' + [guid]::NewGuid() + '/status') 404
$null = Check-Http GET ($taskBase + '/api/v1/trips') 401
$taskEmail = [guid]::NewGuid().ToString('N') + '@example.invalid'
$taskPassword = 'M1gration!' + [guid]::NewGuid().ToString('N')
$taskTenantBody = @{tradeName='Prueba monorepo';ruc=('20' + (Get-Random -Minimum 100000000 -Maximum 999999999));administratorFullName='Prueba monorepo';administratorEmail=$taskEmail;password=$taskPassword}
$taskTenant = Read-Json (Check-Http POST ($taskBase + '/api/v1/tenants') 201 $taskTenantBody)
$taskTenantId = [guid]::Parse($taskTenant.id).ToString()
# Fixture del contenedor de prueba: sustituye solo el hash del token de esta empresa.
$taskRawToken = [guid]::NewGuid().ToString('N') + [guid]::NewGuid().ToString('N')
$taskTokenHash = [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData([Text.Encoding]::UTF8.GetBytes($taskRawToken))).ToLowerInvariant()
$taskSql = "UPDATE iam.access_tokens SET token_hash='$taskTokenHash' WHERE tenant_id='$taskTenantId' AND purpose='EMAIL_VERIFICATION';"
$taskSqlResult = Invoke-Compose exec -T postgres-iam psql -v ON_ERROR_STOP=1 -U rinde -d rinde_iam -At -c $taskSql
if ($LASTEXITCODE -ne 0 -or $taskSqlResult -ne 'UPDATE 1') { throw 'No se pudo preparar el token de prueba.' }
$null = Check-Http POST ($taskBase + '/api/v1/tenants/' + $taskTenantId + '/verification') 200 @{token=$taskRawToken}
$taskAuth = Read-Json (Check-Http POST ($taskBase + '/api/v1/auth/sign-in') 200 @{email=$taskEmail;password=$taskPassword})
if (-not $taskAuth.accessToken) { throw 'No se emitió JWT.' }
$taskHeaders = @{Authorization=('Bearer ' + $taskAuth.accessToken)}
$taskMe = Read-Json (Check-Http GET ($taskBase + '/api/v1/users/me') 200 $null $taskHeaders)
$null = Check-Http GET ($taskBase + '/api/v1/plans') 200 $null $taskHeaders
$null = Check-Http GET ($taskBase + '/api/v1/subscriptions/current') 204 $null $taskHeaders
$taskTripBody = @{origin='Lima';destination='Arequipa';cargoDescription='Prueba de migración';cargoWeightKg=1200;departureDate=(Get-Date).AddDays(2).ToString('yyyy-MM-dd');confirmPastDate=$false}
$taskTrip = Read-Json (Check-Http POST ($taskBase + '/api/v1/trips') 201 $taskTripBody $taskHeaders)
$taskTrips = Read-Json (Check-Http GET ($taskBase + '/api/v1/trips') 200 $null $taskHeaders)
if (-not ($taskTrips | Where-Object { $_.id -eq $taskTrip.id })) { throw 'El viaje no se recuperó desde Trip.' }
$taskOwner = Read-Json (Check-Http POST ($taskBase + '/api/v1/platform/auth/sign-in') 200 @{email='owner@example.invalid';password=$taskOwnerPassword})
$taskOwnerHeaders=@{Authorization=('Bearer '+$taskOwner.accessToken)}
$null=Check-Http GET ($taskBase+'/api/v1/platform/companies') 403 $null $taskHeaders
$null=Check-Http GET ($taskBase+'/api/v1/users') 403 $null $taskOwnerHeaders
$taskPlan=Read-Json (Check-Http POST ($taskBase+'/api/v1/platform/plans') 201 @{name='Plan de prueba';monthlyPrice=99.90;unitLimit=10;reason='Comprobación de catálogo'} $taskOwnerHeaders)
$taskSubscription=Read-Json (Check-Http POST ($taskBase+'/api/v1/subscriptions') 201 @{planId=$taskPlan.planId} $taskHeaders)
$taskCheckout=Read-Json (Check-Http POST ($taskBase+'/api/v1/subscriptions/'+$taskSubscription.subscriptionId+'/checkout') 201 @{} $taskHeaders)
$null=Check-Http POST ($taskBase+'/api/v1/subscriptions/'+$taskSubscription.subscriptionId+'/payments/'+$taskCheckout.paymentId+'/simulation') 200 @{status='CONFIRMED'} $taskHeaders
$null=Check-Http GET ($taskBase+'/api/v1/platform/subscriptions') 200 $null $taskOwnerHeaders
$null=Check-Http GET ($taskBase+'/api/v1/platform/payments') 200 $null $taskOwnerHeaders
$null=Check-Http PATCH ($taskBase+'/api/v1/platform/companies/'+$taskTenantId+'/access') 200 @{suspended=$true;reason='Verificación del bloqueo administrativo'} $taskOwnerHeaders
$null=Check-Http GET ($taskBase+'/api/v1/users/me') 403 $null $taskHeaders
$null=Check-Http GET ($taskBase+'/api/v1/trips') 403 $null $taskHeaders
$null=Check-Http PATCH ($taskBase+'/api/v1/platform/companies/'+$taskTenantId+'/access') 200 @{suspended=$false;reason='Verificación del restablecimiento de acceso'} $taskOwnerHeaders
$null=Check-Http GET ($taskBase+'/api/v1/users/me') 200 $null $taskHeaders
$null=Check-Http GET ($taskBase+'/api/v1/platform/audit/identity') 200 $null $taskOwnerHeaders
$null=Check-Http GET ($taskBase+'/api/v1/platform/audit/billing') 200 $null $taskOwnerHeaders
$taskVehicle=Read-Json (Check-Http POST ($taskBase+'/api/v1/vehicles') 201 @{plateNumber='TEST-001';brand='Marca de prueba';model='Modelo de prueba';modelYear=2026;payloadCapacityKg=10000} $taskHeaders)
$taskDriverEmail=[guid]::NewGuid().ToString('N')+'@example.invalid'
$taskUser=Read-Json (Check-Http POST ($taskBase+'/api/v1/users') 201 @{fullName='Conductor de prueba';email=$taskDriverEmail;role='DRIVER'} $taskHeaders)
$taskDriverToken=[guid]::NewGuid().ToString('N')+[guid]::NewGuid().ToString('N')
$taskDriverHash=[Convert]::ToHexString([Security.Cryptography.SHA256]::HashData([Text.Encoding]::UTF8.GetBytes($taskDriverToken))).ToLowerInvariant()
$taskDriverId=[guid]::Parse($taskUser.id).ToString()
$taskSql="UPDATE iam.access_tokens SET token_hash='$taskDriverHash' WHERE user_id='$taskDriverId' AND purpose='INVITATION';"
$taskSqlResult=Invoke-Compose exec -T postgres-iam psql -v ON_ERROR_STOP=1 -U rinde -d rinde_iam -At -c $taskSql
if ($LASTEXITCODE-ne 0 -or $taskSqlResult-ne 'UPDATE 1') { throw 'No se pudo preparar la invitación de prueba.' }
$null=Check-Http POST ($taskBase+'/api/v1/auth/password') 204 @{token=$taskDriverToken;password=$taskPassword}
$taskDriver=Read-Json (Check-Http POST ($taskBase+'/api/v1/drivers') 201 @{userId=$taskUser.id;fullName='Conductor de prueba';documentType='DNI';documentNumber='12345678';licenseNumber='Q12345678';licenseCategory='AIIIC';licenseExpirationDate=(Get-Date).AddYears(1).ToString('yyyy-MM-dd')} $taskHeaders)
$null=Check-Http PUT ($taskBase+'/api/v1/trips/'+$taskTrip.id+'/assignment') 200 @{vehicleId=$taskVehicle.id;driverId=$taskDriver.id;confirmOverdueMaintenance=$false} $taskHeaders
$taskDriverSession=Read-Json (Check-Http POST ($taskBase+'/api/v1/auth/sign-in') 200 @{email=$taskDriverEmail;password=$taskPassword})
$taskDriverHeaders=@{Authorization=('Bearer '+$taskDriverSession.accessToken)}
$null=Check-Http POST ($taskBase+'/api/v1/trips/'+$taskTrip.id+'/start') 200 @{} $taskDriverHeaders
foreach ($taskExpectedState in @('IN_TRIP','AVAILABLE')) {
 if ($taskExpectedState-eq 'AVAILABLE') { $null=Check-Http POST ($taskBase+'/api/v1/trips/'+$taskTrip.id+'/finish') 200 @{} $taskDriverHeaders }
 $taskDeadline=[DateTime]::UtcNow.AddSeconds(20)
 do {
  $taskVehicleResult=Read-Json (Check-Http GET ($taskBase+'/api/v1/vehicles/'+$taskVehicle.id) 200 $null $taskHeaders)
  if ($taskVehicleResult.status-ne $taskExpectedState) { Start-Sleep -Milliseconds 250 }
 } while ($taskVehicleResult.status-ne $taskExpectedState -and [DateTime]::UtcNow-lt $taskDeadline)
 if ($taskVehicleResult.status-ne $taskExpectedState) { throw ('Fleet no recibió el estado '+$taskExpectedState) }
 $taskChecks.Add(@{fleetState=$taskExpectedState;rabbitConsumerApplied=$true})
}
$taskNotifications=Invoke-Compose exec -T postgres-core psql -U rinde -d rinde -At -c 'SELECT count(*) FROM notification.retry_store;'
if ([int]$taskNotifications-lt 1) { throw 'No se registró el aviso de asignación.' }
$taskChecks.Add(@{tripAssignmentNotificationRecorded=$true;pushProviderConfigured=$false})
foreach ($taskService in @('core','iam','trip','gateway')) {
 $taskId = Invoke-Compose ps -q $taskService
 $taskUser = & docker inspect --format '{{.Config.User}}' $taskId
 if ($taskUser -ne '10001:10001') { throw 'El runtime no usa el usuario esperado.' }
 & docker exec $taskId sh -c 'test ! -e /app/.env'
 if ($LASTEXITCODE -ne 0) { throw 'Se encontró un .env en la imagen.' }
 $taskChecks.Add(@{service=$taskService;runtimeUser=$taskUser;embeddedEnv=$false})
}
$taskCoreTenants = Invoke-Compose exec -T postgres-core psql -U rinde -d rinde -At -c 'SELECT count(*) FROM iam.tenants;'
if ($LASTEXITCODE -ne 0 -or $taskCoreTenants -ne '0') { throw 'La identidad se duplicó en core.' }
$taskChecks.Add(@{identityOnlyInIam=$true;tenantCopiesInCore=0;tripStoredInSeparateDatabase=$true})
$taskReport = @{success=$true;checks=$taskChecks;verificationFixture='Hash del token preparado solo en el PostgreSQL efímero de IAM; verificación e ingreso por HTTP real.';limits=@('Fleet se ejecuta temporalmente en core; su extracción sigue pendiente.','No se prueba cierre distribuido de Settlement.','La lista de planes puede estar vacía en las bases nuevas.')}
$taskReport | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath (Join-Path $PSScriptRoot 'target/result.json') -Encoding utf8
Write-Output ('Comprobaciones Docker aprobadas: ' + $taskChecks.Count)
} finally {
 if ($taskStarted) {
  Invoke-Compose down --volumes
  if ($LASTEXITCODE -ne 0) { Write-Warning ('Revisar la limpieza del proyecto ' + $taskProject) }
 }
 Remove-Item -LiteralPath $taskEnvFile -Force -ErrorAction SilentlyContinue
 Set-Location -LiteralPath $taskPreviousLocation
}
