using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace micropay_apis.Models
{
	public class CICustomerResp
	{
    	public string customerName;
		public string effectiveDate;
		public string phoneNo;
		public string status;
		public string rimNo;
		public string entityType; // Customer, Outlet
		public string deviceID;
		public string outletCode;
		public bool lockedFlag;
		public bool pinChangeFlag;
		public bool firstPinGenerated;
		public List<Account> accountList;
		public ResponseMessage response;
	}
}