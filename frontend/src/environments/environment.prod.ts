import { LogLevel, OpenIdConfiguration } from 'angular-auth-oidc-client';

export const environment = {
  production: true,
  apiUrl: 'https://your-api-domain.com'
};

export const authConfig: OpenIdConfiguration = {
  authority: 'https://login.microsoftonline.com/your-tenant-id',
  redirectUrl: window.location.origin + '/auth/callback',
  clientId: 'your-client-id',
  responseType: 'code',
  scope: 'openid profile email offline_access',
  startCheckSession: false,
  silentRenew: true,
  silentRenewUrl: window.location.origin + '/assets/silent-refresh.html',
  postLogoutRedirectUri: window.location.origin,
  forbiddenRoute: '/auth/forbidden',
  unauthorizedRoute: '/auth/unauthorized',
  logLevel: LogLevel.Warn,
  silentRenewTimeoutInSeconds: 300,
  disableIatOffsetValidation: true,
  autoUserInfo: false,
  useRefreshToken: true,
  customParamsAuthRequest: {
    prompt: 'select_account'
  }
};
