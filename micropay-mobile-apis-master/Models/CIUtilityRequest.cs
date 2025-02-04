using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace micropay_apis.Models
{
	public class CIUtilityRequest
	{
        public string crAcctNo;
        public double transAmt;
        public string currency;
        public string drAcctNo;
        public string description;
        public int transType;
        public string custPhoneNo;
        public string depositorName;
        public string depositorPhoneNo;
        public string receipientPhoneNo;
        public string sourcePhoneNo;
        public Authentication authRequest;
    }
}