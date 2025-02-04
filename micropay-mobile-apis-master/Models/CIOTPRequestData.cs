using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using micropay_apis.APIModals;

namespace micropay_apis.Models
{
	public class CIOTPRequestData
	{ 
		public string acctNo;
		public string customerPhone; 
		public double amount; 
		public string otpCode; 
		public string outletName;
		public string withdrawOutletCode; 
		public OutletAuthentication authRequest;
	}
}