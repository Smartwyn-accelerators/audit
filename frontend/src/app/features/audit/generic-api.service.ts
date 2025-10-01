import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ISearchField } from '../audit/ISearchCriteria';
import { environment } from '../../../environments/environment';
import { ServiceUtils } from './audit-service-util';

@Injectable()
export class GenericApiService<T> {
  protected url = '';
  protected apiUrl = '';
  protected suffix = '';
  
  constructor(protected http: HttpClient) {
    this.apiUrl = environment.apiUrl;
  }

  setSuffix(suffix: string) {
    this.suffix = suffix;
    this.url = environment.apiUrl + '/' + suffix;
  }

  /**
   * Fetches list of items based on
   * given criteria.
   * @param searchFields Search criteria.
   * @param offset No. of items to be skipped.
   * @param limit Maximum no. of records.
   * @param sort Field and direction information for sorting.
   * @returns Observable of items list.
   */
  public getAll(searchFields?: ISearchField[], offset?: number, limit?: number, sort?: string): Observable<T[]> {
    let params = ServiceUtils.buildQueryData(searchFields, offset, limit, sort);

    return this.http
      .get<T[]>(this.url, { params })
      .pipe(
        map((response: any) => {
          return response;
        }),
        catchError(this.handleError)
      );
  }


  protected handleError(err: HttpErrorResponse) {
    let errorMessage;
    if (err.error instanceof ErrorEvent) {
      // A client-side or network error occurred. Handle it accordingly.
      errorMessage = 'An error occurred: ' + err.error.message;
    } else {
      console.log(err);
      errorMessage = 'Server returned code: ' + err.status + ', error message is: ' + err.message;
    }
    console.error(errorMessage);
    return throwError(errorMessage);
  }

}