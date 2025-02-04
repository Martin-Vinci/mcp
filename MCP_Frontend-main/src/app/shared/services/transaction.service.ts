import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'environments/environment';
import { throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { APIResponse } from '../models/api-response';
import { EscrowData } from '../models/escrow-data';
import { TransactionRef } from '../models/transaction-ref';
import { VoucherData } from '../models/voucher-data';
//import { EnvService } from './env.service';

@Injectable({
  providedIn: 'root'
})
export class TransactionService {

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
  
  findTransactions(request: TransactionRef) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/transaction/findTransactions`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  findTransactionDetails(request: number) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/transaction/findTransactionDetails`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  findTransCenteTrustDetails(request: TransactionRef) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/transaction/findTransCenteTrustDetails`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  findTransCenteTrustSummary(request: TransactionRef) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/transaction/findTransCenteTrustSummary`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  findCenteTrustByTransId(request: TransactionRef) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/transaction/findCenteTrustByTransId`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  
  findEscrowTransactions(request: EscrowData) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/transaction/findEscrowTransactions`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  approveEscrowTransaction(request: EscrowData) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/approveEscrowTransaction`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  findTransVouchers(request: VoucherData) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/transaction/findTransVouchers`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }




}
