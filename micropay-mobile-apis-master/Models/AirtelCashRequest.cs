using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace micropay_apis.Models
{
    public class AirtelCashRequest
    {
		public string destAcctNo;
		public string transAmt;
		public string additionalCharge;
		public string currency;
		public string sourceAcctNo;
		public string description;
		public int serviceCode;
		public string externalReference;
		public string depositorPhoneNo;
        public string depositorName;

        public string apiUserName { get; set; }
        public string apiPassword { get; set; }
        public string deviceId { get; set; }
        public string imeiNumber { get; set; }
        public string deviceMake { get; set; }
        public string deviceModel { get; set; }
        public string userPhoneNo { get; set; }
        public string outletCode { get; set; }
        public string pinNo { get; set; }
        public string channelCode { get; set; }



        public InterswitchPaymentRequest paymentRequest;
    }
}
