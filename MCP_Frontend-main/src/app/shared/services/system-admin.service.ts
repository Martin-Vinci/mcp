import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'environments/environment';
import { throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { APIResponse } from '../models/api-response';
import { ServiceChannel } from '../models/biller';
import { ControlParameter } from '../models/control-parameter';
import { UserData } from '../models/user-data';
import { UserTypeRef } from '../models/user-type';
import { UserTypeAccessRight } from '../models/user-type-access';
//import { EnvService } from './env.service';

@Injectable({
  providedIn: 'root'
})
export class SystemAdminService {

  constructor(
     private http: HttpClient, 
  ) { }

  handleError(error: HttpErrorResponse) {
    let errorMessage = 'Unknown error!';
    if (error.error instanceof ErrorEvent) {
      // Client-side errors
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Server-side errors
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
    }
    return throwError(errorMessage);
  }

  maintainUsers(request: UserData) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/system-admin/user/maintainUsers`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  findUsers(request: UserData) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/system-admin/user/findUsers`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  maintainParameters(request: ControlParameter) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/system-parameter/maintainParameters`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  findParameters(request: ControlParameter) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/system-parameter/findParameters`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  maintainMemberType(request: UserTypeRef) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/system-admin/user-role/maintainUserRole`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  findMemberType(request: UserTypeRef) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/system-admin/user-role/findUserRole`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }



  assignAccessRight(request: UserTypeAccessRight[]) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/system-admin/user-role/assignUserRoleAccessRight`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  revokeAccessRight(request: UserTypeAccessRight[]) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/system-admin/user-role/revokeUserRoleAccessRight`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  findAssignedAccessRights(request: UserTypeAccessRight) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/system-admin/user-role/findUserRoleAssignedAccessRights`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  findUnAssignedAccessRights(request: UserTypeAccessRight) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/system-admin/user-role/findUnAssignedUserRoleAccessRights`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }





}
