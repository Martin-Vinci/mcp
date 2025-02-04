using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace micropay_apis.Models
{ 
	public class CIStatement
	{
		public string transDate;
		public string effectiveDate;
		public string reference;
		public decimal? credit;
		public decimal? debit;
		public decimal? txnAmount;
		public decimal? closing;
		public string description;
	}
	public class CIStatementResponse
	{
		public ResponseMessage response;
		public List<CIStatement> data; 
	}
}