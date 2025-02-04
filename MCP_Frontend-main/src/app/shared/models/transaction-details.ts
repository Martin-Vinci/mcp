export class TransactionDetails {
    id: number;
    mainTransId: number;
    crAcctNo: string;
    amount: number;
    isoCode: string;
    drAcctNo: string;
    transType: string;
    transDescr: string;
    postingDt: Date;
    reversalFg: string;
    reversalMessage: string;
    itemNo: number;
    createDate: Date;
    createdBy: string;
    modifyDate: Date;
    modifiedBy: string;
}