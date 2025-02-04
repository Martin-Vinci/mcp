using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace micropay_apis.APIModals
{
    public class OutletAuthentication
    {
		public string pinNo { get; set; }
		public string deviceId;
		public string phoneNo;
		public string channelCode;
		public string outletCode; 
		public string entityName;
		public string deviceActivationCode;
		public string newDeviceFlag;
		public string vCode; 
		public string vPassword;
		public string imeiNumber;
		public string deviceMake;
		public string deviceModel;
		public string sessionId;
	}
}