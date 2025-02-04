using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using micropay_apis.APIModals;

namespace micropay_apis.Models
{
	public class AccountRequest 
	{
		public string accountNo;
        public string entityType;
        public OutletAuthentication authRequest;
	}
}