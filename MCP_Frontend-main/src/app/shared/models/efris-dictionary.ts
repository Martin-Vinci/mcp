export class CountryCode {
    public value: String;
    public name: String;
}
export class CreditNoteMaximumInvoicingDays {
    public value: String;
    public name: String;
}
export class CreditNoteValuePercentLimit {
    public value: String;
    public name: String;
}
export class CurrencyType {
    public value: String;
    public name: String;
}
export class Format {
    public dateFormat: String;
    public timeFormat: String;
}
export class PayWay {
    public value: String;
    public name: String;
}
export class RateUnit {
    public value: String;
    public name: String;
}
export class DictionaryData {
    public creditNoteMaximumInvoicingDays: CreditNoteMaximumInvoicingDays;
    public currencyType: CurrencyType[];
    public creditNoteValuePercentLimit: CreditNoteValuePercentLimit;
    public rateUnit: RateUnit[];
    public format: Format;
    public sector: Sector[];
    public payWay: PayWay[];
    public countryCode: CountryCode[];
}

export class Sector {
    public code: String;
    public name: String;
    public parentClass: String;
    public requiredFill: String;
}


