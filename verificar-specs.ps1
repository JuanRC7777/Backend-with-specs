# Script de Verificación de Especificaciones - Sistema POS
# Ejecuta verificaciones automáticas del cumplimiento de specs

$ErrorActionPreference = "Continue"

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "🔍 VERIFICACIÓN DE ESPECIFICACIONES" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

$resultados = @()

# Función para agregar resultado
function Add-Result {
    param (
        [string]$Test,
        [bool]$Passed,
        [string]$Message
    )
    $resultados += [PSCustomObject]@{
        Test = $Test
        Passed = $Passed
        Message = $Message
    }
    if ($Passed) {
        Write-Host "✅ $Test" -ForegroundColor Green
        if ($Message) { Write-Host "   $Message" -ForegroundColor Gray }
    } else {
        Write-Host "❌ $Test" -ForegroundColor Red
        if ($Message) { Write-Host "   $Message" -ForegroundColor Yellow }
    }
}

# 1. Verificar Java 21
Write-Host "📝 Test 1: Verificar Java 21" -ForegroundColor Yellow
Write-Host "----------------------------------------"
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    if ($javaVersion -match "21") {
        Add-Result -Test "Java 21 instalado" -Passed $true -Message $javaVersion
    } else {
        Add-Result -Test "Java 21 instalado" -Passed $false -Message "Se requiere Java 21"
    }
} catch {
    Add-Result -Test "Java 21 instalado" -Passed $false -Message "Java no encontrado"
}
Write-Host ""

# 2. Verificar MySQL
Write-Host "📝 Test 2: Verificar MySQL" -ForegroundColor Yellow
Write-Host "----------------------------------------"
$mysqlRunning = netstat -ano | Select-String ":3306"
if ($mysqlRunning) {
    Add-Result -Test "MySQL corriendo en puerto 3306" -Passed $true
} else {
    Add-Result -Test "MySQL corriendo en puerto 3306" -Passed $false -Message "MySQL no está corriendo"
}
Write-Host ""

# 3. Verificar estructura de paquetes
Write-Host "📝 Test 3: Verificar estructura hexagonal" -ForegroundColor Yellow
Write-Host "----------------------------------------"
$estructuraOK = $true

$directorios = @(
    "pos-backend/src/main/java/com/empresa/pos/domain/model",
    "pos-backend/src/main/java/com/empresa/pos/domain/exception",
    "pos-backend/src/main/java/com/empresa/pos/application/port/in",
    "pos-backend/src/main/java/com/empresa/pos/application/port/out",
    "pos-backend/src/main/java/com/empresa/pos/application/service",
    "pos-backend/src/main/java/com/empresa/pos/application/dto",
    "pos-backend/src/main/java/com/empresa/pos/infrastructure/adapter/in/web",
    "pos-backend/src/main/java/com/empresa/pos/infrastructure/adapter/out/persistence",
    "pos-backend/src/main/java/com/empresa/pos/infrastructure/config"
)

foreach ($dir in $directorios) {
    if (Test-Path $dir) {
        Write-Host "   ✅ $dir" -ForegroundColor Green
    } else {
        Write-Host "   ❌ $dir" -ForegroundColor Red
        $estructuraOK = $false
    }
}

Add-Result -Test "Estructura hexagonal completa" -Passed $estructuraOK
Write-Host ""

# 4. Verificar entidades de dominio
Write-Host "📝 Test 4: Verificar modelos de dominio" -ForegroundColor Yellow
Write-Host "----------------------------------------"
$modelos = @(
    "pos-backend/src/main/java/com/empresa/pos/domain/model/Producto.java",
    "pos-backend/src/main/java/com/empresa/pos/domain/model/Venta.java",
    "pos-backend/src/main/java/com/empresa/pos/domain/model/DetalleVenta.java",
    "pos-backend/src/main/java/com/empresa/pos/domain/model/Usuario.java"
)

$modelosOK = $true
foreach ($modelo in $modelos) {
    if (Test-Path $modelo) {
        Write-Host "   ✅ $(Split-Path $modelo -Leaf)" -ForegroundColor Green
    } else {
        Write-Host "   ❌ $(Split-Path $modelo -Leaf)" -ForegroundColor Red
        $modelosOK = $false
    }
}

