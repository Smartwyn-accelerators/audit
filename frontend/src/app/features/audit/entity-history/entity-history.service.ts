import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { IEntityHistory } from './entityHistory';
@Injectable({
  providedIn: 'root'
})

export class EntityHistoryService {
  url = environment.apiUrl;

  constructor(private httpclient: HttpClient) {
  }

  public getAll(search:string, offset:number, limit: number): Observable<IEntityHistory[]> {
    return this.httpclient.get<IEntityHistory[]>(this.url + '/audit/changes' + "?search=" + search + "&offset=" + offset + "&limit=" + limit).pipe();
  }
  
  public getByEntity(entity: string, search: string, offset: number, limit: number): Observable<IEntityHistory[]> {
    return this.httpclient
      .get<IEntityHistory[]>(
        this.url + '/audit/' + entity + '?search=' + search + '&offset=' + offset + '&limit=' + limit
      )
      .pipe();
  }
  public getAllEntities(): Observable<any> {
    return this.httpclient.get(this.url + '/audit/entities')
  }
}