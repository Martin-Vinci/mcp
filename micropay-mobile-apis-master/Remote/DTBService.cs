using micropay_apis.APIModals;
using micropay_apis.Models;
using micropay_apis.Services;
using micropay_apis.Utils;
using Newtonsoft.Json;
using System;
using System.IO;
using System.Net;
using System.Text;

namespace micropay_apis.Remote
{
    public class DTBService
    {

        private string sendHTTPostRequest(string httpMethod, string serviceURL, string jsonString)
        {
            string response = "";
            string completeURL = PROPERTIES.DTBUrl + serviceURL;
            try
            {
                Uri url = new Uri(completeURL);
                HttpWebRequest httpRequest = (HttpWebRequest)WebRequest.Create(url);

                httpRequest.Accept = "application/json";
                httpRequest.ContentType = "application/json";
                httpRequest.Timeout = 300000;
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
                DTBResponseData dTBResponseData = new DTBResponseData();
                dTBResponseData.Result = "-99";
                if (ex.ToString().Contains("timed out"))                   
                    dTBResponseData.ResultDesc = "The operation has timed out while processing request at URA";
                else
                    dTBResponseData.ResultDesc = "Undefined error has occured while processing request at URA";
                response = JsonConvert.SerializeObject(dTBResponseData, SystemUtils.getJsonSettings());
            }
            finally
            {
                try
                {
                    LOGGER.info("\nDTB Request : URL > " + completeURL + " ==>: " + jsonString + "\n " +
                            "DTB Response ===>: " + response);
                }
                catch (Exception)
                {
                }
            }
            return response;
        }

         
        public TxnResp validatePRN(CIBillValidationRequest request)
        {
            TxnResp txnResp = new TxnResp();
            try
            {
                string httpMethod = "POST";
                DTBRequestData requestBody = new DTBRequestData
                {
                    UserId = PROPERTIES.DTBUserId,
                    Password = PROPERTIES.DTBPassword,
                    Method = "001",
                    TerminalId = PROPERTIES.DTBTerminalId,
                    AgentCode = PROPERTIES.DTBAgentCode,
                    Trn = "X18U02PD2019", 
                    ScenarioType = "TEST",
                    Prn = request.referenceNo 
                };
                string payLoad = JsonConvert.SerializeObject(requestBody, SystemUtils.getJsonSettings());
                string methodName = "/URAMobile";
                string stringResp = sendHTTPostRequest(httpMethod, methodName, payLoad);

                DTBResponseData dtbResponse = JsonConvert.DeserializeObject<DTBResponseData>(stringResp);
                txnResp.response = new ResponseMessage(dtbResponse.Result == "100" ? "0" : dtbResponse.Result, dtbResponse.ResultDesc);

                if (dtbResponse.Result != "100")
                {
                    txnResp.response.responseMessage = txnResp.response.responseMessage + " while validating PRN at URA";
                    return txnResp;
                }

                txnResp.customerName = dtbResponse.UraTaxpayerName;
                txnResp.totalAmount = (double)dtbResponse.UraAmount;
            }
            catch (MediumsException ex)
            {
                LOGGER.error(ex.ToString());
                ResponseMessage errorMessage = ex.getErrorMessage();
                txnResp.response = new ResponseMessage("-99", errorMessage.responseMessage + " at URA");
            }

            catch (Exception ex)
            {
                LOGGER.error(ex.ToString());
                txnResp.response = new ResponseMessage("-99", ex.Message + " at URA");
            }
            finally
            {
                ResponseMessage response = txnResp.response;
                if (response.responseMessage.ToLower().Contains("insufficient"))
                    response.responseMessage = "@URA|We are unable to process your request right now. Please contact Micropay for support";
                txnResp.response = response;
            }
            return txnResp;
        }
         
        public TxnResp postPRN(TxnData request, string mcpTransId)
        {
            DateTime start; //Start time
            DateTime end;   //End time
            TimeSpan timeDifference; //Time span between start and end = the time span needed to execute your method
            int difference_Miliseconds;

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
                string methodName = "/URAMobile";
                string httpMethod = "POST";
                 

                DTBRequestData dtbRequest = new DTBRequestData
                {
                    UserId = PROPERTIES.DTBUserId,
                    Password = PROPERTIES.DTBPassword,
                    Method = "002",
                    TerminalId = PROPERTIES.DTBTerminalId,
                    AgentCode = PROPERTIES.DTBAgentCode,
                    Trn = "",
                    ScenarioType = "TEST",
                    Prn = null,
                    PrnData = new PrnData
                    {
                        Prn = request.referenceNo,
                        Amount = request.transAmt,
                        Charge = request.surCharge,
                        Tax = request.tax,
                        TxtDate = DateTime.Now.ToString("yyyy/MM/dd"),
                        MobileNo = request.customerPhoneNo,
                        AgentFloatAcct = PROPERTIES.DTBAgentFloatAccount
                    }
                };

                string payLoad = JsonConvert.SerializeObject(dtbRequest, Formatting.Indented);

                // Notify Biller Utilities
                long? notifId = StubClientService.mediumsService.logBillNotification(request, request.billerCode, mcpTransId.ToString(), payLoad, mcpTransId);
                start = DateTime.Now; //Start time

                start = DateTime.Now; //Start time
                string stringResp = sendHTTPostRequest(httpMethod, methodName, payLoad);

                end = DateTime.Now;   //End time 
                timeDifference = end - start;
                difference_Miliseconds = (int)timeDifference.TotalMilliseconds;

                DTBResponseData dtbResponse = JsonConvert.DeserializeObject<DTBResponseData>(stringResp);
                txnResp.response = new ResponseMessage(dtbResponse.Result == "200" ? "0" : dtbResponse.Result, dtbResponse.ResultDesc);

                if (notifId != null)
                {
                    string status = txnResp.response.responseCode == "0" ? "NOTIFIED" : "FAILED";
                    string message = txnResp.response.responseCode == "0" ? dtbResponse.Result : dtbResponse.ResultDesc;
                    StubClientService.mediumsService.updateBillNotificationStatus(notifId, status, message, mcpTransId, difference_Miliseconds, stringResp);
                }

                if (txnResp.response.responseCode != "0")
                {
                    txnResp.response.responseMessage = txnResp.response.responseMessage + " while processing request at URA";
                    return txnResp;
                }

                if (txnResp.response.responseCode == "0")
                {
                    string transRefNo = dtbResponse.BankReceiptRef;
                    return txnResp;
                }
            }
            catch (MediumsException ex)
            {
                LOGGER.error(ex.ToString());
                ResponseMessage errorMessage = ex.getErrorMessage();
                txnResp.response = new ResponseMessage("-99", errorMessage.responseMessage + " at URA");
            }

            catch (Exception ex)
            {
                LOGGER.error(ex.ToString());
                txnResp.response = new ResponseMessage("-99", ex.Message + " at URA");
            }
            finally
            {
                ResponseMessage response = txnResp.response;
                if (response.responseMessage.ToLower().Contains("insufficient"))
                    response.responseMessage = "@URA|We are unable to process your request right now. Please contact Micropay for support";
                txnResp.response = response;
            }
            return txnResp;
        }

    }
}
