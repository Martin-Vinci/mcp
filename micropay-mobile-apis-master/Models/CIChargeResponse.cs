using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace micropay_apis.Models
{
	public class CIChargeResponse
	{
		public double? charge;
		public double? commission;
		public double? wht;
		public double? exciseDuty;
		public double? totalCharge;
		public ResponseMessage response;
	}
}