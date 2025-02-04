import { Injectable } from "@angular/core";
import { Router } from "@angular/router";
import { HttpClient } from "@angular/common/http";
import { map } from "rxjs/operators";
// import { environment } from "environments/environment";
import { APIResponse } from "../models/api-response";
import { LoanAccount } from "../models/loan-account";
import { LoanSchedule } from "../models/loan-account-schedule";
import { CreditAppl } from "../models/loan-application";
import { LoanRepayment } from "../models/loan-repayment";
import { environment } from "environments/environment";
//import { EnvService } from './env.service';


@Injectable({ providedIn: "root" })
export class LoanServiceService {
  constructor(private router: Router,  private http: HttpClient, ) {}

  findAllCreditApplications(request: CreditAppl) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/webcash/findAllCreditApplications`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }));
  }

  creditApplication(request: CreditAppl) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/webcash/creditApplication`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }));
  }

  approveLoan(request: CreditAppl) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/webcash/approveLoan`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }));
  }
 

  findLoanAccounts(request: LoanAccount) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/webcash/findLoanAccount`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }));
  }


  findLoanSchedule(request: LoanSchedule) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/webcash/findLoanSchedule`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }));
  }


  disburseLoan(request: LoanAccount) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/webcash/disburseLoan`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }));
  }

  postLoanRepayment(request: LoanRepayment) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/webcash/postLoanRepayment`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }));
  }

  findTransHistory(request: LoanRepayment) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/webcash/findTransHistory`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }));
  }

}
