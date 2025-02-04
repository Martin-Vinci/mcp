using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace micropay_apis.Models
{
	public class BillerData
	{
		public int? billerId;
		public string billerCode;
		public string description;
		public string acctNo;
		public string endpointUrl;
		public string vendorCode;
		public string vendorPassword;
		public string smsTemplate;
		public string status;
		public ResponseMessage response;
	}
}