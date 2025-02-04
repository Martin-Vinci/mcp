using System;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using micropay_apis.Services;
using micropay_apis.Models;
using micropay_apis.Utils;

namespace micropay_apis.Controllers
{
	public class TransactionController : ApiController
    {
		GateWayService gateWayService = new GateWayService();


		[HttpPost] 
		public HttpResponseMessage cashIn([FromBody] TxnData request)
		{
			TxnResp response = new TxnResp();
			HttpResponseMessage message;
			try
			{
				AccountRequest accountRequest = new AccountRequest();
				accountRequest.accountNo = request.crAcctNo;
				accountRequest.authRequest = request.authRequest;
				CIAccountBalanceResponse accountBalanceResponse = gateWayService.accountBalanceInquiry(accountRequest);
				if (accountBalanceResponse.response.responseCode != "0")
				{
					response.response = accountBalanceResponse.response;
					message = Request.CreateResponse(HttpStatusCode.OK, response);
					return message;
				}

				double totalBalanceAfterTransaction = CONVERTER.toDouble(accountBalanceResponse.availableBalance) + request.transAmt;
				double permissibleBalance = 15000000;
				if (totalBalanceAfterTransaction >= permissibleBalance)
				{
					response.response = new ResponseMessage("-99", "Account balance will exceed the maximum permitted amount [" + permissibleBalance + "] on a customer's account.");
					message = Request.CreateResponse(HttpStatusCode.OK, response);
					return message;
				}

				request.tranCode = TRANCODES.CASH_DEPOSIT;
				request.description = "Cash Deposit " + request.authRequest.outletCode + "=>" + request.customerPhoneNo;
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
		public HttpResponseMessage cashOut([FromBody] TxnData request)
		{
			TxnResp response = new TxnResp();
			HttpResponseMessage message;
			try
			{
				request.tranCode = TRANCODES.CASH_WITHDRAW;
				request.description = "Cash Out " + request.customerPhoneNo + "[" + request.customerName + "]-" + request.drAcctNo;
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
		public HttpResponseMessage outletToOutlet([FromBody] TxnData request)
		{
			TxnResp response = new TxnResp();
			HttpResponseMessage message;
			try
			{
				request.tranCode = TRANCODES.OUTLET_TO_OUTLET;
				request.description = "O2O Transfer DR: [" + request.drAcctNo + "] to CR: [" + request.crAcctNo;
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
		public HttpResponseMessage p2p([FromBody] TxnData request)
		{
			TxnResp response = new TxnResp();
			HttpResponseMessage message;
			try 
			{
				request.tranCode = TRANCODES.FUNDS_TRANSFER;
				request.description = "P2P Transfer " + request.customerPhoneNo + " => " + request.authRequest.phoneNo;
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
		public HttpResponseMessage outletCashIn([FromBody] TxnData request)
		{
			TxnResp response = new TxnResp();
			HttpResponseMessage message;
			try
			{
				request.tranCode = TRANCODES.SUPER_AGENT_TO_OUTLET;
				request.description = "Outlet Cashin SA Code: " + request.authRequest.outletCode + " => " + request.crAcctNo;
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
		public HttpResponseMessage outletCashOut([FromBody] TxnData request)
		{
			TxnResp response = new TxnResp();
			HttpResponseMessage message;
			try
			{
				request.tranCode = TRANCODES.OUTLET_TO_SUPER_AGENT;
				request.description = "Outlet Cashout: Code: " + request.authRequest.outletCode + " => " + request.crAcctNo + "[" + request.customerName + "]";
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
		public HttpResponseMessage superAgentToSuperAgent([FromBody] TxnData request)
		{
			TxnResp response = new TxnResp();
			HttpResponseMessage message;
			try
			{
				request.tranCode = TRANCODES.SUPER_AGENT_TRANSFER;
				request.description = "S2S Source Code: " + request.drAcctNo + " => " + request.crAcctNo + "[" + request.customerName + "]";
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
		public HttpResponseMessage reverseTrans([FromBody] CIReversalRequest request)
		{
			ResponseMessage response = new ResponseMessage();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				response = gateWayService.reverseTrans(request);
				LOGGER.objectInfo(response);
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
		public HttpResponseMessage sendPaymentRequest([FromBody] RequestPayment request)
		{
			ResponseMessage response = new ResponseMessage();
			HttpResponseMessage message;
			try
			{
				LOGGER.objectInfo(request);
				response = gateWayService.sendPaymentRequest(request);
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
        public HttpResponseMessage reviewPaymentRequest([FromBody] RequestPayment request)
        {
			TxnResp response = new TxnResp();
            HttpResponseMessage message;
            try
            {
                LOGGER.objectInfo(request);
                response = gateWayService.reviewPaymentRequest(request);
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
        public HttpResponseMessage findPendingRequest([FromBody] RequestPayment request)
        {
            RequestPaymentData response = new RequestPaymentData();
            HttpResponseMessage message;
            try 
            {
                LOGGER.objectInfo(request);
                response = gateWayService.findPendingRequest(request);
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


    }
}
