using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using micropay_apis.APIModals;
using micropay_apis.equiweb.apis;
using micropay_apis.Models;

namespace micropay_apis.DataAccess
{
	public class eQuiWebDAO 
	{
		public RequestInput prepareBalanceInquiryInputParams(AccountRequest data, TxnData txnRequest, OutletAuthentication authRequest) {
			RequestInput requestInput = new RequestInput();
			return requestInput;
		}

		public RequestInput prepareStatementInquiryInputParams(CIStatementRequest data, TxnData txnRequest, OutletAuthentication authRequest)
		{
			RequestInput requestInput = new RequestInput();
			return requestInput;
		}

		public RequestInput prepareTransInputParams(TxnData request, TxnResp data)
		{
			RequestInput requestInput = new RequestInput();
			return requestInput;
		}
	}
}