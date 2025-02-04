using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace micropay_apis.Models
{
	public class CISMSRequest
	{
		public string phoneNo;
		public string messageText;
		public int institutionId;
		public Authentication authRequest;
	}
}