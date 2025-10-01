import { LogLevel, OpenIdConfiguration } from 'angular-auth-oidc-client';

export const environment = {
  production: false,
  apiUrl: 'https://localhost:5555'
};

export const authConfig: OpenIdConfiguration = {
  authority: 'https://login.microsoftonline.com/ed68e3c1-aef4-48be-9306-a94b9c1d4e0e/v2.0',
  redirectUrl: window.location.origin + '/auth/callback',
  clientId: '21b1da23-7877-43c3-9dbd-48b88d996484',
  responseType: 'code',
  scope: 'openid profile email offline_access',
  startCheckSession: false,
  silentRenew: true,
  silentRenewUrl: window.location.origin + '/assets/silent-refresh.html',
  postLogoutRedirectUri: window.location.origin + '/auth/login',
  forbiddenRoute: '/auth/forbidden',
  unauthorizedRoute: '/auth/unauthorized',
  logLevel: LogLevel.Debug,
  silentRenewTimeoutInSeconds: 300,
  disableIatOffsetValidation: true,
  autoUserInfo: false,
  useRefreshToken: true,
  customParamsAuthRequest: {
    prompt: 'select_account'
  }
};
