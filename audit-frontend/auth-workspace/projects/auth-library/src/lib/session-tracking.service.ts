import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SessionTrackingService {
  startSession() {
    sessionStorage.setItem('sessionStart', new Date().toISOString());
  }

  endSession() {
    const sessionStart = new Date(sessionStorage.getItem('sessionStart') || '');
    const sessionEnd = new Date();
    const duration = sessionEnd.getTime() - sessionStart.getTime();
    this.logSession(duration);
    sessionStorage.removeItem('sessionStart');
  }

  logSession(duration: number) {
    const log = {
      sessionStart: new Date(sessionStorage.getItem('sessionStart') || ''),
      sessionEnd: new Date(),
      duration,
      userId: this.getUserId()
    };
    console.log('Session Log:', log);
    // Send session log to backend or store it locally
  }

  private getUserId(): string {
    return 'user123';  // Replace with actual logic to retrieve user ID
  }
}
