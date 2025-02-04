using System;
using System.Collections.Generic;
using System.Security.Cryptography;
using System.Text;
using micropay_apis.ABModels;
using micropay_apis.APIModals;
using micropay_apis.equiweb.apis;
using micropay_apis.Models;
using micropay_apis.Services;
using micropay_apis.Utils;

namespace micropay_apis.Remote
{
	public class eQuiWebService
	{
		private AuthenticationWrapper getAuthourizeRequest(string userName)
		{
			AuthenticationWrapper resp = new AuthenticationWrapper();
			string channelRequestTimestamp = DateTime.Now.ToString("yyyyMMddHHmmss");
			resp.channelCode = "AGENT_BANKING";
			resp.institutionId = PROPERTIES.INSTITUTION_ID;
			resp.vCode = PROPERTIES.VENDOR_NAME;
			resp.vPassword = PROPERTIES.VENDOR_PASSWORD;
			resp.userName = PROPERTIES.EQUIWEB_USER;
			return resp;
		}

		private double generateRequestId(string userName)
		{
			ServiceRequestWrapper request = new ServiceRequestWrapper();
			ServiceRequestBody requestWrapper = new ServiceRequestBody();
			RequestInput dataWrapper = new RequestInput();
			requestWrapper.serviceCode = "GET_REQUEST_ID";
			requestWrapper.requestInput = dataWrapper;
			request.authRequest = getAuthourizeRequest(userName);
			request.requestBody = requestWrapper;
			ServiceResponseWrapper response = StubClientService.equiwebService.processServiceRequest(request);

			if (response.responseCode != "0")
			{
				LOGGER.objectInfo(request);
				LOGGER.objectInfo(response);
				throw new MediumsException(getRespMessage(response));
			}

			RequestOutput requestOutput = response.requestOutput;
			if (requestOutput.outputParameters == null)
				return 0;

			OutputItems outputItems = requestOutput.outputParameters;
			foreach (Item item in outputItems.exportItems)
			{
				if (item.code.ToUpper().Equals("REQUEST_ID"))
					return double.Parse(item.value);
			}
			return 0;
		}

		private ResponseMessage getRespMessage(ServiceResponseWrapper data)
		{
			ResponseMessage resp = new ResponseMessage();
			resp.responseCode = data.responseCode.ToString();
			if (resp.responseCode != "0")
			{
				resp.responseMessage = "ERROR(" + resp.responseCode + "): " + data.responseMessage + " at eQuiWeb";
				resp.responseMessage = resp.responseMessage.ToUpper();
			}
			else
			{
				resp.responseMessage = "success";
			}
			return resp;
		}


		private TxnResp getTranResp(RequestOutput requestOutput)
		{
			TxnResp resp = new TxnResp();
			//int rest = 1;
			//resp.drAcctBal = 12221;
			//resp.crAcctBal = 232323;
			//resp.transId = "001232232";
			//if (1 == rest)
			//	return resp;


			if (requestOutput.outputParameters == null)
				return resp;

			foreach (Item item in requestOutput.outputParameters.exportItems)
			{
				if (item.code.ToUpper().Equals("DEBIT_ACCOUNT_BALANCE"))
				{
					resp.drAcctBal = CONVERTER.toDouble(item.value.ToString());
				}
				if (item.code.ToUpper().Equals("CREDIT_ACCOUNT_BALANCE"))
				{
					resp.crAcctBal = CONVERTER.toDouble(item.value.ToString());
				}
				if (item.code.ToUpper().Equals("TRANSACTION_ID"))
				{
					resp.transId = item.value;
				}
			}
			return resp;
		}
		private CIAccountBalanceResponse getAccountBalanceResp(RequestOutput requestOutput)
		{
			CIAccountBalanceResponse resp = new CIAccountBalanceResponse();
			if (requestOutput.outputParameters == null)
				return resp;

			foreach (Item item in requestOutput.outputParameters.exportItems)
			{
				if (item.code.ToUpper().Equals("ACCOUNT_TITLE"))
				{
					resp.accountTitle = item.value;
					continue;
				}
				if (item.code.ToUpper().Equals("BRANCH_NAME"))
				{
					resp.branchName = item.value;
					continue;
				}
				if (item.code.ToUpper().Equals("AVAILABLE_BALANCE"))
				{
					resp.availableBalance = item.value;
					continue;
				}
				if (item.code.ToUpper().Equals("ACCOUNT_CURRENCY"))
				{
					resp.currency = item.value;
					continue;
				}

				if (item.code.ToUpper().Equals("ACCOUNT_STATUS"))
				{
					resp.accountStatus = item.value;
					continue;
				}
				if (item.code.ToUpper().Equals("CUSTOMER_NUMBER"))
				{
					resp.customerNo = item.value;
					continue;
				}
			}
			return resp;
		}
		private SignUpResponse getCustomerResp(RequestOutput requestOutput)
		{
			SignUpResponse resp = new SignUpResponse();
			if (requestOutput.outputParameters == null)
				return resp;

			foreach (Item item in requestOutput.outputParameters.exportItems)
			{
				if (item.code.ToUpper().Equals("RIM_NO"))
				{
					resp.customerCode = CONVERTER.toInt(item.value);
					continue;
				}
				if (item.code.ToUpper().Equals("ACCOUNT_NUMBER"))
				{
					resp.accountNo = item.value;
					continue;
				}
			}
			return resp;
		}


