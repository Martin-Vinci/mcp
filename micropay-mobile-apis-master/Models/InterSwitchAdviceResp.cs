using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace micropay_apis.Models
{
	public class InterSwitchAdviceResp
	{
		public string responseMessage { get; set; }
		public string responseCode { get; set; }
		public string rechargePIN { get; set; }
		public string requestReference { get; set; }
		public string transferCode { get; set; }
		public string transactionRef { get; set; }
	}
}