using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace micropay_apis.Models
{
	public class CIMessageResp
	{
		public string smsText;
		public string createDate;
	}
	public class MessageRespData 
	{
		public List<CIMessageResp> data;
		public ResponseMessage response;
	}











}