using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Text;
using micropay_apis.Models;
using micropay_apis.Utils;
using Nancy.Json;
using Newtonsoft.Json;

namespace micropay_apis.Services
{ 
	public class SMSService
	{
        public SMSService()
        {
            ServicePointManager.ServerCertificateValidationCallback = delegate { return true; };
        }

		private JavaScriptSerializer jsonSerializer;
        private string sendHTTPostRequest(string httpMethod, string serviceURL, Dictionary<string, object> requestData)
        {
            string response = "";
            string jsonString = JsonConvert.SerializeObject(requestData, Formatting.Indented);
            string completeURL = PROPERTIES.SMS_GATEWAY;
            LOGGER.info("===================== CompleteURL" + completeURL);
            Uri url = new Uri(completeURL);
            HttpWebRequest httpRequest = (HttpWebRequest)WebRequest.Create(url);
            NetworkCredential myNetworkCredential = new NetworkCredential("admin", "admin");
            CredentialCache myCredentialCache = new CredentialCache();
            myCredentialCache.Add(url, "Basic", myNetworkCredential);
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
            LOGGER.info(jsonString);
            LOGGER.info(response);
            return response;
        }
         
        public string sendSMS(CISMSRequest request)
        {
            jsonSerializer = new JavaScriptSerializer();
            Dictionary<string, object> requestData = new Dictionary<string, object>();
            string methodName = "/agent-banking/mobileUser/maintainMobileUser";
            string httpMethod = "POST";
            requestData.Add("api_id", "API86997544689");
            requestData.Add("api_password", "s$FTq66KwBn2NjQs");
            requestData.Add("sms_type", "T");
            requestData.Add("encoding", "T"); 
            requestData.Add("sender_id", "MICROPAY"); 
            requestData.Add("phonenumber", request.phoneNo);
            requestData.Add("templateid", null);
            requestData.Add("textmessage", request.messageText);
            requestData.Add("V1", null);
            requestData.Add("V2", null);
            requestData.Add("V3", null);
            requestData.Add("V4", null); 
            requestData.Add("V5", null);
            string stringResp = sendHTTPostRequest(httpMethod, methodName, requestData);
            //TxnResult responseData = JsonConvert.DeserializeObject<TxnResult>(stringResp);
            //return getResponseMessage(responseData);
            return stringResp;
        }




    }
}