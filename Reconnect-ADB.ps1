$ip = "100.107.18.46:37831"
$adb = "C:\Users\ruben\AppData\Local\Android\Sdk\platform-tools\adb.exe"
Write-Host "Conectando a ADB inalambrico en $ip..." -ForegroundColor Cyan
& $adb disconnect
& $adb connect $ip
& $adb devices
