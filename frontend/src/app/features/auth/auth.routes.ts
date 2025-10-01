import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { OidcCallbackComponent } from './components/oidc-callback/oidc-callback.component';

export const authRoutes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'callback', component: OidcCallbackComponent }
];
