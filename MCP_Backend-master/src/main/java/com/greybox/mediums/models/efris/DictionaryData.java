package com.greybox.mediums.models.efris;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryData {
    public ArrayList<CreditNoteMaxInvoicingDays> creditNoteMaximumInvoicingDays;
    public ArrayList<CurrencyType> currencyType;
    public ArrayList<CreditNoteValuePercentLimit> creditNoteValuePercentLimit;
    public ArrayList<RateUnit> rateUnit;
    public Format format;
    public ArrayList<Sector> sector;
    public ArrayList<PayWay> payWay;
    public ArrayList<CountryCode> countryCode;
}
