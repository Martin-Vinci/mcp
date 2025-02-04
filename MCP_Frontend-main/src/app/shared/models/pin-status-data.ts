import { ResponseStatus } from "./response-status";

export class PINStatus{
    maxTrialTimes: string;
    pinLocked: string;
    phoneNo: string;
    entityName: string;
    entityType: string;
    createdBy: string;
    institutionId:number;
} 
export class PINStatusData {
    data: PINStatus[];
    response: ResponseStatus;
}