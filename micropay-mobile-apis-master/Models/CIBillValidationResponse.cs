using System;

namespace micropay_apis.Models
{
    public class CIBillValidationResponse
	{
		public string customerName;
		public Double charge;
		public string customerRef;
		public Double outStandingBal;
		public string area; 
		public ResponseMessage response;
	}
}