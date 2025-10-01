import { Injectable } from "@angular/core";
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { of } from "rxjs"; // Import 'of' from rxjs
import { GenericApiService } from "../features/audit/generic-api.service";

@Injectable({ providedIn: "root" })
export class UserService extends GenericApiService<any>{
  apiUrl0 = "https://127.0.0.1:5555/user";

  users: any = [
    {
      id: 1,
      version: 0,
      emailAddress: "romanmuntaha@gmail.com",
      firstName: "Roman",
      lastName: "Muntaha",
      userName: "Admin"
    },
    {
      id: 2,
      version: 0,
      emailAddress: "johnsmith@gmail.com",
      firstName: "John",
      lastName: "Smith",
      userName: "Johny"
    }
  ]

  constructor(public override http: HttpClient) {
    super(http);
    this.setSuffix('user');
  }
  getUsers(): any {
    // return of(this.users);
    return this.http.get(this.apiUrl0 + `?limit=100&offset=0`);
  }

  getUser(id: any): any {
    return this.http.get(this.apiUrl0 + `/${id}`);
  }
  
  getUserRole(id: any): any {
    return this.http.get(this.apiUrl0 + `/${id}/userrole`);
  }
}
