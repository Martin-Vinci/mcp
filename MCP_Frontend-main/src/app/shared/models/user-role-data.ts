import { ResponseStatus } from './response-status';

export class UserRoleData {
    emplClassCode: number;
    effectiveDt: string;
    status: string;
    emplId: number;
    createDt: string;
    restrictId: number;
    isUniquePwd: string;
    minpwdLength: number;
    forcePwdChange: string;
    numberOfGraceLogins: number;
    setExpiryDt: string;
    employeeType: string;
    showLastLogon: string;
    numberOfdaysPwdChange: number;
    description: string;
    allowMultiLogon: string;
    failedLoginLimit: string;
    channelId: string;
    forcePost: string;
    allowBrnchChanging: string;
    allowRpc: string;
    remTranCancel: string;
    allowLoginDuringEoD: string;
    institutionId: number;
    isEdit: boolean;

}

export class UserRoleResponseData {
    data: UserRoleData[];
    response: ResponseStatus;
 }