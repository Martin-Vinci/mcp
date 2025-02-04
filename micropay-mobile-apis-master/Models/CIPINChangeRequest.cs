using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using micropay_apis.APIModals;

namespace micropay_apis.Models
{
	public class CIPINChangeRequest
	{
		public string oldPin;
		public string newPin;
		public string confirmPin;
		public OutletAuthentication authRequest { get; set; }
	}
} 