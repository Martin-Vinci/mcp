using System;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using micropay_apis.Services;
using micropay_apis.Models;
using micropay_apis.Utils;
using micropay_apis.APIModals;

namespace micropay_apis.Controllers
{
	public class EQuiWebController : ApiController
	{
		GateWayService gateWayService = new GateWayService();

		//[HttpPost]
		//public HttpResponseMessage signUp([FromBody] SignUpRequest authRequest)
		//{
		//	SignUpResponse response = new SignUpResponse();
		//	HttpResponseMessage message;
		//	try
		//	{
		//		LOGGER.objectInfo(authRequest);
		//		response = gateWayService.signUp(authRequest);
		//		LOGGER.objectInfo(response);
		//		message = Request.CreateResponse(HttpStatusCode.OK, response);
		//	}
		//	catch (Exception ex)
		//	{
		//		LOGGER.error(ex.ToString());
		//		response.response = MESSAGES.getUndefinedMessage();
		//		message = Request.CreateResponse(HttpStatusCode.OK, response);
		//	}
		//	return message;
		//}

		[HttpPost]
		public HttpResponseMessage signIn([FromBody] OutletAuthentication authRequest)
		{
			CICustomerResp response = new CICustomerResp();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(authRequest);
				response = gateWayService.pinAuthentication(authRequest);
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
		public HttpResponseMessage createCustomer([FromBody] SignUpRequest request)
		{
			SignUpResponse response = new SignUpResponse();
			HttpResponseMessage message;
			try
			{				
				response = gateWayService.customerEnrollment(request);
				LOGGER.objectInfo(request);
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (MediumsException e)
			{
				LOGGER.objectInfo(request);
				LOGGER.error(e.ToString());
				response.response = e.getErrorMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.objectInfo(request);
				LOGGER.error(ex.ToString());
				response.response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}

		//[HttpPost]
		//public HttpResponseMessage creditAppl([FromBody] CreditApplRequest creditRequest)
		//{
		//	CreditApplResponse response = new CreditApplResponse();
		//	HttpResponseMessage message;
		//	try
		//	{
		//		LOGGER.objectInfo(creditRequest);
		//		response = gateWayService.creditAppl(creditRequest);
		//		LOGGER.objectInfo(response);
		//		message = Request.CreateResponse(HttpStatusCode.OK, response);
		//	}
		//	catch (Exception ex)
		//	{
		//		LOGGER.error(ex.ToString());
		//		response.response = MESSAGES.getUndefinedMessage();
		//		message = Request.CreateResponse(HttpStatusCode.OK, response);

		//	}
		//	return message;
		//}


		[HttpPost]
		public HttpResponseMessage accountResponseByPhoneNo([FromBody] OutletAuthentication request)
		{
			CICustomerResp response = new CICustomerResp();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				response = gateWayService.accountResponseByPhoneNo(request);
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
		public HttpResponseMessage accountResponseByAccountNo([FromBody] AccountRequest request)
		{
			CIAccountResponse response = new CIAccountResponse();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				response = gateWayService.accountResponseByAccountNo(request);
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
		public HttpResponseMessage performDevicePairing([FromBody] OutletAuthentication request)
		{
			HttpResponseMessage message;
			ResponseMessage response;
			try
			{
				LOGGER.objectInfo(request);
				response = gateWayService.performDevicePairing(request);
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				response = e.getErrorMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);

			}
			return message;
		}

		 
		[HttpPost]
		public HttpResponseMessage generateDeviceActivationCode([FromBody] OutletAuthentication request)
		{
			HttpResponseMessage message;
			ResponseMessage response;
			try
			{
				LOGGER.objectInfo(request);
				response = gateWayService.generateDeviceActivationCode(request);
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				response = e.getErrorMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);

			}
			return message;
		}





		[HttpPost]
		public HttpResponseMessage findAccountBalance([FromBody] AccountRequest request)
		{
			CIAccountBalanceResponse response = new CIAccountBalanceResponse();
			HttpResponseMessage message;
			gateWayService = new GateWayService();
			try
			{
				LOGGER.objectInfo(request);
				response = gateWayService.accountBalanceInquiry(request);
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
		public HttpResponseMessage findTransactionCharge([FromBody] CIChargeRequest request)
		{
			CIChargeResponse response = new CIChargeResponse();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				response = gateWayService.findTransactionCharge(request);
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


		 
		public HttpResponseMessage findOutletDetails([FromBody] OutletAuthentication request)
		{
			CIOutletResponse response = new CIOutletResponse();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				response = gateWayService.findOutletDetails(request);
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
		public HttpResponseMessage findSuperAgentDetails([FromBody] OutletAuthentication request)
		{
			CIOutletResponse response = new CIOutletResponse();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				response = gateWayService.findSuperAgentDetails(request);
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
		public HttpResponseMessage changePIN([FromBody] CIPINChangeRequest request)
		{
			ResponseMessage response = new ResponseMessage();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				response = gateWayService.changePIN(request);
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				message = Request.CreateResponse(HttpStatusCode.OK, e.getErrorMessage());
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);

			}
			return message;
		}


		[HttpPost]
		public HttpResponseMessage voucherPurchase([FromBody] CIVoucherRequest request)
		{
			CIVoucherResponse response = new CIVoucherResponse();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				response = gateWayService.voucherPurchase(request);
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
		public HttpResponseMessage findVoucherDetails([FromBody] CIVoucherRequest request)
		{
			CIVoucherResponse response = new CIVoucherResponse();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				response = gateWayService.findVoucherDetails(request);
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
		public HttpResponseMessage voucherRedeem([FromBody] CIVoucherRequest request)
		{
			TxnResp response = new TxnResp();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				response = gateWayService.voucherRedeem(request);
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
		public HttpResponseMessage initiateCashWithdraw([FromBody] CIOTPRequestData request)
		{
			CIOTPResponse response = new CIOTPResponse();
			HttpResponseMessage message;
			try
			{ 
				LOGGER.objectInfo(request);
				response = gateWayService.initiateCashWithdraw(request);
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
		public HttpResponseMessage withdrawCodeInquiry([FromBody] CIOTPRequestData request)
		{
			CIOTPResponse response = new CIOTPResponse();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				response = gateWayService.withdrawCodeInquiry(request);
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
		public HttpResponseMessage outletWithdrawCodeInquiry([FromBody] CIOTPRequestData request)
		{
			CIOTPResponse response = new CIOTPResponse();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				response = gateWayService.outletWithdrawCodeInquiry(request);
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
		public HttpResponseMessage initiateOutletCashWithdraw([FromBody] CIOTPRequestData request)
		{
			CIOTPResponse response = new CIOTPResponse();
			HttpResponseMessage message;
			try 
			{
				LOGGER.objectInfo(request);
				response = gateWayService.initiateOutletCashWithdraw(request);
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
		public HttpResponseMessage pairCustomerDevice([FromBody] OutletAuthentication request)
		{
			ResponseMessage response = new ResponseMessage();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				response = gateWayService.pairCustomerDevice(request);
				LOGGER.objectInfo(response);
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				message = Request.CreateResponse(HttpStatusCode.OK, e.getErrorMessage());
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}


		//[HttpPost]
		//public HttpResponseMessage creditRepayment([FromBody] CreditRepaymentRequest request)
		//{
		//	TxnResp response = new TxnResp();
		//	HttpResponseMessage message;
		//	try
		//	{
		//		LOGGER.objectInfo(request);
		//		response = gateWayService.creditRepayment(request);
		//		LOGGER.objectInfo(response);
		//		message = Request.CreateResponse(HttpStatusCode.OK, response);
		//	}
		//	catch (Exception ex)
		//	{
		//		LOGGER.error(ex.ToString());
		//		response.response = MESSAGES.getUndefinedMessage();
		//		message = Request.CreateResponse(HttpStatusCode.OK, response);
		//	}
		//	return message;
		//}

		//[HttpPost]
		//public HttpResponseMessage findCreditApplication([FromBody] CreditApplRequest request)
		//{
		//	CreditRequestData response = new CreditRequestData();
		//	HttpResponseMessage message;
		//	try
		//	{
		//		LOGGER.objectInfo(request);
		//		response = gateWayService.findCreditApplication(request);
		//		LOGGER.objectInfo(response);
		//		message = Request.CreateResponse(HttpStatusCode.OK, response);
		//	}
		//	catch (Exception ex)
		//	{
		//		LOGGER.error(ex.ToString());
		//		response.response = MESSAGES.getUndefinedMessage();
		//		message = Request.CreateResponse(HttpStatusCode.OK, response);
		//	}
		//	return message;
		//}


		[HttpPost]
		public HttpResponseMessage findAccountStatement([FromBody] CIStatementRequest request)
		{
			CIStatementResponse response = new CIStatementResponse();
			HttpResponseMessage message;
			try
			{ 
				LOGGER.objectInfo(request);
				response = gateWayService.doAccountFullStatement(request);
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
		public HttpResponseMessage findMessages([FromBody] OutletAuthentication request)
		{
			MessageRespData response = new MessageRespData();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				response = gateWayService.findMessages(request);
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
		public HttpResponseMessage findIssuedReceipt([FromBody] IssuedReceipt request)
		{
			IssuedReceiptData response = new IssuedReceiptData();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				response = gateWayService.findIssuedReceipt(request);
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
		public HttpResponseMessage logApplicationError([FromBody] ExceptionLog request)
		{
			ResponseMessage response = new ResponseMessage();
			HttpResponseMessage message;
			try
			{
				LOGGER.appError(CONVERTER.objectToXMLString(request));
				message = Request.CreateResponse(HttpStatusCode.OK, MESSAGES.getSuccessMessage());
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response = MESSAGES.getUndefinedMessage();
				message = Request.CreateResponse(HttpStatusCode.OK, response);
			}
			return message;
		}








	}
}
