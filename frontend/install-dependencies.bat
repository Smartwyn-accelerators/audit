@echo off
echo Installing Angular OIDC dependencies...

echo Installing @angular/animations...
call npm install @angular/animations@^20.3.0 --save

echo Installing zone.js...
call npm install zone.js@0.15.1 --save

echo Installing angular-auth-oidc-client...
call npm install angular-auth-oidc-client@^18.0.0 --save

echo Installing @auth0/angular-jwt...
call npm install @auth0/angular-jwt@^5.2.0 --save

echo Dependencies installed successfully!
echo You can now run: ng serve --ssl --port=4200
pause
