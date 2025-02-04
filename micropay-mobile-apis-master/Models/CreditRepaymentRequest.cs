using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace micropay_apis.Models
{
	public class CreditRepaymentRequest
    {
        public DateTime effectiveDt;
        public int emplID;
        public string acctNo;
        public string acctType;
        public string tfrAcctNo;
        public string tfrAcctType;
        public int origBranchNo;
        public int amtCrncyID;
        public int? chargeCode;
        public double? chargeCodeAmt;
        public double tranAmt;
        public string chargeDescr;
        public string tranDescr;
        public Authentication authRequest;
    }
}