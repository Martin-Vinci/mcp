using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using micropay_apis.ABModels;

namespace micropay_apis.Models
{ 
	public class CIVoucherResponse
	{ 

        public string transId;
        public string voucherNo;
        public string transDate;
        public string expiryDate;
        public string status;
        public string sourcePhoneNo;
        public string receipientPhoneNo;
        public string transAmount;
        public string chargeAmt; 
        public string description;
        public string senderName; 
        public double drAcctBal;
        public TxnResp transItems;
        public ResponseMessage response;
    }
}