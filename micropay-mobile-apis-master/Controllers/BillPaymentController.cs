using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using micropay_apis.Services;
using micropay_apis.Models;
using micropay_apis.Utils;
using micropay_apis.APIModals;

namespace micropay_apis.Controllers
{ 
	public class BillPaymentController : ApiController
	{
		GateWayService gateWayService = new GateWayService();
		InterswitchService interswitchService = new InterswitchService();

		[HttpPost]
		public HttpResponseMessage getInterswitchBillerPaymentList([FromBody] BillerProduct request)
		{
			List<InterswitchPaymentItem> response = new List<InterswitchPaymentItem>();
			HttpResponseMessage message;
			try
			{ 
				LOGGER.objectInfo(request);
				response = interswitchService.findPaymentItems(request.billerProdCode);
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}


		[HttpPost]
		public HttpResponseMessage findBillerProductCategories([FromBody] CIBillValidationRequest request)
		{
			List<PickList> data = new List<PickList>(); 
			PickList item; 
			PickListResponse response = new PickListResponse();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				BillerProduct billerProduct = new BillerProduct();
				billerProduct.billerCode = request.billerCode;
				List<BillerProductCategory> billerProductCategories = gateWayService.findProductCategoryByBillerCode(billerProduct);
				if (billerProductCategories != null)
				{
					foreach (BillerProductCategory x in billerProductCategories)
					{
						item = new PickList
						{
							code = x.billerProductCategoryId.ToString(),
							description = x.description
						};
						data.Add(item);
					}
				}
				response.data = data;
				response.response = MESSAGES.getSuccessMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}


		[HttpPost]
		public HttpResponseMessage findBillerProducts([FromBody] CIBillValidationRequest request)
		{
			List<PickList> data = new List<PickList>();
			PickList item;
			PickListResponse response = new PickListResponse();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				BillerProduct billerProduct = new BillerProduct();
				billerProduct.billerCode = request.billerCode;
				billerProduct.billerProdCatId = request.categoryCode;
				billerProduct.channelSource = request.channelSource;
				List<BillerProduct> billerProducts = gateWayService.findBillerProductsByBiller(billerProduct);
				if (billerProducts != null)
				{
					foreach (BillerProduct x in billerProducts)
					{
						item = new PickList
						{
							code = x.billerProdCode,
                            id = x.billerProductId,
                            description = x.description,
							description2 = x.description2,
							amount = x.amount
						};
						data.Add(item);
					}
				}
				response.data = data;
				response.response = MESSAGES.getSuccessMessage();
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				response.response = e.getErrorMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}


		[HttpPost]
		public HttpResponseMessage validateBillPayment([FromBody] CIBillValidationRequest request)
		{
			TxnResp response = new TxnResp();
			HttpResponseMessage message;
			try
            {
				LOGGER.objectInfo(request);
				if(request.billerCode == "URA")
                    response = StubClientService.dtbService.validatePRN(request);
				else if (request.billerCode == "SUREPAY")
                    response = StubClientService.surepayService.validateReference(request);
                else
                response = interswitchService.validateCustomer(request);
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}




		[HttpPost] 
		public HttpResponseMessage processBillPayment([FromBody] TxnData request)
		{
			TxnResp response = new TxnResp();
			HttpResponseMessage message;
			try
			{
				ResponseMessage responseMessage = CONVERTER.getBillerCode(request.tranCode);
				request.billerCode = responseMessage.responseCode;
				request.description = "["+ responseMessage.responseMessage + "]=> " + request.referenceNo;
				OutletAuthentication outletAuthentication = request.authRequest;
				outletAuthentication.outletCode = request.authRequest.outletCode == null ? "SYSTEM" : request.authRequest.outletCode;
				request.authRequest = outletAuthentication;
				LOGGER.objectInfo(request);
				response = gateWayService.fundsTransfer(request);
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				response.response = e.getErrorMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}
		 
		[HttpPost]
		public HttpResponseMessage findBillerProducts2([FromBody] CIBillValidationRequest request)
		{
			List<PickList> data = new List<PickList>();
			PickList item;
			PickListResponse response = new PickListResponse();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				BillerProduct billerProduct = new BillerProduct();
				billerProduct.billerCode = request.billerCode;
				billerProduct.billerProdCatId = request.categoryCode;
                billerProduct.channelSource = request.channelSource;
                List<BillerProduct> billerProducts = gateWayService.findBillerProductsByBiller(billerProduct);
				if (billerProducts != null)
				{
					foreach (BillerProduct x in billerProducts)
					{
						item = new PickList
						{
							code = Convert.ToString(x.billerProductId),
                            id = x.billerProductId,
                            description = x.description,
							amount = x.amount
						};
						data.Add(item);
					}
				}
				response.data = data; 
				response.response = MESSAGES.getSuccessMessage();
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				response.response = e.getErrorMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}


		[HttpPost]
		public HttpResponseMessage processBillPayment2([FromBody] TxnData request)
		{
			TxnResp response = new TxnResp();
			HttpResponseMessage message;
			try
			{
				LOGGER.error("=====================**********************************************");
				BillerProduct billerProduct = gateWayService.findBillerProductsByBillerByProductId(CONVERTER.toInt(request.paymentCode));
				request.transAmt = CONVERTER.toDouble(billerProduct.amount);
				request.paymentCode = billerProduct.billerProdCode;

				ResponseMessage responseMessage = CONVERTER.getBillerCode(request.tranCode);
				request.billerCode = responseMessage.responseCode;
				request.description = "[" + responseMessage.responseMessage + "]=> " + request.referenceNo;

				request.description = "[" + request.billerCode + "]-Purchase => " + request.referenceNo;
				OutletAuthentication outletAuthentication = request.authRequest;
				outletAuthentication.outletCode = request.authRequest.outletCode == null ? "SYSTEM" : request.authRequest.outletCode;
				request.authRequest = outletAuthentication;
				LOGGER.objectInfo(request);
				response = gateWayService.fundsTransfer(request);
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				response.response = e.getErrorMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}
		 

		[HttpPost] 
		public HttpResponseMessage processCorporateAgentService([FromBody] TxnData request)
		{
			CoporateAgencyResponse response = new CoporateAgencyResponse();
			HttpResponseMessage message;
			try
			{
				OutletAuthentication outletAuthentication = request.authRequest;
				outletAuthentication.outletCode = request.authRequest.outletCode == null ? "SYSTEM" : request.authRequest.outletCode;
				request.authRequest = outletAuthentication;
				LOGGER.objectInfo(request);
				response = gateWayService.processCorporateAgentService(request);
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				response.returnCode = -99;
				response.returnMessage = e.getErrorMessage().responseMessage;
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.returnCode = -99;
				response.returnMessage = MESSAGES.getUndefinedMessage().responseMessage;
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}




		[HttpPost]
		public HttpResponseMessage lycaMobilePhoneNumberValidation([FromBody] TxnData request)
		{
			CIAccountResponse response = new CIAccountResponse();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				request.billerCode = "LYCA_DATA";
				response = StubClientService.lycaMobileService.customerResponseByphone(request);
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				response.response = e.getErrorMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);

			}
			return message;
		}


		[HttpPost]
		public HttpResponseMessage umemeYakaMeterInquiry([FromBody] CIBillValidationRequest request)
		{
			CIBillValidationResponse response = new CIBillValidationResponse();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				request.paymentCode = "UMEME";
				response = StubClientService.pegasusService.queryUMEMECustomerDetails(request);
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}


		[HttpPost]
		public HttpResponseMessage nwscReferenceInquiry([FromBody] CIBillValidationRequest request)
		{
			CIBillValidationResponse response = new CIBillValidationResponse();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				request.paymentCode = "NWSC";
				response = StubClientService.pegasusService.queryNWSCCustomerDetails(request);
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}

		[HttpPost]
		public HttpResponseMessage dstvReferenceInquiry([FromBody] CIBillValidationRequest request)
		{
			CIBillValidationResponse response = new CIBillValidationResponse();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				response.customerName = "Grace Ndyareba";
				response.charge = 2480;
				response.response = MESSAGES.getSuccessMessage();
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}

		[HttpPost]
		public HttpResponseMessage gotvReferenceInquiry([FromBody] CIBillValidationRequest request)
		{
			CIBillValidationResponse response = new CIBillValidationResponse();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				response.customerName = "Grace Ndyareba";
				response.charge = 2480;
				response.response = MESSAGES.getSuccessMessage();
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}


		[HttpPost]
		public HttpResponseMessage azamTVReferenceInquiry([FromBody] CIBillValidationRequest request)
		{
			CIBillValidationResponse response = new CIBillValidationResponse();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				response.customerName = "Grace Ndyareba";
				response.charge = 2480;
				response.response = MESSAGES.getSuccessMessage();
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}

		[HttpPost]
		public HttpResponseMessage starTimesTVReferenceInquiry([FromBody] CIBillValidationRequest request)
		{
			CIBillValidationResponse response = new CIBillValidationResponse();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				response.customerName = "Grace Ndyareba";
				response.charge = 2480;
				response.response = MESSAGES.getSuccessMessage();
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}

		[HttpPost]
		public HttpResponseMessage nwscfindCustomerAreas([FromBody] CIBillValidationRequest request)
		{
			int count = 9;
			List<PickList> data = new List<PickList>();
			PickListResponse response = new PickListResponse();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				string[] descriptions = new string[] { "Kampala", "Entebbe", "Jinja", "Iganga", "Lugazi", "Mukono", "Kajjansi", "Kawuku", "Others" };
				PickList item;
				for (int i = 0; i < count; i++)
				{
					item = new PickList
					{
						code = descriptions[i],
						description = descriptions[i]
					};
					data.Add(item);

				}
				response.data = data;
				response.response = MESSAGES.getSuccessMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}

		[HttpPost]
		public HttpResponseMessage airtelAirtimePurchase([FromBody] TxnData request)
		{
			TxnResp response = new TxnResp();
			HttpResponseMessage message;
			try
			{
				//request.tranCode = TRANCODES.AIRTEL_AIRTIME_CUSTOMER;
				request.description = request.authRequest.phoneNo + "- Airtel airtime purchase for " + request.referenceNo;
				OutletAuthentication outletAuthentication = request.authRequest;
				outletAuthentication.outletCode = request.authRequest.outletCode == null ? "SYSTEM" : request.authRequest.outletCode;
				request.authRequest = outletAuthentication;
				LOGGER.objectInfo(request);
				response = gateWayService.fundsTransfer(request);
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				response.response = e.getErrorMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}


		[HttpPost] 
		public HttpResponseMessage phoneNumberInquiry([FromBody] CIBillValidationRequest request)
		{
			TxnResp response = new TxnResp();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);

				request.paymentCode = "151155";
				LOGGER.objectInfo(request);
				response = StubClientService.interswitchService.validateCustomer(request);
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				response.response = e.getErrorMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}	 

		[HttpPost]
		public HttpResponseMessage mtnAirtimePurchase([FromBody] TxnData request)
		{
			TxnResp response = new TxnResp();
			HttpResponseMessage message;
			try
			{
				//request.tranCode = TRANCODES.MTN_AIRTIME_CUSTOMER;
				request.description = request.authRequest.phoneNo + "- MTN airtime purchase for " + request.referenceNo;
				OutletAuthentication outletAuthentication = request.authRequest;
				outletAuthentication.outletCode = request.authRequest.outletCode == null ? "SYSTEM" : request.authRequest.outletCode;
				request.authRequest = outletAuthentication;
				LOGGER.objectInfo(request);
				response = gateWayService.fundsTransfer(request);
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				response.response = e.getErrorMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}


		[HttpPost]
		public HttpResponseMessage findMTNDataCategories([FromBody] CIBillValidationRequest request)
		{
			List<PickList> data = new List<PickList>();
			PickList item;
			PickListResponse response = new PickListResponse();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				BillerProduct billerProduct = new BillerProduct();
				billerProduct.billerCode = "MTN_DATA";
				List<BillerProductCategory> billerProductCategories = gateWayService.findProductCategoryByBillerCode(billerProduct);
				if (billerProductCategories != null)
				{
					foreach (BillerProductCategory x in billerProductCategories) {
						item = new PickList
						{
							code = x.billerProductCategoryId.ToString(),
							description = x.description
						};
						data.Add(item);
					}
				} 
				response.data = data;
				response.response = MESSAGES.getSuccessMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				response.response = e.getErrorMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}


		[HttpPost]
		public HttpResponseMessage findMTNDataPackages([FromBody] CIBillValidationRequest request)
		{
			List<PickList> data = new List<PickList>();
			PickList item;
			PickListResponse response = new PickListResponse();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				BillerProduct billerProduct = new BillerProduct();
				billerProduct.billerCode = "MTN_DATA";
				billerProduct.billerProdCatId = request.categoryCode;
                billerProduct.channelSource = request.channelSource;
                List<BillerProduct> billerProducts = gateWayService.findBillerProductsByBiller(billerProduct);
				if (billerProducts != null) 
				{
					foreach (BillerProduct x in billerProducts)
					{
						item = new PickList
						{
							code = x.billerProdCode,
							description = x.description,
							amount = x.amount
						};
						data.Add(item);
					}
				}			
				response.data = data;
				response.response = MESSAGES.getSuccessMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				response.response = e.getErrorMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}

		[HttpPost]
		public HttpResponseMessage mtnDataPurchase([FromBody] TxnData request)
		{
			TxnResp response = new TxnResp();
			HttpResponseMessage message;
			try 
			{
				MTNDataBundleInquiryBundle mtnDataBundle = StubClientService.mtnServices.dataBundleInquiry(request.referenceNo, request.paymentCode);
				if (mtnDataBundle.statusCode != "0000")
					throw new MediumsException(new ResponseMessage(mtnDataBundle.statusCode, mtnDataBundle.message));


				request.transAmt = (double)mtnDataBundle.data.amount;
				//request.tranCode = TRANCODES.MTN_DATA_CUSTOMER;
				request.description = "MTN Data purchase => " + request.referenceNo;
				OutletAuthentication outletAuthentication = request.authRequest;
				outletAuthentication.outletCode = request.authRequest.outletCode == null ? "SYSTEM" : request.authRequest.outletCode;
				request.authRequest = outletAuthentication;
				LOGGER.objectInfo(request);
				response = gateWayService.fundsTransfer(request);
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				response.response = e.getErrorMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}


		[HttpPost]
		public HttpResponseMessage findAirtelDataCategories([FromBody] CIBillValidationRequest request)
		{
			List<PickList> data = new List<PickList>();
			PickList item;
			PickListResponse response = new PickListResponse();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				BillerProduct billerProduct = new BillerProduct();
				billerProduct.billerCode = "AIRTEL_DATA";
				List<BillerProductCategory> billerProductCategories = gateWayService.findProductCategoryByBillerCode(billerProduct);
				if (billerProductCategories != null)
				{
					foreach (BillerProductCategory x in billerProductCategories)
					{
						item = new PickList
						{
							code = x.billerProductCategoryId.ToString(),
							description = x.description
						};
						data.Add(item);
					}
				}
				response.data = data;
				response.response = MESSAGES.getSuccessMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}


		[HttpPost]
		public HttpResponseMessage findAirtelDataPackages([FromBody] CIBillValidationRequest request)
		{
			List<PickList> data = new List<PickList>();
			PickList item;
			PickListResponse response = new PickListResponse();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				BillerProduct billerProduct = new BillerProduct();
				billerProduct.billerCode = "AIRTEL_DATA";
				billerProduct.billerProdCatId = request.categoryCode;
                billerProduct.channelSource = request.channelSource;
                List<BillerProduct> billerProducts = gateWayService.findBillerProductsByBiller(billerProduct);
				if (billerProducts != null)
				{
					foreach (BillerProduct x in billerProducts)
					{
						item = new PickList
						{
							code = x.billerProdCode,
							description = x.description,
							amount = x.amount
						};
						data.Add(item);
					}
				}
				response.data = data;
				response.response = MESSAGES.getSuccessMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				response.response = e.getErrorMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}  


		[HttpPost]
		public HttpResponseMessage airtelDataPurchase([FromBody] TxnData request)
		{
			TxnResp response = new TxnResp();
			HttpResponseMessage message;
			try
			{
				//request.tranCode = TRANCODES.AIRTEL_DATA_CUSTOMER;
				request.description = request.authRequest.phoneNo + "- Airtel Data purchase for " + request.referenceNo;
				OutletAuthentication outletAuthentication = request.authRequest;
				outletAuthentication.outletCode = request.authRequest.outletCode == null ? "SYSTEM" : request.authRequest.outletCode;
				request.authRequest = outletAuthentication;
				LOGGER.objectInfo(request);
				response = gateWayService.fundsTransfer(request);
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				response.response = e.getErrorMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}

		[HttpPost]
		public HttpResponseMessage nwscPayment([FromBody] TxnData request)
		{
			TxnResp response = new TxnResp();
			HttpResponseMessage message;
			try
			{
				//request.tranCode = TRANCODES.NWSC_CUSTOMER;
				request.paymentCode = "NWSC";
				OutletAuthentication outletAuthentication = request.authRequest;
				request.description = request.authRequest.phoneNo + "=>Pay NWSC => " + request.referenceNo + " : " + request.customerName;
				outletAuthentication.outletCode = request.authRequest.outletCode == null ? "SYSTEM" : request.authRequest.outletCode;
				request.authRequest = outletAuthentication;
				LOGGER.objectInfo(request);
				response = gateWayService.fundsTransfer(request);
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (MediumsException ex2)
			{
				response.response = ex2.getErrorMessage();
				LOGGER.objectInfo(ex2.ToString());
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}

		[HttpPost]
		public HttpResponseMessage umemePayment([FromBody] TxnData request)
		{
			TxnResp response = new TxnResp();
			HttpResponseMessage message;
			try
			{
				//request.tranCode = TRANCODES.UMEME_COLLECTION_CUSTOMER;
				//request.referenceNo = "2121212121";
				request.paymentCode = "UMEME";
				OutletAuthentication outletAuthentication = request.authRequest;
				outletAuthentication.outletCode = request.authRequest.outletCode == null ? "SYSTEM" : request.authRequest.outletCode;
				request.description = request.authRequest.phoneNo + "=>Pay UMEME => " + request.referenceNo + " : " + request.customerName;
				request.authRequest = outletAuthentication;
				LOGGER.objectInfo(request);
				response = gateWayService.fundsTransfer(request);
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (MediumsException ex2)
			{
				response.response = ex2.getErrorMessage();
				LOGGER.objectInfo(ex2.ToString());
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.response = MESSAGES.getUndefinedMessage();				
			}
            finally
            {
                ResponseMessage response2 = response.response;
                if (response2.responseMessage.ToLower().Contains("insufficient"))
                    response2.responseMessage = "@Biller|We are unable to process your request right now. Please contact Micropay for support";
                response.response = response2;
            }
            message = Request.CreateResponse(HttpStatusCode.OK, response);
            return message;
		}


		[HttpPost]
		public HttpResponseMessage yakaPayment([FromBody] TxnData request)
		{
			TxnResp response = new TxnResp();
			HttpResponseMessage message;
			try
			{
				//request.tranCode = TRANCODES.UMEME_COLLECTION_CUSTOMER;
				OutletAuthentication outletAuthentication = request.authRequest;
				request.description = request.authRequest.phoneNo + "=>Pay Yaka => " + request.referenceNo + " : " + request.customerName;
				outletAuthentication.outletCode = request.authRequest.outletCode == null ? "SYSTEM" : request.authRequest.outletCode;
				request.authRequest = outletAuthentication;
				LOGGER.objectInfo(request);
				response = gateWayService.fundsTransfer(request);
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				response.response = e.getErrorMessage();
			}
			finally
			{
				ResponseMessage response2 = response.response;
				if (response2.responseMessage.ToLower().Contains("insufficient"))
					response2.responseMessage = "@Biller|We are unable to process your request right now. Please contact Micropay for support";
				response.response = response2;
			}
            message = Request.CreateResponse(HttpStatusCode.OK, response);
            return message;
		}

		[HttpPost]
		public HttpResponseMessage goTvPayment([FromBody] TxnData request)
		{
			TxnResp response = new TxnResp();
			HttpResponseMessage message;
			try
			{
				//request.tranCode = TRANCODES.DSTV_PAYMENT_CUSTOMER;
				OutletAuthentication outletAuthentication = request.authRequest;
				outletAuthentication.outletCode = request.authRequest.outletCode == null ? "SYSTEM" : request.authRequest.outletCode;
				request.description = request.authRequest.phoneNo + "=>Pay GoTV => " + request.referenceNo + " : " + request.customerName;
				request.authRequest = outletAuthentication;
				LOGGER.objectInfo(request);
				response = gateWayService.fundsTransfer(request);
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				response.response = e.getErrorMessage();
            }
            finally
            {
                ResponseMessage response2 = response.response;
                if (response2.responseMessage.ToLower().Contains("insufficient"))
                    response2.responseMessage = "@Biller|We are unable to process your request right now. Please contact Micropay for support";
                response.response = response2;
            }
            message = Request.CreateResponse(HttpStatusCode.OK, response);
            return message;
		}

		[HttpPost]
		public HttpResponseMessage azamTvPayment([FromBody] TxnData request)
		{
			TxnResp response = new TxnResp();
			HttpResponseMessage message;
			try
			{
				//request.tranCode = TRANCODES.DSTV_PAYMENT_CUSTOMER;
				OutletAuthentication outletAuthentication = request.authRequest;
				outletAuthentication.outletCode = request.authRequest.outletCode == null ? "SYSTEM" : request.authRequest.outletCode;
				request.description = request.authRequest.phoneNo + "=>Pay AzamTV => " + request.referenceNo + " : " + request.customerName;
				request.authRequest = outletAuthentication;
				LOGGER.objectInfo(request);
				response = gateWayService.fundsTransfer(request);
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				response.response = e.getErrorMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}

		[HttpPost]
		public HttpResponseMessage starTimesTvPayment([FromBody] TxnData request)
		{
			TxnResp response = new TxnResp();
			HttpResponseMessage message;
			try
			{
				//request.tranCode = TRANCODES.DSTV_PAYMENT_CUSTOMER;
				OutletAuthentication outletAuthentication = request.authRequest;
				outletAuthentication.outletCode = request.authRequest.outletCode == null ? "SYSTEM" : request.authRequest.outletCode;
				request.description = request.authRequest.phoneNo + "=>Pay StartTimes => " + request.referenceNo + " : " + request.customerName;
				request.authRequest = outletAuthentication;
				LOGGER.objectInfo(request);
				response = gateWayService.fundsTransfer(request);
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				response.response = e.getErrorMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}

		[HttpPost]
		public HttpResponseMessage doDStvPayment([FromBody] TxnData request)
		{
			TxnResp response = new TxnResp();
			HttpResponseMessage message;
			try
			{
				//request.tranCode = TRANCODES.DSTV_PAYMENT_CUSTOMER;
				OutletAuthentication outletAuthentication = request.authRequest;
				outletAuthentication.outletCode = request.authRequest.outletCode == null ? "SYSTEM" : request.authRequest.outletCode;
				request.description = request.authRequest.phoneNo + "=>Pay DSTv => " + request.referenceNo + " : " + request.customerName;
				request.authRequest = outletAuthentication;
				LOGGER.objectInfo(request);
				response = gateWayService.fundsTransfer(request);
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				response.response = e.getErrorMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}


		[HttpPost] 
		public HttpResponseMessage crdbPostTransaction([FromBody] TransactionItem request)
		{
			TxnResp response = new TxnResp();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				request.amount = Math.Round(request.amount);
				response = gateWayService.crdbPostTransaction(request);
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (MediumsException e)
			{ 
				LOGGER.error(e.ToString());
				ResponseMessage responseMessage = e.getErrorMessage();
				response.response = responseMessage;
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				ResponseMessage responseMessage = new ResponseMessage();
				responseMessage.responseCode = "-99";
				responseMessage.responseMessage = ex.Message;
				response.response = responseMessage;
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}

	}
}