Add-Result -Test "Modelos de dominio completos" -Passed $modelosOK
Write-Host ""

# 5. Verificar tests unitarios
Write-Host "📝 Test 5: Verificar tests unitarios" -ForegroundColor Yellow
Write-Host "----------------------------------------"
$tests = @(
    "pos-backend/src/test/java/com/empresa/pos/domain/model/ProductoTest.java",
    "pos-backend/src/test/java/com/empresa/pos/domain/model/VentaTest.java",
    "pos-backend/src/test/java/com/empresa/pos/domain/model/DetalleVentaTest.java",
    "pos-backend/src/test/java/com/empresa/pos/application/service/ProductoServiceTest.java",
    "pos-backend/src/test/java/com/empresa/pos/application/service/VentaServiceTest.java",
    "pos-backend/src/test/java/com/empresa/pos/application/service/AuthServiceTest.java"
)

$testsOK = $true
foreach ($test in $tests) {
    if (Test-Path $test) {
        Write-Host "   ✅ $(Split-Path $test -Leaf)" -ForegroundColor Green
    } else {
        Write-Host "   ❌ $(Split-Path $test -Leaf)" -ForegroundColor Red
        $testsOK = $false
    }
}

Add-Result -Test "Tests unitarios completos" -Passed $testsOK
Write-Host ""

# 6. Compilar proyecto
Write-Host "📝 Test 6: Compilar proyecto" -ForegroundColor Yellow
Write-Host "----------------------------------------"
Push-Location pos-backend
try {
    $compileOutput = ./mvnw.cmd clean compile 2>&1
    if ($LASTEXITCODE -eq 0) {
        Add-Result -Test "Compilación exitosa" -Passed $true
    } else {
        Add-Result -Test "Compilación exitosa" -Passed $false -Message "Error en compilación"
    }
} catch {
    Add-Result -Test "Compilación exitosa" -Passed $false -Message $_.Exception.Message
}
Pop-Location
Write-Host ""

# 7. Ejecutar tests
Write-Host "📝 Test 7: Ejecutar tests unitarios" -ForegroundColor Yellow
Write-Host "----------------------------------------"
Push-Location pos-backend
try {
    Write-Host "Ejecutando tests... (esto puede tomar un momento)" -ForegroundColor Gray
    $testOutput = ./mvnw.cmd test 2>&1
    if ($LASTEXITCODE -eq 0) {
        $testsPassed = ($testOutput | Select-String "Tests run:").ToString()
        Add-Result -Test "Tests unitarios pasan" -Passed $true -Message $testsPassed
    } else {
        Add-Result -Test "Tests unitarios pasan" -Passed $false -Message "Algunos tests fallaron"
    }
} catch {
    Add-Result -Test "Tests unitarios pasan" -Passed $false -Message $_.Exception.Message
}
Pop-Location
Write-Host ""

# 8. Verificar configuración de seguridad
Write-Host "📝 Test 8: Verificar configuración de seguridad" -ForegroundColor Yellow
Write-Host "----------------------------------------"
$securityFiles = @(
    "pos-backend/src/main/java/com/empresa/pos/infrastructure/config/SecurityConfig.java",
    "pos-backend/src/main/java/com/empresa/pos/infrastructure/config/JwtAuthFilter.java",
    "pos-backend/src/main/java/com/empresa/pos/infrastructure/config/JwtUtil.java"
)

$securityOK = $true
foreach ($file in $securityFiles) {
    if (Test-Path $file) {
        Write-Host "   ✅ $(Split-Path $file -Leaf)" -ForegroundColor Green
    } else {
        Write-Host "   ❌ $(Split-Path $file -Leaf)" -ForegroundColor Red
        $securityOK = $false
    }
}

Add-Result -Test "Configuración de seguridad completa" -Passed $securityOK
Write-Host ""

