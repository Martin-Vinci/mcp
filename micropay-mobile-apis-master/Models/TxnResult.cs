using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace micropay_apis.Models
{
	public class TxnResult
	{
		public string code;
		public string message;
		public string data;
	}
	 
	public class MediumTransResp
	{
		public string code { get; set; }
		public string message { get; set; }
		public Data data { get; set; }
	}

	public class Data
	{
		public long? transId { get; set; }
	}
	 
	public class MediumCustomerResp
	{
		public string code { get; set; }
		public string message { get; set; }
		public CICustomerResp data { get; set; }
	}














}