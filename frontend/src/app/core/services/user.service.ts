import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface User {
  id: number;
  emailAddress: string;
  firstName?: string;
  lastName?: string;
  userName?: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  getUser(userId: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/user/${userId}`);
  }

  getUserRole(id: any): any {
    return this.http.get(this.apiUrl + `/user/${id}/userrole`);
  }
}
