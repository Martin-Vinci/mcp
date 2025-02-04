using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace micropay_apis.Models
{
	public class ChargeDetails
	{
        public long chargeCode;
        public string description;
        public string currency;
        public string chargeType;
        public double amt;
        public string effectiveDt;
        public long transCode;
        public string status;
        public string username;
    }
}