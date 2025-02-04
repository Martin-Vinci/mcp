export class ServicePostingPolicy {
    postingDetailId: number;
    serviceId: number;
    postingPriority: number;
    sourceAccount: string;
    destinationAccount: string;
    amountType: string;
    trustPostingCategory: string;
    transCategory: string;
    createdBy: string;
    createDt: Date;
    tranAmtAgentShare: number;
    tranAmtVendorShare: number;
    tranAmtBankShare: number;
    edit: Boolean;
}

export class PostingPolicyItem {
    id: number;
    itemType: string;
}