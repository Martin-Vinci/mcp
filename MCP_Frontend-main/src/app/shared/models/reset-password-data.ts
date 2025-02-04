

import { ResponseStatus } from './response-status';

export class ResetPwdData {
    userName:string;
    userEmail:string;
    oldPwd:string;
    institutionId:number;
    emailAddress:string;
    createdBy:string;
    isAdmin:boolean;

}
export class ResetPwdResponseData {
    data: ResetPwdData;
    response: ResponseStatus;
}