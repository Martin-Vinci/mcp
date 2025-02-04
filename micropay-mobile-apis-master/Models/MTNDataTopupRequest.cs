using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace micropay_apis.Models
{ 
	public class MTNDataTopupRequest
	{
		public string txnRef;
		public bool retry;
		public string subscriptionId;
		public string currencyCode;
		public string sourceCode;
        public string billerCode;
        public string subscriptionName;
		public string phoneNumber;
		public string apiUserName;
		public string apiUserPassword;
	}
}