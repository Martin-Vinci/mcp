import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'environments/environment';
import { throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { APIResponse } from '../models/api-response';
import { Biller, ServiceChannel } from '../models/biller';
import { BillNotif } from '../models/biller-notif';
import { BillerNotifLog } from '../models/biller-notif-log';
import { BillerProduct } from '../models/biller-products';
import { BillerCategory } from '../models/BillerCategory';
import { MessageBox } from '../models/message-box';
import { SearchCriteria } from '../models/search-criteria';
import { TransCode } from '../models/trans-code';
//import { EnvService } from './env.service';

@Injectable({
  providedIn: 'root'
})
export class BillerService {
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

findNotifications(request: BillNotif) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/biller/findNotifications`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }
  

  findNotificationLog(request: BillerNotifLog) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/biller/findNotificationLog`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  findBillers(request: Biller) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/biller/findBillers`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  
  findChannels(request: ServiceChannel) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/service-channel/findChannels`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  maintainServiceChannel(request: ServiceChannel) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/service-channel/maintainServiceChannel`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }




  findAllSMS(request: SearchCriteria) {
    request.phoneNo = request.phoneNo == "" ? null : request.phoneNo;
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/mobileUser/findAllSMS`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  
  maintainBiller(request: Biller) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/biller/maintainBiller`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  findBillerProductCategories(request: BillerCategory) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/biller/findBillerProductCategoryByBillerId`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  maintainBillerProductCategory(request: BillerProduct) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/biller/saveBillerProductCategory`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }



  findBillerProducts(request: BillerProduct) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/biller/findBillerProductsByBiller`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  maintainBillerProduct(request: BillerProduct) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/biller/saveBillerProduct`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


}
