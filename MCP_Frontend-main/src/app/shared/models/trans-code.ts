import { ResponseStatus } from './response-status';

export class TransCode {
    serviceId: number;
    serviceCode: number;
    description: string;
    transType: string;
    billerAcctNo: string;
    bankIncomeAcctNo: string;
    transitAcctNo: string;
    expenseAcctNo: string;
    debitCredit: string;
    status: string;
    minTransAmt: number;
    maxTransAmt: number;
    dailyWithdrawLimit: number;
    transLiteral: string;
    billerCode: string;
    mobileMoneyTaxPercentage: number;
    maintainCommissionPercentage: number;
    maintenanceCalculationBasis: string;
    maintenanceAccount: string;
    createDate: Date;
    createdBy: string;
    modifyDate: Date;
    modifiedBy: string;
    edit: Boolean;
}