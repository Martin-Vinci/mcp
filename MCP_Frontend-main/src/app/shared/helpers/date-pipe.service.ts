import { Injectable } from "@angular/core";
import { addDays, addMonths, addWeeks, addYears, differenceInDays, getDay, getDaysInMonth, getMonth, getYear, parse } from "date-fns";
import { DatePipe } from "@angular/common";


@Injectable({
  providedIn: "root",
})
export class DatePipeService {
  constructor(public datepipe: DatePipe) { }

  isoDateString(dateString: any): any {
    let returnDate: String = "";
    if (dateString == null) {
      return null;
    }
    if (dateString == "") {
      return null;
    }
    // if (dateString.length > 10) returnDate = dateString.substring(0, 10);
    // else {
    //   returnDate = dateString;
    // }
    returnDate = this.datepipe.transform(dateString, "yyyy-MM-dd");

    if (returnDate.includes("-")) {
      if (returnDate.length > 10)
        returnDate = returnDate.substring(0, 10);
      return returnDate;
    }
    let dateValue = parse(returnDate.toString());
    return this.datepipe.transform(dateValue, "yyyy-MM-dd");
  }


  firstDayOfMonth(currentDate: Date): any {
    let dateValue = this.isoDateString(currentDate).toString();
    let dateDay: number = parseInt(this.datepipe.transform(dateValue, "dd"));
    let dateMonth = this.datepipe.transform(dateValue, "MM");
    let dateYear = this.datepipe.transform(dateValue, "yyyy");
    let firstDay = dateYear + '-' + dateMonth + '-01';
    return firstDay;
  }

  dateDiffInDays(startDate: any, endDate: any): number {
    let diff: number;
    diff = differenceInDays(parse(startDate), parse(endDate))
    return diff;
  }

  dateDiffPeriod(startDate: any, endDate: any): string {
    let diff: number;
    let balance: number;
    let years: number;
    let months: number;
    let days: number;
    let output: string;
    diff = differenceInDays(parse(startDate), parse(endDate))
    if (diff < 365)
      years = 0;
    else
      years = Math.trunc(diff / 365);


    balance = diff % 365;
    if (balance < 30)
      months = 0;
    else
      months = Math.trunc(balance / 30);


    if (balance < 1)
      days = 0;
    else
      days = balance % 30;


    output = years + " Years ," + months + " Months ," + days + " Days"
    return output;
  }


  getNextPeriodDate(contractDate: Date, period: String, term: number): String {
    if (contractDate == null || period == null || term == null) {
      alert("Invalid contract date or term period");
      return null;
    }
    let finalDate: Date;
    let maturityDate: String;
    let startDate: string = this.datepipe.transform(contractDate, 'yyyy-MM-dd');

    if (period == "MONTH") {
      contractDate = addMonths(parse(startDate), term);
      finalDate = addDays(parse(contractDate.toISOString()), 1);//add plus 1 to fix date add bug for days
      maturityDate = this.isoDateString(finalDate.toISOString());
    } else if (period == "DAYS") {
      finalDate = addDays(parse(startDate), Number(term) + 1);//add plus 1 to fix date add bug for days
      maturityDate = this.isoDateString(finalDate.toISOString());

    } else if (period == "YEAR") {
      contractDate = addYears(parse(startDate), term);
      finalDate = addDays(parse(contractDate.toISOString()), 1);//add plus 1 to fix date add bug for days
      maturityDate = this.isoDateString(finalDate.toISOString());
    } else if (period == "WEEK") {
      contractDate = addWeeks(parse(startDate), term);
      finalDate = addDays(parse(contractDate.toISOString()), 1);//add plus 1 to fix date add bug for days
      maturityDate = this.isoDateString(finalDate.toISOString());
    }
    return maturityDate;
  }


  getNxtChargeDate(currentDate: string, period: String, term: number): String {
    if (currentDate == null || period == null || term == null) {
      alert("Invalid Next Charge date or term period");
      return null;
    }
    let newDate: Date;
    let nextChargeDate: String;
    let startDate: string = currentDate;
    if (period == "MONTH") {
      newDate = addMonths(parse(startDate), term);
      nextChargeDate = this.isoDateString(newDate.toISOString());
    } else if (period == "DAYS") {
      newDate = addDays(parse(startDate), term);
      nextChargeDate = this.isoDateString(newDate.toISOString());
    } else if (period == "YEAR") {
      newDate = addYears(parse(startDate), term);
      nextChargeDate = this.isoDateString(newDate.toISOString());
    } else if (period == "WEEK") {
      newDate = addWeeks(parse(startDate), term);
      nextChargeDate = this.isoDateString(newDate.toISOString());
    }
    return nextChargeDate;
  }
}
