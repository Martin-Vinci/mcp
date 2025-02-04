import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'environments/environment';
import { throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { AccountData } from '../models/account-data';
import { AgentData } from '../models/agent-data';
import { APIResponse } from '../models/api-response';
import { ChargeData } from '../models/charge-data';
import { ChargeTierData } from '../models/charge-tier-data';
import { CommissionData } from '../models/commission-data';
import { CommissionTier, CommissionTierData } from '../models/commission-tier-data';
import { Customer } from '../models/customer';
import { MobileUser } from '../models/mobile-user';
import { MobileUserAcct } from '../models/mobile-user-acct';
import { OutletData } from '../models/outlet-data';
import { ServicePostingPolicy } from '../models/service-posting-policy';
import { TransCode } from '../models/trans-code';
import { ErrorHandlerService } from './error-handler.service';
//import { EnvService } from './env.service';

@Injectable({
  providedIn: 'root'
})
export class AgentBankingService {

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


  maintainAgents(request: AgentData) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/agent/maintainAgents`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  findAgentAccounts(request: AccountData) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/agent/findAgentAccounts`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  maintainAgentOutlet(request: OutletData) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/outlet/maintainAgentOutlet`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  findAgentOutlet(request: OutletData) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/outlet/findAgentOutlet`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }



  maintainServices(request: TransCode) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/services/maintainServices`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  findServices(request: TransCode) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/services/findServices`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  findServiceCharge(request: ChargeData) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/service-charge/findServiceCharge`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }));
  }

  maintainServiceCharge(request: ChargeData) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/service-charge/maintainServiceCharge`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  maintainServiceChargeTiers(request: ChargeTierData) {
    console.log(request);
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/service-charge-tier/maintainServiceChargeTier`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  findServiceChargeTiers(request: ChargeTierData) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/service-charge-tier/findServiceChargeTier`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }



  findServiceCommission(request: CommissionData) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/service-commission/findServiceCommission`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  maintainServiceCommission(request: CommissionData) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/service-commission/maintainServiceCommission`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  maintainServiceCommissionTiers(request: CommissionTierData) {
    console.log(request);
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/service-commission-tier/maintainServiceCommissionTier`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  findServiceCommissionTiers(request: CommissionTier) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/service-commission-tier/findServiceCommissionTier`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }



  findPostingPolicyAccountTypes(request: ServicePostingPolicy) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/service/findPostingPolicyAccountTypes`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }
  findPostingPolicyAmountTypes(request: ServicePostingPolicy) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/service/findPostingPolicyAmountTypes`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  findServicePostingPolicy(request: ServicePostingPolicy) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/service/findServicePostingPolicy`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  deleteServicePostingDetail(request: ServicePostingPolicy) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/service/deleteServicePostingDetail`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  maintainServicePostingPolicy(request: ServicePostingPolicy) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/service/maintainServicePostingPolicy`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  maintainCustomer(request: Customer) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/customer/maintainCustomer`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  findCustomer(request: Customer) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/customer/findCustomer`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  findAccountsByEntityId(request: AccountData) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/customer/findAccountsByEntityId`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }



  maintainMobileUser(request: MobileUser) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/mobileUser/updateMobileCustomer`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  maintainAgent(request: MobileUser) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/mobileUser/maintainAgent`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  pinReset(request: MobileUser) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/mobileUser/pinReset`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  findPendingCustomers(request: MobileUser) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/mobileUser/findPendingCustomers`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  reviewMobileUser(request: MobileUser) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/mobileUser/reviewMobileUser`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }

  
  findCustomerDetails(request: MobileUser) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/mobileUser/findCustomerDetails`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  findMobileUser(request: MobileUser) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/mobileUser/findMobileUser`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  findMobileUserAccounts(request: MobileUserAcct) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/mobileUser/findMobileUserAccounts`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  saveUserAccount(request: MobileUserAcct) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/mobileUser/saveUserAccount`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  findAgents(request: MobileUser) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/mobileUser/findAgents`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  findOutlets(request: MobileUser) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/mobileUser/findOutlets`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  findTransactingAgents(request: MobileUser) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/mobileUser/findTransactingAgents`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }


  findTransactionBands(request: MobileUser) {
    return this.http.post<APIResponse>(`${environment.apiBackEndUrl}/agent-banking/mobileUser/findTransactionBands`, request)
      .pipe(map(responseStatus => {
        return responseStatus;
      }), catchError(this.handleError));
  }




}
