import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'environments/environment';
import { map } from 'rxjs/operators';
import { APIResponse } from '../models/api-response';
import { Charge } from '../models/charge';
//import { EnvService } from './env.service';


@Injectable({
  providedIn: 'root'
})
export class AgentbakingService {

  constructor(
     private http: HttpClient, 
  ) { }


  findCharges(request: Charge) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/global/charges/findCharges`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }));
  }


  findChargePickList(request: Charge) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/global/charges/queryMetadata`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }));
  }

  maintainCharges(request: Charge) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/global/charges/maintainCharges`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }));
  }
}
