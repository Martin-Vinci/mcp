using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace micropay_apis.Models
{
	public class MobileTopUpRequest
	{
		public string TYPE;
		public string DATE;
		public string EXTNWCODE;
		public string PIN;
		public string LOGINID;
		public string PASSWORD; 
		public string EXTCODE;
		public string EXTREFNUM;
		public string ORIGINEXTREFNUM;
		public string TXNID;
		public string CURRENCY;
		public string COMMENT;
		public string SOURCECODE; 
		public string MSISDN2;
		public double AMOUNT;
		public string LANGUAGE1;
		public string LANGUAGE2;
		public string SELECTOR;
		public string apiUserName;
		public string apiUserPassword;
        public string billerCode;
    }
}