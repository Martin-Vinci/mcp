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

namespace micropay_apis.Remote
{
	public class LycaMobileService
	{
		DateTime start; //Start time
		DateTime end;   //End time
		TimeSpan timeDifference; //Time span between start and end = the time span needed to execute your method
		int difference_Miliseconds;

		private string sendHTTPostRequest(string httpMethod, string serviceURL, string requestData)
		{
			string response = "";
			LOGGER.info("===================== LycaMobile Complete URL: " + serviceURL);
			LOGGER.info("===================== LycaMobile Request => " + requestData);
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

				 
		public ExternalResponse getResponseMessage(XmlDocument requestXML)
		{
			ExternalResponse response = new ExternalResponse();
			string statusCode = CONVERTER.getXMLNode(requestXML, "//AMResponse/STATUS_CODE");
			string message = CONVERTER.getXMLNode(requestXML, "//AMResponse/DESCRIPTION") + " at LycaMobile";
			if (statusCode == "200")
			{
				response.responseCode = MESSAGES.getSuccessMessage().responseCode;
				response.responseMessage = MESSAGES.getSuccessMessage().responseMessage;
				response.transRefNo = CONVERTER.getXMLNode(requestXML, "//AMResponse/TRANSACTION_ID");
			}
			else
			{
				response.responseCode = statusCode;
				response.responseMessage = message;
			}
			return response;
		}


		public CIAccountResponse getPhoneBalidationResponse(XmlDocument requestXML)
		{
			CIAccountResponse response = new CIAccountResponse();
			string statusCode = CONVERTER.getXMLNode(requestXML, "//AMResponse/STATUS_CODE");
			string message = CONVERTER.getXMLNode(requestXML, "//AMResponse/DESCRIPTION") + " at LycaMobile";
			if (statusCode == "200")
			{
				response.response = MESSAGES.getSuccessMessage();
				response.acctTitle = CONVERTER.getXMLNode(requestXML, "//AMResponse/SUBSCRIPTION_NAME") + " " + CONVERTER.getXMLNode(requestXML, "//AMResponse/SUBSCRIPTION_SURNAME");
				response.phoneNo = CONVERTER.getXMLNode(requestXML, "//AMResponse/SUBSCRIPTION_NUMBER");
			}
			else
				response.response = new ResponseMessage(statusCode, message);
			return response;
		}



		public ExternalResponse purchaseAirtime(TxnData txnData, string mcpTransId)
		{
			string httpMethod = "POST";
			ExternalResponse response = new ExternalResponse();

            try
			{
				BillerData billerData = StubClientService.mediumsService.findBillersByCode(txnData.billerCode);

				string timeStamp = DateTime.Now.ToString("YdmHis");
				string fullURL = PROPERTIES.LycaMobileBaseURL + "/vendor_resell/index.php";
				string xmlRequestdata = @"<?xml version=""1.0""?>"
					+ "<AMRequest>"
					+ "<APIName>" + billerData.vendorCode + "</APIName>"
					+ "<APIKey>" + billerData.vendorPassword + "</APIKey>"
					+ "<API>RechargeAccount</API>"
					+ "<MSISDN>" + CONVERTER.formatPhoneNumber(txnData.referenceNo) + "</MSISDN>"
					+ "<AMOUNT>" + txnData.transAmt + "</AMOUNT>"
					+ "<REFERENCE>" + mcpTransId + "</REFERENCE>"
					+ "<TransactionType>Airtime</TransactionType>"
					+ "<BundleCode>e_topup</BundleCode>"
					+ "<TimeStamp>" + timeStamp + "</TimeStamp>"
					+ "<Signature>" + ENCRYPTER.encryptWithMD5(timeStamp+billerData.vendorPassword, true) + "</Signature>"
					+ "</AMRequest>";
				// Notify Biller Utilities
				long? notifId = StubClientService.mediumsService.logBillNotification(txnData, "LYCA_AIRTIME", mcpTransId.ToString(), xmlRequestdata, mcpTransId);
				start = DateTime.Now; //Start time
				string stringResp = sendHTTPostRequest(httpMethod, fullURL, xmlRequestdata);
				end = DateTime.Now;   //End time
				timeDifference = end - start;
				difference_Miliseconds = (int)timeDifference.TotalMilliseconds;
				response = getResponseMessage(CONVERTER.toXML(stringResp));
				if (notifId != null)
				{
					string status = response.responseCode == "0" ? "NOTIFIED" : "FAILED";
					StubClientService.mediumsService.updateBillNotificationStatus(notifId, status, response.responseMessage, response.transRefNo, difference_Miliseconds, xmlRequestdata);
				}
				return response;
			}
			catch (MediumsException ex)
			{
				LOGGER.error(ex.ToString());
				response = new ExternalResponse();
				response.responseCode = ex.getErrorMessage().responseCode;
				response.responseMessage = ex.getErrorMessage().responseMessage;				
			}
			catch (Exception e)
			{
				LOGGER.error(e.ToString());
                response = new ExternalResponse("-99", e.Message + " during airtime purchase");
			}
            finally
            {
                if (response.responseMessage.ToLower().Contains("balance"))
                    response.responseMessage = "@Biller|We are unable to process your request right now. Please contact Micropay for support";
            }
            return response;
        }

		public ExternalResponse purchaseData(TxnData txnData, string mcpTransId)
		{
			string httpMethod = "POST";
            ExternalResponse response = new ExternalResponse();
            try 
			{
				BillerData billerData = StubClientService.mediumsService.findBillersByCode(txnData.billerCode);

				string timeStamp = DateTime.Now.ToString("YdmHis");
				string fullURL = PROPERTIES.LycaMobileBaseURL + "/vendor_resell/index.php";
				string xmlRequestdata = @"<?xml version=""1.0""?>"
					+ "<AMRequest>"
					+ "<APIName>" + billerData.vendorCode + "</APIName>"
					+ "<APIKey>" + billerData.vendorPassword + "</APIKey>"
					+ "<API>RechargeAccount</API>"
					+ "<MSISDN>" + CONVERTER.formatPhoneNumber(txnData.referenceNo) + "</MSISDN>"
					+ "<AMOUNT>" + txnData.transAmt + "</AMOUNT>"
					+ "<REFERENCE>" + mcpTransId + "</REFERENCE>"
					+ "<TransactionType>Bundles</TransactionType>"
					+ "<BundleCode>" + txnData.paymentCode + "</BundleCode>"
					+ "<TimeStamp>" + timeStamp + "</TimeStamp>"
					+ "<Signature>" + ENCRYPTER.encryptWithMD5(timeStamp + billerData.vendorPassword, true) + "</Signature>"
					+ "</AMRequest>";
				// Notify Biller Utilities
				long? notifId = StubClientService.mediumsService.logBillNotification(txnData, "LYCA_DATA", mcpTransId.ToString(), xmlRequestdata, mcpTransId);
				start = DateTime.Now; //Start time
				string stringResp = sendHTTPostRequest(httpMethod, fullURL, xmlRequestdata);
				end = DateTime.Now;   //End time
				timeDifference = end - start;
				difference_Miliseconds = (int)timeDifference.TotalMilliseconds;
				response = getResponseMessage(CONVERTER.toXML(stringResp));
				if (notifId != null)
				{
					string status = response.responseCode == "0" ? "NOTIFIED" : "FAILED";
					StubClientService.mediumsService.updateBillNotificationStatus(notifId, status, response.responseMessage, response.transRefNo, difference_Miliseconds, xmlRequestdata);

					if (response.responseCode == "908")
						response.responseMessage = "Operation failed at LycaMobile. Transaction will be reversed";

				}
				return response;
			}
			catch (MediumsException ex)
			{
				LOGGER.error(ex.ToString());
				response = new ExternalResponse();
				response.responseCode = ex.getErrorMessage().responseCode;
				response.responseMessage = ex.getErrorMessage().responseMessage;
				return response;
			}
			catch (Exception e)
			{
				LOGGER.error(e.ToString());
                response = new ExternalResponse("-99", e.Message + " during airtime purchase");
			}
			   finally
            {
                if (response.responseMessage.ToLower().Contains("balance"))
                    response.responseMessage = "@Biller|We are unable to process your request right now. Please contact Micropay for support";
            }
            return response;
		}
		 
		public CIAccountResponse customerResponseByphone(TxnData txnData)
		{
			string httpMethod = "POST";
			try
			{
				BillerData billerData = StubClientService.mediumsService.findBillersByCode(txnData.billerCode);
				string timeStamp = DateTime.Now.ToString("YdmHis");
				string fullURL = PROPERTIES.LycaMobileBaseURL + "/vendor_resell/index.php";
				string xmlRequestdata = @"<?xml version=""1.0""?>"
					+ "<AMRequest>"
					+ "<APIName>" + billerData.vendorCode + "</APIName>"
					+ "<APIKey>" + billerData.vendorPassword + "</APIKey>"
					+ "<API>SubscriberInfo</API>"
					+ "<MSISDN>" + CONVERTER.formatPhoneNumber(txnData.referenceNo) + "</MSISDN>"					
					+ "<TimeStamp>" + timeStamp + "</TimeStamp>"
					+ "<Signature>" + ENCRYPTER.encryptWithMD5(timeStamp + billerData.vendorPassword, true) + "</Signature>"
					+ "</AMRequest>";
				// Notify Biller Utilities
				string stringResp = sendHTTPostRequest(httpMethod, fullURL, xmlRequestdata);
				end = DateTime.Now;   //End time
				timeDifference = end - start;
				difference_Miliseconds = (int)timeDifference.TotalMilliseconds;
				return getPhoneBalidationResponse(CONVERTER.toXML(stringResp));
			}
			catch (MediumsException ex)
			{
				LOGGER.error(ex.ToString());
				CIAccountResponse response = new CIAccountResponse();
				response.response = ex.getErrorMessage();
				return response;
			}
			catch (Exception e)
			{
				LOGGER.error(e.ToString());
				CIAccountResponse response = new CIAccountResponse();
				response.response = new ResponseMessage("-99", e.Message + " during airtime purchase");
				return response;
			}
		}









	}
}