using System;
using System.Web.Http;
using micropay_apis.APIModals;
using micropay_apis.Models;
using micropay_apis.Services;
using micropay_apis.Utils;

namespace micropay_apis.Controllers
{

	[RoutePrefix("api/V2")]
	public class V2Controller : ApiController
	{
		GateWayService gateWayService = new GateWayService();


		[HttpPost]
		[BasicAuthentication]
		[Route("validateAgent")]
		public ExtValidationResponseData validateAgent([FromBody] ExtValidationRequestData request)
		{
			ExtValidationResponseData response = new ExtValidationResponseData();
			try
			{
				LOGGER.objectInfo(request);

				OutletAuthentication outletAuthentication = new OutletAuthentication();
				outletAuthentication.outletCode = request.agentCode;

				CIOutletResponse outletResponse = gateWayService.findSuperAgentDetails(outletAuthentication);
				if (outletResponse.response.responseCode != "0")
				{
					response.returnCode = outletResponse.response.responseCode;
					response.returnMessage = outletResponse.response.responseMessage;
					return response;
				}

				response.agentAcctNo = outletResponse.outletAccount;
				response.agentCode = request.agentCode;
				response.agentName = outletResponse.outletName;
				response.mobilePhone = outletResponse.outletPhone;
				response.returnCode = "200";
				response.returnMessage = "Transaction Approved";
				LOGGER.objectInfo(response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				response.returnCode = "-99";
				response.returnMessage = e.getErrorMessage().responseMessage;
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.returnCode = "-99";
				response.returnMessage = ex.Message;

			}
			return response;
		}


		[HttpPost]
		[BasicAuthentication]
		[Route("postFinancial")]
		public ExtFinancialResponseData postFinancial([FromBody] ExtFinancialRequestData request)
		{
			ExtFinancialResponseData response = new ExtFinancialResponseData();
			try
			{
				LOGGER.objectInfo(request);
				response = gateWayService.submitEscrowTransaction(request);
				if (response.returnCode == "0")
				{
					response.returnCode = "200";
					response.returnMessage = "Transaction Approved";
					response.transactionReference = response.transactionReference.ToString().PadLeft(12, '0');
				}
				LOGGER.objectInfo(response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				response.returnCode = "-99";
				response.returnMessage = e.getErrorMessage().responseMessage;
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.returnCode = "-99";
				response.returnMessage = ex.Message;
			}
			return response;
		}




		[HttpPost]
		[BasicAuthentication]
		[Route("fundsTransfer")]
		public ResponseMessage fundsTransfer([FromBody] ExtFinancialRequestData request)
		{ 
			ResponseMessage response = new ResponseMessage();
			try
			{
				LOGGER.objectInfo(request);
				response = gateWayService.fundsTransfer(request);
				LOGGER.objectInfo(response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				response = e.getErrorMessage();
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.responseCode = "-99";
				response.responseMessage = ex.Message;
			}
			return response;
		}


		[HttpPost]
		[BasicAuthentication]
		[Route("mtnAirtimeTopUp")]
		public MTNTopupResponse mtnAirtimeTopUp([FromBody] MTNTopupRequest request)
		{
			MTNTopupResponse response = new MTNTopupResponse();
			try
			{
				LOGGER.objectInfo(request);
				if (request.retry)
					response = gateWayService.mtnAirtimeCheckStatus(request);
				else
					response = gateWayService.mtnAirtimeRecharge(request);

				response.walletbalance = null;
				if (response.responseStatus.responseCode == "0")
				{					
					response.responseStatus = new ResponseMessage("200", "Transaction Approved");
				}
				LOGGER.objectInfo(response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				response.responseStatus = new ResponseMessage("-99", e.getErrorMessage().responseMessage);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.responseStatus = new ResponseMessage("-99", "A system error has occured");
			}
			return response;
		}


		[HttpPost]
		[BasicAuthentication]
		[Route("mtnDataTopUp")]
		public MTNDataTopupResponse mtnDataTopUp([FromBody] MTNDataTopupRequest request)
		{
			MTNDataTopupResponse response = new MTNDataTopupResponse();
			try
			{
				LOGGER.objectInfo(request);
				if (request.retry)
					response = gateWayService.mtnDataCheckStatus(request);
				else
					response = gateWayService.mtnDataTopup(request);

				if (response.responseStatus.responseCode == "0")
				{
					response.responseStatus = new ResponseMessage("200", "Transaction Approved");
				}
				LOGGER.objectInfo(response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				response.responseStatus = new ResponseMessage("-99", e.getErrorMessage().responseMessage);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.responseStatus = new ResponseMessage("-99", "A system error has occured");
			}
			return response;
		}


		[HttpPost]
		[BasicAuthentication]
		[Route("airtelAirtimeTopUp")]
		public MobileTopUpResponse airtelAirtimeTopUp([FromBody] MobileTopUpRequest request)
		{
			MobileTopUpResponse response = new MobileTopUpResponse();
			try 
			{
				LOGGER.objectInfo(request);

				if (request.ORIGINEXTREFNUM == null)
					response = gateWayService.airtelAirtimeRecharge(request);
				else
					response = gateWayService.airtelAirtimeCheckStatus(request);

				
				if (response.responseStatus.responseCode == "0")
				{
					response.MESSAGE = "Success";
					response.responseStatus = new ResponseMessage("200", "Transaction Approved");
				}
				LOGGER.objectInfo(response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				response.responseStatus = new ResponseMessage("-99", e.getErrorMessage().responseMessage);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.responseStatus = new ResponseMessage("-99", "A system error has occured");
			}
			return response;
		}

		[HttpPost]
		[BasicAuthentication]
		[Route("airtelDataPurchase")]
		public MobileTopUpResponse airtelDataPurchase([FromBody] MobileTopUpRequest request)
		{
			MobileTopUpResponse response = new MobileTopUpResponse();
			try
			{
				LOGGER.objectInfo(request);
				if (request.ORIGINEXTREFNUM == null)
					response = gateWayService.airtelDataRecharge(request);
				else
					response = gateWayService.checkAirtelDataStatus(request);


				if (response.responseStatus.responseCode == "0")
				{
					response.MESSAGE = "Success";
					response.responseStatus = new ResponseMessage("200", "Transaction Approved");
				}
				LOGGER.objectInfo(response);
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				response.responseStatus = new ResponseMessage("-99", e.getErrorMessage().responseMessage);
			}
			catch (Exception ex)
			{
				LOGGER.error(ex.ToString());
				response.responseStatus = new ResponseMessage("-99", "A system error has occured");
			}
			return response;
		}
	}
}
