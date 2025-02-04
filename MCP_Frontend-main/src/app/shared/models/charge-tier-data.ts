export class ChargeTier {
    chargeTierId: number;
    chargeId: number;
    tierNo: number;
    fromAmt: number;
    toAmt: number;
    chargeAmt: number;
    createDate: Date;
    createdBy: string;
    modifyDate: Date;
    modifiedBy: string;
}

export class ChargeTierData {
    data: ChargeTier[];
    chargeId: number;
    createdBy: String;
    createDate: Date;
}