		private CIAccountResponse getAccountValidationResp(string accountNo, RequestOutput requestOutput)
		{
			CIAccountResponse resp = new CIAccountResponse();
			if (requestOutput.outputParameters == null)
				return resp;

			resp.accountNo = accountNo;
			foreach (Item item in requestOutput.outputParameters.exportItems)
			{
				if (item.code.ToUpper().Equals("CUSTOMER_NAME"))
				{
					resp.acctTitle = item.value;
					continue;
				}
				if (item.code.ToUpper().Equals("PHONE_NUMBER"))
				{
					resp.phoneNo = item.value;
					continue;
				}
				if (item.code.ToUpper().Equals("ENTITY_TYPE"))
				{
					resp.entityType = item.value;
					continue;
				}
			}

			if (requestOutput.outputParameterItems == null)
				return resp;

			foreach (OutputItems outputItems in requestOutput.outputParameterItems)
			{
				foreach (Item item in outputItems.exportItems)
				{
					if (item.code.ToUpper().Equals("ACCOUNT_NUMBER"))
					{
						resp.accountNo = item.value;
						continue;
					}
				}
			}
			return resp;
		}
		private CICustomerResp getPhoneValidationResp(RequestOutput requestOutput)
		{
			CICustomerResp resp = new CICustomerResp();
			if (requestOutput.outputParameters == null)
				return resp;

			foreach (Item item in requestOutput.outputParameters.exportItems)
			{
				if (item.code.ToUpper().Equals("CUSTOMER_NAME"))
				{
					resp.customerName = item.value;
					continue;
				}
				if (item.code.ToUpper().Equals("PHONE_NUMBER"))
				{
					resp.phoneNo = item.value;
					continue;
				}
				if (item.code.ToUpper().Equals("ENTITY_TYPE"))
				{
					resp.entityType = item.value;
					continue;
				}
			}

			if (requestOutput.outputParameterItems == null)
				return resp;


			List<Account> accountList = new List<Account>();
			Account account;

			foreach (OutputItems outputItems in requestOutput.outputParameterItems)
			{
				account = new Account();
				foreach (Item item in outputItems.exportItems)
				{
					if (item.code.ToUpper().Equals("ACCOUNT_TYPE"))
					{
						account.acctType = item.value;
						continue;
					}
					if (item.code.ToUpper().Equals("ACCOUNT_NUMBER"))
					{
						account.acctNo = item.value;
						continue;
					}
					if (item.code.ToUpper().Equals("DESCRIPTION"))
					{
						account.description = item.value;
						continue;
					}
				}
				accountList.Add(account);
			}

			if (accountList.Count > 0)
				resp.accountList = accountList;
			return resp;
		}



