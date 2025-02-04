using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
 
namespace micropay_apis.Models
{
	public class ReversalRequest
	{
		public long originalTranRef; 
		public string reason;
		public bool isReverseFromBank;
	}
}