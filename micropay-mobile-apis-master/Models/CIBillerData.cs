using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace micropay_apis.Models
{
	public class CIBillerData
	{
		public int? id;
		public double? amount;
		public string isoCode;
		public string postedBy;
		public string billerCode;
		public string transDescr;
		public string status;
		public int? transId;
		public string reversalFlag;
		public string reversalReason;
		public string referenceNo;
		public string initiatorPhone;
		public string extenalTransRef;
		public string thirdPartyReference;
		public string responseData;
		public string requestData;
		public int? processingDuration;
		public string channelCode;
		public ResponseMessage response;
	}
}