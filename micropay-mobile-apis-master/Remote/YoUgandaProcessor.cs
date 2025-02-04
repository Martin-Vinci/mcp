using System;
using System.IO;
using System.Net;
using System.Text;
using System.Xml;
using micropay_apis.Models;
using micropay_apis.Utils;

namespace micropay_apis.Services
{
	public class YoUgandaProcessor
	{
		static string makeRequest(string xmlData)
		{
			LOGGER.info("==========> Soap Request ========>" + xmlData);
			HttpWebRequest req = (HttpWebRequest)WebRequest.Create(PROPERTIES.YO_UGANDA_PAYMENT_GATEWAY);
			req.ContentType = "text/xml";
			req.Method = "post";
			byte[] byteArray = Encoding.UTF8.GetBytes(xmlData);
			req.ContentLength = byteArray.Length;
			Stream dataStream = req.GetRequestStream();
			dataStream.Write(byteArray, 0, byteArray.Length);
			dataStream.Close();
			WebResponse response = req.GetResponse();
			Console.WriteLine(((HttpWebResponse)response).StatusDescription);
			dataStream = response.GetResponseStream();
			StreamReader reader = new StreamReader(dataStream);
			string responseString = reader.ReadToEnd();
			reader.Close();
			dataStream.Close();
			response.Close();
			LOGGER.info("=====>Soap Response=====>" + responseString);
			return responseString;
		}

		private ResponseMessage getYoResponse(XmlDocument requestXML) {
			ResponseMessage response = new ResponseMessage();
			string status = requestXML.SelectSingleNode("//AutoCreate/Response/Status").InnerText;
			if (status == "OK")
				response = MESSAGES.getSuccessMessage();
			else
			{
				response.responseCode = requestXML.SelectSingleNode("//AutoCreate/Response/StatusCode").InnerText;
				response.responseMessage = requestXML.SelectSingleNode("//AutoCreate/Response/StatusMessage").InnerText;
			}
			return response;
		}
		 
		public YoBalanceResp doBalanceInquiry(YoAcctBalRequest request)
		{
			YoBalanceResp resp = new YoBalanceResp();
			string yoResp = makeRequest(YOXMLHandler.getBalanceRequestString());
			XmlDocument respXML = new XmlDocument();
			respXML.LoadXml(yoResp);
			resp.response = getYoResponse(respXML);
			if (resp.response.responseCode != "0")
				return resp;
			resp.curBal = CONVERTER.toDouble(respXML.SelectSingleNode("//AutoCreate/Response/Balance/Currency/Balance").InnerText);             
			return resp;
		}

		public YoCashResponse doCashIn(YoCashRequest request)
		{
			YoCashResponse resp = new YoCashResponse();
			string yoResp = makeRequest(YOXMLHandler.getDepositXMLString(request));
			XmlDocument respXML = new XmlDocument();
			respXML.LoadXml(yoResp);
			resp.response = getYoResponse(respXML);
			if (resp.response.responseCode != "0")
				return resp;
			resp.transStatus = respXML.SelectSingleNode("//AutoCreate/Response/TransactionStatus").InnerText;
			resp.transRef = respXML.SelectSingleNode("//AutoCreate/Response/TransactionReference").InnerText;
			resp.mnoTransRef = respXML.SelectSingleNode("//AutoCreate/Response/MNOTransactionReferenceId").InnerText;
			return resp;
		}


		public YoCashResponse doCashOut(YoCashRequest request)
		{
			YoCashResponse resp = new YoCashResponse();
			string yoResp = makeRequest(YOXMLHandler.getWithdrawXMLString(request));
			XmlDocument respXML = new XmlDocument();
			respXML.LoadXml(yoResp);
			resp.response = getYoResponse(respXML);
			if (resp.response.responseCode != "0")
				return resp;
			resp.transStatus = respXML.SelectSingleNode("//AutoCreate/Response/TransactionStatus").InnerText;
			resp.transRef = respXML.SelectSingleNode("//AutoCreate/Response/TransactionReference").InnerText;
			resp.mnoTransRef = respXML.SelectSingleNode("//AutoCreate/Response/MNOTransactionReferenceId").InnerText;
			return resp;
		}




	}
}