using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using micropay_apis.APIModals;
using micropay_apis.equiweb.apis;

namespace micropay_apis.Models
{
	public class CIChargeRequest
	{
		public int transCode;
		public double amount; 
		public string accountNo;
		public OutletAuthentication  authRequest;
	}
}