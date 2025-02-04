import { ResponseStatus } from "./response-status";

export class AuthenticationResponseData {
    fullName: string;
    passwordExpiryDate: string;
    tellerNo: string;
    branchName: string;
    status: string;
    userRoleDescr: string;
    processDate: string;
    lastLoginDate: string;
    username: string;
    passwordChanged: string;
    passwordChangedFlag: boolean;
    passwordExpiryFlag: string;
    supervisor: string;
    licenseDays: string;
    sessionPtid: string;
    userRoleId: string;
    failedLogins: string;
    loginLimits: string;
    employeeID: string;
    branchId: string;
    failedLoginLimit: string;
    token: string;
    responseStatus: ResponseStatus;
}