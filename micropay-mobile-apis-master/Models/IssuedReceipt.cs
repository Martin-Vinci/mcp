using micropay_apis.APIModals;
using System;
using System.Collections.Generic;

namespace micropay_apis.Models
{
    public class IssuedReceipt
    {
        public int? receiptId;
        public string receiptNumber { set; get; }
        public string dateCreated { set; get; }
        public string issuer { set; get; } 
        public string phoneNo { set; get; }
        public string receiptData { set; get; }
        public string txnId { set; get; }
        public string partDate { set; get; }

        public DateTime? fromDate { set; get; }
        public DateTime? toDate { set; get; }

        public OutletAuthentication authRequest { set; get; }
    }
     
    public class IssuedReceiptData
    {
        public List<IssuedReceipt> data;
        public ResponseMessage response;
    }
}