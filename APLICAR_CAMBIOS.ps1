$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$root = (Get-Location).Path
if (-not (Test-Path -LiteralPath (Join-Path $root 'gradlew')) -or -not (Test-Path -LiteralPath (Join-Path $root '.git'))) {
    Write-Host 'ERROR: Abre PowerShell desde la carpeta principal Juegosdedardos (con gradlew y .git).' -ForegroundColor Red
    exit 1
}
$patch = Join-Path $PSScriptRoot 'lint_hardcoded_302.patch'
if (-not (Test-Path -LiteralPath $patch)) { Write-Host 'Falta el archivo .patch'; exit 1 }
$head = (git rev-parse HEAD 2>$null).Trim()
if ($LASTEXITCODE -ne 0) { Write-Host 'Git no puede leer este repositorio'; exit 1 }
$expected = '6c5b92fd941cf02d3427cbdfa15fb82a34d8da99'
if ($head -ne $expected) {
    Write-Host "ATENCION: commit actual $head distinto del esperado $expected" -ForegroundColor Yellow
    Write-Host 'No se aplican los cambios para evitar sobrescribir una version diferente.'
    exit 1
}
& git apply --check --unidiff-zero -- $patch
if ($LASTEXITCODE -ne 0) {
    Write-Host 'ERROR: El parche no coincide con los archivos actuales; no se ha cambiado nada.' -ForegroundColor Red
    exit 1
}
& git apply --unidiff-zero -- $patch
if ($LASTEXITCODE -ne 0) { Write-Host 'ERROR aplicando el parche' -ForegroundColor Red; exit 1 }
Write-Host 'OK: XML y strings.xml corregidos en tu copia local. Sin commit ni push.' -ForegroundColor Green
Write-Host 'Comprueba: .\gradlew :app:assembleDebug y .\gradlew :app:lintDebug'

# Construir un ZIP real con los XML resultantes, manteniendo las rutas.
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zipFinal = Join-Path $root 'Dardos_XML_Corregidos.zip'
if (Test-Path -LiteralPath $zipFinal) { Remove-Item -LiteralPath $zipFinal -Force }
$zip = [System.IO.Compression.ZipFile]::Open($zipFinal, [System.IO.Compression.ZipArchiveMode]::Create)
try {
    $files = @(
        'app/src/main/res/layout/activity_acerca_de.xml',
        'app/src/main/res/layout/activity_ajustes.xml',
        'app/src/main/res/layout/activity_ayuda.xml',
        'app/src/main/res/layout/activity_detalle_partida.xml',
        'app/src/main/res/layout/activity_estadisticas.xml',
        'app/src/main/res/layout/activity_historial_partidas.xml',
        'app/src/main/res/layout/activity_jugadores.xml',
        'app/src/main/res/layout/activity_nueva_partida.xml',
        'app/src/main/res/layout/activity_partida_criquet.xml',
        'app/src/main/res/layout/activity_partida_puntos.xml',
        'app/src/main/res/layout/activity_partida_rondas.xml',
        'app/src/main/res/layout/activity_reglas.xml',
        'app/src/main/res/layout/activity_resultado.xml',
        'app/src/main/res/layout/dialogo_jugador.xml',
        'app/src/main/res/layout/header_menu_lateral.xml',
        'app/src/main/res/layout/item_clasificacion_resultado.xml',
        'app/src/main/res/layout/item_jugador_gestion.xml',
        'app/src/main/res/layout/item_jugador_partida.xml',
        'app/src/main/res/layout/item_partida_historial.xml',
        'app/src/main/res/layout/item_resultado_historico.xml',
        'app/src/main/res/menu/menu_lateral.xml',
        'app/src/main/res/menu/menu_main.xml',
        'app/src/main/res/menu/menu_partida_historial.xml',
        'app/src/main/res/values/strings.xml'
    )
    foreach ($path in $files) {
        $source = Join-Path $root $path
        [void][System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $source, $path)
    }
} finally {
    $zip.Dispose()
}
Write-Host "Creado el ZIP de XML completos: $zipFinal" -ForegroundColor Green

& git status --short
