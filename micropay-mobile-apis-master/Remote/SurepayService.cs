using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Reflection;
using System.Security.Cryptography;
using System.Text;
using System.Threading;
using System.Web.Util;
using micropay_apis.APIModals;
using micropay_apis.Models;
using micropay_apis.Services;
using micropay_apis.Utils;
using Nancy.Json;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
 
namespace micropay_apis.Remote
{
    public class SurepayService
    {
        DateTime start; //Start time
        DateTime end;   //End time
        TimeSpan timeDifference; //Time span between start and end = the time span needed to execute your method
        int difference_Miliseconds;
        private readonly MediumsService mediumsService;
        private readonly JavaScriptSerializer jsonSerializer;
        public SurepayService()
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

        private string sendHTTPostRequest(string httpMethod, string serviceUrl, string jsonString)
        {
            string response = "";
            string completeURL = PROPERTIES.SurePayUrl + serviceUrl;
            try
            {
                Uri url = new Uri(completeURL);
                HttpWebRequest httpRequest = (HttpWebRequest)WebRequest.Create(url);
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
                    LOGGER.info("SurePay Request : URL > " + completeURL + " ==>: " + jsonString + "\n " +
                            "SurePay Response ===>: " + response);
                }
                catch (Exception)
                {
                }
            }
            return response;
        }         
        private string generateSignature(string data, string secretKey)
        {
            LOGGER.info(data + "\n");
            // Step 2: Generate byte encoding of the merchant secret key
            byte[] keyBytes = Encoding.UTF8.GetBytes(secretKey);

            // Step 3: Generate byte encoding of the data
            byte[] dataBytes = Encoding.UTF8.GetBytes(data);

            // Step 4 & 5: Generate HMAC using SHA512 and secret key
            using (var hmacsha512 = new HMACSHA512(keyBytes))
            {
                byte[] hashMessage = hmacsha512.ComputeHash(dataBytes);

                // Step 6: Generate base64-encoded string
                return Convert.ToBase64String(hashMessage);
            }
        }         
        private SurePayRequest prepareRequestData(TxnData request, string mcpTransId)
        {
            SurePayRequest requestData = new SurePayRequest();
            requestData.accountNumber = request.referenceNo;
            requestData.accountName = request.customerName;
            requestData.accountType = "SCHOOLFEES";
            requestData.accountCategory = "SCHOOLFEES";
            requestData.accountProvider = request.schoolName;
            requestData.bankCode = "MICROPAY_TEST";
            requestData.password = "WRCTDGBX";
            requestData.tranAmount = request.transAmt.ToString();
            requestData.tranType = "PAYMENT";
            requestData.tranCategory = "BANK";
            requestData.channel = "MOBILE BANKING";
            requestData.currency = "UGX";
            requestData.paymentDate = DateTime.Now.ToString("yyyy-MM-dd");// "2024-09-19";
            requestData.transactionId = mcpTransId;
            requestData.narration = "Fees Payment[" + requestData.accountNumber + "]";

            // Step 1: Concatenate the request parameters
            string merchantSecretKey =PROPERTIES.SurePayMerchantSecretKey;
            string data = requestData.accountNumber + requestData.accountName + requestData.accountType + requestData.tranAmount + requestData.tranType + requestData.paymentDate + requestData.bankCode + requestData.transactionId;
            string tranSignature = generateSignature(data, merchantSecretKey);
            requestData.tranSignature = tranSignature;
            return requestData;
        }
        public ResponseMessage prepareTxnResponse(string stringResp)
        {
            if (stringResp == null)
                return new ResponseMessage("-99", "Empty response received from Airtel");

            SurePayResp airtelMoneyResponse = JsonConvert.DeserializeObject<SurePayResp>(stringResp);
            ResponseMessage response = new ResponseMessage
            {
                responseMessage = airtelMoneyResponse.statusDesc,
                responseCode = airtelMoneyResponse.statusCode
            };

            if (response.responseCode == "SUCCESS")
            {
                response.responseCode = "0";
            }
            return response;
        }       
        public TxnResp getTransactionStatus(string transactionId)
        {
            // this.jsonSerializer = new JavaScriptSerializer();
            TxnResp txnResp = new TxnResp(); 
            try
            {
                string methodName = "/api/bankPayments/getTransactionStatus";
                string httpMethod = "POST";

                SurePayTxnStatusCheckReqst requestData = new SurePayTxnStatusCheckReqst();
                requestData.transactionId = transactionId;
                requestData.bankCode = "MICROPAY_TEST";
                requestData.password = "WRCTDGBX";
                string payLoad = JsonConvert.SerializeObject(requestData, Formatting.Indented);
                start = DateTime.Now; //Start time
                string stringResp = sendHTTPostRequest(httpMethod, methodName, payLoad);


                ResponseMessage response = prepareTxnResponse(stringResp);
                SurePayResp surePayResp = JsonConvert.DeserializeObject<SurePayResp>(stringResp);
                txnResp.response = response;
                if (response.responseCode == "0")
                {
                    return txnResp;
                }

            }
            catch (MediumsException ex)
            {
                LOGGER.error(ex.ToString());
                ResponseMessage errorMessage = ex.getErrorMessage();
                txnResp.response = new ResponseMessage("-99", errorMessage.responseMessage + " at Sure Pay");
            }

            catch (Exception ex)
            {
                LOGGER.error(ex.ToString());
                txnResp.response = new ResponseMessage("-99", ex.Message + " at Sure Pay");
            }
            finally
            {
                ResponseMessage response = txnResp.response;
                if (response.responseMessage.ToLower().Contains("insufficient"))
                    response.responseMessage = "@Sure Pay|We are unable to process your request right now. Please contact Micropay for support";
                txnResp.response = response;
            }
            return txnResp;
        }
        public TxnResp validateReference(CIBillValidationRequest request)
        {
            // this.jsonSerializer = new JavaScriptSerializer();
            TxnResp txnResp = new TxnResp();
            try
            {
                string methodName = "/api/bankPayments/validateReference";
                string httpMethod = "POST";
                 
                SurePayValidationReqst requestData = new SurePayValidationReqst();
                requestData.accountNumber = request.referenceNo;
                requestData.accountType = "SCHOOLFEES";
                requestData.accountCategory = "SCHOOLFEES";
                requestData.bankCode = "MICROPAY_TEST";
                requestData.password = "WRCTDGBX";
                string payLoad = JsonConvert.SerializeObject(requestData, Formatting.Indented);
                start = DateTime.Now; //Start time
                string stringResp = sendHTTPostRequest(httpMethod, methodName, payLoad);

                end = DateTime.Now;   //End time
                timeDifference = end - start;
                difference_Miliseconds = (int)timeDifference.TotalMilliseconds;

                ResponseMessage response = prepareTxnResponse(stringResp);
                SurePayValidationResp surePayResp = JsonConvert.DeserializeObject<SurePayValidationResp>(stringResp);

                txnResp.response = response;
                if (response.responseCode != "0")
                    return txnResp;

                txnResp.customerName = surePayResp.accountName;
                txnResp.studentClass = surePayResp.addendum;
                txnResp.schoolName = surePayResp.accountProvider;
                txnResp.totalAmount = surePayResp.outstandingBalance == null || surePayResp.outstandingBalance == null
                                        ? 0
                                        : Convert.ToDouble(surePayResp.outstandingBalance);

            }
            catch (MediumsException ex)
            {
                LOGGER.error(ex.ToString());
                ResponseMessage errorMessage = ex.getErrorMessage();
                txnResp.response = new ResponseMessage("-99", errorMessage.responseMessage + " at Sure Pay");
            }

            catch (Exception ex)
            {
                LOGGER.error(ex.ToString());
                txnResp.response = new ResponseMessage("-99", ex.Message + " at Sure Pay");
            }
            finally
            {
                ResponseMessage response = txnResp.response;
                if (response.responseMessage.ToLower().Contains("insufficient"))
                    response.responseMessage = "@Sure Pay|We are unable to process your request right now. Please contact Micropay for support";
                txnResp.response = response;
            }
            return txnResp;
        }
        public TxnResp makePayment(TxnData request, string mcpTransId)
        {
            // this.jsonSerializer = new JavaScriptSerializer();
            TxnResp txnResp = new TxnResp();
            try
            {

                if (request.billerCode == null)
                {
                    txnResp.response = new ResponseMessage("-99", "Invalid biller code specified during biller processing");
                    return txnResp;
                }

                BillerData billerData = StubClientService.mediumsService.findBillersByCode(request.billerCode);
                string methodName = "/api/bankPayments/makePayment";
                string httpMethod = "POST";

                SurePayRequest surePayRequest = prepareRequestData(request, mcpTransId);
                string payLoad = JsonConvert.SerializeObject(surePayRequest, Formatting.Indented);

                // Notify Biller Utilities
                long? notifId = mediumsService.logBillNotification(request, request.billerCode, mcpTransId.ToString(), payLoad, mcpTransId);
                start = DateTime.Now; //Start time

                string stringResp = sendHTTPostRequest(httpMethod, methodName, payLoad);

                end = DateTime.Now;   //End time
                timeDifference = end - start;
                difference_Miliseconds = (int)timeDifference.TotalMilliseconds;

                ResponseMessage response = prepareTxnResponse(stringResp);
                SurePayResp surePayResp = JsonConvert.DeserializeObject<SurePayResp>(stringResp);

                txnResp.response = response;
                if (notifId != null)
                {
                    string status = response.responseCode == "0" ? "NOTIFIED" : "FAILED";
                    string message = response.responseCode == "0" ? surePayResp.statusDesc : response.responseMessage;
                    mediumsService.updateBillNotificationStatus(notifId, status, message, mcpTransId, difference_Miliseconds, stringResp);
                }

                if (response.responseCode == "0")
                {
                   return txnResp;
                }

            }
            catch (MediumsException ex)
            {
                LOGGER.error(ex.ToString());
                ResponseMessage errorMessage = ex.getErrorMessage();
                txnResp.response = new ResponseMessage("-99", errorMessage.responseMessage + " at Sure Pay");
            }

            catch (Exception ex)
            {
                LOGGER.error(ex.ToString());
                txnResp.response = new ResponseMessage("-99", ex.Message + " at Sure Pay");
            }
            finally
            {
                ResponseMessage response = txnResp.response;
                if (response.responseMessage.ToLower().Contains("insufficient"))
                    response.responseMessage = "@Sure Pay|We are unable to process your request right now. Please contact Micropay for support";
                txnResp.response = response;
            }
            return txnResp;
        }
    }
}
