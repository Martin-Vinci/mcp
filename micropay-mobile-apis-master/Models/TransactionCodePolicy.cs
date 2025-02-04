using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
 
namespace micropay_apis.Models
{
	public class TransactionCodePolicy
	{
		public long transCode;
		public string transDescr;
		public string sourceAcctPolicy;
		public string destinationAcctPolicy; 
		public string amountType;
		public string transCategory;
		public long postingPriority; 
		public double transAmtBankShare;
		public double transAmtVendorShare;
	}
}