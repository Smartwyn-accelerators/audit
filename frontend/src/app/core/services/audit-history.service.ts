import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthenticationService } from './authentication.service';

@Injectable({
    providedIn: 'root',
})
export class AuditHistoryService {

    private readonly auditFrontendApiUrl = 'https://localhost:5555/audit/frontend';
    private readonly auditApiUrl = 'https://localhost:5555/audit';

    constructor(private readonly http: HttpClient, private authService: AuthenticationService) { }

    /**
     * Logs an audit event to the backend
     * @param userId - The ID of the user
     * @param sessionId - The current session ID
     * @param action - The action being logged
     * @param details - Additional details of the event
     * @returns Observable for the HTTP POST request
     */
    logAuditEvent(
        userId: any,
        sessionId: string,
        action: string,
        details: Record<string, any>
    ): Observable<any> {
        const payload = {
            userId,
            sessionId,
            action,
            details,
        };

        return this.http.post(this.auditFrontendApiUrl, payload);
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

    /**
     * Retrieves all audit logs from the backend
     * @returns Observable for the HTTP GET request
     */
    getAllLogs(): Observable<any> {
        return this.http.get(`${this.auditApiUrl}`);
    }
}
