# Quick Service Test Script
Write-Host "=== Testing UPI Services ===" -ForegroundColor Cyan

# Test Eureka
Write-Host "`n1. Testing Eureka Server..." -ForegroundColor Yellow
try {
    $eureka = Invoke-WebRequest -Uri "http://localhost:8761" -UseBasicParsing -TimeoutSec 5
    Write-Host "   ✅ Eureka Server: RUNNING" -ForegroundColor Green
} catch {
    Write-Host "   ❌ Eureka Server: NOT RUNNING" -ForegroundColor Red
}

# Test API Gateway
Write-Host "`n2. Testing API Gateway..." -ForegroundColor Yellow
try {
    $gateway = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -TimeoutSec 5 -ErrorAction SilentlyContinue
    Write-Host "   ✅ API Gateway: RUNNING" -ForegroundColor Green
} catch {
    Write-Host "   ❌ API Gateway: NOT RUNNING" -ForegroundColor Red
}

# Test Utility Service
Write-Host "`n3. Testing Utility Service..." -ForegroundColor Yellow
try {
    $utility = Invoke-WebRequest -Uri "http://localhost:8080/api/utilities/categories" -UseBasicParsing -TimeoutSec 5
    Write-Host "   ✅ Utility Service: RUNNING" -ForegroundColor Green
    $categories = $utility.Content | ConvertFrom-Json
    Write-Host "   📊 Found $($categories.Count) payment categories" -ForegroundColor Cyan
} catch {
    Write-Host "   ❌ Utility Service: NOT RUNNING" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test Frontend
Write-Host "`n4. Testing Frontend..." -ForegroundColor Yellow
try {
    $frontend = Invoke-WebRequest -Uri "http://localhost:4200" -UseBasicParsing -TimeoutSec 5
    Write-Host "   ✅ Frontend: RUNNING" -ForegroundColor Green
} catch {
    Write-Host "   ❌ Frontend: NOT RUNNING" -ForegroundColor Red
}

# Test DTH Operators
Write-Host "`n5. Testing DTH Operators..." -ForegroundColor Yellow
try {
    $dth = Invoke-WebRequest -Uri "http://localhost:8080/api/utilities/recharge/dth/operators" -UseBasicParsing -TimeoutSec 5
    $operators = $dth.Content | ConvertFrom-Json
    Write-Host "   ✅ DTH Operators: AVAILABLE" -ForegroundColor Green
    Write-Host "   📊 Found $($operators.Count) DTH operators:" -ForegroundColor Cyan
    foreach ($op in $operators) {
        Write-Host "      - $($op.displayName)" -ForegroundColor White
    }
} catch {
    Write-Host "   ❌ DTH Operators: FAILED" -ForegroundColor Red
}

# Test Credit Card Issuers
Write-Host "`n6. Testing Credit Card Issuers..." -ForegroundColor Yellow
try {
    $cc = Invoke-WebRequest -Uri "http://localhost:8080/api/utilities/bills/credit-card/issuers" -UseBasicParsing -TimeoutSec 5
    $issuers = $cc.Content | ConvertFrom-Json
    Write-Host "   ✅ Credit Card Issuers: AVAILABLE" -ForegroundColor Green
    Write-Host "   📊 Found $($issuers.Count) issuers" -ForegroundColor Cyan
} catch {
    Write-Host "   ❌ Credit Card Issuers: FAILED" -ForegroundColor Red
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Cyan
Write-Host "`nIf any service shows ❌, please start it first!" -ForegroundColor Yellow
Write-Host "If all show ✅ but frontend not working, try:" -ForegroundColor Yellow
Write-Host "  1. Hard refresh browser (Ctrl+Shift+R)" -ForegroundColor White
Write-Host "  2. Clear localStorage and login again" -ForegroundColor White
Write-Host "  3. Check browser console for errors (F12)" -ForegroundColor White
