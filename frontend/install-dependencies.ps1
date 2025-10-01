# PowerShell script to install Angular OIDC dependencies
# Run this script from the timesheet directory

Write-Host "Installing Angular OIDC dependencies..." -ForegroundColor Green

# Set execution policy for current user if needed
try {
    Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser -Force
    Write-Host "Execution policy set successfully" -ForegroundColor Yellow
} catch {
    Write-Host "Could not set execution policy. You may need to run as administrator." -ForegroundColor Red
}

# Install dependencies
Write-Host "Installing @angular/animations..." -ForegroundColor Blue
npm install @angular/animations@^20.3.0 --save

Write-Host "Installing zone.js..." -ForegroundColor Blue
npm install zone.js@0.15.1 --save

Write-Host "Installing angular-auth-oidc-client..." -ForegroundColor Blue
npm install angular-auth-oidc-client@^18.0.0 --save

Write-Host "Installing @auth0/angular-jwt..." -ForegroundColor Blue
npm install @auth0/angular-jwt@^5.2.0 --save

Write-Host "Dependencies installed successfully!" -ForegroundColor Green
Write-Host "You can now run: ng serve --ssl --port=4200" -ForegroundColor Cyan
