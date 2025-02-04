using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace micropay_apis.Models
{
	 
	public class MTNDataBundleInquiryBundle
	{
		public string statusCode { get; set; }
		public string message { get; set; }
		public string customerId { get; set; }
		public BundleData data { get; set; }
	}

	public class BundleData 
	{
		public string id { get; set; }
		public string name { get; set; }
		public string currency { get; set; }
		public double? amount { get; set; }
		public object bundleCategory { get; set; }
		public string bundleType { get; set; }
		public string bundleValidity { get; set; }
	}
}