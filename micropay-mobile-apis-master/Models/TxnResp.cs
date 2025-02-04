using System.Collections.Generic;
using micropay_apis.ABModels;

namespace micropay_apis.Models
{
    public class TxnResp
	{ 
		public string transId;
		public double? drAcctBal;
		public double? crAcctBal;
		public double chargeAmt { get; set; }
		public double totalAmount { set; get; }
        public string bankRef;
		public string utilityRef;
		public string requestId;
		public string noOfUnits;
		public string serviceFee;
		public string studentClass;
		public string payAccount;
		public string debtRecovery;
		public string customerName;
        public string schoolName;
        public string receiptNo;
		public string forexAdjustment;
		public string fuelAdjustment;
		public string inflationAdjustment;
		public string purchaseBreak;
		public string vat; 
		public string tokenValue;
		public string printData { get; set; }
		public List<ChildTrans> transItems;
		public ResponseMessage response { get; set; }

		public TxnResp() { 
		
		}

        public TxnResp(ResponseMessage response)
        {
			this.response = response;
        }

    }
}