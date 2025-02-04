using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using micropay_apis.APIModals;
using micropay_apis.Models;
using micropay_apis.Remote;

namespace micropay_apis.Services
{
	public class PinAuthenticationService
	{
		readonly MediumsService mediumsService = new MediumsService();
		public ServiceResponseWrapper pinAuthentication(ServiceRequestWrapper request)
		{
			ServiceResponseWrapper serviceResponseWrapper = new ServiceResponseWrapper();
			CICustomerResp customerResp = mediumsService.pinAuthentication(request.authRequest);
			serviceResponseWrapper.responseCode = customerResp.response.responseCode;
			serviceResponseWrapper.responseMessage = customerResp.response.responseMessage;

			List<Item> exportItems = new List<Item>();
			Item item = new Item
			{
				code = "CUSTOMER_NAME",
				value = customerResp.customerName
			};
			exportItems.Add(item);
			item = new Item
			{
				code = "EFFECTIVE_DATE",
				value = customerResp.effectiveDate
			};
			exportItems.Add(item);
			item = new Item
			{
				code = "MOBILE_PHONE",
				value = customerResp.phoneNo
			};
			exportItems.Add(item);
			item = new Item
			{
				code = "STATUS",
				value = customerResp.status
			};
			exportItems.Add(item);
			item = new Item
			{
				code = "ENTITY_NUMBER",
				value = customerResp.rimNo
			};

			exportItems.Add(item);
			item = new Item
			{
				code = "ENTITY_TYPE",
				value = customerResp.entityType
			};
			exportItems.Add(item);
			item = new Item
			{
				code = "DEVICE_ID",
				value = customerResp.deviceID
			};

			exportItems.Add(item);
			item = new Item
			{
				code = "OUTLET_CODE",
				value = customerResp.outletCode
			};
			exportItems.Add(item);
			item = new Item
			{
				code = "LOCKED_FLAG",
				value = customerResp.lockedFlag.ToString()
			};
			exportItems.Add(item);
			item = new Item
			{
				code = "PIN_CHANGE_FLAG",
				value = customerResp.pinChangeFlag.ToString()
			};
			exportItems.Add(item);
			item = new Item
			{
				code = "FIRST_PIN_GENERATED",
				value = customerResp.firstPinGenerated.ToString()
			};
			return serviceResponseWrapper;
		}
	}
}