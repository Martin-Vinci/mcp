using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Text;
using System.Xml;
using Interswitch;
using micropay_apis.Models;
using micropay_apis.Remote;
using micropay_apis.Utils;
using Nancy.Json;
using Newtonsoft.Json;
 
namespace micropay_apis.Services
{
	public class InterswitchServiceOLD
	{
		MediumsService mediumsService;
		DateTime start; //Start time
		DateTime end;   //End time
		TimeSpan timeDifference; //Time span between start and end = the time span needed to execute your method
		int difference_Miliseconds;

		public InterswitchServiceOLD()
		{
			ServicePointManager.SecurityProtocol = SecurityProtocolType.Ssl3
					| SecurityProtocolType.Tls
					| SecurityProtocolType.Tls11
					| SecurityProtocolType.Tls12;
		}

		private ResponseMessage getResponseMessage(string responseCode, string responseMessage)
		{
			if (responseCode == "90000")
				return MESSAGES.getSuccessMessage();
			if (responseCode == "9000")
				return MESSAGES.getSuccessMessage();

			ResponseMessage response = new ResponseMessage();
			response.responseCode = responseCode;
			string message = responseMessage + " at Interswitch";
			response.responseMessage = message.ToUpper();
			return response;
		}
		JavaScriptSerializer jsonSerializer;
		private string sendHTTPostRequest(string httpMethod, string methodName, string payLoad, string additionalParameters)
		{
			string fullUrl = PROPERTIES.InterSwitchSVABaseURL;
			string response = null;
			
			fullUrl = String.Concat(fullUrl, methodName);
			LOGGER.info("===================== InterSwitch Complete URL: " + fullUrl);
			LOGGER.info("===================== InterSwitch Request => " + payLoad);
			string clientId = PROPERTIES.InterSwitchClientId;
			string clientSecret = PROPERTIES.InterSwitchClientSecret;
			Config authConfig = new Config(httpMethod, fullUrl, clientId, clientSecret, null, additionalParameters);
			Uri url = new Uri(fullUrl);
			HttpWebRequest httpRequest = (HttpWebRequest)WebRequest.Create(url);
			NetworkCredential myNetworkCredential = new NetworkCredential("admin", "admin");
			CredentialCache myCredentialCache = new CredentialCache();
			myCredentialCache.Add(url, "Basic", myNetworkCredential);
			httpRequest.PreAuthenticate = true;
			httpRequest.Credentials = myCredentialCache;

			httpRequest.Headers.Add("Authorization", authConfig.Authorization);
			httpRequest.Headers.Add("Timestamp", authConfig.TimeStamp);
			httpRequest.Headers.Add("Nonce", authConfig.Nonce);
			httpRequest.Headers.Add("SignatureMethod", "SHA512");
			httpRequest.Headers.Add("Signature", authConfig.Signature);
			httpRequest.Headers.Add("TerminalId", PROPERTIES.InterSwitchTerminalId);

			httpRequest.Accept = "application/json";
			httpRequest.ContentType = "application/json";
			httpRequest.Method = httpMethod;

			//LOGGER.info("================================== Interswitch Header Customer ID: " + requestData["customerId"]);
			LOGGER.info(httpRequest.Headers.ToString());
			//LOGGER.info("================================== Interswitch PayLoad Customer ID: " + requestData["customerId"]);

			if (httpMethod == "GET")
			{
				using (WebResponse getResponse = httpRequest.GetResponse())
				{
					var webStream = getResponse.GetResponseStream();
					var reader = new StreamReader(webStream);
					response = reader.ReadToEnd();
				}
			}
			else
			{
				byte[] bytes = Encoding.UTF8.GetBytes(payLoad);
				using (Stream stream = httpRequest.GetRequestStream())
				{
					stream.Write(bytes, 0, bytes.Length);
					stream.Close();
				}
				using (HttpWebResponse httpResponse = (HttpWebResponse)httpRequest.GetResponse())
				{
					using (Stream stream = httpResponse.GetResponseStream())
					{
						response = (new StreamReader(stream)).ReadToEnd();
					}
				}
			}
			LOGGER.info("===================== Interswitch Response => " + response);
			return response;
		}

		public TxnResp validateCustomer(CIBillValidationRequest request)
		{
			jsonSerializer = new JavaScriptSerializer();
			TxnResp response = new TxnResp();

			DateTime now = DateTime.UtcNow;
			string unixTimeMilliseconds = new DateTimeOffset(now).ToUnixTimeMilliseconds().ToString();

			string txnRef = PROPERTIES.InterSwitchRequestReferencePrefix + unixTimeMilliseconds.Substring(4);// commonService.generateReference().ToString().PadLeft(12, '0');
			Dictionary<string, object> requestData = new Dictionary<string, object>();
			string methodName = "/v1A/svapayments/validateCustomer";
			string httpMethod = "POST";
			requestData.Add("requestReference", txnRef);
			requestData.Add("customerId", request.referenceNo);
			requestData.Add("bankCbnCode", PROPERTIES.InterSwitchBankCBNCode);
			requestData.Add("amount", request.transAmt);
			requestData.Add("customerMobile", request.mobilePhone);
			requestData.Add("terminalId", PROPERTIES.InterSwitchTerminalId);
			requestData.Add("customerEmail", "xxxxxxxxx@gmail.com");
			requestData.Add("paymentCode", request.paymentCode);
			string payLoad = jsonSerializer.Serialize(requestData);
			string stringResp = sendHTTPostRequest(httpMethod, methodName, payLoad, null);
			InterSwitchInquiryResp responseData = JsonConvert.DeserializeObject<InterSwitchInquiryResp>(stringResp);

			ResponseMessage responseMessage = getResponseMessage(responseData.responseCode, responseData.responseMessage);
			response.response = responseMessage;
			response.requestId = txnRef;
			//response.utilityRef = responseData.transactionRef;
			//response.customerName = responseData.customerName;
			//response.chargeAmt = responseData.surcharge;
			return response;
		}

