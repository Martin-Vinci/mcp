using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Reflection;
using System.Text;
using System.Threading;
using micropay_apis.Models;
using micropay_apis.Services;
using micropay_apis.Utils;
using Nancy.Json;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using Org.BouncyCastle.Asn1.Tests;

namespace micropay_apis.Remote
{
    public class AirtelMoneyService
    {
        DateTime start; //Start time
        DateTime end;   //End time
        TimeSpan timeDifference; //Time span between start and end = the time span needed to execute your method
        int difference_Miliseconds;
        private readonly MediumsService mediumsService;
        private readonly JavaScriptSerializer jsonSerializer;
        public AirtelMoneyService()
        {
            ServicePointManager.SecurityProtocol = SecurityProtocolType.Ssl3
                    | SecurityProtocolType.Tls
                    | SecurityProtocolType.Tls11
                    | SecurityProtocolType.Tls12;

            jsonSerializer = new JavaScriptSerializer();
            mediumsService = new MediumsService();
        }

        private ExternalResponse getResponseMessage(AirtelMoneyResponse request)
        {
            ExternalResponse responseMessage = new ExternalResponse();
            Status statusMessage = request.status;

            if (statusMessage.code == "200")
            {
                responseMessage.responseCode = "0";
                responseMessage.responseMessage = "success";
                return responseMessage;
            }

            responseMessage.responseCode = statusMessage.code;
            responseMessage.responseMessage = statusMessage.message + " at Airtel";
            return responseMessage;
        }


        private long getAirtelMoneyRef()
        {
            string reference = DateTime.Now.ToString("yyMMddHHmmssfff");
            return long.Parse(reference);
        }


