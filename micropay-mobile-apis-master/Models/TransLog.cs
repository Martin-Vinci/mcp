using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace micropay_apis.Models
{
	public class TransLog
	{
		public string biller_code;
		public string mobile_phone;
		public string channel_code;
		public string request_data;
		public string response_data;
		public DateTime request_date;
		public long processing_duration;
	}
}