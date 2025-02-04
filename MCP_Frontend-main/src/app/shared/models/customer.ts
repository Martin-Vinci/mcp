import { ResponseStatus } from "./response-status";

export class Customer{
        id: number;
        firstName: string;
        middleName: string;
        lastName: string;
        title: string;
        status: string;
        occupation: string;
        emailAddress: string;
        latitudes: string;
        longitudes: string;
        rowVersion: number;
        birthDt: Date;
        placeOfResidence: string;
        fcsNo: string;
        maritalStatus: string;
        businessType: string;
        idType: string;
        idNo: string;
        nextOfKinName: string;
        nextOfKinAddress: string;
        gps: string;
        phoneNo: string;
        createBy: string;
        createDate: Date;
        modifiedDate: Date;
        modifiedBy: string;
        password: string;
        edit: Boolean;
}