import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HTTP_INTERCEPTORS } from '@angular/common/http';
import { Observable } from 'rxjs';
import { EnvService } from '../_services/env.service';
import { SecurityService } from '../_services/security.service';

const TOKEN_HEADER_KEY = 'Authorization';
@Injectable()
export class JwtInterceptor implements HttpInterceptor {
    constructor(private accountService: SecurityService,
        ) { }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        // add auth header with jwt if user is logged in and request is to the api url
        let authReq = request;
        if (this.accountService.currentUser != null) {
            const token = this.accountService.currentUser.token;
            request = authReq.clone({ headers: authReq.headers.set(TOKEN_HEADER_KEY, token.toString()) });
        }
        return next.handle(request);
    }
}

export const authInterceptorProviders = [
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true }
];