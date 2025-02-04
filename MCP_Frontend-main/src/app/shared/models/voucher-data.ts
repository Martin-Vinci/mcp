import { ResponseStatus } from "./response-status";

export class VoucherData {
    id: number;
    amount: number;
    voucherCode: string;
    sourcePhoneNo: string;
    recipientPhoneNo: string;
    narration: string;
    buyTransId: number;
    redeemTransId: number;
    expiryDate: Date;
    createDate: Date;
    status: string;
}

