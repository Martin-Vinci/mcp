import { Injectable } from "@angular/core";
import { Router } from "@angular/router";
import { HttpClient } from "@angular/common/http";
import { BehaviorSubject, Observable } from "rxjs";
import { map } from "rxjs/operators";
// import { environment } from "environments/environment";
import { APIResponse } from "../models/api-response";
import { Customer } from "../models/customer";
import { environment } from "environments/environment";



@Injectable({ providedIn: "root" })
export class CustomerService {
  constructor(private router: Router,  private http: HttpClient, ) {}

  findAllCustomers(request: Customer) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/webcash/findAllCustomers`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }));
  }

  signUp(request: Customer) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/webcash/signUp`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }));
  }

}
