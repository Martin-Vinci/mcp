using Interswitch;
using micropay_apis.ABModels;
using micropay_apis.APIModals;
using micropay_apis.Models;
using micropay_apis.Remote;
using micropay_apis.Utils;
using Nancy.Json;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Text;

namespace micropay_apis.Services
{
    public class InterswitchService
    {
        private MediumsService mediumsService;
        private DateTime start;
        private DateTime end;
        private TimeSpan timeDifference;
        private int difference_Miliseconds;
        private JavaScriptSerializer jsonSerializer;

        public InterswitchService()
        {
            ServicePointManager.SecurityProtocol = SecurityProtocolType.Ssl3 | SecurityProtocolType.Tls | SecurityProtocolType.Tls11 | SecurityProtocolType.Tls12;
        }

        private ResponseMessage getResponseMessage2(JObject jObject)
        {
            ResponseMessage responseMessage2 = new ResponseMessage();
            responseMessage2.responseCode = Extensions.Value<string>((IEnumerable<JToken>)jObject["code"]);
            responseMessage2.responseMessage = Extensions.Value<string>((IEnumerable<JToken>)jObject["message"]);
            if (responseMessage2.responseCode == "00")
                responseMessage2.responseCode = "0";
            else
                responseMessage2.responseMessage += " at Micropay Core";
            return responseMessage2;
        }

        private ResponseMessage getResponseMessage(JObject jObject)
        {
            string str1 = Extensions.Value<string>((IEnumerable<JToken>)jObject["responseCode"]);
            string str2 = Extensions.Value<string>((IEnumerable<JToken>)jObject["responseMessage"]);
            switch (str1)
            {
                case "90000":
                    return MESSAGES.getSuccessMessage();
                case "9000":
                    return MESSAGES.getSuccessMessage();
                default:
                    ResponseMessage responseMessage = new ResponseMessage();
                    responseMessage.responseCode = str1;
                    string str3 = str2 + " at Biller";
                    responseMessage.responseMessage = str3;
                    return responseMessage;
            }
        }

        private string sendHTTPostRequest(
          string httpMethod,
          string methodName,
          string payLoad,
          string additionalParameters)
        {
            string switchSvaBaseUrl = PROPERTIES.InterSwitchSVABaseURL;
            string str1 = (string)null;
            string str2 = switchSvaBaseUrl + methodName;
            LOGGER.info("===================== InterSwitch Complete URL: " + str2);
            LOGGER.info("===================== InterSwitch Request => " + payLoad);
            string interSwitchClientId = PROPERTIES.InterSwitchClientId;
            string switchClientSecret = PROPERTIES.InterSwitchClientSecret;
            Config config = new Config(httpMethod, str2, interSwitchClientId, switchClientSecret, (string)null, additionalParameters);
            Uri uri = new Uri(str2);
            HttpWebRequest httpWebRequest = (HttpWebRequest)WebRequest.Create(uri);
            NetworkCredential cred = new NetworkCredential("admin", "admin");
            CredentialCache credentialCache = new CredentialCache();
            credentialCache.Add(uri, "Basic", cred);
            httpWebRequest.PreAuthenticate = true;
            httpWebRequest.Credentials = (ICredentials)credentialCache;
            httpWebRequest.Headers.Add("Authorization", config.Authorization);
            httpWebRequest.Headers.Add("Timestamp", config.TimeStamp);
            httpWebRequest.Headers.Add("Nonce", config.Nonce);
            httpWebRequest.Headers.Add("SignatureMethod", "SHA512");
            httpWebRequest.Headers.Add("Signature", config.Signature);
            httpWebRequest.Headers.Add("TerminalId", PROPERTIES.InterSwitchTerminalId);
            httpWebRequest.Accept = "application/json";
            httpWebRequest.ContentType = "application/json";
            httpWebRequest.Method = httpMethod;
            LOGGER.info(httpWebRequest.Headers.ToString());
            if (httpMethod == "GET")
            {
                using (WebResponse response = httpWebRequest.GetResponse())
                    str1 = new StreamReader(response.GetResponseStream()).ReadToEnd();
            }
            else
            {
                byte[] bytes = Encoding.UTF8.GetBytes(payLoad);
                using (Stream requestStream = httpWebRequest.GetRequestStream())
                {
                    requestStream.Write(bytes, 0, bytes.Length);
                    requestStream.Close();
                }
                using (HttpWebResponse response = (HttpWebResponse)httpWebRequest.GetResponse())
                {
                    using (Stream responseStream = response.GetResponseStream())
                        str1 = new StreamReader(responseStream).ReadToEnd();
                }
            }
            LOGGER.info("===================== Interswitch Response => " + str1);
            return str1;
        }

