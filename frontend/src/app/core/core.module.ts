import { NgModule } from '@angular/core';
// import { SharedModule } from '../../../common/shared';
import { CoreRoutingModule } from './core.routing';
// import { OidcCallbackComponent } from '../landing/oidc-callback/oidc-callback.component';
// import { LoginComponent } from '../landing/login/login.component';

@NgModule({
	declarations: [],
	exports: [],
  imports: [
    // SharedModule,
    CoreRoutingModule
  ]
})
export class CoreModule {
}
