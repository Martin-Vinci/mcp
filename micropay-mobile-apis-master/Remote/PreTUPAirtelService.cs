using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Text;
using System.Web;
using System.Xml;
using micropay_apis.Models;
using micropay_apis.Services;
using micropay_apis.Utils;
using Nancy.Json;
using Newtonsoft.Json;

namespace micropay_apis.Remote
{
	public class PreTUPAirtelService
	{
		MediumsService mediumsService;
		DateTime start; //Start time
		DateTime end;   //End time
		TimeSpan timeDifference; //Time span between start and end = the time span needed to execute your method
		int difference_Miliseconds;
		public PreTUPAirtelService()
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
			response.responseMessage = responseMessage + " at Interswitch";
			return response;
		}

		 
		JavaScriptSerializer jsonSerializer;
		private string sendHTTPostRequest(string httpMethod, string serviceURL, string requestData)
		{
			string response = "";
			LOGGER.info("===================== Airtel CompleteURL: " + serviceURL);
			LOGGER.info("===================== Airtel Request => " + requestData);
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
			LOGGER.info("===================== Airtel Response => " + response);
			return response;
		}


		private MobileTopUpResponse getPreTupResponse(XmlDocument requestXML)
		{
			MobileTopUpResponse response = new MobileTopUpResponse();
			string statusCode = CONVERTER.getXMLNode(requestXML, "//COMMAND/TXNSTATUS");
			string message = CONVERTER.getXMLNode(requestXML, "//COMMAND/MESSAGE") + " at Airtel";
			response.TXNSTATUS = statusCode;
			if (statusCode == "200")
			{
				response.responseStatus = new ResponseMessage("0", "success");			
				response.TYPE = CONVERTER.getXMLNode(requestXML, "//COMMAND/TYPE");
				response.TXNSTATUS = CONVERTER.getXMLNode(requestXML, "//COMMAND/TXNSTATUS");
				response.DATE = CONVERTER.getXMLNode(requestXML, "//COMMAND/DATE");
				response.EXTREFNUM = CONVERTER.getXMLNode(requestXML, "//COMMAND/EXTREFNUM");
				response.TXNID = CONVERTER.getXMLNode(requestXML, "//COMMAND/TXNID");
				response.MESSAGE = CONVERTER.getXMLNode(requestXML, "//COMMAND/MESSAGE");
			}
			else
			{
				response.responseStatus = new ResponseMessage(statusCode, message);
				response.TYPE = CONVERTER.getXMLNode(requestXML, "//COMMAND/TYPE");
				response.DATE = CONVERTER.getXMLNode(requestXML, "//COMMAND/DATE");
				response.EXTREFNUM = CONVERTER.getXMLNode(requestXML, "//COMMAND/EXTREFNUM");
				response.TXNID = CONVERTER.getXMLNode(requestXML, "//COMMAND/TXNID");
				response.MESSAGE = CONVERTER.getXMLNode(requestXML, "//COMMAND/MESSAGE");
			}
			return response;
		}

		 
		private string removeCountryCode(string phoneNo)
		{
			List<string> countries_list = new List<string>
			{
				"+256",
				"256"
			};
			foreach (var country in countries_list)
			{
				phoneNo = phoneNo.Replace(country, "");
			}
			phoneNo = phoneNo.TrimStart('0');
			return phoneNo;
		}
		 

