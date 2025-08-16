import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ApiInterceptorService {

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const startTime = Date.now();
    return next.handle(req).pipe(
      tap(event => {
        if (event instanceof HttpResponse) {
          const duration = Date.now() - startTime;
          this.logRequest(req, event, duration);
        }
      })
    );
  }

  logRequest(req: HttpRequest<any>, res: HttpResponse<any>, duration: number) {
    const log = {
      url: req.url,
      method: req.method,
      requestPayload: req.body,
      responsePayload: res.body,
      statusCode: res.status,
      duration,
      timestamp: new Date().toISOString(),
      userId: this.getUserId(),  // Assuming a method to get user ID
      sessionId: this.getSessionId()
    };
    console.log('API Request Log:', log);
    // Send log to backend or store it locally
  }

  private getUserId(): string {
    return 'user123';  // Replace with actual logic to retrieve user ID
  }

  private getSessionId(): string {
    return 'session456';  // Replace with actual session ID logic
  }
}
