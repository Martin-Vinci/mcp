using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace micropay_apis.Models
{
	public class CreditApplRequest
	{
		public string acctNo;
		public string classCode;
		public int purposeId;
		public int rsmId;
		public string customerName;
		public string applDate;
		public string status;
		public double creditAmount;
		public int creditTerm;
		public string creditPeriod;
		public string creditRemarks;
		public string collateralName;
		public string addressLine1;
		public string addressLine2;
		public string collateralType;
		public int collateralCategoryId;
		public string collateralState;
		public double marketValue;
		public int rimNo;
		public int currencyId;
		public Authentication authRequest;
	}

	public class CreditRequestData
	{
		public CreditApplRequest[] data;
		public ResponseMessage response;
	}
} 