		private TxnResp accountToAccountTransfer(TxnData request, ChildTrans childTrans)
		{
			ServiceRequestWrapper serviceRequestWrapper = new ServiceRequestWrapper();
			ServiceRequestBody serviceRequestBody = new ServiceRequestBody();
			List<Item> importParams = new List<Item>();
			TxnResp resp = new TxnResp();
			try
			{
				Item item = new Item();
				item.code = "CREDIT_ACCOUNT";
				item.value = childTrans.destAcctNo;
				importParams.Add(item);

				item = new Item();
				item.code = "DEBIT_ACCOUNT";
				item.value = childTrans.sourceAcctNo;
				importParams.Add(item);

				item = new Item();
				item.code = "TRANS_AMOUNT";
				item.value = childTrans.transAmt.ToString();
				importParams.Add(item);

				item = new Item();
				item.code = "TRANS_DESCRIPTION";
				item.value = childTrans.description;
				importParams.Add(item);

				RequestInput dataWrapper = new RequestInput();
				InputItem inputData = new InputItem();
				inputData.items = importParams.ToArray();
				dataWrapper.inputItems = inputData;
				serviceRequestBody.serviceCode = "FUNDS_TRANSFER";
				serviceRequestBody.requestId = generateRequestId(request.authRequest.outletCode);
				serviceRequestBody.requestInput = dataWrapper;
				serviceRequestWrapper.authRequest = getAuthourizeRequest(request.authRequest.outletCode);
				serviceRequestWrapper.requestBody = serviceRequestBody;
				LOGGER.objectInfo(serviceRequestWrapper);
				ServiceResponseWrapper response = StubClientService.equiwebService.processServiceRequest(serviceRequestWrapper);
				LOGGER.objectInfo(response);
				resp.response = getRespMessage(response);
				if (resp.response.responseCode != "0")
					return resp;

				resp = getTranResp(response.requestOutput);
				resp.response = getRespMessage(response);
			}
			catch (Exception e)
			{
				resp.response = new ResponseMessage("-99", e.Message + " at eQuiWeb");
				LOGGER.error(e.ToString());
			}
			return resp;
		}


		public TxnResp fundsTransfer(ExtFinancialRequestData request)
		{
			ServiceRequestWrapper serviceRequestWrapper = new ServiceRequestWrapper();
			ServiceRequestBody serviceRequestBody = new ServiceRequestBody();
			List<Item> importParams = new List<Item>();
			TxnResp resp = new TxnResp();
			try
			{
				Item item = new Item();
				item.code = "CREDIT_ACCOUNT";
				item.value = request.primaryAcctNo;
				importParams.Add(item);

				item = new Item();
				item.code = "DEBIT_ACCOUNT";
				item.value = request.contraAcctNo;
				importParams.Add(item);

				item = new Item();
				item.code = "TRANS_AMOUNT";
				item.value = request.paymentAmount.ToString();
				importParams.Add(item);

				item = new Item();
				item.code = "TRANS_DESCRIPTION";
				item.value = request.adviceNote;
				importParams.Add(item);

				RequestInput dataWrapper = new RequestInput();
				InputItem inputData = new InputItem();
				inputData.items = importParams.ToArray();
				dataWrapper.inputItems = inputData;

				if (request.transType == "GL2DP")
					serviceRequestBody.serviceCode = "GL_TO_DEPOSIT";
				if (request.transType == "GL2GL")
					serviceRequestBody.serviceCode = "GL_TO_GL";

				serviceRequestBody.requestId = generateRequestId(request.apiUserName);
				serviceRequestBody.requestInput = dataWrapper;
				serviceRequestWrapper.authRequest = getAuthourizeRequest(request.apiUserName);
				serviceRequestWrapper.requestBody = serviceRequestBody;
				LOGGER.objectInfo(serviceRequestWrapper);
				ServiceResponseWrapper response = StubClientService.equiwebService.processServiceRequest(serviceRequestWrapper);
				LOGGER.objectInfo(response);
				resp.response = getRespMessage(response);
				if (resp.response.responseCode != "0")
					return resp;

				resp = getTranResp(response.requestOutput);
				resp.response = getRespMessage(response);
			}
			catch (Exception e)
			{
				resp.response = new ResponseMessage("-99", e.Message + " at eQuiWeb");
				LOGGER.error(e.ToString());
			}
			return resp;
		}


