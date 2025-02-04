using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Security.Cryptography;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using micropay_apis.APIModals;
using micropay_apis.Models;
using micropay_apis.Services;
using micropay_apis.Utils;
using Nancy.Json;
using Newtonsoft.Json;
using Org.BouncyCastle.Asn1.Tests;

namespace micropay_apis.Remote
{
    public class CoporateAgencyService
    {
        private MediumsService mediumsService;
        DateTime start; //Start time
        DateTime end;   //End time
        TimeSpan timeDifference; //Time span between start and end = the time span needed to execute your method
        int difference_Miliseconds;
        JavaScriptSerializer jsonSerializer;
        public CoporateAgencyService()
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
            string message = responseMessage + " at Banka";
            response.responseMessage = message.ToUpper();
            return response;
        }


        private string sendHTTPostRequest(string httpMethod, string methodName, string payLoad)
        {
            string response = "";
            string completeURL = PROPERTIES.CORPORATE_AGENT_URL + methodName;
            LOGGER.info("===================== Banka CompleteURL: " + completeURL);
            LOGGER.info("===================== Banka Request => " + payLoad);
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
            if (payLoad != null)
            {
                byte[] bytes = Encoding.UTF8.GetBytes(payLoad);
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
            LOGGER.info("===================== Banka Response => " + response);
            return response;
        }


        private string generateSignatureWithPrivateKey(string dataToSign)
        {
            {
                X509Certificate2 cert = new X509Certificate2(PROPERTIES.CORPORATE_AGENT_PRIVATE_CERTIFICATE_PATH, PROPERTIES.CENTE_PRIVATE_CERT_PWD);
                RSACryptoServiceProvider cryptoServiceProvider = (RSACryptoServiceProvider)cert.PrivateKey;
                // Hash the data
                SHA1Managed sha1 = new SHA1Managed();
                ASCIIEncoding encoding = new ASCIIEncoding();
                byte[] data = encoding.GetBytes(dataToSign);
                byte[] hash = sha1.ComputeHash(data);
                // Sign the hash
                string digital_signature = Convert.ToBase64String(cryptoServiceProvider.SignHash(hash, CryptoConfig.MapNameToOID("SHA1")));
                return digital_signature;

            }
        }


        private string generateSignatureWithPublicKey(string dataToSign)
        {
            string output = string.Empty;
            X509Certificate2 cert = new X509Certificate2(PROPERTIES.CORPORATE_AGENT_PUBLIC_CERTIFICATE_PATH, "", X509KeyStorageFlags.UserKeySet);
            using (RSACryptoServiceProvider csp = (RSACryptoServiceProvider)cert.PublicKey.Key)
            {
                byte[] bytesData = Encoding.UTF8.GetBytes(dataToSign);
                byte[] bytesEncrypted = csp.Encrypt(bytesData, false);
                output = Convert.ToBase64String(bytesEncrypted);
            }
            return output;
        }

        private string getRequestId()
        {
            return DateTime.Now.ToString("ddMMYYHHmmss");
        }



        private Models.Authorization getAuthorization()
        {
            Models.Authorization authorization = new Models.Authorization();
            string requestId = getRequestId();
            authorization.requestId = requestId;
            authorization.channelCode = PROPERTIES.CORPORATE_AGENT_CHANNELCODE;
            authorization.requestSignature = generateSignatureWithPrivateKey(requestId);
            return authorization;
        }

        private Entry getEntry(string key, string value)
        {
            Entry item = new Entry();
            item.key = key;
            item.value = value;
            return item;
        }


        private DeviceInformation getDeviceInformation(OutletAuthentication outletAuthentication)
        {
            DeviceInformation deviceInformation = new DeviceInformation();
            deviceInformation.deviceId = outletAuthentication.deviceId;
            deviceInformation.model = outletAuthentication.deviceModel;
            deviceInformation.imei = outletAuthentication.imeiNumber;
            return deviceInformation;
        }

        public CoporateAgencyResponse billPaymentValidation(TxnData request)
        {
            mediumsService = new MediumsService();
            jsonSerializer = new JavaScriptSerializer();
            ExternalResponse response = new ExternalResponse();

            OutletAuthentication authentication = request.authRequest;
            authentication.channelCode = PROPERTIES.CORPORATE_AGENT_CHANNELCODE;
            authentication.deviceId = PROPERTIES.CORPORATE_AGENT_DEVICE_ID;
            authentication.deviceModel = "IPHONE";
            authentication.imeiNumber = PROPERTIES.CORPORATE_AGENT_IMEI_NUMBER;
            request.authRequest = authentication;

            CoporateAgentRequest coporateAgentRequest = new CoporateAgentRequest();
            List<Entry> entries = new List<Entry>
            {
                getEntry("ACTION", "VALIDATE_AND_INITIATE_BILL_PAYMENT"),
                getEntry("BILLER", request.billerCode),
                getEntry("CUST_NO", request.referenceNo),
                getEntry("AMOUNT", request.transAmt.ToString()),
                getEntry("MSISDN", PROPERTIES.CORPORATE_AGENT_OUTLET_PHONE),
                getEntry("CUSTOMER_BANK", "CENTENARY")
            };
            coporateAgentRequest.authorization = getAuthorization();
            coporateAgentRequest.deviceInformation = getDeviceInformation(request.authRequest);
            coporateAgentRequest.entries = entries;
            string methodName = "";
            string httpMethod = "POST";
            string payLoad = jsonSerializer.Serialize(coporateAgentRequest);
            start = DateTime.Now; //Start time
            CoporateAgencyResponse responseData = null;
            try
            {
                string jsonData = sendHTTPostRequest(httpMethod, methodName, payLoad);
                responseData = JsonConvert.DeserializeObject<CoporateAgencyResponse>(jsonData);
                end = DateTime.Now;   //End time
                timeDifference = end - start;
                difference_Miliseconds = (int)timeDifference.TotalMilliseconds;
                if (responseData.returnCode != null)
                    if (responseData.returnCode == 200)
                        responseData.returnCode = 0;
            }
            catch (Exception e)
            {
                LOGGER.error(e.ToString());
                responseData = new CoporateAgencyResponse
                {
                    returnCode = -99,
                    returnMessage = "An error occurred while processing request at Centenary"
                };
                if (e.Message.Contains("timed out"))
                    responseData.returnMessage = "Transaction processing did not complete in time at Centenary. Process Aborted";
            }
            finally
            {
                if (responseData != null)
                    if (responseData.returnMessage.ToLower().Contains("insufficient"))
                        responseData.returnMessage = "@Centenary|We are unable to process your request right now. Please contact Micropay for support";
            }
            return responseData;
        }

        public CoporateAgencyResponse billPaymentInitiation(TxnData request)
        {
            mediumsService = new MediumsService();
            jsonSerializer = new JavaScriptSerializer();
            ExternalResponse response = new ExternalResponse();

            OutletAuthentication authentication = request.authRequest;
            authentication.channelCode = PROPERTIES.CORPORATE_AGENT_CHANNELCODE;
            authentication.deviceId = PROPERTIES.CORPORATE_AGENT_DEVICE_ID;
            authentication.deviceModel = "IPHONE";
            authentication.imeiNumber = PROPERTIES.CORPORATE_AGENT_IMEI_NUMBER;
            request.authRequest = authentication;

            CoporateAgentRequest coporateAgentRequest = new CoporateAgentRequest();
            List<Entry> entries = new List<Entry>
            {
                getEntry("ACTION", "INITIATE_BILL_PAYMENT"),
                getEntry("BILLER", request.billerCode),
                getEntry("CUST_NO", request.referenceNo),
                getEntry("AMOUNT", request.transAmt.ToString()),
                getEntry("MSISDN", PROPERTIES.CORPORATE_AGENT_OUTLET_PHONE),
                getEntry("CUSTOMER_BANK", "CENTENARY")
            };
            coporateAgentRequest.authorization = getAuthorization();
            coporateAgentRequest.deviceInformation = getDeviceInformation(request.authRequest);
            coporateAgentRequest.entries = entries;
            string methodName = "";
            string httpMethod = "POST";
            string payLoad = jsonSerializer.Serialize(coporateAgentRequest);
            start = DateTime.Now; //Start time
            CoporateAgencyResponse responseData = null;
            try
            {
                string jsonData = sendHTTPostRequest(httpMethod, methodName, payLoad);
                responseData = JsonConvert.DeserializeObject<CoporateAgencyResponse>(jsonData);
                end = DateTime.Now;   //End time
                timeDifference = end - start;
                difference_Miliseconds = (int)timeDifference.TotalMilliseconds;
                if (responseData.returnCode != null)
                    if (responseData.returnCode == 200)
                        responseData.returnCode = 0;
            }
            catch (Exception e)
            {
                LOGGER.error(e.ToString());
                responseData = new CoporateAgencyResponse
                {
                    returnCode = -99,
                    returnMessage = "An error occurred while processing request at Centenary"
                };
                if (e.Message.Contains("timed out"))
                    responseData.returnMessage = "Transaction processing did not complete in time at Centenary. Process Aborted";
            }
            finally
            {
                if (responseData != null)
                    if (responseData.returnMessage.ToLower().Contains("insufficient"))
                        responseData.returnMessage = "@Centenary|We are unable to process your request right now. Please contact Micropay for support";
            }
            return responseData;
        }


        public CoporateAgencyResponse billPaymentConfirmation(TxnData request)
        {
            mediumsService = new MediumsService();
            jsonSerializer = new JavaScriptSerializer();

            OutletAuthentication authentication = request.authRequest;
            authentication.channelCode = PROPERTIES.CORPORATE_AGENT_CHANNELCODE;
            authentication.deviceId = PROPERTIES.CORPORATE_AGENT_DEVICE_ID;
            authentication.deviceModel = "IPHONE";
            authentication.imeiNumber = PROPERTIES.CORPORATE_AGENT_IMEI_NUMBER;
            request.authRequest = authentication;

            CoporateAgentRequest coporateAgentRequest = new CoporateAgentRequest();
            List<Entry> entries = new List<Entry>
            {
                getEntry("ACTION", "CONFIRM_BILL_PAYMENT"),
                getEntry("BILLER", request.billerCode),
                getEntry("CUST_NO", request.referenceNo),
                getEntry("AMOUNT", request.transAmt.ToString()),
                getEntry("TOTAL_AMOUNT", request.outstandingAmount.ToString()),
                getEntry("MSISDN", PROPERTIES.CORPORATE_AGENT_OUTLET_PHONE),
                getEntry("CUSTOMER_BANK", "CENTENARY"),
                getEntry("OUTLET_PIN", generateSignatureWithPublicKey(PROPERTIES.CORPORATE_AGENT_OUTLET_PIN)),
                getEntry("PROGRESS_INDICATOR", request.billerTransRef)
            };
            coporateAgentRequest.authorization = getAuthorization();
            coporateAgentRequest.deviceInformation = getDeviceInformation(request.authRequest);
            coporateAgentRequest.entries = entries;
            string methodName = "";
            string httpMethod = "POST";
            string payLoad = jsonSerializer.Serialize(coporateAgentRequest);
            start = DateTime.Now; //Start time
            CoporateAgencyResponse responseData = null;
            try
            {
                string jsonData = sendHTTPostRequest(httpMethod, methodName, payLoad);
                responseData = JsonConvert.DeserializeObject<CoporateAgencyResponse>(jsonData);
                end = DateTime.Now;   //End time
                timeDifference = end - start;
                difference_Miliseconds = (int)timeDifference.TotalMilliseconds;
                if (responseData.returnCode != null)
                    if (responseData.returnCode == 200)
                        responseData.returnCode = 0;
            }
            catch (Exception e)
            {
                LOGGER.error(e.ToString());
                responseData = new CoporateAgencyResponse
                {
                    returnCode = -99,
                    returnMessage = "An error occurred while processing request at Centenary"
                };
                if (e.Message.Contains("timed out"))
                    responseData.returnMessage = "Transaction processing did not complete in time at Centenary. Process Aborted";
            }
            finally
            {
                if (responseData.returnMessage.ToLower().Contains("insufficient"))
                    responseData.returnMessage = "@Centenary|We are unable to process your request right now. Please contact Micropay for support";
            }
            return responseData;
        }


        public CoporateAgencyResponse cashDepositValidation(TxnData request)
        {
            mediumsService = new MediumsService();
            jsonSerializer = new JavaScriptSerializer();
            ExternalResponse response = new ExternalResponse();

            CoporateAgencyResponse responseData = null;
            try
            {

                OutletAuthentication authentication = request.authRequest;
                authentication.channelCode = PROPERTIES.CORPORATE_AGENT_CHANNELCODE;
                authentication.deviceId = PROPERTIES.CORPORATE_AGENT_DEVICE_ID;
                authentication.deviceModel = "IPHONE";
                authentication.imeiNumber = PROPERTIES.CORPORATE_AGENT_IMEI_NUMBER;
                request.authRequest = authentication;

                CoporateAgentRequest coporateAgentRequest = new CoporateAgentRequest();
                List<Entry> entries = new List<Entry>
                {
                    getEntry("ACTION", "CASHIN_VALIDATION"),
                    getEntry("MSISDN", PROPERTIES.CORPORATE_AGENT_OUTLET_PHONE),
                    getEntry("RECIPIENT", request.referenceNo),
                    getEntry("DEPOSITORS_NAME", request.depositorName),
                    getEntry("DEPOSITORS_PHONE", request.depositorPhoneNo),
                    getEntry("AMOUNT", request.transAmt.ToString()),
                    getEntry("CUSTOMER_BANK", "CENTENARY")
                };
                coporateAgentRequest.authorization = getAuthorization();
                coporateAgentRequest.deviceInformation = getDeviceInformation(request.authRequest);
                coporateAgentRequest.entries = entries;
                string methodName = "";
                string httpMethod = "POST";
                string payLoad = jsonSerializer.Serialize(coporateAgentRequest);
                start = DateTime.Now; //Start time

                string jsonData = sendHTTPostRequest(httpMethod, methodName, payLoad);
                responseData = JsonConvert.DeserializeObject<CoporateAgencyResponse>(jsonData);
                end = DateTime.Now;   //End time
                timeDifference = end - start;
                difference_Miliseconds = (int)timeDifference.TotalMilliseconds;
                if (responseData.returnCode != null)
                    if (responseData.returnCode == 200)
                        responseData.returnCode = 0;
            }
            catch (Exception e)
            {
                LOGGER.error(e.ToString());
                responseData = new CoporateAgencyResponse
                {
                    returnCode = -99,
                    returnMessage = "An error occurred while processing request at Centenary"
                };
                if (e.Message.Contains("timed out"))
                    responseData.returnMessage = "Transaction processing did not complete in time at Centenary. Process Aborted";
            }
            finally
            {
                if (responseData != null)
                    if (responseData.returnMessage.ToLower().Contains("insufficient"))
                        responseData.returnMessage = "@Centenary|We are unable to process your request right now. Please contact Micropay for support";
            }
            return responseData;
        }

        public CoporateAgencyResponse cashDepositConfirmation(TxnData request)
        {
            mediumsService = new MediumsService();
            jsonSerializer = new JavaScriptSerializer();

            CoporateAgencyResponse responseData = null;
            try
            {

                OutletAuthentication authentication = request.authRequest;
                authentication.channelCode = PROPERTIES.CORPORATE_AGENT_CHANNELCODE;
                authentication.deviceId = PROPERTIES.CORPORATE_AGENT_DEVICE_ID;
                authentication.deviceModel = "IPHONE";
                authentication.imeiNumber = PROPERTIES.CORPORATE_AGENT_IMEI_NUMBER;
                request.authRequest = authentication;

                CoporateAgentRequest coporateAgentRequest = new CoporateAgentRequest();
                List<Entry> entries = new List<Entry>
                {
                    getEntry("ACTION", "CUSTOMER_CASHIN"),
                    getEntry("MSISDN", PROPERTIES.CORPORATE_AGENT_OUTLET_PHONE),
                    getEntry("RECIPIENT", request.referenceNo),
                    getEntry("DEPOSITORS_NAME", request.depositorName),
                    getEntry("DEPOSITORS_PHONE", request.depositorPhoneNo),
                    getEntry("AMOUNT", request.transAmt.ToString()),
                    getEntry("CUSTOMER_BANK", "CENTENARY"),
                    getEntry("OUTLET_PIN", generateSignatureWithPublicKey(PROPERTIES.CORPORATE_AGENT_OUTLET_PIN)),
                    getEntry("PROGRESS_INDICATOR", request.billerTransRef)
                };
                coporateAgentRequest.authorization = getAuthorization();
                coporateAgentRequest.deviceInformation = getDeviceInformation(request.authRequest);
                coporateAgentRequest.entries = entries;
                string methodName = "";
                string httpMethod = "POST";
                string payLoad = jsonSerializer.Serialize(coporateAgentRequest);
                start = DateTime.Now; //Start time

                string jsonData = sendHTTPostRequest(httpMethod, methodName, payLoad);
                responseData = JsonConvert.DeserializeObject<CoporateAgencyResponse>(jsonData);
                end = DateTime.Now;   //End time
                timeDifference = end - start;
                difference_Miliseconds = (int)timeDifference.TotalMilliseconds;
                if (responseData.returnCode != null)
                    if (responseData.returnCode == 200)
                        responseData.returnCode = 0;
            }
            catch (Exception e)
            {
                LOGGER.error(e.ToString());
                responseData = new CoporateAgencyResponse
                {
                    returnCode = -99,
                    returnMessage = "An error occurred while processing request at Centenary"
                };
                if (e.Message.Contains("timed out"))
                    responseData.returnMessage = "Transaction processing did not complete in time at Centenary. Process Aborted";
            }
            finally
            {
                if (responseData != null)
                    if (responseData.returnMessage.ToLower().Contains("insufficient"))
                        responseData.returnMessage = "@Centenary|We are unable to process your request right now. Please contact Micropay for support";
            }
            return responseData;
        }

        public CoporateAgencyResponse cashWithdrawValidation(TxnData request)
        {
            mediumsService = new MediumsService();
            jsonSerializer = new JavaScriptSerializer();

            CoporateAgencyResponse responseData = null;
            try
            {
                OutletAuthentication authentication = request.authRequest;
                authentication.channelCode = PROPERTIES.CORPORATE_AGENT_CHANNELCODE;
                authentication.deviceId = PROPERTIES.CORPORATE_AGENT_DEVICE_ID;
                authentication.deviceModel = "IPHONE";
                authentication.imeiNumber = PROPERTIES.CORPORATE_AGENT_IMEI_NUMBER;
                request.authRequest = authentication;

                CoporateAgentRequest coporateAgentRequest = new CoporateAgentRequest();
                List<Entry> entries = new List<Entry>
                {
                    getEntry("ACTION", "COMPLETE_CASHOUT_VALIDATION"),
                    getEntry("MSISDN", PROPERTIES.CORPORATE_AGENT_OUTLET_PHONE),
                    getEntry("WITHDRAW_CODE", request.withdrawCode),
                    getEntry("CUSTOMER_ACCOUNT", request.referenceNo),
                    getEntry("ID_TYPE", request.idType ?? "NIN"),
                    getEntry("ID_NUMBER", request.idValue),
                    //entries.Add(getEntry("AMOUNT", request.transAmt.ToString()));
                    getEntry("CUSTOMER_BANK", "CENTENARY")
                };
                coporateAgentRequest.authorization = getAuthorization();
                coporateAgentRequest.deviceInformation = getDeviceInformation(request.authRequest);
                coporateAgentRequest.entries = entries;
                string methodName = "";
                string httpMethod = "POST";
                string payLoad = jsonSerializer.Serialize(coporateAgentRequest);
                start = DateTime.Now; //Start time
                string jsonData = sendHTTPostRequest(httpMethod, methodName, payLoad);
                responseData = JsonConvert.DeserializeObject<CoporateAgencyResponse>(jsonData);
                end = DateTime.Now;   //End time
                timeDifference = end - start;
                difference_Miliseconds = (int)timeDifference.TotalMilliseconds;
                if (responseData.returnCode != null)
                    if (responseData.returnCode == 200)
                        responseData.returnCode = 0;
            }
            catch (Exception e)
            {
                LOGGER.error(e.ToString());
                responseData = new CoporateAgencyResponse
                {
                    returnCode = -99,
                    returnMessage = "An error occurred while processing request at Centenary"
                };
                if (e.Message.Contains("timed out"))
                    responseData.returnMessage = "Transaction processing did not complete in time at Centenary. Process Aborted";
            }
            finally
            {
                if (responseData != null)
                    if (responseData.returnMessage.ToLower().Contains("insufficient"))
                        responseData.returnMessage = "@Centenary|We are unable to process your request right now. Please contact Micropay for support";
            }
            return responseData;
        }

        public CoporateAgencyResponse cashWithdrawCompletion(TxnData request)
        {
            mediumsService = new MediumsService();
            jsonSerializer = new JavaScriptSerializer();

            CoporateAgencyResponse responseData = null;
            try
            {
                OutletAuthentication authentication = request.authRequest;
                authentication.channelCode = PROPERTIES.CORPORATE_AGENT_CHANNELCODE;
                authentication.deviceId = PROPERTIES.CORPORATE_AGENT_DEVICE_ID;
                authentication.deviceModel = "IPHONE";
                authentication.imeiNumber = PROPERTIES.CORPORATE_AGENT_IMEI_NUMBER;
                request.authRequest = authentication;

                CoporateAgentRequest coporateAgentRequest = new CoporateAgentRequest();
                List<Entry> entries = new List<Entry>
                {
                    getEntry("ACTION", "COMPLETE_CASHOUT"),
                    getEntry("MSISDN", PROPERTIES.CORPORATE_AGENT_OUTLET_PHONE),
                    getEntry("CUSTOMER_ACCOUNT", request.referenceNo),
                    getEntry("WITHDRAW_CODE", request.withdrawCode),
                    getEntry("CUSTOMER_BANK", "CENTENARY"),
                    getEntry("ID_TYPE", request.idType ?? "NIN"),
                    getEntry("ID_NUMBER", request.idValue),
                    getEntry("OUTLET_PIN", generateSignatureWithPublicKey(PROPERTIES.CORPORATE_AGENT_OUTLET_PIN)),
                    getEntry("PROGRESS_INDICATOR", request.billerTransRef)
                };
                coporateAgentRequest.authorization = getAuthorization();
                coporateAgentRequest.deviceInformation = getDeviceInformation(request.authRequest);
                coporateAgentRequest.entries = entries;
                string methodName = "";
                string httpMethod = "POST";
                string payLoad = jsonSerializer.Serialize(coporateAgentRequest);
                start = DateTime.Now; //Start time

                string jsonData = sendHTTPostRequest(httpMethod, methodName, payLoad);
                responseData = JsonConvert.DeserializeObject<CoporateAgencyResponse>(jsonData);
                end = DateTime.Now;   //End time
                timeDifference = end - start;
                difference_Miliseconds = (int)timeDifference.TotalMilliseconds;
                if (responseData.returnCode != null)
                    if (responseData.returnCode == 200)
                        responseData.returnCode = 0;
            }
            catch (Exception e)
            {
                LOGGER.error(e.ToString());
                responseData = new CoporateAgencyResponse
                {
                    returnCode = -99,
                    returnMessage = "An error occurred while processing request to Centenary"
                };
                if (e.Message.Contains("timed out"))
                    responseData.returnMessage = "Transaction processing did not complete in time at Centenary. Process Aborted";
            }
            finally
            {
                if (responseData != null)
                    if (responseData.returnMessage.ToLower().Contains("insufficient"))
                        responseData.returnMessage = "@Centenary|We are unable to process your request right now. Please contact Micropay for support";
            }
            return responseData;
        } 
        public CoporateAgencyResponse abcCashInValidation(TxnData request)
        {
            mediumsService = new MediumsService();
            jsonSerializer = new JavaScriptSerializer();
            ExternalResponse response = new ExternalResponse();

            CoporateAgencyResponse responseData = null;
            try
            {

                OutletAuthentication authentication = request.authRequest;
                authentication.channelCode = PROPERTIES.CORPORATE_AGENT_CHANNELCODE;
                authentication.deviceId = PROPERTIES.CORPORATE_AGENT_DEVICE_ID;
                authentication.deviceModel = "IPHONE";
                authentication.imeiNumber = PROPERTIES.CORPORATE_AGENT_IMEI_NUMBER;
                request.authRequest = authentication;

                CoporateAgentRequest coporateAgentRequest = new CoporateAgentRequest();
                List<Entry> entries = new List<Entry>
                {
                    getEntry("ACTION", "ABC_CASHIN_VALIDATION"),
                    getEntry("MSISDN", PROPERTIES.CORPORATE_AGENT_OUTLET_PHONE),
                    getEntry("RECIPIENT", request.referenceNo),
                    getEntry("DEPOSITORS_NAME", request.depositorName),
                    getEntry("DEPOSITORS_PHONE", request.depositorPhoneNo),
                    getEntry("AMOUNT", request.transAmt.ToString()),
                    getEntry("ABC_BANK_NAME", request.bankName),
                    getEntry("ABC_BANK_CODE", request.bankCode),
                    getEntry("CUSTOMER_BANK", "CENTENARY")
                };
                coporateAgentRequest.authorization = getAuthorization();
                coporateAgentRequest.deviceInformation = getDeviceInformation(request.authRequest);
                coporateAgentRequest.entries = entries;
                string methodName = "";
                string httpMethod = "POST";
                string payLoad = jsonSerializer.Serialize(coporateAgentRequest);
                start = DateTime.Now; //Start time

                string jsonData = sendHTTPostRequest(httpMethod, methodName, payLoad);
                responseData = JsonConvert.DeserializeObject<CoporateAgencyResponse>(jsonData);
                end = DateTime.Now;   //End time
                timeDifference = end - start;
                difference_Miliseconds = (int)timeDifference.TotalMilliseconds;
                if (responseData.returnCode != null)
                    if (responseData.returnCode == 200)
                        responseData.returnCode = 0;
            }
            catch (Exception e)
            {
                LOGGER.error(e.ToString());
                responseData = new CoporateAgencyResponse
                {
                    returnCode = -99,
                    returnMessage = "An error occurred while processing request at Centenary"
                };
                if (e.Message.Contains("timed out"))
                    responseData.returnMessage = "Transaction processing did not complete in time at Centenary. Process Aborted";
            }
            finally
            {
                if (responseData != null)
                    if (responseData.returnMessage.ToLower().Contains("insufficient"))
                        responseData.returnMessage = "@Centenary|We are unable to process your request right now. Please contact Micropay for support";
            }
            return responseData;
        }
        public CoporateAgencyResponse abcCashInConfirmation(TxnData request, string mcpTransId)
        {
            mediumsService = new MediumsService();
            jsonSerializer = new JavaScriptSerializer();

            CoporateAgencyResponse responseData = new CoporateAgencyResponse();
            try
            {
                string mcpTxnId = Convert.ToString(mcpTransId);

                if (request.billerCode == null)
                {
                    responseData = new CoporateAgencyResponse
                    {
                        returnCode = -99,
                        returnMessage = "Invalid biller code specified during biller processing"
                    };
                }
          
                OutletAuthentication authentication = request.authRequest;
                authentication.channelCode = PROPERTIES.CORPORATE_AGENT_CHANNELCODE;
                authentication.deviceId = PROPERTIES.CORPORATE_AGENT_DEVICE_ID;
                authentication.deviceModel = "IPHONE";
                authentication.imeiNumber = PROPERTIES.CORPORATE_AGENT_IMEI_NUMBER;
                request.authRequest = authentication;

                CoporateAgentRequest coporateAgentRequest = new CoporateAgentRequest();
                List<Entry> entries = new List<Entry>
                {
                    getEntry("ACTION", "ABC_CASHIN"),
                    getEntry("MSISDN", PROPERTIES.CORPORATE_AGENT_OUTLET_PHONE),
                    getEntry("RECIPIENT", request.referenceNo),
                    getEntry("DEPOSITORS_NAME", request.depositorName),
                    getEntry("DEPOSITORS_PHONE", request.depositorPhoneNo),
                    getEntry("AMOUNT", request.transAmt.ToString()),
                    getEntry("ABC_BANK_NAME", request.bankName),
                    getEntry("ABC_BANK_CODE", request.bankCode),
                    getEntry("CUSTOMER_BANK", "CENTENARY"),
                    getEntry("OUTLET_PIN", generateSignatureWithPublicKey(PROPERTIES.CORPORATE_AGENT_OUTLET_PIN)),
                    getEntry("PROGRESS_INDICATOR", request.billerTransRef)
                };
                coporateAgentRequest.authorization = getAuthorization();
                coporateAgentRequest.deviceInformation = getDeviceInformation(request.authRequest);
                coporateAgentRequest.entries = entries;
                string methodName = "";
                string httpMethod = "POST";
                string payLoad = jsonSerializer.Serialize(coporateAgentRequest);

                // Notify Biller Utilities
                long? notifId = mediumsService.logBillNotification(request, request.billerCode, mcpTransId.ToString(), payLoad, mcpTransId.ToString());
                start = DateTime.Now; //Start time

                string jsonData = sendHTTPostRequest(httpMethod, methodName, payLoad);
                responseData = JsonConvert.DeserializeObject<CoporateAgencyResponse>(jsonData);
                end = DateTime.Now;   //End time
                timeDifference = end - start;
                difference_Miliseconds = (int)timeDifference.TotalMilliseconds;
                if (responseData.returnCode != null)
                    if (responseData.returnCode == 200)
                        responseData.returnCode = 0;

                if (notifId != null)
                {
                    string status = responseData.returnCode == 0 ? "NOTIFIED" : "FAILED";
                    mediumsService.updateBillNotificationStatus(notifId, status, responseData.returnMessage, mcpTransId.ToString(), difference_Miliseconds, responseData.returnMessage);
                }
            }
            catch (Exception e)
            {
                LOGGER.error(e.ToString());
                responseData = new CoporateAgencyResponse
                {
                    returnCode = -99,
                    returnMessage = "An error occurred while processing request at Centenary"
                };
                if (e.Message.Contains("timed out"))
                    responseData.returnMessage = "Transaction processing did not complete in time at Centenary. Process Aborted";
            }
            finally
            {
                if (responseData != null)
                    if (responseData.returnMessage.ToLower().Contains("insufficient"))
                        responseData.returnMessage = "@Centenary|We are unable to process your request right now. Please contact Micropay for support";
            }
            return responseData;
        }


    }
}