# Timesheet Application with OIDC Authentication

This Angular 20 application demonstrates OpenID Connect (OIDC) authentication using Azure AD.

## Features

- ✅ Angular 20 with standalone components
- ✅ OIDC authentication with Azure AD
- ✅ JWT token management
- ✅ Automatic token refresh
- ✅ Route guards for protected routes
- ✅ Material Design UI components
- ✅ Silent token renewal

## Prerequisites

- Node.js 18+
- Angular CLI 20+
- Azure AD tenant with admin access

## Setup Instructions

### 1. Install Dependencies

```bash
npm install
```

### 2. Configure Azure AD

1. Go to [Azure Portal](https://portal.azure.com)
2. Navigate to "Azure Active Directory" > "App registrations"
3. Click "New registration"
4. Enter application name: "Angular Timesheet App"
5. Select "Accounts in this organizational directory only"
6. Set redirect URI to `https://localhost:4200/auth/callback`
7. Click "Register"

### 3. Get Azure AD Configuration

1. Copy the **Application (client) ID** and **Directory (tenant) ID**
2. Go to "Certificates & secrets" > "New client secret"
3. Copy the secret value
4. Go to "API permissions" > "Add a permission" > "Microsoft Graph" > "Delegated permissions"
5. Add: `openid`, `profile`, `email`, `offline_access`
6. Click "Grant admin consent"

### 4. Update Environment Configuration

Edit `src/environments/environment.ts` and `src/environments/environment.prod.ts`:

```typescript
export const authConfig: OpenIdConfiguration = {
  authority: 'https://login.microsoftonline.com/YOUR-TENANT-ID',
  clientId: 'YOUR-CLIENT-ID',
  // ... other configuration
};
```

### 5. Configure Backend API

Update the `apiUrl` in your environment files to point to your backend API:

```typescript
export const environment = {
  production: false,
  apiUrl: 'https://your-backend-api.com'
};
```

### 6. Start the Application

```bash
ng serve --ssl --port=4200
```

Navigate to `https://localhost:4200`

## Project Structure

```
src/
├── app/
│   ├── core/
│   │   ├── guards/           # Route guards
│   │   ├── interceptors/     # HTTP interceptors
│   │   ├── models/           # TypeScript interfaces
│   │   └── services/         # Core services
│   ├── features/
│   │   ├── auth/             # Authentication components
│   │   └── home/             # Home page component
│   └── environments/         # Environment configuration
└── assets/
    ├── images/               # Static images
    └── silent-refresh.html   # Silent refresh page
```

## Key Components

### Authentication Service
- Manages OIDC authentication flow
- Handles custom API token storage and refresh (Azure AD tokens handled automatically)
- Provides user information

### Guards
- `authGuard`: Protects routes requiring authentication
- `landingGuard`: Handles initial route logic

### Interceptors
- `authInterceptor`: Adds authentication headers to requests
- `errorInterceptor`: Handles 401/403 errors and custom API token refresh (Azure AD tokens handled automatically)

## API Endpoints

The application expects the following backend endpoints:

- `GET /auth/getAuthorizationToken` - Exchange OIDC token for JWT
- `POST /auth/refresh` - Refresh expired JWT token

## Troubleshooting

### Common Issues

1. **CORS Errors**: Ensure your backend allows requests from `https://localhost:4200`
2. **Invalid Tokens**: Verify Azure AD configuration matches your environment files
3. **Silent Refresh Issues**: Check that `silent-refresh.html` is accessible
4. **Redirect Issues**: Verify redirect URLs match exactly in Azure AD

### Debug Mode

Enable debug logging by setting `logLevel: LogLevel.Debug` in your auth configuration.

## Security Considerations

- Always use HTTPS in production
- Implement proper token storage mechanisms
- Configure CORS appropriately
- Set up Content Security Policy headers

## Development

### Adding New Protected Routes

1. Create your component
2. Add route with `canActivate: [authGuard]`
3. Update navigation as needed

### Adding Permissions

1. Update `PermissionService` with new permission logic
2. Use `hasPermission()` method in components
3. Update backend to include permissions in JWT tokens

## License

This project is part of the FastCode Accelerators suite.