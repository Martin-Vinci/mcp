import { TransactionDetails } from "./transaction-details";

export class TransactionRef {
    id: number;
    crAcctNo: string;
    drAcctNo: string;
    amount: number;
    isoCode: string;
    postedBy: string;
    serviceCode: number;
    transDescr: string;
    successFlag: string;
    transDate: Date;
    utilPosted: string;
    reversalFlag: string;
    reversalReason: string;
    externalTransRef: string;
    depositorPhone: string;
    depositorName: string;
    agentCommission: number;
    bankCommission: number;
    totalCharge: number;
    exciseDuty: number;
    withholdTax: number;
    systemDate: Date;
    externalBankCode: string;
    externalAcct: string;
    initiatorPhoneNo: string;
    startDate: Date;
    endDate: Date;
}

export class TransData {
    mainTrans: TransactionRef;
    childTrans: TransactionDetails[];
}

export class RPTTransaction {
    id: number;
    crAcctNo: string;
    drAcctNo: string;
    amount: number;
    serviceCode: number;
    transDescr: string;
    systemDate: Date;
    successFlag: string;
    agentCommission: number;
    bankCommission: number;
    totalCharge: number;
    exciseDuty: number;
    withholdTax: number;
}


export class TransactionBand {
    transAmount: number;
    transCount: number;
    description: string;
    serviceCode: number;
    startDate: Date;
    endDate: Date;
}