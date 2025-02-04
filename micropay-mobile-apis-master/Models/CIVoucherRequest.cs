using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using micropay_apis.APIModals;

namespace micropay_apis.Models
{
	public class CIVoucherRequest
	{
        public double transAmt;
        public string currency;
        public string sourceAcct; 
        public string description;
        public string sourcePhoneNo;
        public string receipientPhone;
        public string voucherNo; // Optional for Voucher Buy.
        public string outletAcctNo;
        public OutletAuthentication authRequest;
    }
}