import { ErrorHandler, Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class GlobalErrorHandlerService implements ErrorHandler {
  handleError(error: any): void {
    const errorLog = {
      message: error.message,
      stack: error.stack,
      timestamp: new Date().toISOString(),
      userId: this.getUserId(),
      sessionId: this.getSessionId(),
    };
    console.error('Error Log:', errorLog);
    // Here you can send the error log to the backend or store it locally
  }

  private getUserId(): string {
    return 'user123'; // Replace with actual logic to retrieve user ID
  }

  private getSessionId(): string {
    return 'session456'; // Replace with actual session ID logic
  }
}
