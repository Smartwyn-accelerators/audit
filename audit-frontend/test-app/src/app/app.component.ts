import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { ActionTrackingService, ApiInterceptorService, SessionTrackingService } from 'auth-library'; // Import your services
import { HttpClient, HttpResponse } from '@angular/common/http';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit, OnDestroy {
  constructor(
    private actionTrackingService: ActionTrackingService,
    private apiInterceptorService: ApiInterceptorService, // Inject ApiInterceptorService
    private sessionTrackingService: SessionTrackingService, // Inject SessionTrackingService
    private router: Router,
    private http: HttpClient // Inject HttpClient
  ) {}

  ngOnInit() {
    this.sessionTrackingService.startSession(); // Start session on component initialization
  }

  ngOnDestroy() {
    this.sessionTrackingService.endSession(); // End session on component destruction
  }

  trackFormSubmission(formName: string) {
    console.log(`Form submitted: ${formName}`);
    this.actionTrackingService.trackFormSubmission(formName); // Call your tracking logic
  }

  navigateToUrl(url: string) {
    // Call endSession before navigating away
    this.sessionTrackingService.endSession(); // End session when navigating
    // Call the tracking service before navigating
    this.actionTrackingService.trackNavigation(url);
    this.router.navigate([url]);
  }

  // Function to make a test API call
  testApiCall() {
    this.http.get<any>('https://jsonplaceholder.typicode.com/posts').subscribe(
      (response: any) => {
        console.log('API Response:', response);
        const duration = 0; // Set duration to zero as we're just logging the response
        this.apiInterceptorService.logRequest(
          { url: 'https://jsonplaceholder.typicode.com/posts', method: 'GET', body: null } as any,
          { body: response, status: 200, statusText: 'OK', headers: {}, config: {}, type: 0 } as unknown as HttpResponse<any>,
          duration
        );
      },
      (error) => {
        console.error('API Error:', error);
      }
    );
  }

  testErrorHandling() {
    throw new Error('This is a test error for the global error handler.');
  }
}