        public TxnResp validateCustomer(CIBillValidationRequest request)
        {
            this.jsonSerializer = new JavaScriptSerializer();
            TxnResp txnResp1 = new TxnResp();
            string str1 = new DateTimeOffset(DateTime.UtcNow).ToUnixTimeMilliseconds().ToString();
            string str2 = PROPERTIES.InterSwitchRequestReferencePrefix + str1.Substring(4);
            string serviceURL = "/interswitch/validation";
            string httpMethod = "POST";
            InterswitchPaymentRequest requestData = new InterswitchPaymentRequest();
            requestData.requestReference = str2;
            requestData.amount = request.transAmt;
            requestData.customerId = request.referenceNo;
            requestData.phoneNumber = request.mobilePhone;
            requestData.paymentCode = long.Parse(request.paymentCode);
            requestData.billerCode = request.billerCode;
            requestData.customerName = request.customerName;
            requestData.sourceOfFunds = "NA";
            requestData.narration = "Payment for order";
            requestData.depositorName = "Bob Johnson";
            requestData.location = request.customerArea;
            requestData.alternateCustomerId = "54321";
            requestData.transactionCode = "T12345";
            requestData.customerToken = "token_value";
            requestData.collectionsAccountNumber = "78901234";
            requestData.pin = "";
            requestData.otp = request.withdrawCode;
            requestData.currencyCode = "UGX";
            JsonConvert.SerializeObject((object)requestData);
            JObject jObject = (JObject)JsonConvert.DeserializeObject(StubClientService.mediumsService.sendHTTPostRequest(httpMethod, serviceURL, (object)requestData));
            ResponseMessage responseMessage = this.getResponseMessage(jObject);
            txnResp1.response = responseMessage;
            txnResp1.requestId = str2;
            if (responseMessage.responseCode == "0")
            {
                JObject jobject = Extensions.Value<JObject>((IEnumerable<JToken>)jObject["response"]);
                txnResp1.utilityRef = Extensions.Value<string>((IEnumerable<JToken>)jobject["transactionReference"]);
                txnResp1.customerName = Extensions.Value<string>((IEnumerable<JToken>)jobject["customerName"]);
                TxnResp txnResp2 = txnResp1;
                double? nullable = Extensions.Value<double?>((IEnumerable<JToken>)jobject["surcharge"]);
                double num1 = nullable.HasValue ? Extensions.Value<double>((IEnumerable<JToken>)jobject["surcharge"]) : 0.0;
                txnResp2.chargeAmt = num1;
                TxnResp txnResp3 = txnResp1;
                nullable = Extensions.Value<double?>((IEnumerable<JToken>)jobject["totalAmount"]);
                double num2 = nullable.HasValue ? Extensions.Value<double>((IEnumerable<JToken>)jobject["totalAmount"]) : 0.0;
                txnResp3.totalAmount = num2;
            }
            return txnResp1;
        }