		private TxnResp glToDpTransfer(TxnData request, ChildTrans childTrans)
		{
			ServiceRequestWrapper serviceRequestWrapper = new ServiceRequestWrapper();
			ServiceRequestBody serviceRequestBody = new ServiceRequestBody();
			List<Item> importParams = new List<Item>();
			TxnResp resp = new TxnResp();
			try
			{
				Item item = new Item();
				item.code = "CREDIT_ACCOUNT";
				item.value = childTrans.destAcctNo;
				importParams.Add(item);

				item = new Item();
				item.code = "DEBIT_ACCOUNT";
				item.value = childTrans.sourceAcctNo;
				importParams.Add(item);

				item = new Item();
				item.code = "TRANS_AMOUNT";
				item.value = childTrans.transAmt.ToString();
				importParams.Add(item);

				item = new Item();
				item.code = "TRANS_DESCRIPTION";
				item.value = childTrans.description;
				importParams.Add(item);

				RequestInput dataWrapper = new RequestInput();
				InputItem inputData = new InputItem();
				inputData.items = importParams.ToArray();
				dataWrapper.inputItems = inputData;
				serviceRequestBody.serviceCode = "GL_TO_DEPOSIT";
				serviceRequestBody.requestId = generateRequestId(request.authRequest.outletCode);
				serviceRequestBody.requestInput = dataWrapper;
				serviceRequestWrapper.authRequest = getAuthourizeRequest(request.authRequest.outletCode);
				serviceRequestWrapper.requestBody = serviceRequestBody;
				LOGGER.objectInfo(serviceRequestWrapper);
				ServiceResponseWrapper response = StubClientService.equiwebService.processServiceRequest(serviceRequestWrapper);
				LOGGER.objectInfo(response);
				resp.response = getRespMessage(response);
				if (resp.response.responseCode != "0")
					return resp;

				resp = getTranResp(response.requestOutput);
				resp.response = getRespMessage(response);
			}
			catch (Exception e)
			{
				resp.response = new ResponseMessage("-99", e.Message + " at eQuiWeb");
				LOGGER.error(e.ToString());
			}
			return resp;
		}
		private TxnResp dpToGLTransfer(TxnData request, ChildTrans childTrans)
		{
			ServiceRequestWrapper serviceRequestWrapper = new ServiceRequestWrapper();
			ServiceRequestBody serviceRequestBody = new ServiceRequestBody();
			List<Item> importParams = new List<Item>();
			TxnResp resp = new TxnResp();
			try
			{
				Item item = new Item();
				item.code = "CREDIT_ACCOUNT";
				item.value = childTrans.destAcctNo;
				importParams.Add(item);

				item = new Item();
				item.code = "DEBIT_ACCOUNT";
				item.value = childTrans.sourceAcctNo;
				importParams.Add(item);

				item = new Item();
				item.code = "TRANS_AMOUNT";
				item.value = childTrans.transAmt.ToString();
				importParams.Add(item);

				item = new Item();
				item.code = "TRANS_DESCRIPTION";
				item.value = childTrans.description;
				importParams.Add(item);

				RequestInput dataWrapper = new RequestInput();
				InputItem inputData = new InputItem();
				inputData.items = importParams.ToArray();
				dataWrapper.inputItems = inputData;
				serviceRequestBody.serviceCode = "DEPOSIT_TO_GL";
				serviceRequestBody.requestId = generateRequestId(request.authRequest.outletCode);
				serviceRequestBody.requestInput = dataWrapper;
				serviceRequestWrapper.authRequest = getAuthourizeRequest(request.authRequest.outletCode);
				serviceRequestWrapper.requestBody = serviceRequestBody;
				LOGGER.objectInfo(serviceRequestWrapper);
				ServiceResponseWrapper response = StubClientService.equiwebService.processServiceRequest(serviceRequestWrapper);
				LOGGER.objectInfo(response);
				resp.response = getRespMessage(response);
				if (resp.response.responseCode != "0")
					return resp;

				resp = getTranResp(response.requestOutput);
				resp.response = getRespMessage(response);
			}
			catch (Exception e)
			{
				resp.response = new ResponseMessage("-99", e.Message + " at eQuiWeb");
				LOGGER.error(e.ToString());
			}
			return resp;
		}

