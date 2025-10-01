
import { RouterModule, Routes } from '@angular/router';
import { ModuleWithProviders } from "@angular/core";
// import { AuthGuard } from 'src/app/core/guards/auth.guard';
// import { LoginComponent } from '../landing/login/login.component';
// import { OidcCallbackComponent } from '../landing/oidc-callback/oidc-callback.component';

const routes: Routes = [
	// { path: '', component: LoginComponent, pathMatch: 'full' },
	// { path: 'login', component: LoginComponent },
	// { path: 'dashboard',  component: DashboardComponent ,canActivate: [ AuthGuard ]  },
];

export const CoreRoutingModule: ModuleWithProviders<any> = RouterModule.forChild(routes);