using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
 
namespace micropay_apis.Models
{
	public class EntityAmount
	{
		public double commission;
		public double withHoldTax; 
		public double exciseDuty;
		public double totalCharge;
		public double netCharge;
		public double vendorShare;
		public double bankShare;
		public ResponseMessage response;
	}
}