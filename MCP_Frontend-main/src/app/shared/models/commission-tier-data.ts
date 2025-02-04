export class CommissionTier {
    commissionTierId: number;
    commissionId: number;
    tierNo: number;
    fromAmt: number;
    toAmt: number;
    commissionAmount: number;
    createDate: Date;
    createdBy: string;
    modifyDate: Date;
    modifiedBy: string;
}

export class CommissionTierData {
    data: CommissionTier[];
    commissionId: number;
    createdBy: String;
    createDate: Date;
}