        public List<InterswitchPaymentItem> findPaymentItems(string billerId)
        {
            this.jsonSerializer = new JavaScriptSerializer();
            TxnResp txnResp = new TxnResp();
            Dictionary<string, object> dictionary = new Dictionary<string, object>();
            return JsonConvert.DeserializeObject<PaymentResponse>(this.sendHTTPostRequest("GET", "/v1/quickteller/billers/" + billerId + "/paymentitems", (string)null, (string)null)).paymentitems;
        }

        public string findBillers()
        {
            this.jsonSerializer = new JavaScriptSerializer();
            Dictionary<string, object> dictionary = new Dictionary<string, object>();
            string billers = this.sendHTTPostRequest("GET", "/v1/quickteller/billers", (string)null, (string)null);
            JsonConvert.DeserializeObject<InterSwitchInquiryResp>(billers);
            return billers;
        }

        public TxnResp sendAdviceRequest(TxnData request, string mcpTransId)
        {
            this.mediumsService = new MediumsService();
            this.jsonSerializer = new JavaScriptSerializer();
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


                string serviceURL = "/interswitch/pay";
                string httpMethod = "POST";
                InterswitchPaymentRequest requestData = new InterswitchPaymentRequest();
                requestData.requestReference = request.billerCode == "WENRECO" || request.billerCode == "TUGENDE" ? request.requestId : mcpTransId;
                requestData.amount = request.transAmt;
                requestData.customerId = request.referenceNo.Trim();
                requestData.phoneNumber = request.customerPhoneNo;
                requestData.paymentCode = long.Parse(request.paymentCode);
                requestData.customerName = request.customerName;
                requestData.billerCode = request.billerCode;
                requestData.sourceOfFunds = "NA";
                requestData.narration = "Utility Payment";
                requestData.depositorName = request.customerName;
                requestData.location = "Kampala";
                requestData.alternateCustomerId = "54321";
                requestData.transactionCode = "T12345";
                requestData.customerToken = "token_value";
                requestData.collectionsAccountNumber = "78901234";
                requestData.pin = "";
                requestData.otp = request.withdrawCode;
                requestData.currencyCode = "UGX";
                string requestLog = JsonConvert.SerializeObject((object)requestData);
                long? notificationId = StubClientService.mediumsService.logBillNotification(request, request.billerCode, mcpTransId, requestLog, mcpTransId);
                this.start = DateTime.Now;
                JObject jObject = null;
                string str = null;
                ResponseMessage responseMessage;
                try
                {
                    str = StubClientService.mediumsService.sendHTTPostRequest(httpMethod, serviceURL, (object)requestData);
                    LOGGER.info(str);
                    jObject = (JObject)JsonConvert.DeserializeObject(str);

                    this.end = DateTime.Now;
                    this.timeDifference = this.end - this.start;
                    this.difference_Miliseconds = (int)this.timeDifference.TotalMilliseconds;
                    responseMessage = this.getResponseMessage(jObject);
                    txnResp.response = responseMessage;
                    if (responseMessage.responseCode == "0")
                    {
                        JObject jsonObject = Extensions.Value<JObject>((IEnumerable<JToken>)jObject["response"]);
                        txnResp.utilityRef = requestData.requestReference;
                        if (request.billerCode == "WENRECO")
                        {
                            TxnResp tokenValue = this.getTokenValue(jsonObject);
                            txnResp.noOfUnits = tokenValue.noOfUnits;
                            txnResp.receiptNo = tokenValue.receiptNo;
                            txnResp.tokenValue = tokenValue.tokenValue;
                        }
                    }
                }
                catch (Exception ex)
                {
                    LOGGER.error(ex.ToString());
                    responseMessage = new ResponseMessage("-99", ex.Message + " at Biller");
                    txnResp.response = responseMessage;
                }
                if (notificationId.HasValue)
                {
                    string status = responseMessage.responseCode == "0" ? "NOTIFIED" : "FAILED";
                    this.mediumsService.updateBillNotificationStatus(notificationId, status, responseMessage.responseMessage, requestData.requestReference, this.difference_Miliseconds, str);
                }
            }
            catch (MediumsException ex)
            {
                LOGGER.error(ex.ToString());
                ResponseMessage errorMessage = ex.getErrorMessage();
                txnResp.response = new ResponseMessage("-99", errorMessage.responseMessage + " at Biller");
            }
            catch (Exception ex)
            {
                LOGGER.error(ex.ToString());
                txnResp.response = new ResponseMessage("-99", ex.Message + " at Biller");
            }
            finally
            {
                ResponseMessage response = txnResp.response;
                if (response.responseMessage.ToLower().Contains("insufficient"))
                    response.responseMessage = "@Biller|We are unable to process your request right now. Please contact Micropay for support";
                txnResp.response = response;
            }
            return txnResp;
        }

