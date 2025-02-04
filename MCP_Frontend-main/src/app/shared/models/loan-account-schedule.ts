export class LoanSchedule {
    id: number;
    scheduleNo: number;
    loanId: number;
    principalAmount: number;
    interestAmount: number;
    principalPaid: number;
    interestPaid: number;
    principalUnpaid: number;
    interestUnpaid: number;
    dueDate: Date;
    paymentDate: Date;
    status: string;
    createdBy: string;
    createDate: Date;
    modifiedBy: string;
    modifiedDate: Date;
    totalAmount: number;
    amountUnPaid: number;
    amountPaid: number;
}