		private TxnResp glToGLTransfer(TxnData request, ChildTrans childTrans)
		{
			ServiceRequestWrapper serviceRequestWrapper = new ServiceRequestWrapper();
			ServiceRequestBody serviceRequestBody = new ServiceRequestBody();
			List<Item> importParams = new List<Item>();
			TxnResp resp = new TxnResp();
			try
			{
				Item item = new Item();
				item.code = "CREDIT_ACCOUNT";
				item.value = childTrans.destAcctNo;
				importParams.Add(item);

				item = new Item();
				item.code = "DEBIT_ACCOUNT";
				item.value = childTrans.sourceAcctNo;
				importParams.Add(item);

				item = new Item();
				item.code = "TRANS_AMOUNT";
				item.value = childTrans.transAmt.ToString();
				importParams.Add(item);

				item = new Item();
				item.code = "TRANS_DESCRIPTION";
				item.value = childTrans.description;
				importParams.Add(item);

				RequestInput dataWrapper = new RequestInput();
				InputItem inputData = new InputItem();
				inputData.items = importParams.ToArray();
				dataWrapper.inputItems = inputData;
				serviceRequestBody.serviceCode = "GL_TO_GL";
				serviceRequestBody.requestId = generateRequestId(request.authRequest.outletCode);
				serviceRequestBody.requestInput = dataWrapper;
				serviceRequestWrapper.authRequest = getAuthourizeRequest(request.authRequest.outletCode);
				serviceRequestWrapper.requestBody = serviceRequestBody;
				LOGGER.objectInfo(serviceRequestWrapper);
				ServiceResponseWrapper response = StubClientService.equiwebService.processServiceRequest(serviceRequestWrapper);
				LOGGER.objectInfo(response);
				resp.response = getRespMessage(response);
				if (resp.response.responseCode != "0")
					return resp;

				resp = getTranResp(response.requestOutput);
				resp.response = getRespMessage(response);
			}
			catch (Exception e)
			{
				resp.response = new ResponseMessage("-99", e.Message + " at eQuiWeb");
				LOGGER.error(e.ToString());
			}
			return resp;
		}
		public TxnResp fundsTransfer1(TxnData request, TxnResp childTrans)
		{
			string originCreditAcctNo, originDebitAcctNo;
			ChildTrans[] transferRequest = childTrans.transItems.ToArray();
			TxnResp non_main_txn = null;
			TxnResp main_txn_response = new TxnResp();
			try
			{
				for (int i = 0; i < transferRequest.Length; i++)
				{

					if (transferRequest[i].transType == "P2P")
					{
						LOGGER.info("========================== Transaction Type: " + transferRequest[i].transType);
						non_main_txn = accountToAccountTransfer(request, transferRequest[i]);
					}
					else if (transferRequest[i].transType == "GL2GL")
					{
						LOGGER.info("xxxxxxxxxxxxxxxxxxxxxxxxxxxxx Transaction Type: " + transferRequest[i].transType);
						non_main_txn = glToGLTransfer(request, transferRequest[i]);
					}
					else if (transferRequest[i].transType == "GL2DP")
					{
						LOGGER.info("############################## Transaction Type: " + transferRequest[i].transType);
						non_main_txn = glToDpTransfer(request, transferRequest[i]);
					}
					else if (transferRequest[i].transType == "DP2GL")
					{
						LOGGER.info("++++++++++++++++++++++++++++++ Transaction Type: " + transferRequest[i].transType);
						non_main_txn = dpToGLTransfer(request, transferRequest[i]);
					}

					if (non_main_txn.response.responseCode == "0")
					{
						transferRequest[i].originTrans = non_main_txn.transId;
						if (transferRequest[i].sourceAcctNo.Trim() == request.drAcctNo.Trim())
						{
							main_txn_response = non_main_txn;
							main_txn_response.drAcctBal = non_main_txn.drAcctBal;
						}

						if (transferRequest[i].destAcctNo.Trim() == request.crAcctNo)
						{
							main_txn_response = non_main_txn;
							main_txn_response.crAcctBal = non_main_txn.crAcctBal;
						}

						continue;
					}
					else
					{
						main_txn_response = non_main_txn;
					}


					// If the transaction posting was not successful then reverse
					for (int j = 0; j < i; j++)
					{
						// Check for only those transactions that posted in the previous loop
						if (transferRequest[j].originTrans != null)
						{
							// Counter Assign the accounts for reversal.
							originCreditAcctNo = transferRequest[j].destAcctNo;
							originDebitAcctNo = transferRequest[j].sourceAcctNo;

							transferRequest[j].destAcctNo = originDebitAcctNo;
							transferRequest[j].sourceAcctNo = originCreditAcctNo;
							transferRequest[j].description = "*Reversal*-" + transferRequest[j].description;

							if (transferRequest[j].transType == "P2P")
								accountToAccountTransfer(request, transferRequest[j]);
							else if (transferRequest[j].transType == "GL2GL")
								glToGLTransfer(request, transferRequest[j]);
							else if (transferRequest[j].transType == "GL2DP")
								dpToGLTransfer(request, transferRequest[j]);
							else if (transferRequest[j].transType == "DP2GL")
								glToDpTransfer(request, transferRequest[j]);
						}
					}
					break;

				}
				return main_txn_response;
			}
			catch (Exception e)
			{
				main_txn_response.response = new ResponseMessage("-99", e.Message + " at eQuiWeb");
				LOGGER.error(e.ToString());
			}
			return main_txn_response;
		}

		  
		public ResponseMessage reverseTransaction1(TxnData request, TxnResp childTrans, string reversalReason)
		{
			string originCreditAcctNo, originDebitAcctNo;
			ChildTrans[] transferRequest = childTrans.transItems.ToArray();
			ResponseMessage main_txn_response = new ResponseMessage();
			try
			{
				for (int i = 0; i < transferRequest.Length; i++)
				{
					// Counter Assign the accounts for reversal.
					originCreditAcctNo = transferRequest[i].destAcctNo;
					originDebitAcctNo = transferRequest[i].sourceAcctNo;

					transferRequest[i].destAcctNo = originDebitAcctNo;
					transferRequest[i].sourceAcctNo = originCreditAcctNo;
					transferRequest[i].description = "*Reversal*-" + reversalReason;
					LOGGER.info("========================== Reversal Transaction Type: " + transferRequest[i].transType);
					if (transferRequest[i].transType == "P2P")
					{
						accountToAccountTransfer(request, transferRequest[i]);
					}
					else if (transferRequest[i].transType == "GL2GL")
					{
						glToGLTransfer(request, transferRequest[i]);
					}
					else if (transferRequest[i].transType == "GL2DP")
					{
						dpToGLTransfer(request, transferRequest[i]);
					}
					else if (transferRequest[i].transType == "DP2GL")
					{
						glToDpTransfer(request, transferRequest[i]);
					}
				}
				return MESSAGES.getSuccessMessage();
			}
			catch (Exception e)
			{
				main_txn_response = new ResponseMessage("-99", e.Message + " at eQuiWeb");
				LOGGER.error(e.ToString());
			}
			return main_txn_response;
		}