# 9. Verificar controllers
Write-Host "📝 Test 9: Verificar controllers REST" -ForegroundColor Yellow
Write-Host "----------------------------------------"
$controllers = @(
    "pos-backend/src/main/java/com/empresa/pos/infrastructure/adapter/in/web/AuthController.java",
    "pos-backend/src/main/java/com/empresa/pos/infrastructure/adapter/in/web/ProductoController.java",
    "pos-backend/src/main/java/com/empresa/pos/infrastructure/adapter/in/web/VentaController.java",
    "pos-backend/src/main/java/com/empresa/pos/infrastructure/adapter/in/web/GlobalExceptionHandler.java"
)

$controllersOK = $true
foreach ($controller in $controllers) {
    if (Test-Path $controller) {
        Write-Host "   ✅ $(Split-Path $controller -Leaf)" -ForegroundColor Green
    } else {
        Write-Host "   ❌ $(Split-Path $controller -Leaf)" -ForegroundColor Red
        $controllersOK = $false
    }
}

Add-Result -Test "Controllers REST completos" -Passed $controllersOK
Write-Host ""

# 10. Verificar documentación
Write-Host "📝 Test 10: Verificar documentación" -ForegroundColor Yellow
Write-Host "----------------------------------------"
$docs = @(
    "pos-backend/README.md",
    "pos-backend/Dockerfile",
    "GUIA_PRUEBAS.md",
    "GUIA_POSTMAN.md"
)

$docsOK = $true
foreach ($doc in $docs) {
    if (Test-Path $doc) {
        Write-Host "   ✅ $(Split-Path $doc -Leaf)" -ForegroundColor Green
    } else {
        Write-Host "   ❌ $(Split-Path $doc -Leaf)" -ForegroundColor Red
        $docsOK = $false
    }
}

Add-Result -Test "Documentación completa" -Passed $docsOK
Write-Host ""

# Resumen
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "📊 RESUMEN DE VERIFICACIÓN" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

$totalTests = $resultados.Count
$passedTests = ($resultados | Where-Object { $_.Passed }).Count
$failedTests = $totalTests - $passedTests
$percentage = [math]::Round(($passedTests / $totalTests) * 100, 2)

Write-Host "Total de tests: $totalTests" -ForegroundColor White
Write-Host "Tests pasados: $passedTests" -ForegroundColor Green
Write-Host "Tests fallidos: $failedTests" -ForegroundColor Red
Write-Host "Porcentaje de cumplimiento: $percentage%" -ForegroundColor $(if ($percentage -ge 90) { "Green" } elseif ($percentage -ge 70) { "Yellow" } else { "Red" })
Write-Host ""

# Mostrar tests fallidos
if ($failedTests -gt 0) {
    Write-Host "Tests fallidos:" -ForegroundColor Red
    $resultados | Where-Object { -not $_.Passed } | ForEach-Object {
        Write-Host "  ❌ $($_.Test)" -ForegroundColor Red
        if ($_.Message) {
            Write-Host "     $($_.Message)" -ForegroundColor Yellow
        }
    }
    Write-Host ""
}

# Recomendaciones
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "💡 RECOMENDACIONES" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

if ($percentage -ge 90) {
    Write-Host "✅ ¡Excelente! Tu proyecto cumple con la mayoría de las especificaciones." -ForegroundColor Green
    Write-Host "   Revisa el archivo REPORTE_CUMPLIMIENTO_SPECS.md para detalles." -ForegroundColor Gray
} elseif ($percentage -ge 70) {
    Write-Host "⚠️  Tu proyecto está bien, pero hay áreas de mejora." -ForegroundColor Yellow
    Write-Host "   Revisa el archivo REPORTE_CUMPLIMIENTO_SPECS.md para detalles." -ForegroundColor Gray
} else {
    Write-Host "❌ Tu proyecto necesita trabajo adicional." -ForegroundColor Red
    Write-Host "   Revisa el archivo REPORTE_CUMPLIMIENTO_SPECS.md para detalles." -ForegroundColor Gray
}

Write-Host ""
Write-Host "Para más información, consulta:" -ForegroundColor Cyan
Write-Host "  - REPORTE_CUMPLIMIENTO_SPECS.md" -ForegroundColor White
Write-Host "  - .kiro/specs/sistema-pos/tasks.md" -ForegroundColor White
Write-Host ""
