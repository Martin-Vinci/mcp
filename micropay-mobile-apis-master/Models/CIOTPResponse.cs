using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace micropay_apis.Models
{
	public class CIOTPResponse
	{
		public string withdrawCode;
		public string createDate;
		public string expiryDate;
		public string accountNo;
		public string phoneNo;
		public string customerName;
		public double? transAmount;
		public string codeStatus;
		public ResponseMessage response;
	}
}