using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace micropay_apis.Models
{
	public class MTNTopupResponse
	{
		public string resultcode;
		public string resultdescription;
		public string value;
		public string taxvalue;
		public string timestamp;
		public string account;
		public string transno;
		public string expirydate;
		public string agentid;
		public string agenttransno;
		public string product_description;
		public string topupvalue;
		public string accountvalue;
		public string walletbalance;
		public ResponseMessage responseStatus;
	}
}