		public MobileTopUpResponse airtelAirtimeRecharge(MobileTopUpRequest request, TxnData txnData, string mcpTransId)
		{
			mediumsService = new MediumsService();
			string httpMethod = "POST";
			try
			{
				BillerData billerData = mediumsService.findBillersByCode("AIRTEL_AIRTIME");
				string fullURL = PROPERTIES.PRETUPS_AIRTEL + "?REQUEST_GATEWAY_CODE=MCPY&REQUEST_GATEWAY_TYPE=EXTGW&LOGIN=pretups&PASSWORD=0971500a350af5c3d1c0b12221a0558c&SOURCE_TYPE=EXTGW&SERVICE_PORT=190";
				string xmlRequestdata = @"<?xml version=""1.0""?>"
				+ "<COMMAND>"
				+ "<TYPE>EXRCTRFREQ</TYPE>"
				+ "<DATE>" + DateTime.Now.ToString("dd/MM/yyyy HH:mm:ss") + "</DATE>"
				+ "<EXTNWCODE>UG</EXTNWCODE>"
				+ "<MSISDN>" + billerData.acctNo + "</MSISDN>"
				+ "<PIN>" + billerData.vendorPassword + "</PIN>"
				+ "<LOGINID></LOGINID>"
				+ "<PASSWORD></PASSWORD>"
				+ "<EXTCODE>" + billerData.vendorCode + "</EXTCODE>"
				+ "<EXTREFNUM>" + request.EXTREFNUM + "</EXTREFNUM>"
				+ "<MSISDN2>" + removeCountryCode(request.MSISDN2) + "</MSISDN2>"
				+ "<AMOUNT>" + request.AMOUNT + "</AMOUNT>"
				+ "<LANGUAGE1>1</LANGUAGE1>"
				+ "<LANGUAGE2>1</LANGUAGE2>"
				+ "<SELECTOR>1</SELECTOR>"
				+ "</COMMAND>";
				// Notify Biller Utilities
				mediumsService = new MediumsService();
				long? notifId = mediumsService.logBillNotification(txnData, "AIRTEL_AIRTIME", mcpTransId.ToString(), xmlRequestdata, request.EXTREFNUM);
				start = DateTime.Now; //Start time
				string stringResp = sendHTTPostRequest(httpMethod, fullURL, xmlRequestdata);
				end = DateTime.Now;   //End time
				timeDifference = end - start;
				difference_Miliseconds = (int)timeDifference.TotalMilliseconds;
				XmlDocument respXML = new XmlDocument();
				respXML.LoadXml(stringResp);
				MobileTopUpResponse response = getPreTupResponse(respXML);
				if (notifId != null)
				{
					string status = response.responseStatus.responseCode == "0" ? "NOTIFIED" : "FAILED";
					mediumsService.updateBillNotificationStatus(notifId, status, response.MESSAGE, response.TXNID, difference_Miliseconds, stringResp);
				}
				return response;
			}
			catch (MediumsException ex) {
				LOGGER.error(ex.ToString());
				MobileTopUpResponse response = new MobileTopUpResponse();
				response.responseStatus = ex.getErrorMessage();
				return response;
			}
			catch (Exception e)
			{
				LOGGER.error(e.ToString());
				MobileTopUpResponse response = new MobileTopUpResponse();
				response.responseStatus = new ResponseMessage("-99", e.Message + " during airtime purchase");
				return response;
			}
		}		  
		 
		 
		public MobileTopUpResponse checkAirtimeStatus(MobileTopUpRequest request)
		{
			mediumsService = new MediumsService();
			MobileTopUpResponse response = new MobileTopUpResponse();
			try  
			{ 
				CIBillerData billerRequest = new CIBillerData();
				billerRequest.thirdPartyReference = request.ORIGINEXTREFNUM;
				billerRequest.channelCode = request.SOURCECODE;
				billerRequest.billerCode = "AIRTEL_AIRTIME";
				CIBillerData billerData = mediumsService.findBillerNotificationByReferenceNo(billerRequest);
				if (billerData.response.responseCode != "0")
				{
					response.responseStatus = billerData.response;
					return response;
				}
				XmlDocument respXML = new XmlDocument();
				respXML.LoadXml(billerData.responseData); 
				response = getPreTupResponse(respXML);
				return response;
			}
			catch (Exception e)
			{
				LOGGER.error(e.ToString());
				response = new MobileTopUpResponse();
				response.responseStatus = new ResponseMessage("-99", e.Message + " during Airtel airtime purchase");
				return response;
			}
		}		 