		public CIAccountBalanceResponse doAccountBalance(AccountRequest request)
		{
			CIAccountBalanceResponse accountBalance = new CIAccountBalanceResponse();
			ServiceRequestWrapper serviceRequestWrapper = new ServiceRequestWrapper();
			ServiceRequestBody serviceRequestBody = new ServiceRequestBody();
			List<Item> importParams = new List<Item>();
			try
			{
				Item item = new Item();
				item.code = "PRIMARY_ACCOUNT";
				item.value = request.accountNo;
				importParams.Add(item);
				RequestInput dataWrapper = new RequestInput();
				InputItem inputData = new InputItem();
				inputData.items = importParams.ToArray();
				dataWrapper.inputItems = inputData;
				serviceRequestBody.serviceCode = "BALANCE_INQUIRY";
				serviceRequestBody.requestId = generateRequestId(request.authRequest.outletCode);
				serviceRequestBody.requestInput = dataWrapper;
				serviceRequestWrapper.authRequest = getAuthourizeRequest(request.authRequest.outletCode);
				serviceRequestWrapper.requestBody = serviceRequestBody;
				LOGGER.objectInfo(serviceRequestWrapper);
				ServiceResponseWrapper response = StubClientService.equiwebService.processServiceRequest(serviceRequestWrapper);
				LOGGER.objectInfo(response);

				accountBalance.response = getRespMessage(response);
				if (accountBalance.response.responseCode != "0")
					return accountBalance;

				accountBalance = getAccountBalanceResp(response.requestOutput);
				accountBalance.response = MESSAGES.getSuccessMessage();
			}
			catch (Exception e)
			{
				accountBalance.response = new ResponseMessage("-99", e.Message + " at eQuiWeb");
				LOGGER.error(e.ToString());
			}
			return accountBalance;
		}
		public CIAccountResponse accountInquiryByAccountNo(AccountRequest request)
		{
			CIAccountResponse accountResponse = new CIAccountResponse();
			try
			{
				ServiceRequestWrapper serviceRequestWrapper = new ServiceRequestWrapper();
				ServiceRequestBody serviceRequestBody = new ServiceRequestBody();
				List<Item> importParams = new List<Item>();
				Item item = new Item();
				item.code = "PRIMARY_ACCOUNT";
				item.value = request.accountNo;
				importParams.Add(item);
				RequestInput dataWrapper = new RequestInput();
				InputItem inputData = new InputItem();
				inputData.items = importParams.ToArray();
				dataWrapper.inputItems = inputData;
				serviceRequestBody.serviceCode = "CUSTOMER_INQUIRY";
				serviceRequestBody.requestId = generateRequestId(PROPERTIES.EQUIWEB_USER);
				serviceRequestBody.requestInput = dataWrapper;
				serviceRequestWrapper.authRequest = getAuthourizeRequest(PROPERTIES.EQUIWEB_USER);
				serviceRequestWrapper.requestBody = serviceRequestBody;
				LOGGER.objectInfo(serviceRequestWrapper);
				ServiceResponseWrapper response = StubClientService.equiwebService.processServiceRequest(serviceRequestWrapper);
				LOGGER.objectInfo(response);

				accountResponse.response = getRespMessage(response);
				if (accountResponse.response.responseCode != "0")
					return accountResponse;

				accountResponse = getAccountValidationResp(request.accountNo, response.requestOutput);
				accountResponse.response = MESSAGES.getSuccessMessage();
			}
			catch (Exception e)
			{
				accountResponse.response = new ResponseMessage("-99", e.Message + " at eQuiWeb");
				LOGGER.error(e.ToString());
			}
			return accountResponse;
		}
		 