        public TxnResp postAirtelWithdraw(TxnData request)
        {
            this.mediumsService = new MediumsService();
            this.jsonSerializer = new JavaScriptSerializer();
            TxnResp txnResp = new TxnResp();
            try
            {
                if (request.billerCode == null)
                    return new TxnResp(new ResponseMessage("-99", "Invalid biller code specified during interswitch processing"));
                string serviceURL = "/interswitch/processAirtelMoneyWithdraw";
                string httpMethod = "POST";
                InterswitchPaymentRequest interswitchPaymentRequest = new InterswitchPaymentRequest();
                interswitchPaymentRequest.amount = request.transAmt;
                interswitchPaymentRequest.customerId = request.referenceNo;
                interswitchPaymentRequest.phoneNumber = request.customerPhoneNo;
                interswitchPaymentRequest.paymentCode = long.Parse(request.paymentCode);
                interswitchPaymentRequest.customerName = request.customerName;
                interswitchPaymentRequest.billerCode = request.billerCode;
                interswitchPaymentRequest.sourceOfFunds = "NA";
                interswitchPaymentRequest.narration = "Utility Payment";
                interswitchPaymentRequest.depositorName = request.customerName;
                interswitchPaymentRequest.location = "Kampala";
                interswitchPaymentRequest.alternateCustomerId = "54321";
                interswitchPaymentRequest.transactionCode = "T12345";
                interswitchPaymentRequest.customerToken = "token_value";
                interswitchPaymentRequest.collectionsAccountNumber = "78901234";
                interswitchPaymentRequest.pin = "";
                interswitchPaymentRequest.otp = request.withdrawCode;
                interswitchPaymentRequest.currencyCode = "UGX";
                OutletAuthentication authRequest = request.authRequest;
                AirtelCashRequest requestData = new AirtelCashRequest()
                {
                    apiUserName = authRequest.vCode,
                    apiPassword = ENCRYPTER.encryptMCPValue(authRequest.vPassword),
                    deviceId = authRequest.deviceId,
                    imeiNumber = authRequest.imeiNumber,
                    deviceMake = authRequest.deviceMake,
                    deviceModel = authRequest.deviceModel,
                    userPhoneNo = authRequest.phoneNo,
                    outletCode = authRequest.outletCode,
                    pinNo = ENCRYPTER.encryptMCPValue(authRequest.pinNo),
                    channelCode = authRequest.channelCode
                };
                requestData.destAcctNo = request.crAcctNo;
                requestData.transAmt = request.crAcctNo;
                requestData.currency = request.currency;
                requestData.sourceAcctNo = request.drAcctNo;
                requestData.description = request.description;
                requestData.serviceCode = request.tranCode;
                requestData.paymentRequest = interswitchPaymentRequest;
                string requestLog = JsonConvert.SerializeObject((object)requestData);
                string str = DateTime.Now.ToString("ddHHmmss");
                long? notificationId = StubClientService.mediumsService.logBillNotification(request, request.billerCode, str, requestLog, str);
                this.start = DateTime.Now;
                string responseData = StubClientService.mediumsService.sendHTTPostRequest(httpMethod, serviceURL, (object)requestData);
                this.end = DateTime.Now;
                this.timeDifference = this.end - this.start;
                this.difference_Miliseconds = (int)this.timeDifference.TotalMilliseconds;
                JObject jObject = (JObject)JsonConvert.DeserializeObject(responseData);
                ResponseMessage responseMessage2 = this.getResponseMessage2(jObject);
                if (responseMessage2.responseCode != "0")
                    throw new MediumsException(responseMessage2);
                JObject jobject = Extensions.Value<JObject>((IEnumerable<JToken>)jObject["data"]);
                txnResp.response = MESSAGES.getSuccessMessage();
                txnResp.transId = Extensions.Value<string>((IEnumerable<JToken>)jobject["transId"]);
                txnResp.chargeAmt = Extensions.Value<double>((IEnumerable<JToken>)jobject["chargeAmt"]);
                txnResp.drAcctBal = Extensions.Value<double?>((IEnumerable<JToken>)jobject["drAcctBalance"]);
                txnResp.crAcctBal = Extensions.Value<double?>((IEnumerable<JToken>)jobject["crAcctBalance"]);
                JEnumerable<JToken> jenumerable = jobject["transDetails"].Children();
                List<ChildTrans> childTransList = new List<ChildTrans>();
                foreach (JToken jtoken in jenumerable)
                    childTransList.Add(new ChildTrans()
                    {
                        destAcctNo = Extensions.Value<string>((IEnumerable<JToken>)jtoken[(object)"destAcctNo"]),
                        transAmt = Extensions.Value<double>((IEnumerable<JToken>)jtoken[(object)"transAmt"]),
                        sourceAcctNo = Extensions.Value<string>((IEnumerable<JToken>)jtoken[(object)"sourceAcctNo"]),
                        description = Extensions.Value<string>((IEnumerable<JToken>)jtoken[(object)"description"]),
                        amountType = Extensions.Value<string>((IEnumerable<JToken>)jtoken[(object)"amountType"]),
                        transType = Extensions.Value<string>((IEnumerable<JToken>)jtoken[(object)"transType"])
                    });
                txnResp.transItems = childTransList;
                if (notificationId.HasValue)
                {
                    string status = responseMessage2.responseCode == "0" ? "NOTIFIED" : "FAILED";
                    this.mediumsService.updateBillNotificationStatus(notificationId, status, responseMessage2.responseMessage, interswitchPaymentRequest.requestReference, this.difference_Miliseconds, responseData);
                }
                return txnResp;
            }
            catch (MediumsException ex)
            {
                LOGGER.error(ex.ToString());
                return new TxnResp(new ResponseMessage("-99", ex.getErrorMessage().responseMessage + " at Biller"));
            }
            catch (Exception ex)
            {
                LOGGER.error(ex.ToString());
                return new TxnResp(new ResponseMessage("-99", ex.Message + " at Biller"));
            }
        }

        private TxnResp getTokenValue(JObject jsonObject)
        {
            TxnResp tokenValue = new TxnResp();
            try
            {
                tokenValue.tokenValue = Extensions.Value<string>(jsonObject["token"]);
                tokenValue.receiptNo = Extensions.Value<string>((IEnumerable<JToken>)jsonObject["retrievalReference"]);
                string[] strArray1 = Extensions.Value<string>((IEnumerable<JToken>)jsonObject["additionalData"]).Split(',');
                for (int index = 0; index < strArray1.Length; ++index)
                {
                    if (strArray1[index] != null && strArray1[index].ToLower().Contains("unit"))
                    {
                        string[] strArray2 = strArray1[index].Split(':');
                        string str = strArray2.Length > 1 ? strArray2[1] : strArray2[0];
                        tokenValue.noOfUnits = str.Replace("\"", "").Replace("\\", "");
                    }
                }
            }
            catch (Exception ex)
            {
                LOGGER.error(ex.ToString());
            }
            return tokenValue;
        }
    }
}
