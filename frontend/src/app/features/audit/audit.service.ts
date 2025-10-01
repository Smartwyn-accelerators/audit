import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthenticationService } from '../../core/services/authentication.service';
@Injectable({
  providedIn: 'root'
})

export class AuditService {
  url = environment.apiUrl;
  private readonly auditFrontendApiUrl = 'https://127.0.0.1:5555/audit/frontend';

  constructor(private httpclient: HttpClient, private authService: AuthenticationService) {
  }


  public getAll(search: string, offset: number, limit: number) {
    return this.httpclient.get(this.url + '/audit', {
      params: {
        search: search,
        offset: offset.toString(),
        limit: limit.toString()
      }
    }).pipe()
  }

      /**
     * Logs an audit event to the backend
     * @param email - The email of the user
     * @param sessionId - The current session ID
     * @param action - The action being logged
     * @param details - Additional details of the event
     * @returns Observable for the HTTP POST request
     */
      logAuditEvent(
        email: any,
        sessionId: string,
        action: string,
        details: Record<string, any>
    ): Observable<any> {
        const payload = {
            userId: email,
            sessionId,
            action,
            details,
        };

        return this.httpclient.post(this.auditFrontendApiUrl, payload);
    }

    /**
     * Audits a navigation event
     * @param urlAfterRedirects - The URL navigated to
     * @returns Observable for the HTTP POST request
     */
    auditNavigation(urlAfterRedirects: string): Observable<any> {
        const userId = this.authService.getLoggedinUserId();
        return this.logAuditEvent(userId, '', 'frontend_api', {
            urlAfterRedirects: urlAfterRedirects
        });
    }
}