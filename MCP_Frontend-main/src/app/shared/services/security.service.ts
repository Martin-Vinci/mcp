import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, map, timeout } from 'rxjs/operators';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { AuthenticationResponseData, AuthenticationRequestData } from '../models/authentication-data';
import { PasswordChangeData } from '../models/password-change-data';
import { ResetPwdData, ResetPwdResponseData } from '../models/reset-password-data';
import { ResponseStatus } from '../models/response-status';
import { environment } from 'environments/environment';
import { APIResponse } from '../models/api-response';

@Injectable({ providedIn: 'root' })
export class SecurityService {
    private userSubject: BehaviorSubject<AuthenticationResponseData>;
    public authResponse: Observable<AuthenticationResponseData>;
    constructor(
        private router: Router,
         private http: HttpClient,
        private ngxService: NgxUiLoaderService
    ) {
        this.userSubject = new BehaviorSubject<AuthenticationResponseData>(JSON.parse(sessionStorage.getItem('user')));
        this.authResponse = this.userSubject.asObservable();
    }

    public get currentUser(): AuthenticationResponseData {
        return this.userSubject.value;
    }

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


    getAccessRights(accessCode: string): boolean {
        let resp = false;
        let x = JSON.parse(sessionStorage.getItem('ScreenAccessRights'));
        for (const element of x) {
            if (element.menuCode == accessCode) {
                resp = true;
                break;
            }
        }
        return resp;
    }


    getMenus(userTypeId: number) {
        //console.log(this.pickListRequest);
        return this.http.get(`${environment.apiBackEndUrl}/security/getMenus/${userTypeId}`).pipe(catchError(this.handleError));
    }


    loginUser(authRequest: AuthenticationRequestData) {
        let authData: AuthenticationResponseData;
        return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/security/loginUser`, authRequest)
            .pipe(timeout(900000), map(authResponse => {
                if (authResponse.code == "00") {
                    authData = JSON.parse(JSON.stringify(authResponse.data));
                    sessionStorage.setItem('user', JSON.stringify(authData));
                    this.userSubject.next(authData);
                }
                return authResponse;
            }), catchError(this.handleError));
    }

    logoutUser() {
        let authRequest = new AuthenticationRequestData;
        authRequest.employeeId = this.currentUser.employeeId;
        authRequest.token = this.currentUser.token;
        authRequest.userName = this.currentUser.userName;
        this.ngxService.start();
        sessionStorage.removeItem('user');
        this.userSubject.next(null);
        sessionStorage.clear();
        this.router.navigate(['/sessions/signin']);
        return this.http.post<ResponseStatus>(`${environment.apiBackEndUrl}/security/logoutUser`, authRequest)
            .pipe(map((authResponse) => {
                // remove user from local storage and set current user to null  
                this.ngxService.stop();
                return authResponse;
            },
                (error) => {
                    console.log(error)
                    this.ngxService.stop();
                })
            );
    }


    submitPasswordChange(request: PasswordChangeData) {
        return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/security/changePassword`, request)
            .pipe(map(responseStatus => {
                //console.log(responseStatus);
                return responseStatus;
            }));
    }


    resetUserPassword(userData: PasswordChangeData) {
        //console.log(userData);      
        return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/security/resetUserPassword`, userData)
            .pipe(map(responseStatus => {
                //console.log(responseStatus);
                return responseStatus;
            }));
    }

    verifyAuthentication(authRequest: AuthenticationRequestData) {
        let authData: AuthenticationResponseData;
        return this.http.post<AuthenticationResponseData>(`${environment.apiBackEndUrl}/verifyAuthentication`, authRequest)
            .pipe(map(authResponse => {
                authData = JSON.parse(JSON.stringify(authResponse));
                return authResponse;
            }));
    }


    commitBranchChangeInfo(authResponse: AuthenticationResponseData) {
        sessionStorage.setItem('user', JSON.stringify(authResponse));
        this.userSubject.next(authResponse);
    }

    findClientIp() {
        const ipAddress = this.http.get("http://api.ipify.org/?/format=json");
        alert(JSON.stringify(ipAddress));
        return ipAddress;
    }



    logoutAllUserSessions(authRequest: AuthenticationRequestData) {
        return this.http.post<ResponseStatus>(`${environment.apiBackEndUrl}/logoutUser`, authRequest)
            .pipe(map((authResponse) => {
                // remove user from local storage and set current user to null  
                sessionStorage.removeItem('user');
                this.userSubject.next(null);
                sessionStorage.clear();
                this.router.navigate(['/sessions/signin']);
                return authResponse;
            })
            );
    }


    clearSession(authRequest: AuthenticationRequestData) {
        return this.http.post<ResponseStatus>(`${environment.apiBackEndUrl}/logoutUser`, authRequest)
            .pipe(map((authResponse) => {
                return authResponse;
            },
                (error) => {
                    console.log(error)
                })
            );
    }

    findEmployeeInfo(data: ResetPwdData) {
        //console.log(this.pickListRequest);
        return this.http.post<ResetPwdResponseData>(`${environment.apiBackEndUrl}/findEmployeeInfo`, data)
            .pipe(map(responseStatus => {
                //console.log(responseStatus);
                return responseStatus;
            }));
    }

}