using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace micropay_apis.Models
{
	public class ResponseMessage
	{
		public string responseCode { get; set; }
		public string responseMessage { get; set; }

		public ResponseMessage()
		{
		}
		public ResponseMessage(string respCode, string respMsg) {
			responseCode = respCode;
			responseMessage = respMsg;
		}
	}
}