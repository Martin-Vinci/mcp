using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Text;
using System.Xml;
using micropay_apis.Models;
using micropay_apis.Utils;
using Nancy.Json;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace micropay_apis.Remote
{
	public class MTNServices
	{
		MediumsService mediumsService;
		DateTime start; //Start time
		DateTime end;   //End time
		TimeSpan timeDifference; //Time span between start and end = the time span needed to execute your method
		int difference_Miliseconds;
		public MTNServices()
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

			ResponseMessage response = new ResponseMessage();
			response.responseCode = responseCode;
			response.responseMessage = responseMessage + " at MTN";
			return response;
		}

		private JavaScriptSerializer jsonSerializer;
		private string sendHTTPostRequest(string httpMethod, string serviceURL, string jsonString, string contentType)
		{
			string response = "";	
			LOGGER.info("===================== MTN CompleteURL: " + serviceURL);
			LOGGER.info("===================== MTN Request => " + jsonString);
			Uri url = new Uri(serviceURL);
			HttpWebRequest httpRequest = (HttpWebRequest)WebRequest.Create(url);
			//NetworkCredential myNetworkCredential = new NetworkCredential("admin", "admin");
			//CredentialCache myCredentialCache = new CredentialCache();
			//myCredentialCache.Add(url, "Basic", myNetworkCredential);
			httpRequest.Headers.Add("x-api-key", "P1CUqKGXN2kCrT0EpNsYcUnv0kMuvbEr");
			httpRequest.Headers.Add("transactionId", DateTime.Now.ToString("yyyyMMddHHmmssfff"));
			httpRequest.PreAuthenticate = true;
			//httpRequest.Credentials = myCredentialCache;

			httpRequest.Accept = contentType;
			httpRequest.ContentType = contentType;
			httpRequest.Method = httpMethod;
			if (jsonString != null)
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
			LOGGER.info("===================== MTN Response => " + response);
			return response;
		}


		private string sendHttpRequest(string httpMethod, string serviceURL, string requestData)
		{
			string response = "";
			LOGGER.info("===================== MTN Airtime CompleteURL: " + serviceURL);
			LOGGER.info("===================== MTN Airtime Request => " + requestData);


			Uri url = new Uri(serviceURL);
			HttpWebRequest httpRequest = (HttpWebRequest)WebRequest.Create(url);
			NetworkCredential myNetworkCredential = new NetworkCredential("admin", "admin");
			CredentialCache myCredentialCache = new CredentialCache();
			myCredentialCache.Add(url, "Basic", myNetworkCredential);
			httpRequest.PreAuthenticate = true;
			httpRequest.Credentials = myCredentialCache;

			httpRequest.Accept = "application/xml";
			httpRequest.ContentType = "application/xml";
			httpRequest.Method = httpMethod;
			if (requestData != null)
			{
				byte[] bytes = Encoding.UTF8.GetBytes(requestData);
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
			LOGGER.info("===================== MTN Airtime Response => " + response);
			return response;
		}


		public string removeCountryCode(string phoneNo)
		{
			List<string> countries_list = new List<string>
			{
				"+256",
				"256",
				"0"
			};
			foreach (var country in countries_list)
			{
				phoneNo = phoneNo.Replace(country, "");
			}
			return phoneNo;
		}

		string getXMLNode(XmlDocument requestXML, string path) {
			try
			{
				return requestXML.SelectSingleNode(path).InnerText;
			}
			catch (Exception e) {
				LOGGER.error(e.ToString());
				return null;
			}
		}




		private MTNTopupResponse getXMLResponse(XmlDocument requestXML)
		{
			MTNTopupResponse response = new MTNTopupResponse();
			string statusCode = getXMLNode(requestXML, "//agiml/response/resultcode");
			string message = getXMLNode(requestXML, "//agiml/response/resultdescription");
			if (statusCode == "0")
			{
				response.responseStatus = new ResponseMessage("0", "success");
				response.resultcode = statusCode;
				response.value = getXMLNode(requestXML, "//agiml/response/value");
				response.taxvalue = getXMLNode(requestXML, "//agiml/response/taxvalue");
				response.timestamp = getXMLNode(requestXML, "//agiml/response/timestamp");
				response.account = getXMLNode(requestXML, "//agiml/response/account");
				response.transno = getXMLNode(requestXML, "//agiml/response/transno");
				response.expirydate = getXMLNode(requestXML, "//agiml/response/expirydate");
				response.agentid = getXMLNode(requestXML, "//agiml/response/agentid");
				response.agenttransno = getXMLNode(requestXML, "//agiml/response/agenttransno");
				response.resultdescription = getXMLNode(requestXML, "//agiml/response/resultdescription");
				response.product_description = getXMLNode(requestXML, "//agiml/response/product_description");
				response.topupvalue = getXMLNode(requestXML, "//agiml/response/topupvalue");
				response.accountvalue = getXMLNode(requestXML, "//agiml/response/accountvalue");
				response.walletbalance = getXMLNode(requestXML, "//agiml/response/walletbalance");
			}
			else
			{
				response.responseStatus = new ResponseMessage(statusCode, message);
				response.value = getXMLNode(requestXML, "//agiml/response/value");
				response.timestamp = getXMLNode(requestXML, "//agiml/response/timestamp");
				response.account = getXMLNode(requestXML, "//agiml/response/account");
				response.transno = getXMLNode(requestXML, "//agiml/response/transno");
				response.agenttransno = getXMLNode(requestXML, "//agiml/response/agenttransno");
				response.resultdescription = getXMLNode(requestXML, "//agiml/response/resultdescription");
			}
			return response;
		}


		public MTNTopupResponse mtnAirtimeRecharge(MTNTopupRequest request, TxnData txnData, string mcpTransId)
		{
			mediumsService = new MediumsService();
			string httpMethod = "POST";
			try 
			{
				BillerData billerData = mediumsService.findBillersByCode("MTN_AIRTIME");
				string fullURL = PROPERTIES.MTNAirtimeURL;

				string xmlRequestdata = @"<?xml version=""1.0""?>"
					 + "<agiml>"
					 + "<header>"
					 + "<interface>TOPUP</interface>"
					 + "</header>"
					 + "<process>"
					 + "<agentcode>" + billerData.acctNo + "</agentcode>"
					 + "<authkey>" + billerData.vendorCode + "</authkey>"
					 + "<retry>" + request.retry + "</retry>"
					 + "<agenttransno>" + request.txnRef + "</agenttransno>"
					 + "<agenttimestamp>" + DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss") + "</agenttimestamp>"
					 + "<account>" + CONVERTER.formatPhoneNumber(request.mobilePhone) + "</account>"
					 + "<productcode>MOBTOPUP</productcode>"
					 + "<value>" + request.amount + "</value>"
					 + "<terminalid>PaulUGTest</terminalid>"
					 + "<comments>" + request.comments + "</comments>"
					 + "</process>"
					 + "</agiml>";

				// Notify Biller Utilities
				long? notifId = mediumsService.logBillNotification(txnData, "MTN_AIRTIME", mcpTransId, xmlRequestdata, request.txnRef);
				start = DateTime.Now; //Start time
				string stringResp = sendHttpRequest(httpMethod, fullURL, xmlRequestdata);
				end = DateTime.Now;   //End time
				timeDifference = end - start;
				difference_Miliseconds = (int)timeDifference.TotalMilliseconds;
				XmlDocument respXML = new XmlDocument();
				respXML.LoadXml(stringResp);
				MTNTopupResponse mtnTopUpResponse = getXMLResponse(respXML);
				if (notifId != null)
				{
					string status = mtnTopUpResponse.resultcode == "0" ? "NOTIFIED" : "FAILED";
					mediumsService.updateBillNotificationStatus(notifId, status, mtnTopUpResponse.resultdescription, mtnTopUpResponse.transno, difference_Miliseconds, stringResp);

					ResponseMessage responseMessage = mtnTopUpResponse.responseStatus;
					if (responseMessage.responseCode == "14")
						responseMessage.responseMessage = "Airtime purchase failed at MTN. Transaction will be reversed";

					mtnTopUpResponse.responseStatus = responseMessage;
				}	
				return mtnTopUpResponse;
			}
			catch (MediumsException ex)
			{
				LOGGER.error(ex.ToString());
				MTNTopupResponse response = new MTNTopupResponse();
				response.responseStatus = ex.getErrorMessage();
				return response;
			}
			catch (Exception e)
			{
				LOGGER.error(e.ToString());
				MTNTopupResponse response = new MTNTopupResponse();
				response.responseStatus = new ResponseMessage("-99", e.Message + " during MTN airtime purchase");
				return response;
			}
		} 
		  
		public MTNTopupResponse checkAirtimeStatus(MTNTopupRequest request)
		{
			mediumsService = new MediumsService();
			MTNTopupResponse response = new MTNTopupResponse();
			try
			{
				CIBillerData billerRequest = new CIBillerData();
				billerRequest.thirdPartyReference = request.txnRef;
				billerRequest.channelCode = request.sourceCode;
				billerRequest.billerCode = "MTN_AIRTIME";
				CIBillerData billerData = mediumsService.findBillerNotificationByReferenceNo(billerRequest);
				if (billerData.response.responseCode != "0")
				{
					response.responseStatus = billerData.response;
					return response;
				}
				XmlDocument respXML = new XmlDocument();
				respXML.LoadXml(billerData.responseData);
				response = getXMLResponse(respXML);

				ResponseMessage responseMessage = response.responseStatus;
				if (responseMessage.responseCode == "14")
					responseMessage.responseMessage = "Airtime purchase failed at MTN. Transaction will be reversed";

				return response;
			}
			catch (Exception e)
			{
				LOGGER.error(e.ToString());
				response = new MTNTopupResponse();
				response.responseStatus = new ResponseMessage("-99", e.Message + " during Airtel airtime purchase");
				return response;
			}
		}


		public MTNDataTopupResponse mtnDataRecharge(MTNDataTopupRequest request, TxnData txnData, string mcpTransId)
		{
			mediumsService = new MediumsService();
			jsonSerializer = new JavaScriptSerializer();
			Dictionary<string, object> requestData = new Dictionary<string, object>();
			MTNDataTopupResponse response;
			try
			{
				BillerData billerData = mediumsService.findBillersByCode(request.billerCode);
				string fullURL = PROPERTIES.MTNDataURL + "/v2/customers/" + billerData.acctNo + "/subscriptions";
				string httpMethod = "POST";
				string contentType = "application/json";
				requestData.Add("customerId", billerData.acctNo);
				requestData.Add("subscriptionId", request.subscriptionId);
				requestData.Add("subscriptionProviderId", "CIS");
				requestData.Add("subscriptionName", request.subscriptionName);
				requestData.Add("registrationChannel", "MicroPay_MTNdata");
				requestData.Add("subscriptionPaymentSource", "EVDS");
				requestData.Add("sendSMSNotification", false);
				requestData.Add("beneficiaryId", CONVERTER.formatPhoneNumber(request.phoneNumber));
				string jsonString = JsonConvert.SerializeObject(requestData, Newtonsoft.Json.Formatting.Indented);
				// Notify Biller Utilities
				long? notifId = mediumsService.logBillNotification(txnData, request.billerCode, mcpTransId, jsonString, request.txnRef);
				start = DateTime.Now; //Start time
				string stringResp = sendHTTPostRequest(httpMethod, fullURL, jsonString, contentType);
				end = DateTime.Now;   //End time
				timeDifference = end - start;
				difference_Miliseconds = (int)timeDifference.TotalMilliseconds;
				response = JsonConvert.DeserializeObject<MTNDataTopupResponse>(stringResp);
				String notificationStatus;
				if (response.statusCode == "0000")
				{
					notificationStatus = "NOTIFIED";
					response.responseStatus = new ResponseMessage("0", "success");
				}
				else
				{
					notificationStatus = "FAILED";
					response.responseStatus = new ResponseMessage(response.statusCode, response.statusDescription);
				}
				if (notifId != null)
				{
					mediumsService.updateBillNotificationStatus(notifId, notificationStatus, response.statusDescription, response.correlationId, difference_Miliseconds, stringResp);
				}
				return response;
			}
			catch (MediumsException ex)
			{
				LOGGER.error(ex.ToString());
				response = new MTNDataTopupResponse();
				response.responseStatus = ex.getErrorMessage();
				return response;
			}
			catch (Exception e)
			{
				LOGGER.error(e.ToString());
				response = new MTNDataTopupResponse();
				response.responseStatus = new ResponseMessage("-99", e.Message + " during MTN data purchase");
				return response;
			}
		}
		 
		 
		public MTNDataTopupResponse mtnDataCheckStatus(MTNDataTopupRequest request)
		{
			mediumsService = new MediumsService();
			MTNDataTopupResponse response = new MTNDataTopupResponse();
			try
			{
				CIBillerData billerRequest = new CIBillerData();
				billerRequest.thirdPartyReference = request.txnRef;
				billerRequest.channelCode = request.sourceCode;
				billerRequest.billerCode = "MTN_DATA";
				CIBillerData billerData = mediumsService.findBillerNotificationByReferenceNo(billerRequest);
				if (billerData.response.responseCode != "0")
				{
					response.responseStatus = billerData.response;
					return response;
				}
				response = JsonConvert.DeserializeObject<MTNDataTopupResponse>(billerData.responseData);
				response.responseStatus = new ResponseMessage("0", "success");
				return response;
			}
			catch (Exception e)
			{
				LOGGER.error(e.ToString());
				response = new MTNDataTopupResponse();
				response.responseStatus = new ResponseMessage("-99", e.Message + " during MTN data purchase");
				return response;
			}
		}



		public MTNDataBundleInquiryBundle dataBundleInquiry(string productCode, string phoneNo)
		{
			string httpMethod = "GET";
			string contentType = "application/json";
			string methodName = PROPERTIES.MTNDataURL + "/v3/products/" + productCode + "/" + phoneNo;
			string stringResp = sendHTTPostRequest(httpMethod, methodName, null, contentType);
			MTNDataBundleInquiryBundle response = JsonConvert.DeserializeObject<MTNDataBundleInquiryBundle>(stringResp);
			return response;
		}
	}
}