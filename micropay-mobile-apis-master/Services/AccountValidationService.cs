using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using micropay_apis.APIModals;
using micropay_apis.Models;
using micropay_apis.Remote;
using micropay_apis.Utils;

namespace micropay_apis.Services
{
	public class AccountValidationService
	{
		EQuiWebService eQuiWebService;
		MediumsService mediumsService;
		public string accountResponseByPhoneNo(ServiceRequestWrapper serviceRequestWrapper)
		{
			eQuiWebService = new EQuiWebService();
			mediumsService = new MediumsService();
			SignInRequest signRequest = new SignInRequest();
			signRequest.phoneNo = "256778503221";
			CICustomerResp customer = eQuiWebService.accountResponseByPhoneNo(signRequest);
			if (customer.response.responseCode != "0")
				throw new MediumsException(customer.response);
			customer.phoneNo = signRequest.phoneNo;
			mediumsService.customerEnrollment(customer);
			return null;
		}
		 
		public string signUp()
		{
			eQuiWebService = new EQuiWebService();
			mediumsService = new MediumsService();
			SignInRequest signRequest = new SignInRequest();
			signRequest.phoneNo = "256778503221";
			CICustomerResp customer = eQuiWebService.accountResponseByPhoneNo(signRequest);
			if (customer.response.responseCode != "0")
				throw new MediumsException(customer.response);
			customer.phoneNo = signRequest.phoneNo;
			mediumsService.customerEnrollment(customer);
			return null;
		}

		private ParameterData getParameters(ServiceRequestWrapper serviceRequestWrapper) {
			ParameterData parameterData = new ParameterData();
			foreach (Item item in serviceRequestWrapper.requestBody.requestInput.inputItems.items)
            {
                if (item.code == "MOBILE_PHONE")
                {
					parameterData.MOBILE_PHONE = item.value;
                }
            }
			return parameterData;
		}
	}
}