        private string sendHTTPostRequest(string httpMethod, string serviceURL, string jsonString, string authToken)
        {
            string response = "";
            string completeURL = PROPERTIES.AIRTEL_MONEY_URL + serviceURL;
            try
            {
                Uri url = new Uri(completeURL);
                HttpWebRequest httpRequest = (HttpWebRequest)WebRequest.Create(url);
                if (authToken != null)
                    httpRequest.Headers.Add("Authorization", "Bearer " + authToken);

                httpRequest.Headers.Add("X-Country", "UG");
                httpRequest.Headers.Add("X-Currency", "UGX");

                LOGGER.info("================================== Airtel_Money Header");
                LOGGER.info(httpRequest.Headers.ToString());

                httpRequest.Accept = "application/json";
                httpRequest.ContentType = "application/json";
                httpRequest.Method = httpMethod;
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
                }
            }
            catch (Exception ex)
            {
                LOGGER.error(ex.ToString());
            }
            finally
            {
                try
                {
                    LOGGER.info("Airtel_Money Request : URL > " + completeURL + " ==>: " + jsonString + "\n " +
                            "Airtel_Money Response ===>: " + response);
                }
                catch (Exception)
                {
                }
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


        private AirtelMoneyRequest prepareRequestData(TxnData request, string mcpTransId, string apiName)
        {

            long _ref = getAirtelMoneyRef();
            Subscriber subscriber = new Subscriber();
            subscriber.msisdn = removeCountryCode(request.referenceNo);

            Transaction transaction = new Transaction();
            transaction.id = mcpTransId;
            transaction.amount = (long)request.transAmt;

            List<AdditionalInfo> additional_info = new List<AdditionalInfo>();
            additional_info.Add(new AdditionalInfo("remark", "AM withdraw > " + mcpTransId));

            AirtelMoneyRequest airtelMoney = new AirtelMoneyRequest();
            airtelMoney.additional_info = additional_info;
            airtelMoney.subscriber = subscriber;
            airtelMoney.transaction = transaction;
            airtelMoney.reference = _ref;
            if (apiName.Equals("cashin"))
                airtelMoney.pin = PROPERTIES.AIRTEL_MONEY_PIN;
            return airtelMoney;
        }
         
         
        public ResponseMessage prepareTxnResponse(string stringResp)
        {
            if (stringResp == null)
                return new ResponseMessage("-99", "Empty response received from Airtel");

            AirtelMoneyResponse airtelMoneyResponse = JsonConvert.DeserializeObject<AirtelMoneyResponse>(stringResp);
            ResponseMessage response = new ResponseMessage
            {
                responseMessage = airtelMoneyResponse.data.message,
                responseCode = airtelMoneyResponse.data.status
            };

            if (response.responseCode == "SUCCESS")
            {
                response.responseCode = "0";
            }
            return response;
        }


        private string getAuthToken()
        {
            string methodName = "/auth/oauth2/token";
            string httpMethod = "POST";
            Dictionary<string, object> requestData = new Dictionary<string, object>();
            requestData.Add("client_id", PROPERTIES.AIRTEL_MONEY_CLIENT_ID);
            requestData.Add("client_secret", PROPERTIES.AIRTEL_MONEY_CLIENT_SECRET);
            requestData.Add("grant_type", "client_credentials");
            string payLoad = JsonConvert.SerializeObject(requestData, Formatting.Indented);
            string stringResp = sendHTTPostRequest(httpMethod, methodName, payLoad, null);
            var data = (JObject)JsonConvert.DeserializeObject(stringResp);

            var accessToken = data["access_token"].Value<string>();
            var expiryPeriod = data["expires_in"].Value<string>();

            return accessToken;
        }


        public TxnResp findAirtelTxnStatus(string reference, string apiName)
        {
            ResponseMessage message;
            string methodName = "/standard/v1/" + apiName + "/" + reference;
            string httpMethod = "GET";

            string authToken = getAuthToken();
            if (authToken == null)
            {
                message = new ResponseMessage();
                message.responseCode = "-99";
                message.responseMessage = "Transaction failed. Failure to generate authorization token";
            }

            string stringResp = sendHTTPostRequest(httpMethod, methodName, null, authToken);
            message = prepareTxnResponse(stringResp);

            if (message.responseCode == "0")
            {
                AirtelMoneyResponse airtelMoneyResponse = JsonConvert.DeserializeObject<AirtelMoneyResponse>(stringResp);
                string statusCode = airtelMoneyResponse.data.transaction.status;
                string messageText = airtelMoneyResponse.status.message;    
                if (statusCode == "TIP")
                    statusCode = "6000";
                else if (statusCode == "TS")
                    statusCode = "0";

                message = new ResponseMessage(statusCode, messageText);
            }
            TxnResp response = new TxnResp();
            response.response = message;
            return response;
        }


        public TxnResp cashOut(TxnData request, string mcpTransId)
        {
            // this.jsonSerializer = new JavaScriptSerializer();
            TxnResp txnResp = new TxnResp();
            try
            {
                string mcpTxnId = Convert.ToString(mcpTransId);

                if (request.billerCode == null)
                {
                    txnResp.response = new ResponseMessage("-99", "Invalid biller code specified during biller processing");
                    return txnResp;
                }

                BillerData billerData = StubClientService.mediumsService.findBillersByCode(request.billerCode);
                string methodName = "/standard/v2/cashout/";
                string httpMethod = "POST";

                AirtelMoneyRequest airtelMoney = prepareRequestData(request, mcpTransId, "cashOut");
                string payLoad = JsonConvert.SerializeObject(airtelMoney, Formatting.Indented);

                // Notify Biller Utilities
                long? notifId = mediumsService.logBillNotification(request, request.billerCode, mcpTransId.ToString(), payLoad, mcpTransId);
                start = DateTime.Now; //Start time

                string authToken = getAuthToken();
                if (authToken == null)
                {
                    ResponseMessage message = new ResponseMessage("-99", "Airtel Money|Transaction failed. Failure to generate authorization token");
                    txnResp.response = message;
                    if (notifId != null)
                        mediumsService.updateBillNotificationStatus(notifId, "FAILED", message.responseMessage, mcpTransId, 0, null);
                    return txnResp;
                }

                start = DateTime.Now; //Start time
                string stringResp = sendHTTPostRequest(httpMethod, methodName, payLoad, authToken);

                end = DateTime.Now;   //End time
                timeDifference = end - start;
                difference_Miliseconds = (int)timeDifference.TotalMilliseconds;

                ResponseMessage response = prepareTxnResponse(stringResp);
                txnResp.response = response;
                if (response.responseCode == "0")
                {
                    AirtelMoneyResponse airtelMoneyResponse = JsonConvert.DeserializeObject<AirtelMoneyResponse>(stringResp);
                    string transRefNo = airtelMoneyResponse.data.id;
                    string apiName = "cashout";
                    while (true)
                    {
                        int milliseconds = 10000;
                        Thread.Sleep(milliseconds);
                        LOGGER.info("==============  Checking status for Airtel Money " + apiName + " | " + DateTime.Now.ToString("dd-MM-yyyy HH:mm:ss"));
                        txnResp = findAirtelTxnStatus(transRefNo, apiName);
                        if (txnResp.response.responseCode.ToUpper().Contains("6000"))
                            continue;

                        if (notifId != null)
                        {
                            string status = txnResp.response.responseCode == "0" ? "NOTIFIED" : "FAILED";
                            StubClientService.mediumsService.updateBillNotificationStatus(notifId, status, txnResp.response.responseMessage, transRefNo, difference_Miliseconds, null);
                        }
                        break;
                    }
                    return txnResp;
                }

                txnResp.response = response;
                if (notifId != null)
                {
                    string status = response.responseCode == "0" ? "PENDING" : "FAILED";
                    mediumsService.updateBillNotificationStatus(notifId, status, response.responseMessage, mcpTransId, difference_Miliseconds, stringResp);
                }
            }
            catch (MediumsException ex)
            {
                LOGGER.error(ex.ToString());
                ResponseMessage errorMessage = ex.getErrorMessage();
                txnResp.response = new ResponseMessage("-99", errorMessage.responseMessage + " at Airtel Money");
            }

            catch (Exception ex)
            {
                LOGGER.error(ex.ToString());
                txnResp.response = new ResponseMessage("-99", ex.Message + " at Airtel Money");
            }
            finally
            {
                ResponseMessage response = txnResp.response;
                if (response.responseMessage.ToLower().Contains("insufficient"))
                    response.responseMessage = "@Airtel Money|We are unable to process your request right now. Please contact Micropay for support";
                txnResp.response = response;
            }
            return txnResp;
        }

        public TxnResp cashIn(TxnData request, string mcpTransId)
        {
            // this.jsonSerializer = new JavaScriptSerializer();
            TxnResp txnResp = new TxnResp();
            try
            {
                string mcpTxnId = Convert.ToString(mcpTransId);

                if (request.billerCode == null)
                {
                    txnResp.response = new ResponseMessage("-99", "Invalid biller code specified during biller processing");
                    return txnResp;
                }

                BillerData billerData = StubClientService.mediumsService.findBillersByCode(request.billerCode);
                string methodName = "/standard/v2/cashin/";
                string httpMethod = "POST";

                AirtelMoneyRequest airtelMoney = prepareRequestData(request, mcpTransId, "cashin");
                string payLoad = JsonConvert.SerializeObject(airtelMoney, Formatting.Indented);

                // Notify Biller Utilities
                long? notifId = mediumsService.logBillNotification(request, request.billerCode, mcpTransId.ToString(), payLoad, mcpTransId);
                start = DateTime.Now; //Start time

                string authToken = getAuthToken();
                if (authToken == null)
                {
                    ResponseMessage message = new ResponseMessage("-99", "Airtel Money|Transaction failed. Failure to generate authorization token");
                    txnResp.response = message;
                    if (notifId != null)
                        mediumsService.updateBillNotificationStatus(notifId, "FAILED", message.responseMessage, mcpTransId, 0, null);
                    return txnResp;
                }

                start = DateTime.Now; //Start time
                string stringResp = sendHTTPostRequest(httpMethod, methodName, payLoad, authToken);

                end = DateTime.Now;   //End time
                timeDifference = end - start;
                difference_Miliseconds = (int)timeDifference.TotalMilliseconds;
               
                ResponseMessage response = prepareTxnResponse(stringResp);
                AirtelMoneyResponse airtelMoneyResponse = JsonConvert.DeserializeObject<AirtelMoneyResponse>(stringResp);

                txnResp.response = response;
                if (notifId != null)
                {
                    string status = response.responseCode == "0" ? "NOTIFIED" : "FAILED";
                    string message = response.responseCode == "0" ? airtelMoneyResponse.data.message : response.responseMessage;
                    mediumsService.updateBillNotificationStatus(notifId, status, message, mcpTransId, difference_Miliseconds, stringResp);
                }

                if (response.responseCode == "0")
                {                    
                    string transRefNo = airtelMoneyResponse.data.transaction.id;

                    //string apiName = "cashin";
                    //while (true)
                    //{
                    //    int milliseconds = 10000;
                    //    Thread.Sleep(milliseconds);
                    //    LOGGER.info("==============  Checking status for Airtel Money " + apiName + " | " + DateTime.Now.ToString("dd-MM-yyyy HH:mm:ss"));
                    //    txnResp = findAirtelTxnStatus(transRefNo, apiName);
                    //    if (txnResp.response.responseCode.ToUpper().Contains("6000"))
                    //        continue;

                    //    if (notifId != null)
                    //    {
                    //        string status = txnResp.response.responseCode == "0" ? "NOTIFIED" : "FAILED";
                    //        StubClientService.mediumsService.updateBillNotificationStatus(notifId, status, txnResp.response.responseMessage, transRefNo, difference_Miliseconds, null);
                    //    }
                    //    break;
                    //}
                    return txnResp;
                }

            }
            catch (MediumsException ex)
            {
                LOGGER.error(ex.ToString());
                ResponseMessage errorMessage = ex.getErrorMessage();
                txnResp.response = new ResponseMessage("-99", errorMessage.responseMessage + " at Airtel Money");
            }

            catch (Exception ex)
            {
                LOGGER.error(ex.ToString());
                txnResp.response = new ResponseMessage("-99", ex.Message + " at Airtel Money");
            }
            finally
            {
                ResponseMessage response = txnResp.response;
                if (response.responseMessage.ToLower().Contains("insufficient"))
                    response.responseMessage = "@Airtel Money|We are unable to process your request right now. Please contact Micropay for support";
                txnResp.response = response;
            }
            return txnResp;
        }

    }
}