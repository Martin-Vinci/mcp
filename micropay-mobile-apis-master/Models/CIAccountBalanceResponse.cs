using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace micropay_apis.Models
{
	public class CIAccountBalanceResponse
	{
		public string accountTitle;
		public string branchName;
		public string availableBalance;
		public string currency;
		public string accountStatus;
		public string customerNo;
		public ResponseMessage response;
	}
}