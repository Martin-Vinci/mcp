import { ResponseStatus } from "./response-status";

export class CreditAppl {
  id: number;
  customerName: string;
  creditType: string;
  startDate: Date;
  endDate: Date;
  repayTerm: number;
  repayPeriod: string;
  nextPmtAmt: number;
  status: string;
  custId: number;
  createdBy: string;
  createDate: Date;
  modifiedBy: string;
  modifiedDate: Date;
  rowVersion: number;
  applAmt: number;
  loanPurpose: string;
}

