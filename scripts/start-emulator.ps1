# Arranca el AVD del proyecto (GPU host; evita swiftshader sin opengl32sw).
$sdk = "$env:LOCALAPPDATA\Android\Sdk"
if (-not (Test-Path "$sdk\emulator\emulator.exe")) {
    Write-Error "SDK no encontrado en $sdk. Instala Android Studio / SDK."
    exit 1
}
$avd = "Medium_Phone_API_36.0"
$running = & "$sdk\platform-tools\adb.exe" devices 2>&1 | Out-String
if ($running -match "emulator-\d+\s+device") {
    Write-Host "Emulador ya en ejecución."
    exit 0
}
if ($running -match "emulator-\d+\s+offline") {
    & "$sdk\platform-tools\adb.exe" kill-server | Out-Null
    Get-Process qemu-system-x86_64, emulator -ErrorAction SilentlyContinue | Stop-Process -Force
    Start-Sleep -Seconds 2
}
Remove-Item "$env:USERPROFILE\.android\avd\Medium_Phone.avd\hardware-qemu.ini.lock" -Recurse -Force -ErrorAction SilentlyContinue
Start-Process -FilePath "$sdk\emulator\emulator.exe" -ArgumentList "-avd", $avd, "-gpu", "host", "-no-snapshot-load"
Write-Host "Iniciando $avd ... Espera el arranque en Android Studio o: adb wait-for-device shell getprop sys.boot_completed"
