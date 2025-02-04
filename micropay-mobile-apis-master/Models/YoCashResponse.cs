using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace micropay_apis.Models
{
	public class YoCashResponse
	{
		public string transStatus;
		public string transRef;
		public string mnoTransRef;
		public ResponseMessage response;
	}
}