		public MobileTopUpResponse airtelDataRecharge(MobileTopUpRequest request, TxnData txnData, string mcpTransId)
		{
			mediumsService = new MediumsService();
			string httpMethod = "POST";
			try
			{
				BillerData billerData = mediumsService.findBillersByCode(request.billerCode);
				string fullURL = PROPERTIES.PRETUPS_AIRTEL + "?REQUEST_GATEWAY_CODE=MCPY&REQUEST_GATEWAY_TYPE=EXTGW&LOGIN=pretups&PASSWORD=0971500a350af5c3d1c0b12221a0558c&SOURCE_TYPE=EXTGW&SERVICE_PORT=190";
				string xmlRequestdata = @"<?xml version=""1.0""?>"
				+ "<COMMAND>"
				+ "<TYPE>VASEXTRFREQ</TYPE>"
				+ "<DATE></DATE>"
				+ "<EXTNWCODE>UG</EXTNWCODE>"
				+ "<MSISDN>" + billerData.acctNo + "</MSISDN>"
				+ "<PIN>" + billerData.vendorPassword + "</PIN>"
				+ "<LOGINID></LOGINID>"
				+ "<PASSWORD></PASSWORD>"
				+ "<EXTCODE>" + billerData.vendorCode + "</EXTCODE>"
				+ "<EXTREFNUM>" + request.EXTREFNUM + "</EXTREFNUM>"
				+ "<MSISDN2>" + removeCountryCode(request.MSISDN2) + "</MSISDN2>"
				+ "<AMOUNT>" + request.AMOUNT + "</AMOUNT>"
				+ "<LANGUAGE1>1</LANGUAGE1>" 
				+ "<LANGUAGE2>1</LANGUAGE2>" 
				+ "<SELECTOR>" + request.SELECTOR + "</SELECTOR>" 
				+ "</COMMAND>";
				// Notify Biller Utilities
				mediumsService = new MediumsService();
				long? notifId = mediumsService.logBillNotification(txnData, request.billerCode, mcpTransId.ToString(), xmlRequestdata, request.EXTREFNUM);
				start = DateTime.Now; //Start time
				string stringResp = sendHTTPostRequest(httpMethod, fullURL, xmlRequestdata);
				end = DateTime.Now;   //End time
				timeDifference = end - start;
				difference_Miliseconds = (int)timeDifference.TotalMilliseconds;
				XmlDocument respXML = new XmlDocument();
				respXML.LoadXml(stringResp);
				MobileTopUpResponse response = getPreTupResponse(respXML);
				if (notifId != null)
				{
					string status = response.responseStatus.responseCode == "0" ? "NOTIFIED" : "FAILED";
					mediumsService.updateBillNotificationStatus(notifId, status, response.MESSAGE, response.TXNID, difference_Miliseconds, stringResp);
				}
				return response;
			}
			catch (MediumsException ex)
			{
				LOGGER.error(ex.ToString());
				MobileTopUpResponse response = new MobileTopUpResponse();
				response.responseStatus = ex.getErrorMessage();
				return response;
			}
			catch (Exception e)
			{
				LOGGER.error(e.ToString());
				MobileTopUpResponse response = new MobileTopUpResponse();
				response.responseStatus = new ResponseMessage("-99", e.Message + " during airtime purchase");
				return response;
			}
		}

		  
		public MobileTopUpResponse checkAirtelDataStatus(MobileTopUpRequest request)
		{
			mediumsService = new MediumsService();
			MobileTopUpResponse response = new MobileTopUpResponse();
			try
			{
				CIBillerData billerRequest = new CIBillerData();
				billerRequest.thirdPartyReference = request.ORIGINEXTREFNUM;
				billerRequest.channelCode = request.SOURCECODE;
				billerRequest.billerCode = "AIRTEL_DATA";
				CIBillerData billerData = mediumsService.findBillerNotificationByReferenceNo(billerRequest);
				if (billerData.response.responseCode != "0")
				{
					response.responseStatus = billerData.response;
					return response;
				}
				XmlDocument respXML = new XmlDocument();
				respXML.LoadXml(billerData.responseData);
				response = getPreTupResponse(respXML);
				return response;
			}
			catch (Exception e)
			{
				LOGGER.error(e.ToString());
				response = new MobileTopUpResponse();
				response.responseStatus = new ResponseMessage("-99", e.Message + " during Airtel airtime purchase");
				return response;
			}
		}


	}
}