using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using micropay_apis.APIModals;

namespace micropay_apis.Models
{ 
	public class TxnData
    { 
        public string crAcctNo; 
        public double transAmt; 
        public double outstandingAmount;
        public double surCharge;
        public double tax;
        public string currency; 
        public string drAcctNo;
        public string description;
        public int tranCode;
        public long? originTransId;
        public int serviceCode;
        public string tranStatus;
        public string referenceNo {  get; set; }
        public string packageName;
        public string externalTransId;
        public string customerArea;
        public string customerType; 
        public string customerName; 
        public string bankName;
        public string bankCode;
        public string withdrawCode; 
        public string depositorName;
        public string schoolName;
        public string className;
        public string depositorPhoneNo;
        public string billerTransRef; 
        public string idType;
        public string idValue;
        public string requestId;
        public string billerCode { set; get; }
        public string paymentCode { get; set; } 
        public string customerPhoneNo { get; set; }
        public OutletAuthentication authRequest;
    }
}