		public List<InterswitchPaymentItem> findPaymentItems(string billerId)
		{
			jsonSerializer = new JavaScriptSerializer();
			TxnResp response = new TxnResp();
			Dictionary<string, object> requestData = new Dictionary<string, object>();
			string methodName = "/v1/quickteller/billers/" + billerId + "/paymentitems";
			string httpMethod = "GET";
			string stringResp = sendHTTPostRequest(httpMethod, methodName, null, null);
			PaymentResponse objectList = JsonConvert.DeserializeObject<PaymentResponse>(stringResp);
			return objectList.paymentitems;
		}		 
		public string findBillers()
		{
			jsonSerializer = new JavaScriptSerializer();
			TxnResp response = new TxnResp();
			Dictionary<string, object> requestData = new Dictionary<string, object>();
			string methodName = "/v1/quickteller/billers";
			string httpMethod = "GET";
			string stringResp = sendHTTPostRequest(httpMethod, methodName, null, null);
			InterSwitchInquiryResp responseData = JsonConvert.DeserializeObject<InterSwitchInquiryResp>(stringResp);
			return stringResp;
		}

		public ExternalResponse sendAdviceRequest(TxnData request, string mcpTransId)
		{
			mediumsService = new MediumsService();
			jsonSerializer = new JavaScriptSerializer();
			ExternalResponse response = new ExternalResponse();
			Dictionary<string, object> requestData = new Dictionary<string, object>();
			try
			{
				if (request.billerCode == null)
					return new ExternalResponse("-99", "Invalid biller code specified during interswitch processing");

				string methodName = "/v1A/svapayments/sendAdviceRequest";
				string httpMethod = "POST";
				double amount = request.transAmt * 100;
				double surcharge = request.surCharge;
				string terminalId = PROPERTIES.InterSwitchTerminalId;
				string customerId = request.referenceNo;
				string bankCbnCode = PROPERTIES.InterSwitchBankCBNCode;
				string requestReference = request.requestId;// commonService.generateReference().ToString().PadLeft(12, '0');
				string customerMobile = request.customerPhoneNo;
				string transactionRef = request.billerTransRef;
				string customerEmail = "interswitch@gmail.com";
				string paymentCode = request.paymentCode;

				requestData.Add("amount", amount);
				requestData.Add("requestReference", requestReference);
				requestData.Add("surcharge", surcharge);
				requestData.Add("terminalId", terminalId);
				requestData.Add("customerId", customerId);
				requestData.Add("bankCbnCode", bankCbnCode);
				requestData.Add("customerMobile", customerMobile);
				requestData.Add("transactionRef", transactionRef);
				requestData.Add("customerEmail", customerEmail);
				requestData.Add("paymentCode", paymentCode);
				string additionalParameters = amount + terminalId + requestReference + customerId + paymentCode;
				string payLoad = jsonSerializer.Serialize(requestData);

				// Notify Biller Utilities
				long? notifId = mediumsService.logBillNotification(request, request.billerCode, mcpTransId, payLoad, mcpTransId);
				start = DateTime.Now; //Start time
				string jsonData = sendHTTPostRequest(httpMethod, methodName, payLoad, additionalParameters);
				InterSwitchAdviceResp responseData = JsonConvert.DeserializeObject<InterSwitchAdviceResp>(jsonData);
				end = DateTime.Now;   //End time
				timeDifference = end - start;
				difference_Miliseconds = (int)timeDifference.TotalMilliseconds;

				ResponseMessage responseMessage = getResponseMessage(responseData.responseCode, responseData.responseMessage);
				response = new ExternalResponse(responseMessage.responseCode, responseMessage.responseMessage);
				response.transRefNo = responseData.requestReference;
				if (notifId != null)
				{
					string status = responseMessage.responseCode == "0" ? "NOTIFIED" : "FAILED";
					mediumsService.updateBillNotificationStatus(notifId, status, responseMessage.responseMessage, responseData.transactionRef, difference_Miliseconds, jsonData);
				}
				return response;
			}
			catch (MediumsException e)
			{
				LOGGER.error(e.ToString());
				ResponseMessage responseMessage = e.getErrorMessage();
				response = new ExternalResponse("-99", responseMessage.responseMessage + " at Interswitch");
			}
			catch (Exception e)
			{
				LOGGER.error(e.ToString());
				response = new ExternalResponse("-99", e.Message + " at Interswitch");
			}
			return response;
		}
	}
}