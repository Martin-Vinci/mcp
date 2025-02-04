﻿using System;

namespace micropay_apis.Utils
{
    public class SMS_TEMPLATE
    {
        public static string CUSTOMER_CASHIN_SUCCESS = "{CustomerName}, You have received" + Environment.NewLine + "UGX:{Amount}" + Environment.NewLine + "From:{OutletCode}-{OutletName}" + Environment.NewLine + "Account: {DrAcctNo}" + Environment.NewLine + "Trans ID: {TransId}" + Environment.NewLine + "Reason: UGX {Reason}" + Environment.NewLine + "Micropay Bal: UGX {CrAcctBal}" + Environment.NewLine + "Charge: UGX {Charge}" + Environment.NewLine + "Date: {TransDate}";
        public static string OUTLET_CASHIN_SUCCESS = "{CustomerName}, You have sent" + Environment.NewLine + "UGX:{Amount}" + Environment.NewLine + "To:{CustomerPhone}-{CustomerName}" + Environment.NewLine + "Account: {DrAcctNo}" + Environment.NewLine + "Trans ID: {TransId}" + Environment.NewLine + "Reason: UGX {Reason}" + Environment.NewLine + "Micropay Bal: UGX {CrAcctBal}" + Environment.NewLine + "Charge: UGX {Charge}" + Environment.NewLine + "Date: {TransDate}";
        public static string CUSTOMER_CASHOUT_SUCCESS = "{CustomerName}, You have withdrawn" + Environment.NewLine + "UGX:{Amount}" + Environment.NewLine + "Outlet:{DrOutletCode}-{DrOutletName}" + Environment.NewLine + "Account: {DrAcctNo}" + Environment.NewLine + "Trans ID: {TransId}" + Environment.NewLine + "Micropay Bal: UGX {DrAcctBal}" + Environment.NewLine + "Charge: UGX {Charge}Date: {TransDate}";
        public static string OUTLET_CASHOUT_SUCCESS = "{DrOutletName}, You have deposited" + Environment.NewLine + "UGX:{Amount}" + Environment.NewLine + "Outlet:{DrOutletCode}-{DrOutletName}" + Environment.NewLine + "Account: {DrAcctNo}" + Environment.NewLine + "Trans ID: {TransId}" + Environment.NewLine + "Micropay Bal: UGX {CrAcctBal}" + Environment.NewLine + "Charge: UGX {Charge}" + Environment.NewLine + "Date: {TransDate}";
        public static string CUSTOMER_CASHOUT_INITIATION_SUCCESS = "{CustomerName}, You have inititated a withdraw of" + Environment.NewLine + "UGX:{Amount}" + Environment.NewLine + "Outlet:{OutletCode}-{OutletName}" + Environment.NewLine + "Withdraw code: {WithdrawCode}" + Environment.NewLine + "Expires: {ExpiryDate}";
        public static string CUSTOMER_UMEME_SUCCESS = "Dear Customer, payment of {Amount} for A/C {ReferenceNo} received by UMEME. Token no. {TokenNo}, Units. {NoOfUnits}. Thank you for paying.";
        public static string CUSTOMER_NWSC_SUCCESS = "NWSC payment to " + Environment.NewLine + "A/C:{ReferenceNo} completed" + Environment.NewLine + "UGX:{Amount}" + Environment.NewLine + "Charge: UGX {Charge}" + Environment.NewLine + "Total: UGX {TotalAmount}" + Environment.NewLine + "Biller Ref: {BillerRef}" + Environment.NewLine + "Bank Ref: {TransId}";
        public static string CR_OUTLET_TO_SUPERAGENT_SUCCESS = "{CustomerName}, You have withdrawn" + Environment.NewLine + "UGX:{Amount}" + Environment.NewLine + "Outlet:{DrOutletCode}-{DrOutletName}" + Environment.NewLine + "Account: {DrAcctNo}" + Environment.NewLine + "Trans ID: {TransId}" + Environment.NewLine + "Micropay Bal: UGX {DrAcctBal}" + Environment.NewLine + "Charge: UGX {Charge}" + Environment.NewLine + "Date: {TransDate}";
        public static string DR_OUTLET_TO_SUPERAGENT_SUCCESS = "{CustomerName}, You have withdrawn" + Environment.NewLine + "UGX:{Amount}" + Environment.NewLine + "Outlet:{DrOutletCode}-{DrOutletName}" + Environment.NewLine + "Account: {DrAcctNo}" + Environment.NewLine + "Trans ID: {TransId}" + Environment.NewLine + "Micropay Bal: UGX {DrAcctBal}" + Environment.NewLine + "Charge: UGX {Charge}" + Environment.NewLine + "Date: {TransDate}";
        public static string MTN_CASHIN_CUSTOMER = "{CustomerName}, You have processed MTN MoMo deposit of " + Environment.NewLine + "UGX:{Amount} to {ReferenceNo}" + Environment.NewLine + "Micropay Bal: UGX {DrAcctBal}" + Environment.NewLine + "Trans ID: {TransId}" + Environment.NewLine + "Charge: UGX {Charge}" + Environment.NewLine + "Date: {TransDate}";
        public static string MTN_CASHIN_AGENT = "You have processed MTN MoMo Deposit of UGX:{Amount} from {ReferenceNo}-{CustomerName} Outlet:{DrOutletCode} Account: {DrAcctNo} Trans ID:{TransId} Micropay Bal:{DrAcctBal} UGX Charge: UGX {Charge} Date: {TransDate}";
        public static string MTN_CASHOUT_CUSTOMER = "{CustomerName}, You have processed MTN MoMo withdraw of " + Environment.NewLine + "UGX:{Amount} from {ReferenceNo}" + Environment.NewLine + "Trans ID: {TransId}" + Environment.NewLine + "Charge: UGX {Charge}" + Environment.NewLine + "Date: {TransDate}";
        public static string MTN_CASHOUT_AGENT = "You have processed MTN MoMo withdraw of " + Environment.NewLine + "UGX:{Amount} from {ReferenceNo}-{CustomerName}" + Environment.NewLine + "Outlet:{DrOutletCode}-{DrOutletName}" + Environment.NewLine + "Account: {DrAcctNo}" + Environment.NewLine + "Trans ID: {TransId}" + Environment.NewLine + "Micropay Bal: UGX {CrAcctBal}" + Environment.NewLine + "Charge: UGX {Charge}" + Environment.NewLine + "Date: {TransDate}";
        public static string AIRTEL_CASHIN_CUSTOMER = "{CustomerName}, You have processed Airtel Money deposit of " + Environment.NewLine + "UGX:{Amount} to {ReferenceNo}" + Environment.NewLine + "Trans ID: {TransId}" + Environment.NewLine + "Micropay Bal: UGX {DrAcctBal}" + Environment.NewLine + "Charge: UGX {Charge}" + Environment.NewLine + "Date: {TransDate}";
        public static string AIRTEL_CASHIN_AGENT = "You have processed Airtel Money Deposit of UGX:{Amount} from {ReferenceNo}-{CustomerName} Outlet:{DrOutletCode} Account: {DrAcctNo} Trans ID:{TransId} Micropay Bal:{DrAcctBal} UGX Charge: UGX {Charge} Date: {TransDate}";
        public static string AIRTEL_CASHOUT_AGENT = "{CustomerName}, You have withdrawn" + Environment.NewLine + "UGX:{Amount}" + Environment.NewLine + "Outlet:{DrOutletCode}-{DrOutletName}" + Environment.NewLine + "Account: {DrAcctNo}" + Environment.NewLine + "Trans ID: {TransId}" + Environment.NewLine + "Micropay Bal: UGX {DrAcctBal}" + Environment.NewLine + "Charge: UGX {Charge}" + Environment.NewLine + "Date: {TransDate}";
        public static string TUGENDE_CUSTOMER = "Tugende Loan Payment recieved for {CustomerName}-A/C {ReferenceNo}.Thank you for paying. UGX:{Amount}. Trans ID: {TransId}. Micropay Bal: UGX {DrAcctBal}, Charge: UGX {Charge} Date: {TransDate}";
        public static string TUGENDE_AGENT = "{CustomerName}, Tugende Payment received" + Environment.NewLine + "UGX:{Amount}" + Environment.NewLine + "Outlet:{DrOutletCode}-{DrOutletName}" + Environment.NewLine + "Account: {DrAcctNo}" + Environment.NewLine + "Trans ID: {TransId}" + Environment.NewLine + "Micropay Bal: UGX {DrAcctBal}" + Environment.NewLine + "Charge: UGX {Charge}" + Environment.NewLine + "Date: {TransDate}";
        public static string WENRECO_CUSTOMER = "{CustomerName}, WENRECO Payment recieved for A/C {ReferenceNo}. Token no. {TokenNo}, Units. {NoOfUnits}. Thank you for paying." + Environment.NewLine + "UGX:{Amount}" + Environment.NewLine + "Trans ID: {TransId}" + Environment.NewLine + "Micropay Bal: UGX {DrAcctBal}" + Environment.NewLine + "Charge: UGX {Charge}" + Environment.NewLine + "Date: {TransDate}";
        public static string WENRECO_AGENT = "{CustomerName}, WENRECO Payment recieved for A/C {ReferenceNo}. Token no. {TokenNo}, Units. {NoOfUnits}. Thank you for paying." + Environment.NewLine + "UGX:{Amount}" + Environment.NewLine + "Outlet:{DrOutletCode}-{DrOutletName}" + Environment.NewLine + "Account: {DrAcctNo}" + Environment.NewLine + "Trans ID: {TransId}" + Environment.NewLine + "Micropay Bal: UGX {DrAcctBal}" + Environment.NewLine + "Charge: UGX {Charge}" + Environment.NewLine + "Date: {TransDate}";

        public static string AIRTEL_DATA_CUSTOMER = "{CustomerName}, You have processed Airtel Money deposit of " + Environment.NewLine + "UGX:{Amount} to {ReferenceNo}" + Environment.NewLine + "Trans ID: {TransId}" + Environment.NewLine + "Micropay Bal: UGX {DrAcctBal}" + Environment.NewLine + "Charge: UGX {Charge}" + Environment.NewLine + "Date: {TransDate}";
        public static string AIRTEL_DATA_AGENT = "You have processed Airtel Money Deposit of UGX:{Amount} from {ReferenceNo}-{CustomerName} Outlet:{DrOutletCode} Account: {DrAcctNo} Trans ID:{TransId} Micropay Bal:{DrAcctBal} UGX Charge: UGX {Charge} Date: {TransDate}";




    }
}