using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Text;
using System.Web;
using micropay_apis.ABModels;
using micropay_apis.APIModals;
using micropay_apis.Models;
using micropay_apis.Utils;
using Nancy.Json;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace micropay_apis.Remote
{
	public class MediumsService
	{
		private JavaScriptSerializer jsonSerializer;

		private OutletAuthentication getDefaultBaseRequest(OutletAuthentication authRequest)
		{
			if (authRequest == null)
				authRequest = new OutletAuthentication();
			authRequest.vCode = authRequest.vCode == null ? PROPERTIES.API_USER_NAME : authRequest.vCode;
			authRequest.vPassword = authRequest.vPassword == null ? PROPERTIES.API_PASSWORD : authRequest.vPassword;
			return authRequest;
		}


		private ResponseMessage getResponseMessage(JObject jObject)
		{
			ResponseMessage response = new ResponseMessage();
			response.responseCode = jObject["code"].Value<string>();
			response.responseMessage = jObject["message"].Value<string>();
			if (response.responseCode == "00")
				response.responseCode = "0";
			else
				response.responseMessage = response.responseMessage + " at Micropay Core";
			return response;
		}

		private Dictionary<string, object> getBaseRequest(OutletAuthentication request)
		{
			request = getDefaultBaseRequest(request);
			Dictionary<string, object> requestData = new Dictionary<string, object>();
			requestData.Add("apiUserName", request.vCode);
			requestData.Add("apiPassword", ENCRYPTER.encryptMCPValue(request.vPassword));
			requestData.Add("deviceId", request.deviceId);
			requestData.Add("imeiNumber", request.imeiNumber);
			requestData.Add("deviceMake", request.deviceMake);
			requestData.Add("deviceModel", request.deviceModel);
			requestData.Add("userPhoneNo", request.phoneNo);
			requestData.Add("outletCode", request.outletCode);
			requestData.Add("pinNo", ENCRYPTER.encryptMCPValue(request.pinNo));
			requestData.Add("channelCode", request.channelCode);
			return requestData;
		}


		public string sendHTTPostRequest(string httpMethod, string serviceURL, object requestData)
		{
			string response = "";
			string jsonString = JsonConvert.SerializeObject(requestData, Formatting.Indented);
			string completeURL = PROPERTIES.MICROPAY_CORE_API + serviceURL;
			LOGGER.info("===================== Mediums CompleteURL: " + completeURL);
			LOGGER.info("===================== Mediums Request => " + jsonString);
			Uri url = new Uri(completeURL);
			HttpWebRequest httpRequest = (HttpWebRequest)WebRequest.Create(url);
			NetworkCredential myNetworkCredential = new NetworkCredential("admin", "admin");
			CredentialCache myCredentialCache = new CredentialCache();
			myCredentialCache.Add(url, "Basic", myNetworkCredential);
            httpRequest.Timeout = 300000;
            httpRequest.PreAuthenticate = true;
			httpRequest.Credentials = myCredentialCache;

			httpRequest.Accept = "application/json";
			httpRequest.ContentType = "application/json";
			httpRequest.Method = httpMethod;
			if (requestData != null)
			{
				byte[] bytes = Encoding.UTF8.GetBytes(jsonString);
				using (Stream stream = httpRequest.GetRequestStream())
				{
					stream.Write(bytes, 0, bytes.Length);
					stream.Close();
				}
			}
			using (HttpWebResponse httpResponse = (HttpWebResponse)httpRequest.GetResponse())
			{
				using (Stream stream = httpResponse.GetResponseStream())
				{
					response = (new StreamReader(stream)).ReadToEnd();
				}
			}
			LOGGER.info("===================== Mediums Response => " + response);
			return response;
		}

		public SignUpResponse customerEnrollment(SignUpRequest request)
		{
			SignUpResponse signUpResponse = new SignUpResponse();
			jsonSerializer = new JavaScriptSerializer();
			Dictionary<string, object> requestData = getBaseRequest(request.authRequest);
			string methodName = "/agent-banking/createCustomer";
			string httpMethod = "POST";
			requestData.Add("firstName", request.firstName);
			requestData.Add("surName", request.surName);
			requestData.Add("dateOfBirth", request.dateOfBirth.ToString("yyyy-MM-dd"));
			requestData.Add("town", request.town ?? "2");
			requestData.Add("idType", request.idType.ToString());
			requestData.Add("idNumber", request.idNumber);
			requestData.Add("titleId", request.titleId.ToString());
			requestData.Add("mobilePhone", CONVERTER.formatPhoneNumber(request.mobilePhone));
			requestData.Add("idExpiryDate", request.idExpiryDate?.ToString("yyyy-MM-dd") ?? DateTime.Now.AddDays(900).ToString("yyyy-MM-dd"));
			requestData.Add("idIssueDate", request.idIssueDate?.ToString("yyyy-MM-dd") ?? null);
			requestData.Add("gender", request.gender);
			requestData.Add("photoBase64String", Convert.ToBase64String(request.customerPhoto));
			requestData.Add("signatureBase64String", Convert.ToBase64String(request.customerSign));
			string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			var data = (JObject)JsonConvert.DeserializeObject(stringResp);
			ResponseMessage response = getResponseMessage(data);
			if (response.responseCode != "0")
			{
				throw new MediumsException(response);
			}

			signUpResponse.response = MESSAGES.getSuccessMessage();
			var accountDetails = data["data"].Value<JObject>();
			signUpResponse.customerCode = accountDetails["customerNo"].Value<int?>();
			signUpResponse.accountNo = accountDetails["accountNo"].Value<string>();
			return signUpResponse;
		}





		public TxnResp test()
		{

			string stringResp = "{\"code\":\"00\",\"message\":\"approved\",\"data\":{\"transId\":345616,\"chargeAmt\":2127.5000,\"drAcctBalance\":1405478.90,\"crAcctBalance\":106619274.00,\"cbsTransId\":\"2018046\",\"transDetails\":[{\"destAcctNo\":\"3100000002\",\"transAmt\":70000.0,\"sourceAcctNo\":\"2700000073\",\"description\":\"TRANS_AMOUNT-256703966859=>Pay UMEME => 14371612087 : NDYAREEBA STUART GRACE\",\"amountType\":\"TRANS_AMOUNT\",\"transType\":\"P2P\",\"cbsTransId\":\"2018046\",\"mcpTransId\":0,\"mcpTransDetailId\":0,\"createDate\":null,\"reversalFailureReason\":null,\"main\":true},{\"destAcctNo\":\"01-01-00-1990302\",\"transAmt\":1850.00,\"sourceAcctNo\":\"2700000073\",\"description\":\"CHARGE-256703966859=>Pay UMEME => 14371612087 : NDYAREEBA STUART GRACE\",\"amountType\":\"CHARGE\",\"transType\":\"DP2GL\",\"cbsTransId\":\"2018048\",\"mcpTransId\":0,\"mcpTransDetailId\":0,\"createDate\":null,\"reversalFailureReason\":null,\"main\":false},{\"destAcctNo\":\"01-01-00-2100404\",\"transAmt\":277.5000,\"sourceAcctNo\":\"2700000073\",\"description\":\"EXCISE_DUTY-256703966859=>Pay UMEME => 14371612087 : NDYAREEBA STUART GRACE\",\"amountType\":\"EXCISE_DUTY\",\"transType\":\"DP2GL\",\"cbsTransId\":\"2018049\",\"mcpTransId\":0,\"mcpTransDetailId\":0,\"createDate\":null,\"reversalFailureReason\":null,\"main\":false},{\"destAcctNo\":\"2300000224\",\"transAmt\":925.0000,\"sourceAcctNo\":\"01-01-00-1990302\",\"description\":\"AGENT_COMMISSION-256703966859=>Pay UMEME => 14371612087 : NDYAREEBA STUART GRACE\",\"amountType\":\"AGENT_COMMISSION\",\"transType\":\"GL2DP\",\"cbsTransId\":\"2018050\",\"mcpTransId\":0,\"mcpTransDetailId\":0,\"createDate\":null,\"reversalFailureReason\":null,\"main\":false},{\"destAcctNo\":\"01-01-00-2100403\",\"transAmt\":92.50000,\"sourceAcctNo\":\"2300000224\",\"description\":\"WITHHOLD_TAX-256703966859=>Pay UMEME => 14371612087 : NDYAREEBA STUART GRACE\",\"amountType\":\"WITHHOLD_TAX\",\"transType\":\"DP2GL\",\"cbsTransId\":\"2018051\",\"mcpTransId\":0,\"mcpTransDetailId\":0,\"createDate\":null,\"reversalFailureReason\":null,\"main\":false},{\"destAcctNo\":\"01-01-00-4050406\",\"transAmt\":925.0000,\"sourceAcctNo\":\"01-01-00-1990302\",\"description\":\"NET_CHARGE-256703966859=>Pay UMEME => 14371612087 : NDYAREEBA STUART GRACE\",\"amountType\":\"NET_CHARGE\",\"transType\":\"GL2GL\",\"cbsTransId\":\"1750862\",\"mcpTransId\":0,\"mcpTransDetailId\":0,\"createDate\":null,\"reversalFailureReason\":null,\"main\":false},{\"destAcctNo\":\"01-01-00-1050103\",\"transAmt\":70000.0,\"sourceAcctNo\":\"01-01-00-1050102\",\"description\":\"TRANS_AMOUNT-256703966859=>Pay UMEME => 14371612087 : NDYAREEBA STUART GRACE\",\"amountType\":\"TRANS_AMOUNT\",\"transType\":\"GL2GL\",\"cbsTransId\":\"1750864\",\"mcpTransId\":0,\"mcpTransDetailId\":0,\"createDate\":null,\"reversalFailureReason\":null,\"main\":false},{\"destAcctNo\":\"01-01-00-1050103\",\"transAmt\":925.0000,\"sourceAcctNo\":\"01-01-00-1050102\",\"description\":\"NET_CHARGE-256703966859=>Pay UMEME => 14371612087 : NDYAREEBA STUART GRACE\",\"amountType\":\"NET_CHARGE\",\"transType\":\"GL2GL\",\"cbsTransId\":\"1750866\",\"mcpTransId\":0,\"mcpTransDetailId\":0,\"createDate\":null,\"reversalFailureReason\":null,\"main\":false},{\"destAcctNo\":\"01-01-00-1050103\",\"transAmt\":277.5000,\"sourceAcctNo\":\"01-01-00-1050102\",\"description\":\"EXCISE_DUTY-256703966859=>Pay UMEME => 14371612087 : NDYAREEBA STUART GRACE\",\"amountType\":\"EXCISE_DUTY\",\"transType\":\"GL2GL\",\"cbsTransId\":\"1750868\",\"mcpTransId\":0,\"mcpTransDetailId\":0,\"createDate\":null,\"reversalFailureReason\":null,\"main\":false},{\"destAcctNo\":\"01-01-00-1050103\",\"transAmt\":92.50000,\"sourceAcctNo\":\"01-01-00-1050102\",\"description\":\"WITHHOLD_TAX-256703966859=>Pay UMEME => 14371612087 : NDYAREEBA STUART GRACE\",\"amountType\":\"WITHHOLD_TAX\",\"transType\":\"GL2GL\",\"cbsTransId\":\"1750870\",\"mcpTransId\":0,\"mcpTransDetailId\":0,\"createDate\":null,\"reversalFailureReason\":null,\"main\":false}]}}";
			var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
			ResponseMessage response = getResponseMessage(jsonObject);
			if (response.responseCode != "0")
			{
				throw new MediumsException(response);
			}
			var data = jsonObject["data"].Value<JObject>();
			TxnResp txnResp = new TxnResp();
			txnResp.response = MESSAGES.getSuccessMessage();
			txnResp.transId = data["transId"].Value<string>();
			txnResp.chargeAmt = data["chargeAmt"].Value<double>();
			txnResp.drAcctBal = data["drAcctBalance"].Value<double?>();
			txnResp.crAcctBal = data["crAcctBalance"].Value<double?>();

			var accountList = data["transDetails"].Children();
			List<ChildTrans> accounts = new List<ChildTrans>();
			ChildTrans item;
			foreach (var account in accountList)
			{
				item = new ChildTrans();
				item.destAcctNo = account["destAcctNo"].Value<string>();
				item.transAmt = account["transAmt"].Value<double>();
				item.sourceAcctNo = account["sourceAcctNo"].Value<string>();
				item.description = account["description"].Value<string>();
				item.amountType = account["amountType"].Value<string>();
				item.transType = account["transType"].Value<string>();
				accounts.Add(item);
			}
			txnResp.transItems = accounts;
			return txnResp;
		}



		public TxnResp fundsTransfer(TxnData request)
		{
			double additionalCharge = 0;
			if (request.tranCode == 70055 || request.tranCode == 70056)
				additionalCharge = request.surCharge;

			jsonSerializer = new JavaScriptSerializer();
			Dictionary<string, object> requestData = getBaseRequest(request.authRequest);
			string methodName = "/agent-banking/fundsTransfer";
			string httpMethod = "POST";
			requestData.Add("destAcctNo", request.crAcctNo);
			requestData.Add("transAmt", request.transAmt);
			requestData.Add("additionalCharge", additionalCharge);
			requestData.Add("currency", request.currency);
			requestData.Add("sourceAcctNo", request.drAcctNo);
			requestData.Add("description", request.description);
			requestData.Add("serviceCode", request.tranCode);
			requestData.Add("externalReference", request.externalTransId);
			requestData.Add("depositorPhoneNo", request.depositorPhoneNo);
			requestData.Add("depositorName", request.depositorName);
			string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
			ResponseMessage response = getResponseMessage(jsonObject);
			if (response.responseCode != "0")
			{
				throw new MediumsException(response);
			}
			var data = jsonObject["data"].Value<JObject>();
			TxnResp txnResp = new TxnResp();
			txnResp.response = MESSAGES.getSuccessMessage();
			txnResp.transId = data["transId"].Value<string>();
			txnResp.chargeAmt = data["chargeAmt"].Value<double>();
			txnResp.drAcctBal = data["drAcctBalance"].Value<double?>();
			txnResp.crAcctBal = data["crAcctBalance"].Value<double?>();

			var accountList = data["transDetails"].Children();
			List<ChildTrans> accounts = new List<ChildTrans>();
			ChildTrans item;
			foreach (var account in accountList)
			{
				item = new ChildTrans();
				item.destAcctNo = account["destAcctNo"].Value<string>();
				item.transAmt = account["transAmt"].Value<double>();
				item.sourceAcctNo = account["sourceAcctNo"].Value<string>();
				item.description = account["description"].Value<string>();
				item.amountType = account["amountType"].Value<string>();
				item.transType = account["transType"].Value<string>();
				accounts.Add(item);
			}
			txnResp.transItems = accounts;
			return txnResp;
		}
		 

        public TxnResp completeTransaction(TxnData request)
        {
            double additionalCharge = 0;
            if (request.tranCode == 70055 || request.tranCode == 70056)
                additionalCharge = request.surCharge;

            jsonSerializer = new JavaScriptSerializer();
            Dictionary<string, object> requestData = getBaseRequest(request.authRequest);
            string methodName = "/agent-banking/completeTransaction";
            string httpMethod = "POST";
            requestData.Add("destAcctNo", request.crAcctNo);
            requestData.Add("originTransId", request.originTransId);
            requestData.Add("transAmt", request.transAmt);
            requestData.Add("additionalCharge", additionalCharge);
            requestData.Add("currency", request.currency);
            requestData.Add("sourceAcctNo", request.drAcctNo);
            requestData.Add("description", request.description);
            requestData.Add("serviceCode", request.tranCode);
            requestData.Add("externalReference", request.externalTransId);
            requestData.Add("depositorPhoneNo", request.depositorPhoneNo);
            requestData.Add("depositorName", request.depositorName);
            string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
            var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
            ResponseMessage response = getResponseMessage(jsonObject);
            if (response.responseCode != "0")
            {
                throw new MediumsException(response);
            }
            var data = jsonObject["data"].Value<JObject>();
            TxnResp txnResp = new TxnResp();
            txnResp.response = MESSAGES.getSuccessMessage();
            txnResp.transId = data["transId"].Value<string>();
            txnResp.chargeAmt = data["chargeAmt"].Value<double>();
            txnResp.drAcctBal = data["drAcctBalance"].Value<double?>();
            txnResp.crAcctBal = data["crAcctBalance"].Value<double?>();

            var accountList = data["transDetails"].Children();
            List<ChildTrans> accounts = new List<ChildTrans>();
            ChildTrans item;
            foreach (var account in accountList)
            {
                item = new ChildTrans();
                item.destAcctNo = account["destAcctNo"].Value<string>();
                item.transAmt = account["transAmt"].Value<double>();
                item.sourceAcctNo = account["sourceAcctNo"].Value<string>();
                item.description = account["description"].Value<string>();
                item.amountType = account["amountType"].Value<string>();
                item.transType = account["transType"].Value<string>();
                accounts.Add(item);
            }
            txnResp.transItems = accounts;
            return txnResp;
        }
		 
        public TxnResp logTransaction(TxnData request)
        {
            double additionalCharge = 0;
            if (request.tranCode == 70055 || request.tranCode == 70056)
                additionalCharge = request.surCharge;

            jsonSerializer = new JavaScriptSerializer();
            Dictionary<string, object> requestData = getBaseRequest(request.authRequest);
            string methodName = "/agent-banking/logTransaction";
            string httpMethod = "POST";
            requestData.Add("destAcctNo", request.crAcctNo);
            requestData.Add("originTransId", request.originTransId);
            requestData.Add("transAmt", request.transAmt);
            requestData.Add("additionalCharge", additionalCharge);
            requestData.Add("currency", request.currency);
            requestData.Add("sourceAcctNo", request.drAcctNo);
            requestData.Add("description", request.description);
            requestData.Add("serviceCode", request.tranCode);
            requestData.Add("externalReference", request.externalTransId);
            requestData.Add("depositorPhoneNo", request.depositorPhoneNo);
            requestData.Add("depositorName", request.depositorName);
            string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
            var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
            ResponseMessage response = getResponseMessage(jsonObject);
            if (response.responseCode != "0")
            {
                throw new MediumsException(response);
            }
            var data = jsonObject["data"].Value<JObject>();
            TxnResp txnResp = new TxnResp();
            txnResp.response = MESSAGES.getSuccessMessage();
            txnResp.transId = data["transId"].Value<string>();
            txnResp.chargeAmt = data["chargeAmt"].Value<double>();
            txnResp.drAcctBal = data["drAcctBalance"].Value<double?>();
            txnResp.crAcctBal = data["crAcctBalance"].Value<double?>();

            var accountList = data["transDetails"].Children();
            List<ChildTrans> accounts = new List<ChildTrans>();
            ChildTrans item;
            foreach (var account in accountList)
            {
                item = new ChildTrans();
                item.destAcctNo = account["destAcctNo"].Value<string>();
                item.transAmt = account["transAmt"].Value<double>();
                item.sourceAcctNo = account["sourceAcctNo"].Value<string>();
                item.description = account["description"].Value<string>();
                item.amountType = account["amountType"].Value<string>();
                item.transType = account["transType"].Value<string>();
                accounts.Add(item);
            }
            txnResp.transItems = accounts;
            return txnResp;
        }




        public TxnResp submitEscrowTransaction(TxnData request)
		{
			jsonSerializer = new JavaScriptSerializer();
			Dictionary<string, object> requestData = new Dictionary<string, object>();
			string methodName = "/agent-banking/saveEscrowTransaction";
			string httpMethod = "POST";
			requestData.Add("destAcctNo", request.crAcctNo);
			requestData.Add("transAmt", request.transAmt);
			requestData.Add("currency", request.currency);
			requestData.Add("sourceAcctNo", request.drAcctNo);
			requestData.Add("description", request.description);
			requestData.Add("serviceCode", request.tranCode);
			requestData.Add("externalReference", request.externalTransId);
			requestData.Add("depositorPhoneNo", request.depositorPhoneNo);
			requestData.Add("depositorName", request.depositorName);
			string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
			ResponseMessage response = getResponseMessage(jsonObject);
			if (response.responseCode != "0")
			{
				throw new MediumsException(response);
			}
			var data = jsonObject["data"].Value<JObject>();
			TxnResp txnResp = new TxnResp();
			txnResp.response = MESSAGES.getSuccessMessage();
			txnResp.transId = data["transId"].Value<string>();
			return txnResp;
		}

		public TxnResp reverseTrans(CIReversalRequest request)
		{
			jsonSerializer = new JavaScriptSerializer();
			TxnResp txnResp;
			try
			{
				Dictionary<string, object> requestData = getBaseRequest(request.authRequest);
				string methodName = "/agent-banking/reverseTrans";
				string httpMethod = "POST";
				requestData.Add("originTranId", request.transId);
				requestData.Add("reversalReason", request.reason);
				string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
				var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
				ResponseMessage response = getResponseMessage(jsonObject);
				txnResp = new TxnResp();
				if (response.responseCode != "0")
				{
					txnResp.response = response;
					return txnResp;
				}

				txnResp.response = response;
				//var data = jsonObject["data"].Value<JObject>();
				//txnResp.response = MESSAGES.getSuccessMessage();
				//txnResp.transId = data["transId"].Value<string>();

				//var accountList = data["transDetails"].Children();
				//List<ChildTrans> accounts = new List<ChildTrans>();
				//ChildTrans item;
				//foreach (var account in accountList)
				//{
				//	item = new ChildTrans();
				//	item.destAcctNo = account["destAcctNo"].Value<string>();
				//	item.transAmt = account["transAmt"].Value<double>();
				//	item.sourceAcctNo = account["sourceAcctNo"].Value<string>();
				//	item.description = account["description"].Value<string>();
				//	item.amountType = account["amountType"].Value<string>();
				//	accounts.Add(item);
				//}
				//txnResp.transItems = accounts;
				return txnResp;
			}
			catch (Exception e)
			{
				txnResp = new TxnResp
				{
					response = new ResponseMessage("-99", "An error occured during reversal")
				};
				LOGGER.error(e.ToString());
			}
			return txnResp;


		}
		public CICustomerResp pinAuthentication(OutletAuthentication request)
		{
			jsonSerializer = new JavaScriptSerializer();
			string methodName = "/agent-banking/pinAuthentication";
			Dictionary<string, object> requestData = getBaseRequest(request);
			requestData.Add("newDeviceFlag", request.newDeviceFlag);
			string httpMethod = "POST";
			string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
			ResponseMessage message = getResponseMessage(jsonObject);
			if (message.responseCode != "0")
			{
				throw new MediumsException(message);
			}
			var data = jsonObject["data"].Value<JObject>();
			CICustomerResp response = new CICustomerResp();
			response.response = MESSAGES.getSuccessMessage();
			response.customerName = data["customerName"].Value<string>();
			response.effectiveDate = data["effectiveDate"].Value<string>();
			response.phoneNo = data["phoneNo"].Value<string>();
			response.status = data["status"].Value<string>();
			response.rimNo = data["rimNo"].Value<string>();
			response.entityType = data["entityType"].Value<string>();
			response.deviceID = data["deviceID"].Value<string>();
			response.outletCode = data["outletCode"].Value<string>();
			response.lockedFlag = data["lockedFlag"].Value<Boolean>();
			response.pinChangeFlag = data["pinChangeFlag"].Value<Boolean>();

			var accountList = data["accountList"].Children();
			List<Account> accounts = new List<Account>();
			Account item;
			foreach (var account in accountList)
			{
				item = new Account();
				item.acctNo = toString(account["acctNo"].Value<string>());
				item.acctType = account["acctType"].Value<string>();
				item.description = toString(account["description"].Value<string>());
				accounts.Add(item);
			}
			response.accountList = accounts;
			return response;
		}


		public CICustomerResp accountInquiryByPhoneNo(OutletAuthentication request)
		{

			jsonSerializer = new JavaScriptSerializer();
			Dictionary<string, object> requestData = getBaseRequest(request);
			string methodName = "/agent-banking/accountInquiryByPhoneNo";
			string httpMethod = "POST";
			requestData.Add("phoneNo", request.phoneNo);
			string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
			ResponseMessage response = getResponseMessage(jsonObject);
			if (response.responseCode != "0")
			{
				throw new MediumsException(response);
			}
			var data = jsonObject["data"].Value<JObject>();
			CICustomerResp txnResp = new CICustomerResp();
			txnResp.response = MESSAGES.getSuccessMessage();

			txnResp.customerName = data["customerName"].Value<string>();
			txnResp.phoneNo = data["phoneNo"].Value<string>();
			txnResp.entityType = data["entityType"].Value<string>();
			var accountList = data["accountList"].Children();
			List<Account> accounts = new List<Account>();
			Account item;
			foreach (var account in accountList)
			{
				item = new Account();
				item.acctType = account["acctType"].Value<string>();
				item.acctNo = account["accountNo"].Value<string>();
				item.description = account["acctTitle"].Value<string>();
				accounts.Add(item);
			}
			txnResp.accountList = accounts;
			return txnResp;
		}


		public ResponseMessage performDevicePairing(OutletAuthentication request)
		{
			jsonSerializer = new JavaScriptSerializer();
			Dictionary<string, object> requestData = getBaseRequest(request);
			string methodName = "/agent-banking/performDevicePairing";
			string httpMethod = "POST";
			requestData.Add("phoneNumber", request.phoneNo);
			requestData.Add("authImsi", request.deviceId);
			requestData.Add("authImei", request.imeiNumber);
			requestData.Add("activationCode", request.deviceActivationCode);
			string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			var data = (JObject)JsonConvert.DeserializeObject(stringResp);
			ResponseMessage response = getResponseMessage(data);
			if (response.responseCode != "0")
			{
				throw new MediumsException(response);
			}
			return MESSAGES.getSuccessMessage();
		}


		public ResponseMessage generateDeviceActivationCode(OutletAuthentication request)
		{
			jsonSerializer = new JavaScriptSerializer();
			Dictionary<string, object> requestData = getBaseRequest(request);
			string methodName = "/agent-banking/generateDeviceActivationCode";
			string httpMethod = "POST";
			requestData.Add("phoneNumber", request.phoneNo);
			requestData.Add("authImsi", request.deviceId);
			requestData.Add("authImei", request.imeiNumber);
			string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			var data = (JObject)JsonConvert.DeserializeObject(stringResp);
			ResponseMessage response = getResponseMessage(data);
			if (response.responseCode != "0")
			{
				throw new MediumsException(response);
			}
			return MESSAGES.getSuccessMessage();
		}




		string toString(Object objectData)
		{
			try
			{
				if (objectData != null)
					return objectData.ToString().Trim();
				else
					return null;
			}
			catch (Exception e)
			{
				return null;
			}
		}



		public BillerData findBillersByCode(string billCode)
		{
			Dictionary<string, object> requestData = getBaseRequest(new OutletAuthentication());
			string methodName = "/biller/findBillersByCode";
			string httpMethod = "POST";
			requestData.Add("billerCode", billCode);
			string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
			ResponseMessage response = getResponseMessage(jsonObject);
			if (response.responseCode != "0")
			{
				throw new MediumsException(response);
			}

			var data = jsonObject["data"].Value<JObject>();
			BillerData billerResp = new BillerData();
			billerResp.billerId = data["id"].Value<int?>();
			billerResp.billerCode = toString(data["billerCode"].Value<string>());
			billerResp.description = toString(data["description"].Value<string>());
			billerResp.acctNo = toString(data["acctNo"].Value<string>());
			billerResp.endpointUrl = toString(data["endpointUrl"].Value<string>());
			billerResp.vendorCode = toString(data["vendorCode"].Value<string>());
			billerResp.vendorPassword = toString(data["vendorPassword"].Value<string>());
			billerResp.smsTemplate = toString(data["smsTemplate"].Value<string>());
			billerResp.status = toString(data["status"].Value<string>());
			billerResp.response = MESSAGES.getSuccessMessage();

            if (billerResp.status.Trim().ToUpper() != "ACTIVE")
                throw new MediumsException(new ResponseMessage("-99", "Invalid biller status in MCP"));

            return billerResp;
		}


		public List<BillerProductCategory> findProductCategoryByBillerCode(BillerProduct request)
		{
			Dictionary<string, object> requestData = getBaseRequest(new OutletAuthentication());
			string methodName = "/biller/findProductCategoryByBillerCode";
			string httpMethod = "POST";
			requestData.Add("billerCode", request.billerCode);
			string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
			ResponseMessage response = getResponseMessage(jsonObject);
			if (response.responseCode != "0")
			{
				throw new MediumsException(response);
			}

			var accountList = jsonObject["data"].Children();
			List<BillerProductCategory> accounts = new List<BillerProductCategory>();
			BillerProductCategory item;
			foreach (var account in accountList)
			{
				item = new BillerProductCategory();
				item.description = account["description"].Value<string>();
				item.billerProductCategoryId = account["billerProductCategoryId"].Value<int?>();
				item.status = account["status"].Value<string>();
				accounts.Add(item);
			}
			return accounts;
		}



		public List<BillerProduct> findBillerProductsByBiller(BillerProduct request)
		{
			Dictionary<string, object> requestData = getBaseRequest(new OutletAuthentication());
			string methodName = "/biller/findMobileBillerProductsByBiller";
			string httpMethod = "POST";
			requestData.Add("billerCode", request.billerCode);
			requestData.Add("billerProdCatId", request.billerProdCatId);
			if(request.channelSource != null)
                requestData.Add("channelSource", request.channelSource); 
            string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
			ResponseMessage response = getResponseMessage(jsonObject);
			if (response.responseCode != "0")
			{
				throw new MediumsException(response);
			}

			var accountList = jsonObject["data"].Children();
			List<BillerProduct> accounts = new List<BillerProduct>();
			BillerProduct item;
			String status;
			foreach (var product in accountList)
			{
				status = product["status"].Value<string>();
				if (status != "Active")
					continue;

				item = new BillerProduct();
                item.description2 = product["description2"].Value<string>();
                item.description = product["description"].Value<string>();
				item.billerProdCode = product["billerProdCode"].Value<string>();
				item.billerProductId = product["billerProductId"].Value<int?>();
				item.amount = product["amount"].Value<double?>();
				accounts.Add(item);
			}
			return accounts;
		}


		public BillerProduct findBillerProductsByBillerByProductId(int billerProdId)
		{
			Dictionary<string, object> requestData = getBaseRequest(new OutletAuthentication());
			string methodName = "/biller/findBillerProductsByBiller";
			string httpMethod = "POST";
			requestData.Add("billerProductId", billerProdId);
			string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
			ResponseMessage response = getResponseMessage(jsonObject);
			if (response.responseCode != "0")
			{
				throw new MediumsException(response);
			}

			var accountList = jsonObject["data"].Children();
			BillerProduct item = new BillerProduct();
			foreach (var account in accountList)
			{

				item.description = account["description"].Value<string>();
				item.billerProdCode = account["billerProdCode"].Value<string>();
				item.billerProductId = account["billerProductId"].Value<int?>();
				item.amount = account["amount"].Value<double?>();
			}
			return item;
		}


		public CICustomerResp accountResponseByPhoneNo(OutletAuthentication request)
		{
			jsonSerializer = new JavaScriptSerializer();
			Dictionary<string, object> requestData = getBaseRequest(request);
			string methodName = "/agent-banking/findAccountsByPhoneNo";
			string httpMethod = "POST";
			string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
			ResponseMessage message = getResponseMessage(jsonObject);
			if (message.responseCode != "0")
			{
				throw new MediumsException(message);
			}
			var data = jsonObject["data"].Value<JObject>();
			CICustomerResp response = new CICustomerResp();
			response.response = MESSAGES.getSuccessMessage();
			response.customerName = data["customerName"].Value<string>();
			response.effectiveDate = data["effectiveDate"].Value<string>();
			response.phoneNo = data["phoneNo"].Value<string>();
			response.status = data["status"].Value<string>();
			response.rimNo = data["rimNo"].Value<string>();
			response.entityType = data["entityType"].Value<string>();
			response.deviceID = data["deviceID"].Value<string>();
			response.outletCode = data["outletCode"].Value<string>();
			response.lockedFlag = data["lockedFlag"].Value<Boolean>();
			response.pinChangeFlag = data["pinChangeFlag"].Value<Boolean>();

			var accountList = data["accountList"].Children();
			List<Account> accounts = new List<Account>();
			Account item;
			foreach (var account in accountList)
			{
				item = new Account();
				item.acctNo = account["acctNo"].Value<string>();
				item.acctType = account["acctType"].Value<string>();
				item.description = account["description"].Value<string>();
				accounts.Add(item);
			}
			response.accountList = accounts;
			return response;
		}
		public CIAccountResponse accountResponseByAccountNo(AccountRequest request)
		{
			jsonSerializer = new JavaScriptSerializer();
			Dictionary<string, object> requestData = new Dictionary<string, object>();
			string methodName = "/agent-banking/fundsTransfer";
			string httpMethod = "POST";
			//requestData.Add("destAcctNo", request.crAcctNo);
			//requestData.Add("transAmt", request.transAmt);
			//requestData.Add("currency", request.currency);
			//requestData.Add("sourceAcctNo", request.drAcctNo);
			//requestData.Add("description", request.description);
			//requestData.Add("serviceCode", request.tranCode);
			//requestData.Add("externalReference", request.externalTransId);
			//requestData.Add("depositorPhoneNo", request.depositorPhoneNo);
			//requestData.Add("depositorName", request.depositorName);

			string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			MediumTransResp responseData = JsonConvert.DeserializeObject<MediumTransResp>(stringResp);
			CIAccountResponse txnResp = new CIAccountResponse();

			ResponseMessage response = new ResponseMessage
			{
				responseCode = responseData.code,
				responseMessage = responseData.message
			};
			if (response.responseCode != "0")
			{
				throw new MediumsException(response);
			}
			txnResp.response = MESSAGES.getSuccessMessage();
			return txnResp;
		}
		public CIChargeResponse findTransactionCharge(CIChargeRequest request)
		{
			jsonSerializer = new JavaScriptSerializer();
			Dictionary<string, object> requestData = getBaseRequest(request.authRequest);
			string methodName = "/agent-banking/findTransCharges";
			string httpMethod = "POST";
			requestData.Add("transAmt", request.amount);
			requestData.Add("serviceCode", request.transCode);
			string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
			ResponseMessage message = getResponseMessage(jsonObject);
			if (message.responseCode != "0")
			{
				throw new MediumsException(message);
			}
			var data = jsonObject["data"].Value<JObject>();
			CIChargeResponse response = new CIChargeResponse();
			response.response = MESSAGES.getSuccessMessage();
			response.charge = data["totalCharge"].Value<double?>();
			response.commission = data["agentCommissionAmount"].Value<double?>();
			response.wht = data["withholdTax"].Value<double?>();
			response.exciseDuty = data["exciseDuty"].Value<double?>();
			response.totalCharge = data["totalCharge"].Value<double?>();
			response.response = MESSAGES.getSuccessMessage();
			return response;
		}

		public ResponseMessage changePIN(CIPINChangeRequest request)
		{
			jsonSerializer = new JavaScriptSerializer();
			Dictionary<string, object> requestData = getBaseRequest(request.authRequest);
			string methodName = "/agent-banking/pinChange";
			string httpMethod = "POST";

			requestData["pinNo"] = ENCRYPTER.encryptMCPValue(request.oldPin);
			requestData.Add("newPin", ENCRYPTER.encryptMCPValue(request.newPin));
			requestData.Add("confirmPin", ENCRYPTER.encryptMCPValue(request.confirmPin));
			string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
			ResponseMessage message = getResponseMessage(jsonObject);
			if (message.responseCode != "0")
			{
				throw new MediumsException(message);
			}
			return MESSAGES.getSuccessMessage();
		}
		public CIOutletResponse findOutletDetails(OutletAuthentication request)
		{
			jsonSerializer = new JavaScriptSerializer();
			Dictionary<string, object> requestData = getBaseRequest(request);
			string methodName = "/agent-banking/findOutletDetails";
			string httpMethod = "POST";
			string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
			ResponseMessage message = getResponseMessage(jsonObject);
			if (message.responseCode != "0")
				throw new MediumsException(message);

			var data = jsonObject["data"].Value<JObject>();
			CIOutletResponse response = new CIOutletResponse();
			response.response = MESSAGES.getSuccessMessage();
			response.outletAccount = toString(data["acctNo"].Value<string>());
			response.outletName = toString(data["outletName"].Value<string>());
			response.parentAgent = toString(data["outletName"].Value<string>());
			response.outletPhone = toString(data["outletPhone"].Value<string>());
			response.response = MESSAGES.getSuccessMessage();
			return response;
		}
		public CIOutletResponse findSuperAgentDetails(OutletAuthentication request)
		{
			jsonSerializer = new JavaScriptSerializer();
			Dictionary<string, object> requestData = getBaseRequest(request);
			string methodName = "/agent-banking/findSuperAgentDetails";
			string httpMethod = "POST";
			string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
			ResponseMessage message = getResponseMessage(jsonObject);
			if (message.responseCode != "0")
				throw new MediumsException(message);

			var data = jsonObject["data"].Value<JObject>();
			CIOutletResponse response = new CIOutletResponse();
			response.response = MESSAGES.getSuccessMessage();
			response.outletAccount = toString(data["acctNo"].Value<string>());
			response.outletName = toString(data["outletName"].Value<string>());
			response.parentAgent = toString(data["outletName"].Value<string>());
			response.outletPhone = toString(data["outletPhone"].Value<string>());
			response.response = MESSAGES.getSuccessMessage();
			return response;
		}

		public MessageRespData findMessages(OutletAuthentication request)
		{
			jsonSerializer = new JavaScriptSerializer();
			Dictionary<string, object> requestData = getBaseRequest(request);
			string methodName = "/agent-banking/findRecipientSMS";
			string httpMethod = "POST";

			string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
			ResponseMessage message = getResponseMessage(jsonObject);
			if (message.responseCode != "0")
				throw new MediumsException(message);

			var data = jsonObject["data"].Children();
			MessageRespData response = new MessageRespData();
			response.response = MESSAGES.getSuccessMessage();
			List<CIMessageResp> accounts = new List<CIMessageResp>();
			CIMessageResp item;
			foreach (var account in data)
			{
				item = new CIMessageResp();
				item.smsText = account["messageText"].Value<string>();
				item.createDate = account["timeGenerated"].Value<string>();
				accounts.Add(item);
			}
			response.data = accounts;
			return response;
		}

		public CIVoucherResponse voucherPurchase(CIVoucherRequest request)
		{
			jsonSerializer = new JavaScriptSerializer();
			Dictionary<string, object> requestData = getBaseRequest(request.authRequest);
			string methodName = "/agent-banking/buyVoucher";
			string httpMethod = "POST";
			requestData.Add("transAmt", request.transAmt);
			requestData.Add("currency", request.currency);
			requestData.Add("sourceAcct", request.sourceAcct);
			requestData.Add("description", request.description);
			requestData.Add("sourcePhoneNo", request.sourcePhoneNo);
			requestData.Add("recipientPhone", request.receipientPhone);
			requestData.Add("voucherNo", request.voucherNo);
			requestData.Add("outletAcctNo", request.outletAcctNo);
			string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
			ResponseMessage response = getResponseMessage(jsonObject);
			if (response.responseCode != "0")
			{
				throw new MediumsException(response);
			}
			var data = jsonObject["data"].Value<JObject>();
			CIVoucherResponse txnResp = new CIVoucherResponse();
			txnResp.response = MESSAGES.getSuccessMessage();
			txnResp.transId = data["buyTransId"].Value<string>();
			txnResp.voucherNo = data["voucherCode"].Value<string>();
			txnResp.expiryDate = data["expiryDate"].Value<string>();
			txnResp.status = data["status"].Value<string>();
			txnResp.transAmount = data["amount"].Value<string>();
			var transData = data["transRespData"].Value<JObject>();

			TxnResp txnRespData = new TxnResp();
			txnResp.transId = transData["transId"].Value<string>();
			txnResp.chargeAmt = transData["chargeAmt"].Value<string>();
			var accountList = transData["transDetails"].Children();
			List<ChildTrans> accounts = new List<ChildTrans>();
			ChildTrans item;
			foreach (var account in accountList)
			{
				item = new ChildTrans();
				item.destAcctNo = account["destAcctNo"].Value<string>();
				item.transAmt = account["transAmt"].Value<double>();
				item.sourceAcctNo = account["sourceAcctNo"].Value<string>();
				item.description = account["description"].Value<string>();
				item.amountType = account["amountType"].Value<string>();
				item.transType = account["transType"].Value<string>();
				accounts.Add(item);
			}
			txnRespData.transItems = accounts;
			txnResp.transItems = txnRespData;
			return txnResp;
		}


		public TxnResp voucherRedeem(CIVoucherRequest request)
		{
			jsonSerializer = new JavaScriptSerializer();
			Dictionary<string, object> requestData = getBaseRequest(request.authRequest);
			string methodName = "/agent-banking/redeemVoucher";
			string httpMethod = "POST";
			requestData.Add("transAmt", request.transAmt);
			requestData.Add("currency", request.currency);
			requestData.Add("sourceAcct", request.sourceAcct);
			requestData.Add("description", request.description);
			requestData.Add("sourcePhoneNo", request.description);
			requestData.Add("recipientPhone", request.receipientPhone);
			requestData.Add("voucherNo", request.voucherNo);
			requestData.Add("outletAcctNo", request.outletAcctNo);
			string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
			ResponseMessage response = getResponseMessage(jsonObject);
			if (response.responseCode != "0")
				throw new MediumsException(response);

			var data = jsonObject["data"].Value<JObject>();
			TxnResp txnResp = new TxnResp();
			txnResp.response = MESSAGES.getSuccessMessage();
			txnResp.transId = data["transId"].Value<string>();

			var accountList = data["transDetails"].Children();
			List<ChildTrans> accounts = new List<ChildTrans>();
			ChildTrans item;
			foreach (var account in accountList)
			{
				item = new ChildTrans();
				item.destAcctNo = account["destAcctNo"].Value<string>();
				item.transAmt = account["transAmt"].Value<double>();
				item.sourceAcctNo = account["sourceAcctNo"].Value<string>();
				item.description = toString(account["description"].Value<string>());
				item.amountType = account["amountType"].Value<string>();
				item.transType = account["transType"].Value<string>();
				accounts.Add(item);
			}
			txnResp.transItems = accounts;
			return txnResp;
		}
		public CIVoucherResponse findVoucherDetails(CIVoucherRequest request)
		{
			jsonSerializer = new JavaScriptSerializer();
			Dictionary<string, object> requestData = getBaseRequest(request.authRequest);
			string methodName = "/agent-banking/validateVoucher";
			string httpMethod = "POST";
			requestData.Add("transAmt", request.transAmt);
			requestData.Add("currency", request.currency);
			requestData.Add("sourceAcct", request.sourceAcct);
			requestData.Add("description", request.description);
			requestData.Add("sourcePhoneNo", request.description);
			requestData.Add("recipientPhone", request.receipientPhone);
			requestData.Add("voucherNo", request.voucherNo);
			requestData.Add("outletAcctNo", request.outletAcctNo);
			string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
			ResponseMessage message = getResponseMessage(jsonObject);
			if (message.responseCode != "0")
				throw new MediumsException(message);

			var data = jsonObject["data"].Value<JObject>();
			CIVoucherResponse response = new CIVoucherResponse();
			response.response = MESSAGES.getSuccessMessage();
			response.voucherNo = data["voucherNo"].Value<string>();
			response.transDate = data["transDate"].Value<string>();
			response.expiryDate = data["expiryDate"].Value<string>();
			response.status = data["status"].Value<string>();
			response.sourcePhoneNo = data["sourcePhoneNo"].Value<string>();
			response.receipientPhoneNo = data["recipientPhoneNo"].Value<string>();
			response.transAmount = data["transAmount"].Value<string>();
			response.description = data["description"].Value<string>();
			response.senderName = data["senderName"].Value<string>();
			response.response = MESSAGES.getSuccessMessage();
			return response;
		}
		public CIOTPResponse initiateCashWithdraw(CIOTPRequestData request)
		{
			jsonSerializer = new JavaScriptSerializer();
			Dictionary<string, object> requestData = getBaseRequest(request.authRequest);
			string methodName = "/agent-banking/initiateCustomerCashout";
			string httpMethod = "POST";
			requestData.Add("customerPhone", request.authRequest.phoneNo);
			requestData.Add("customerAccount", request.acctNo);
			requestData.Add("amount", request.amount);
			string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
			ResponseMessage message = getResponseMessage(jsonObject);
			if (message.responseCode != "0")
				throw new MediumsException(message);

			var data = jsonObject["data"].Value<JObject>();
			CIOTPResponse response = new CIOTPResponse();
			response.response = MESSAGES.getSuccessMessage();
			response.withdrawCode = data["otpCode"].Value<string>();
			response.expiryDate = data["expiryDate"].Value<string>();
			response.accountNo = data["accountNo"].Value<string>();
			response.phoneNo = data["phoneNo"].Value<string>();
			response.transAmount = data["amount"].Value<double?>();
			response.response = MESSAGES.getSuccessMessage();
			return response;
		}
		public CIOTPResponse initiateOutletCashWithdraw(CIOTPRequestData request)
		{
			jsonSerializer = new JavaScriptSerializer();
			Dictionary<string, object> requestData = getBaseRequest(request.authRequest);
			string methodName = "/agent-banking/initiateOutletCashout";
			string httpMethod = "POST";
			requestData.Add("customerPhone", request.authRequest.phoneNo);
			requestData.Add("customerAccount", request.acctNo);
			requestData.Add("amount", request.amount);
			requestData.Add("withdrawCode", request.otpCode);
			string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
			ResponseMessage message = getResponseMessage(jsonObject);
			if (message.responseCode != "0")
				throw new MediumsException(message);

			var data = jsonObject["data"].Value<JObject>();
			CIOTPResponse response = new CIOTPResponse();
			response.response = MESSAGES.getSuccessMessage();
			response.withdrawCode = data["otpCode"].Value<string>();
			response.expiryDate = data["expiryDate"].Value<string>();
			response.accountNo = data["accountNo"].Value<string>();
			response.phoneNo = data["phoneNo"].Value<string>();
			response.transAmount = data["amount"].Value<double?>();
			response.response = MESSAGES.getSuccessMessage();
			return response;
		}
		public CIOTPResponse customerWithdrawCodeInquiry(CIOTPRequestData request)
		{
			jsonSerializer = new JavaScriptSerializer();
			Dictionary<string, object> requestData = getBaseRequest(request.authRequest);
			string methodName = "/agent-banking/validateCustomerCashoutOTP";
			string httpMethod = "POST";
			requestData.Add("customerPhone", CONVERTER.formatPhoneNumber(request.customerPhone));
			requestData.Add("customerAccount", request.acctNo);
			requestData.Add("amount", request.amount);
			requestData.Add("withdrawCode", request.otpCode);
			string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
			ResponseMessage message = getResponseMessage(jsonObject);
			if (message.responseCode != "0")
				throw new MediumsException(message);

			var data = jsonObject["data"].Value<JObject>();
			CIOTPResponse response = new CIOTPResponse();
			response.response = MESSAGES.getSuccessMessage();
			response.withdrawCode = data["otpCode"].Value<string>();
			response.expiryDate = data["expiryDate"].Value<string>();
			response.accountNo = data["accountNo"].Value<string>();
			response.phoneNo = data["phoneNo"].Value<string>();
			response.transAmount = data["amount"].Value<double?>();
			response.customerName = data["customerName"].Value<string>();
			response.response = MESSAGES.getSuccessMessage();
			return response;
		}


		public CIOTPResponse outletWithdrawCodeInquiry(CIOTPRequestData request)
		{
			jsonSerializer = new JavaScriptSerializer();
			Dictionary<string, object> requestData = getBaseRequest(request.authRequest);
			string methodName = "/agent-banking/validateOutletCashoutOTP";
			string httpMethod = "POST";
			requestData.Add("withdrawOutletCode", request.withdrawOutletCode);
			requestData.Add("customerAccount", request.acctNo);
			requestData.Add("amount", request.amount);
			requestData.Add("withdrawCode", request.otpCode);
			string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
			ResponseMessage message = getResponseMessage(jsonObject);
			if (message.responseCode != "0")
				throw new MediumsException(message);

			var data = jsonObject["data"].Value<JObject>();
			CIOTPResponse response = new CIOTPResponse();
			response.response = MESSAGES.getSuccessMessage();
			response.withdrawCode = data["otpCode"].Value<string>();
			response.expiryDate = data["expiryDate"].Value<string>();
			response.accountNo = toString(data["accountNo"].Value<string>());
			response.phoneNo = toString(data["phoneNo"].Value<string>());
			response.transAmount = data["amount"].Value<double?>();
			response.customerName = toString(data["customerName"].Value<string>());
			response.response = MESSAGES.getSuccessMessage();
			return response;
		}

		public ResponseMessage pairCustomerDevice(OutletAuthentication request)
		{
			jsonSerializer = new JavaScriptSerializer();
			Dictionary<string, object> requestData = getBaseRequest(request);
			string methodName = "/agent-banking/pairCustomerDevice";
			string httpMethod = "POST";
			string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			return MESSAGES.getSuccessMessage();
		}

		public ResponseMessage sendSMS(string mobilePhone, string messageText, bool deliverSMS)
		{
			jsonSerializer = new JavaScriptSerializer();
			Dictionary<string, object> requestData = new Dictionary<string, object>();
			string methodName = "/agent-banking/sendSMS";
			string httpMethod = "POST";
			requestData.Add("messageText", messageText);
			requestData.Add("recipientNumber", mobilePhone);
			requestData.Add("deliverSMS", deliverSMS);
			string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			return MESSAGES.getSuccessMessage();
		}


        public ResponseMessage sendPaymentRequest(RequestPayment request)
        {
            jsonSerializer = new JavaScriptSerializer();
            Dictionary<string, object> requestData = getBaseRequest(request.authRequest);
            string methodName = "/agent-banking/requestPayment";
            string httpMethod = "POST"; 
            requestData.Add("fromPhone", request.fromPhone);
            requestData.Add("requesterPhone", request.requesterPhone);
            requestData.Add("requesterReason", request.requesterReason);
            requestData.Add("customerName", request.customerName);
            requestData.Add("amount", request.amount);
            string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
            return MESSAGES.getSuccessMessage();
        }		 
		 

        public TxnResp reviewPaymentRequest(RequestPayment request)
        {
            jsonSerializer = new JavaScriptSerializer();
            Dictionary<string, object> requestData = getBaseRequest(request.authRequest);
            string methodName = "/agent-banking/reviewPaymentRequest";
            string httpMethod = "POST";
            requestData.Add("fromPhone", request.fromPhone);
            requestData.Add("requestId", request.requestId);
            requestData.Add("action", request.action);
            string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);

			var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
			ResponseMessage response = getResponseMessage(jsonObject);
			if (response.responseCode != "0")
			{
				throw new MediumsException(response);
			}
			var data = jsonObject["data"].Value<JObject>();
			TxnResp txnResp = new TxnResp();
			txnResp.response = MESSAGES.getSuccessMessage();
			txnResp.transId = data["transId"].Value<string>();
			txnResp.chargeAmt = data["chargeAmt"].Value<double>();
			txnResp.drAcctBal = data["drAcctBalance"].Value<double?>();
			txnResp.crAcctBal = data["crAcctBalance"].Value<double?>();

			var accountList = data["transDetails"].Children();
			List<ChildTrans> accounts = new List<ChildTrans>();
			ChildTrans item;
			foreach (var account in accountList)
			{
				item = new ChildTrans();
				item.destAcctNo = account["destAcctNo"].Value<string>();
				item.transAmt = account["transAmt"].Value<double>();
				item.sourceAcctNo = account["sourceAcctNo"].Value<string>();
				item.description = account["description"].Value<string>();
				item.amountType = account["amountType"].Value<string>();
				item.transType = account["transType"].Value<string>();
				accounts.Add(item);
			}
			txnResp.transItems = accounts;
			return txnResp;
        }
		 
		 
        public RequestPaymentData findPendingRequest(RequestPayment request)
        {
            jsonSerializer = new JavaScriptSerializer();
            Dictionary<string, object> requestData = getBaseRequest(request.authRequest);
            string methodName = "/agent-banking/findPendingRequest";
            string httpMethod = "POST";
            requestData.Add("fromPhone", request.fromPhone);
            string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
            var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
            ResponseMessage message = getResponseMessage(jsonObject);
            if (message.responseCode != "0")
                throw new MediumsException(message); 

            var data = jsonObject["data"].Children();
            RequestPaymentData response = new RequestPaymentData();
            response.response = MESSAGES.getSuccessMessage();
            List<RequestPayment> accounts = new List<RequestPayment>();
            RequestPayment item;
            foreach (var account in data)
            {
                item = new RequestPayment
                {
                    requesterPhone = account["requesterPhone"].Value<string>(),
                    customerName = account["customerName"].Value<string>(),
                    createDate = account["createDate"].Value<string>(),
                    amount = account["amount"].Value<decimal?>(),
                    requesterReason = account["requesterReason"].Value<string>(),
                    requestId = account["requestId"].Value<int?>()
                };
                accounts.Add(item);
            }
            response.data = accounts;
            return response;
        }

		 
        public CIStatementResponse doAccountFullStatement(CIStatementRequest request)
        {
            jsonSerializer = new JavaScriptSerializer();
            Dictionary<string, object> requestData = getBaseRequest(request.authRequest);
            string methodName = "/agent-banking/doAccountFullStatement";
            string httpMethod = "POST";
            requestData.Add("accountNo", request.accountNo);
            requestData.Add("statementType", request.statementType);
            requestData.Add("entityType", request.entityType);
            requestData.Add("fromDate", request.fromDate);
            requestData.Add("toDate", request.toDate);
            string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
            var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
            ResponseMessage message = getResponseMessage(jsonObject);
            if (message.responseCode != "0")
                throw new MediumsException(message);

            var data = jsonObject["data"].Children();
            CIStatementResponse response = new CIStatementResponse();
            response.response = MESSAGES.getSuccessMessage();
            List<CIStatement> accounts = new List<CIStatement>();
            CIStatement item;
            foreach (var account in data)
            {
                item = new CIStatement
                {
                    effectiveDate = account["effectiveDate"].Value<string>(),
                    txnAmount = account["txnAmount"].Value<int?>(),
					closing = account["closing"].Value<decimal?>(),
                    description = account["description"].Value<string>()
                };
                accounts.Add(item);
            }
            response.data = accounts;
            return response;
        }
		 

        public CIAccountBalanceResponse doAccountBalance(AccountRequest request)
        {
            jsonSerializer = new JavaScriptSerializer();
            Dictionary<string, object> requestData = getBaseRequest(request.authRequest);
            string methodName = "/agent-banking/doAccountBalance";
            string httpMethod = "POST";
            requestData.Add("acctNo", request.accountNo);
            requestData.Add("entityType", request.entityType);
            string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
            var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
            ResponseMessage message = getResponseMessage(jsonObject);
            if (message.responseCode != "0")
                throw new MediumsException(message);

            var data = jsonObject["data"].Value<JObject>();
            CIAccountBalanceResponse response = new CIAccountBalanceResponse();
            response.response = MESSAGES.getSuccessMessage();
            response.accountTitle = data["accountTitle"].Value<string>();
            response.branchName = data["branchName"].Value<string>();
            response.availableBalance = toString(data["availableBalance"].Value<decimal?>());
            response.currency = toString(data["currency"].Value<string>());
            response.accountStatus = data["accountStatus"].Value<string>();
            response.customerNo = toString(data["customerNo"].Value<string>());
            response.response = MESSAGES.getSuccessMessage();
            return response;
        }


        public CIBillerData findBillerNotificationByReferenceNo(CIBillerData request)
		{
			jsonSerializer = new JavaScriptSerializer();
			CIBillerData response = new CIBillerData();
			Dictionary<string, object> requestData = new Dictionary<string, object>();
			try
			{
				string methodName = "/biller/findBillerNotificationByReferenceNo";
				string httpMethod = "POST";
				requestData.Add("billerCode", request.billerCode);
				requestData.Add("thirdPartyReference", request.thirdPartyReference);
				requestData.Add("channelCode", request.channelCode);
				string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
				var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
				ResponseMessage responseMessage = getResponseMessage(jsonObject);
				response.response = responseMessage;
				if (responseMessage.responseCode != "0")
				{
					response.response = responseMessage;
					return null;
				}
				var data = jsonObject["data"].Value<JObject>();
				response.id = data["id"].Value<int?>();
				response.amount = data["amount"].Value<double?>();
				response.isoCode = data["isoCode"].Value<string>();
				response.postedBy = data["postedBy"].Value<string>();
				response.billerCode = data["billerCode"].Value<string>();
				response.transDescr = data["transDescr"].Value<string>();
				response.status = data["status"].Value<string>();
				response.transId = data["transId"].Value<int?>();
				response.reversalFlag = data["reversalFlag"].Value<string>();
				response.reversalReason = data["reversalReason"].Value<string>();
				response.referenceNo = data["referenceNo"].Value<string>();
				response.initiatorPhone = data["initiatorPhone"].Value<string>();
				response.extenalTransRef = data["extenalTransRef"].Value<string>();
				response.thirdPartyReference = data["thirdPartyReference"].Value<string>();
				response.responseData = data["responseData"].Value<string>();
				response.requestData = data["requestData"].Value<string>();
				response.processingDuration = data["processingDuration"].Value<int?>();
				response.channelCode = data["channelCode"].Value<string>();
				return response;
			}
			catch (Exception e)
			{
				LOGGER.error(e.ToString());
				return null;
			}
		}

		public long? logBillNotification(TxnData request, string billerCode, string transId,
			string requestLog, string thirdPartyReference)
		{
			jsonSerializer = new JavaScriptSerializer();
			Dictionary<string, object> requestData = new Dictionary<string, object>();
			string methodName = "/agent-banking/logBillNotification";
			string httpMethod = "POST";
			requestData.Add("amount", request.transAmt);
			requestData.Add("isoCode", request.currency);
			//requestData.Add("transDate", DateTime.Now.ToString("dd/MM/yyyy HH:mm:ss"));
			requestData.Add("postedBy", request.authRequest.outletCode);
			requestData.Add("billerCode", billerCode);
			requestData.Add("transDescr", request.description);
			requestData.Add("status", "FAILED");
			requestData.Add("transId", transId);
			requestData.Add("referenceNo", request.referenceNo);
			requestData.Add("initiatorPhone", request.authRequest.phoneNo);
			requestData.Add("thirdPartyReference", thirdPartyReference);
			requestData.Add("channelCode", request.authRequest.channelCode);
			requestData.Add("requestData", requestLog);
			string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
			ResponseMessage response = getResponseMessage(jsonObject);
			if (response.responseCode != "0")
				throw new MediumsException(response);

			var data = jsonObject["data"].Value<JObject>();
			return data["id"].Value<long?>();
		}


		public void updateBillNotificationStatus(long? notificationId, string status, string failureReason, string externalRefId, int processingDuration,
			string responseData)
		{
			jsonSerializer = new JavaScriptSerializer();
			Dictionary<string, object> requestData = new Dictionary<string, object>();
			try
			{
				string methodName = "/agent-banking/updateBillNotificationStatus";
				string httpMethod = "POST";
				requestData.Add("id", notificationId);
				requestData.Add("status", status);
				requestData.Add("reversalReason", failureReason);
				requestData.Add("extenalTransRef", externalRefId);
				requestData.Add("responseData", responseData);
				requestData.Add("processingDuration", processingDuration);
				string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			}
			catch (Exception e)
			{
				LOGGER.error(e.ToString());
			}
		}


		public ResponseMessage saveReceipt(IssuedReceipt issuedReceipt)
		{
			jsonSerializer = new JavaScriptSerializer();
			Dictionary<string, object> requestData = new Dictionary<string, object>();
			try
			{
				string methodName = "/agent-banking/saveReceipt";
				string httpMethod = "POST";
				requestData.Add("issuer", issuedReceipt.issuer);
				requestData.Add("outletCode", issuedReceipt.phoneNo);
				requestData.Add("receiptData", issuedReceipt.receiptData);
				requestData.Add("txnId", issuedReceipt.txnId);
				string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			}
			catch (Exception e)
			{
				LOGGER.error(e.ToString());
			}
			return MESSAGES.getSuccessMessage();
		}


		public bool isNumeric(string str)
		{
			return double.TryParse(str, out _);
		}


		public IssuedReceiptData findIssuedReceipt(IssuedReceipt request)
		{
			if (request.txnId != null)
				if (request.txnId.Trim() == "")
					request.txnId = null;
				else if (!isNumeric(request.txnId))
					throw new MediumsException(new ResponseMessage("-99", "Transaction ID supplied must be a number"));

			OutletAuthentication authentication = request.authRequest;

			jsonSerializer = new JavaScriptSerializer();
			Dictionary<string, object> requestData = new Dictionary<string, object>();
			string methodName = "/agent-banking/findIssuedReceipt";
			string httpMethod = "POST";
			requestData.Add("outletCode", authentication.phoneNo);
			requestData.Add("txnId", request.txnId);
			requestData.Add("fromDate", request.fromDate == null ? null : (Object)request.fromDate.Value.ToString("yyyy-MM-dd"));
			requestData.Add("toDate", request.toDate == null ? null : (Object)request.toDate.Value.ToString("yyyy-MM-dd"));
			string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
			var jsonObject = (JObject)JsonConvert.DeserializeObject(stringResp);
			ResponseMessage message = getResponseMessage(jsonObject);
			if (message.responseCode != "0")
				throw new MediumsException(message);

			var data = jsonObject["data"].Children();
			IssuedReceiptData response = new IssuedReceiptData();
			response.response = MESSAGES.getSuccessMessage();
			List<IssuedReceipt> accounts = new List<IssuedReceipt>();
			IssuedReceipt item;
			foreach (var account in data)
			{
				item = new IssuedReceipt();
				item.receiptId = account["receiptId"].Value<int?>();
				item.receiptNumber = account["receiptNumber"].Value<string>();
				item.dateCreated = account["dateCreated"].Value<string>();
				item.receiptData = account["receiptData"].Value<string>();
				accounts.Add(item);
			}
			response.data = accounts;
			return response;
		}
	}
}