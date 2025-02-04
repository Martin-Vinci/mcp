using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using micropay_apis.APIModals;

namespace micropay_apis.Models
{
	public class CIStatementRequest
	{
		public string accountNo;
		public string statementType;
        public string entityType;
        public DateTime? fromDate;
		public DateTime? toDate;
		public string transType; 
		public OutletAuthentication authRequest;
	}
}