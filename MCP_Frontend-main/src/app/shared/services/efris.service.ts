import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'environments/environment';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { APIResponse } from '../models/api-response';
import { EfrisCommodity } from '../models/efris-commodity';
import { EfrisInvoice } from '../models/efris-invoice';
import { InvoiceData } from '../models/efris-invoice-data';
import { GoodsData } from '../models/goods-data';
import { TaxPayer } from '../models/tax-payer';

@Injectable({
  providedIn: 'root'
})
export class EfrisService {

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


  queryTaxPayerInformation(request: TaxPayer) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/efris/queryTaxPayerInformation`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  querySystemDictionaryUpdate(request: TaxPayer) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/efris/querySystemDictionaryUpdate`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  queryCommodityCategoryByParentCode(request: EfrisCommodity) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/efris/queryCommodityCategoryByParentCode`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  goodsUpload(request: GoodsData) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/efris/goodsUpload`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  goodsAndServiceInquiry(request: GoodsData) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/efris/goodsAndServiceInquiry`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  generateInvoice(request: InvoiceData) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/efris/createInvoice`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  findAllInvoices(request: EfrisInvoice) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/efris/findAllInvoices`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  invoiceDownLoad(invoiceId: string | undefined): Observable<Blob> {
    console.log("============================== " + invoiceId);
    return this.http.get(`${environment.apiBackEndUrl}/efris/invoiceDownLoad/${invoiceId}`, {
      responseType: 'blob'
    });
  }
}
