export class Biller {
    id: number;
    billerCode: number;
    description: number;
    acctNo: number;
    vendorCode: number;
    vendorPassword: number;
    smsTemplate: number;
    status: number;
    createDate: Date;
    createdBy: string;
    modifyDate: Date;
    modifiedBy: string;
    edit: Boolean;
}

export class ServiceChannel {    
    channelId: number;
    channelCode: string;
    description: string;
    channelUsername: string;
    channelPassword: string;
    enforcePwdExpiry: string;
    expiryDate: Date;
    status: string;
    createDt: Date;
    createdBy: string;
    modifiedDate: Date;
    modifiedBy: string;
}