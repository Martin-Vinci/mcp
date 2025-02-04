using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace micropay_apis.Models
{
	public class ExternalResponse
	{
		public string responseCode;
		public string responseMessage;
		public string transRefNo;

		public ExternalResponse()
		{
		}
		public ExternalResponse(string respCode, string respMsg)
		{
			responseCode = respCode;
			responseMessage = respMsg;
		}

	}



}