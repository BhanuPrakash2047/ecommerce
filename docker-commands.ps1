#!/usr/bin/env pwsh

# ============================================
# Docker Commands for Snack Ecommerce
# ============================================

Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "Snack Ecommerce - Docker Commands" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host ""

# Function to build image
function Build-Image {
    Write-Host "Building Docker image..." -ForegroundColor Yellow
    docker build -t snack-ecommerce:latest .
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Image built successfully!" -ForegroundColor Green
    } else {
        Write-Host "❌ Build failed!" -ForegroundColor Red
    }
}

# Function to run container
function Run-Container {
    Write-Host "Starting Docker container..." -ForegroundColor Yellow
    docker run -d `
        --name snack-ecommerce-app `
        -p 8080:8080 `
        --env-file .env `
        snack-ecommerce:latest
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Container started successfully!" -ForegroundColor Green
        Write-Host "🌐 Application will be available at: http://localhost:8080" -ForegroundColor Cyan
    } else {
        Write-Host "❌ Failed to start container!" -ForegroundColor Red
    }
}

# Function to check status
function Check-Status {
    Write-Host "Checking container status..." -ForegroundColor Yellow
    docker ps -a | Select-Object -First 20
}

# Function to view logs
function View-Logs {
    Write-Host "Viewing container logs (press Ctrl+C to exit)..." -ForegroundColor Yellow
    docker logs -f snack-ecommerce-app
}

# Function to stop container
function Stop-Container {
    Write-Host "Stopping container..." -ForegroundColor Yellow
    docker stop snack-ecommerce-app
    Write-Host "✅ Container stopped" -ForegroundColor Green
}

# Function to remove container
function Remove-Container {
    Write-Host "Removing container..." -ForegroundColor Yellow
    docker rm snack-ecommerce-app
    Write-Host "✅ Container removed" -ForegroundColor Green
}

# Main menu
Write-Host "Choose an option:" -ForegroundColor Cyan
Write-Host "1. Build image" -ForegroundColor White
Write-Host "2. Run container" -ForegroundColor White
Write-Host "3. Check status" -ForegroundColor White
Write-Host "4. View logs" -ForegroundColor White
Write-Host "5. Stop container" -ForegroundColor White
Write-Host "6. Remove container" -ForegroundColor White
Write-Host ""

$option = Read-Host "Enter option (1-6)"

switch ($option) {
    "1" { Build-Image }
    "2" { Run-Container }
    "3" { Check-Status }
    "4" { View-Logs }
    "5" { Stop-Container }
    "6" { Remove-Container }
    default { Write-Host "Invalid option!" -ForegroundColor Red }
}
