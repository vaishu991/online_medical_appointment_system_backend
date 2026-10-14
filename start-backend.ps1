$ErrorActionPreference = 'Stop'

$projectRoot = $PSScriptRoot
$mavenWrapper = Join-Path $projectRoot 'mvnw.cmd'

if (-not (Test-Path -LiteralPath $mavenWrapper)) {
  throw "Maven wrapper was not found at $mavenWrapper."
}

$securePassword = Read-Host 'Enter your MySQL root password' -AsSecureString
if ($securePassword.Length -eq 0) {
  throw 'A MySQL password is required to start the backend.'
}

$passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
try {
  $env:SPRING_DATASOURCE_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
  Set-Location -LiteralPath $projectRoot
  Write-Host 'Starting the Online Medical Appointment backend...'
  & $mavenWrapper spring-boot:run
  $mavenExitCode = $LASTEXITCODE
  if ($mavenExitCode -ne 0) {
    throw "Backend stopped with exit code $mavenExitCode. Check the MySQL password and backend output above."
  }
}
finally {
  [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
  Remove-Item Env:SPRING_DATASOURCE_PASSWORD -ErrorAction SilentlyContinue
}