		//public CICustomerResp accountInquiryByPhoneNo(OutletAuthentication request)
		//{
		//	CICustomerResp accountResponse = new CICustomerResp();
		//	try
		//	{
		//		ServiceRequestWrapper serviceRequestWrapper = new ServiceRequestWrapper();
		//		ServiceRequestBody serviceRequestBody = new ServiceRequestBody();
		//		List<Item> importParams = new List<Item>();
		//		Item item = new Item();
		//		item.code = "MOBILE_PHONE";
		//		item.value = CONVERTER.formatPhoneNumber(request.phoneNo);
		//		importParams.Add(item);
		//		RequestInput dataWrapper = new RequestInput();
		//		InputItem inputData = new InputItem();
		//		inputData.items = importParams.ToArray();
		//		dataWrapper.inputItems = inputData;
		//		serviceRequestBody.serviceCode = "CUSTOMER_INQUIRY";
		//		serviceRequestBody.requestId = generateRequestId(null);
		//		serviceRequestBody.requestInput = dataWrapper;
		//		serviceRequestWrapper.authRequest = getAuthourizeRequest(null);
		//		serviceRequestWrapper.requestBody = serviceRequestBody;
		//		LOGGER.objectInfo(serviceRequestWrapper);
		//		ServiceResponseWrapper response = StubClientService.equiwebService.processServiceRequest(serviceRequestWrapper);
		//		LOGGER.objectInfo(response);

		//		accountResponse.response = getRespMessage(response);
		//		if (accountResponse.response.responseCode != "0")
		//			return accountResponse;

