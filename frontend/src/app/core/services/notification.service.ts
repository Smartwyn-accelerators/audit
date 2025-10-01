import { HttpClient } from '@angular/common/http';
import { Injectable, NgZone } from '@angular/core';
import { Observable, Subject } from 'rxjs';
// @ts-ignore
import { EventSourcePolyfill } from 'event-source-polyfill';
import { AuthenticationService } from './authentication.service';

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private eventSource: EventSourcePolyfill | null = null;
  private reconnectAttempts = 0;
  private readonly maxReconnectAttempts = 20;
  private readonly reconnectDelay = 5000; // 5 seconds
  private readonly subject = new Subject<any>();
  constructor(
    private authService: AuthenticationService, 
    public zone: NgZone,
    public http: HttpClient
  ) { }

  connect(url: string): Observable<any> {
    if (!this.eventSource) {
      this.startEventSource(url);
    }
    return this.subject.asObservable();
  }

  private startEventSource(url: string): void {
    const headers = {
      Authorization: this.authService.authorizationToken,
    };

    try {
      this.eventSource = new EventSourcePolyfill(url, { headers });
      
    } catch(error) {
      console.log()
    }
    this.eventSource.withCredentials = true;
    this.eventSource.onopen = (res: Event) => {
      console.log('SSE connection opened')
    };
    this.eventSource.onmessage = (event: MessageEvent) => {
      try {
        const data = JSON.parse(event.data);
        this.zone.run(() => this.subject.next(data));
      } catch (err) {
        console.error('Error parsing SSE event data:', err);
      }
    };

    this.eventSource.onerror = () => {
      this.eventSource?.close();
      this.eventSource = null;
      this.reconnectAttempts++;

      if (this.reconnectAttempts <= this.maxReconnectAttempts) {
        setTimeout(() => this.startEventSource(url), this.reconnectDelay);
      }
      // else {
      //   this.zone.run(() =>
      //     this.subject.error('Max reconnection attempts reached.')
      //   );
      // }
    };
  }


  disconnect(): void {
    this.eventSource?.close();
    this.eventSource = null;
    this.reconnectAttempts = 0;
  }

  getNotificationList() {
    return this.http.get<any>("https://127.0.0.1:5555/api/notifications?limit=1000&offset=0")
  }
}