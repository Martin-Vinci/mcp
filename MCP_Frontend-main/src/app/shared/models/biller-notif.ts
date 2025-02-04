export class BillNotif {
    id: number;
    amount: number;
    isoCode: string;
    postedBy: string;
    billerCode: string;
    transDescr: string;
    status: string;
    transId: number;
    transDate: Date;
    reversalFlag: string;
    reversalReason: string;
    referenceNo: string;
    initiatorPhone: string;
    extenalTransRef: string;
    thirdPartyReference: string;
    responseData: string;
    requestData: string;
    processingDuration: number;
    channelCode: string;
}