		//		accountResponse = getPhoneValidationResp(response.requestOutput);
		//		accountResponse.response = MESSAGES.getSuccessMessage();
		//	}
		//	catch (Exception e)
		//	{
		//		accountResponse.response = new ResponseMessage("-99", e.Message + " at eQuiWeb");
		//		LOGGER.error(e.ToString());
		//	}
		//	return accountResponse;
		//}


		public SignUpResponse createCustomer(SignUpRequest request)
		{
			ServiceRequestWrapper serviceRequestWrapper = new ServiceRequestWrapper();
			ServiceRequestBody serviceRequestBody = new ServiceRequestBody();
			List<Item> importParams = new List<Item>();
			SignUpResponse signUpResponse = new SignUpResponse();
			try
			{
				Item item = new Item();
				item.code = "FIRST_NAME";
				item.value = request.firstName;
				importParams.Add(item);

				item = new Item();
				item.code = "LAST_NAME";
				item.value = request.surName;
				importParams.Add(item);

				item = new Item();
				item.code = "BIRTH_DT";
				item.value = request.dateOfBirth.ToString("yyyy-MM-dd");
				importParams.Add(item);

				item = new Item();
				item.code = "TOWN_ID";
				item.value = request.town ?? "2";
				importParams.Add(item);

				item = new Item();
				item.code = "IDENT_ID";
				item.value = request.idType.ToString();
				importParams.Add(item);

				item = new Item();
				item.code = "ID_VALUE";
				item.value = request.idNumber;
				importParams.Add(item);

				item = new Item();
				item.code = "TITLE_ID";
				item.value = request.titleId.ToString();
				importParams.Add(item);

				item = new Item();
				item.code = "MOBILE_PHONE";
				item.value = CONVERTER.formatPhoneNumber(request.mobilePhone);
				importParams.Add(item);

				item = new Item();
				item.code = "ID_EXPIRY_DT";
				item.value = request.idExpiryDate?.ToString("yyyy-MM-dd") ?? DateTime.Now.AddDays(900).ToString("yyyy-MM-dd");
				importParams.Add(item);

				item = new Item();
				item.code = "ID_ISSUE_DT";
				item.value = request.idIssueDate?.ToString("yyyy-MM-dd") ?? null;
				importParams.Add(item);

				item = new Item();
				item.code = "GENDER";
				item.value = request.gender;
				importParams.Add(item);

				item = new Item();
				item.code = "CLASS_CODE";
				item.value = "1";
				importParams.Add(item);

				item = new Item();
				item.code = "CUSTOMER_PHOTO";
				item.value = Convert.ToBase64String(request.customerPhoto);
				importParams.Add(item);

				item = new Item();
				item.code = "CUSTOMER_SIGNATURE";
				item.value = Convert.ToBase64String(request.customerSign);
				importParams.Add(item);

				RequestInput dataWrapper = new RequestInput();
				InputItem inputData = new InputItem();
				inputData.items = importParams.ToArray();
				dataWrapper.inputItems = inputData;
				serviceRequestBody.serviceCode = "CREATE_CUSTOMER_DATA";
				serviceRequestBody.requestId = generateRequestId(request.authRequest.outletCode);
				serviceRequestBody.requestInput = dataWrapper;
				serviceRequestWrapper.authRequest = getAuthourizeRequest(request.authRequest.outletCode);
				serviceRequestWrapper.requestBody = serviceRequestBody;

				LOGGER.objectInfo(serviceRequestWrapper);
				ServiceResponseWrapper response = StubClientService.equiwebService.processServiceRequest(serviceRequestWrapper);
				LOGGER.objectInfo(response);

				signUpResponse.response = getRespMessage(response);
				if (signUpResponse.response.responseCode != "0")
					return signUpResponse;

				signUpResponse = getCustomerResp(response.requestOutput);
				signUpResponse.response = getRespMessage(response);
			}
			catch (Exception e)
			{
				signUpResponse.response = new ResponseMessage("-99", e.Message + " at eQuiWeb");
				LOGGER.error(e.ToString());
			}
			return signUpResponse;
		}
	}
}