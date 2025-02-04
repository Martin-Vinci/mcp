import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'environments/environment';
import { throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { APIResponse } from '../models/api-response';
import { Biller } from '../models/biller';
import { BillNotif } from '../models/biller-notif';
import { BillerNotifLog } from '../models/biller-notif-log';
import { SearchCriteria } from '../models/search-criteria';
import { TransactionRef } from '../models/transaction-ref';
//import { EnvService } from './env.service';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
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

  findUserListingReport(request: any) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/biller/findNotifications`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  findAgentCommissionReport(request: any) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/biller/findNotificationLog`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  findAgentListingReport(request: any) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/biller/findBillers`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  findCustomerListingReport(request: any) {
    request.phoneNo = request.phoneNo == "" ? null : request.phoneNo;
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/mobileUser/findAllSMS`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  findTransactionListingReport(request: TransactionRef) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/reports/findTransactions`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  findActiveAgents(request: TransactionRef) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/reports/findActiveAgents`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  } 

  findTransactionBandReport(request: any) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/reports/findTransactionBands`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  findVoucherListingReport(request: any) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/transaction/findTransactions`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  findDashboardStatistics(request: SearchCriteria) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/reports/findDashboardStatistics`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }
  findAgentFloatLevels(request: SearchCriteria) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/reports/findAgentFloatLevels`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }
  findCustomerAccountBalanceLevels(request: SearchCriteria) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/reports/findCustomerAccountBalanceLevels`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }
  findActiveCustomerCategories(request: SearchCriteria) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/reports/findActiveCustomerCategories`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  
  findActiveAgentsByTransactions(request: SearchCriteria) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/reports/findActiveAgentsByTransactions`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  findUserAccountsByCategoryAndFloatLevels(request: SearchCriteria) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/reports/findUserAccountsByCategoryAndFloatLevels`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  findMobileUsersByGender(request: SearchCriteria) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/reports/findMobileUsersByGender`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }
}
