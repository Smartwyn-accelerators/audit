import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';


import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ActionTrackingService, ApiInterceptorService, AuthLibraryModule } from 'auth-library';

import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { RouterModule, Routes  } from '@angular/router';
import { FormsModule } from '@angular/forms'; 
import { AnotherPageComponent } from './another-page/another-page.component';

const routes: Routes = [
  { path: '', component: AppComponent }, // Home route
  { path: 'another-page', component: AnotherPageComponent } // Define route for another page
];
@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AuthLibraryModule,
    RouterModule.forRoot(routes),
    HttpClientModule,
    FormsModule // Make sure this is imported
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: ApiInterceptorService, multi: true }
    // ActionTrackingService 
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
