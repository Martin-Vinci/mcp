import { ResponseStatus } from "./response-status";

export class AuthenticationRequestData {
    id: string;
    userName: string;
    password: string;
    ipAddress: string;
    token: string;
    institutionId: number;
    employeeId: number;
    loginAction: string;
    sessionPtid: number;
}

export class AuthenticationResponseData {
    employeeId: number;
    userName: string;
    fullName: string;
    userRoleId: number;
    emailAddress: string;
    phoneNo: string;
    receiveBillerStmnt: string;
    lockUser: string;
    pwdEnhancedFlag: string;
    userPwd: string;
    status: string;
    processDate: any;
    securityRoleDesc: string;
    token: string;
    licenseDays: number;
}
