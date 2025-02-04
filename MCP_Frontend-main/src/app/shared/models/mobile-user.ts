export class MobileUser {
    id: number;
    phoneNumber: string;
    pin: string;
    customerName: string;
    dateCreated: Date;
    lockedFlag: Boolean;
    acctType: string;
    failedLoginCount: number;
    pinChangeFlag: Boolean;
    authImsi: string;
    authImei: string;
    activationCode: string;
    useAndroidChannel: Boolean;
    useUssdChannel: Boolean;
    wapOtp: string;
    wapOtpExpiry: Date;
    approvalStatus: Boolean;
    createdBy: number;
    approvedBy: string;
    dateApproved: Date;
    birthDate: Date;
    physicalAddress: string;
    postalAddress: string;
    gender: string;
    entityCode: string;
    outletCode: string;
    reviewAction: string;
    accountsCategory: string;
    lastTransDate: Date;
    activeDays: number;
    rsmId: number;
    startDate: Date;
    endDate: Date; 
}



export class MobileUserRPT {
    phoneNumber: string;
    customerName: string;
    dateCreated: Date;
    lockedFlag: Boolean;
    acctType: string;
    failedLoginCount: number;
    pinChangeFlag: Boolean;
    createdBy: number;
    birthDate: Date;
    physicalAddress: string;
    postalAddress: string;
    gender: string;
    outletCode: string;
    lastTransDate: Date;
    activeDays: number;
    startDate: Date;
    endDate: Date; 
}