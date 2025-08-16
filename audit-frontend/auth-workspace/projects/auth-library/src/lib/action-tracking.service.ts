import { Injectable } from '@angular/core';
import { Subscription } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ActionTrackingService {
  private subscription: Subscription | null = null;

  constructor() {}

  trackNavigation(url: string) {
    console.log('Navigation ended to:', url);
    // Your tracking logic here
  }

  stopTracking() {
    if (this.subscription) {
      this.subscription.unsubscribe();
      this.subscription = null; // Reset to null after unsubscribing
    }
  }

  trackFormSubmission(formName: string) {
    console.log(`Tracking form submission for: ${formName}`);
    // Your tracking logic here
  }
}
