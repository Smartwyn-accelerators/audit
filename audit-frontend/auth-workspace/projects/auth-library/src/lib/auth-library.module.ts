import { ErrorHandler, NgModule } from '@angular/core';
import { AuthLibraryComponent } from './auth-library.component';
import { ActionTrackingService } from './action-tracking.service';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { ApiInterceptorService } from './api-interceptor.service';
import { SessionTrackingService } from './session-tracking.service';
import { GlobalErrorHandlerService } from './global-error-handler.service';
import { RouterModule } from '@angular/router';

@NgModule({
  declarations: [
    AuthLibraryComponent
  ],
  imports: [
     HttpClientModule
  ],
  exports: [
    AuthLibraryComponent 
  ],
  providers: [ SessionTrackingService, ActionTrackingService,
    { provide: HTTP_INTERCEPTORS, useClass: ApiInterceptorService, multi: true },
    {
      provide: ErrorHandler,
      useClass: GlobalErrorHandlerService, // Register the error handler here
    },
  ], 
})
export class AuthLibraryModule { }
