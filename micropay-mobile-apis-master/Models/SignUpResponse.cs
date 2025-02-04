using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace micropay_apis.Models
{
	public class SignUpResponse
	{
		public int? customerCode;
		public string accountNo;
		public string acctType;
		public string branchName;
		public ResponseMessage response;
	}
}