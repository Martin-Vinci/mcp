using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace micropay_apis.Models
{
	public class MobileTopUpResponse
	{
		public string TYPE;
		public string TXNSTATUS;
		public string DATE;
		public string EXTREFNUM;
		public string TXNID;
		public string REQSTATUS;
		public string MESSAGE;
		public ResponseMessage responseStatus;
	}
}