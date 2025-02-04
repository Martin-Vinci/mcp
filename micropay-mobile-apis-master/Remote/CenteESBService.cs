using micropay_apis.CenteESB;
using micropay_apis.Models;
using micropay_apis.Utils;
using System;
using System.Collections.Generic;
using System.Net;
using System.Security.Cryptography;
using System.Text;

namespace micropay_apis.Remote
{
    public class CenteESBService
    {
        private readonly ChannelSoapWs esbStub;
        public CenteESBService()
        {
            ServicePointManager.SecurityProtocol = SecurityProtocolType.Ssl3
                    | SecurityProtocolType.Tls
                    | SecurityProtocolType.Tls11
                    | SecurityProtocolType.Tls12;

            esbStub = new ChannelSoapWs();
        }
        private channelAuthorization getAuthourizeRequest()
        {
            channelAuthorization resp = new channelAuthorization();
            string password = PROPERTIES.CRDB_ESB_PASSWORD;
            string channelRequestTimestamp = DateTime.Now.ToString("yyyyMMddHHmmss");
            resp.channelCode = PROPERTIES.ESB_CHANNEL_CODE;
            resp.channelRequestDigest = getAPIPassword(channelRequestTimestamp, password);
            resp.channelRequestTimestamp = channelRequestTimestamp;
            return resp;
        }


        private static string toHex(byte[] bytes, bool upperCase)
        {
            StringBuilder result = new StringBuilder(bytes.Length * 2);

            for (int i = 0; i < bytes.Length; i++)
                result.Append(bytes[i].ToString(upperCase ? "X2" : "x2"));
            return result.ToString();
        }
        private string sha256(string reqBody)
        {
            string hashString;
            using (var sha256 = SHA256Managed.Create())
            {
                var hash = sha256.ComputeHash(Encoding.Default.GetBytes(reqBody));
                hashString = toHex(hash, false);
            }
            return hashString;
        }
        private string getAPIPassword(string timeStamp, string password)
        {
            string stringToHarsh = timeStamp.ToString() + password;
            return sha256(stringToHarsh);
        }
        private ResponseMessage getRespMessage(channelServiceExport data)
        {
            ResponseMessage resp = new ResponseMessage();
            resp.responseCode = data.returnCode.ToString();
            resp.responseMessage = data.returnMessage;
            return resp;
        }

        private TxnResp getTranResp(TransactionItem requestData, serviceExportParamWrapper transOutput)
        {
            TxnResp resp = new TxnResp();
            if (requestData.crAcctNo == null || transOutput.batchOutputParams == null)
            {
                return resp;
            }
            foreach (financialTransferAccountExport item in transOutput.batchOutputParams)
            {
                if (item.account.Equals(requestData.drAcctNo))
                {
                    resp.drAcctBal = (double?)item.availableBalance;
                }
                else if (item.account.Trim().Equals(requestData.crAcctNo.Trim()))
                {
                    resp.crAcctBal = (double?)item.availableBalance;
                }
            }
            return resp;
        }


        public TxnResp postToESB(TransactionItem esbTransRequest)
        {
            channelServiceImport request = new channelServiceImport();
            serviceInputParamWrapper transWrapper = new serviceInputParamWrapper();
            channelRequestWrapper requestWrapper = new channelRequestWrapper();
            List<serviceInputParamWrapperEntry> importParams = new List<serviceInputParamWrapperEntry>();
            serviceInputParamWrapperEntry param;
            param = new serviceInputParamWrapperEntry
            {
                key = "IN_USER_ID",
                value = PROPERTIES.CRDB_ESB_UNIQUE_USER
            };
            importParams.Add(param);
            param = new serviceInputParamWrapperEntry
            {
                key = "IN_EXTERNAL_TRANSACTION_REFERENCE",
                value = esbTransRequest.mainTransId.ToString()
            };
            importParams.Add(param);
            param = new serviceInputParamWrapperEntry
            {
                key = "SOURCE_ACCOUNT",
                value = esbTransRequest.drAcctNo
            };
            importParams.Add(param);
            param = new serviceInputParamWrapperEntry
            {
                key = "TRANSACTION_NARRATION",
                value = esbTransRequest.transDescr
            };
            importParams.Add(param);
            param = new serviceInputParamWrapperEntry
            {
                key = "DESTINATION_ACCOUNT",
                value = esbTransRequest.crAcctNo
            };
            importParams.Add(param);
            param = new serviceInputParamWrapperEntry
            {
                key = "TRANSACTION_AMOUNT",
                value = esbTransRequest.amount.ToString()
            };
            importParams.Add(param);
            transWrapper.importParams = importParams.ToArray();
            if (esbTransRequest.entryType == "TRUST_TO_MICROPAY")
                requestWrapper.serviceCode = "DEBIT_TRUST_ACCOUNT_CREDIT_COLLECTION_ACCOUNT";
            else if(esbTransRequest.entryType == "MICROPAY_TO_TRUST")
                requestWrapper.serviceCode = "DEBIT_COLLECTION_ACCOUNT_CREDIT_TRUST_ACCOUNT";
            else
                throw new MediumsException(new ResponseMessage("-99", "Invalid Entry Type specified"));

            string currentTimeMilliseconds = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds().ToString();
            requestWrapper.requestReference = currentTimeMilliseconds;
            request.authorization = getAuthourizeRequest();
            requestWrapper.serviceInputs = transWrapper;
            request.request = requestWrapper;
            TxnResp resp;
            try
            {
                LOGGER.objectInfo(request);
                channelServiceExport response = esbStub.ChannelService(request);
                LOGGER.objectInfo(response);

                resp = getTranResp(esbTransRequest, response.serviceOutput);
                resp.response = getRespMessage(response);
                resp.transId = esbTransRequest.mainTransId.ToString();
                //if (resp.response.responseCode == "0")
                //{
                //	ReferenceData references = getReferenceResponses(response.serviceOutput.exportParameters);
                //	resp.transId = references.profitsReference;
                //}
            }
            catch (Exception ex)
            {
                LOGGER.error(ex.ToString());
                resp = new TxnResp();
                string message = ex.Message + " during Centenary Trust Processing";
                if (ex.Message.Contains("timed out"))
                    message = "Trust Transaction has timed out from Centenary ESB";
                ResponseMessage respMessage = new ResponseMessage("6578", message);
                resp.response = respMessage;
            }
            return resp;
        }


        public TxnResp reverseFromESB(ReversalRequest mainData)
        {
            channelServiceImport request = new channelServiceImport();
            serviceInputParamWrapper transWrapper = new serviceInputParamWrapper();
            channelRequestWrapper requestWrapper = new channelRequestWrapper();
            serviceInputParamWrapperEntry[] importParams = new serviceInputParamWrapperEntry[2];
            serviceInputParamWrapperEntry item = new serviceInputParamWrapperEntry();
            item.key = "IN_EXTERNAL_TRANSACTION_REFERENCE";
            item.value = mainData.originalTranRef.ToString();
            importParams[0] = item;

            item = new serviceInputParamWrapperEntry();
            item.key = "IN_USER_ID";
            item.value = PROPERTIES.CRDB_ESB_UNIQUE_USER;
            importParams[1] = item;

            transWrapper.importParams = importParams;
            requestWrapper.serviceCode = "MICROPAY_TRANSACTIONAL_REVERSAL";
            string currentTimeMilliseconds = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds().ToString();
            requestWrapper.requestReference = currentTimeMilliseconds;
            request.authorization = getAuthourizeRequest();
            requestWrapper.serviceInputs = transWrapper;
            request.request = requestWrapper;
            TxnResp resp = new TxnResp();
            try
            {
                LOGGER.objectInfo(request);
                channelServiceExport response = esbStub.ChannelService(request);
                LOGGER.objectInfo(response);
                resp.response = getRespMessage(response);
            }

            catch (Exception ex)
            {
                LOGGER.error(ex.ToString());
                resp = new TxnResp();
                string message = ex.Message + " during ESB Processing";
                if (ex.Message.Contains("timed out"))
                    message = "Reversal Request has Timeout at Centenary ESB, Process has been aborted";
                ResponseMessage respMessage = new ResponseMessage("-4300", message);
                resp.response = respMessage;
            }
            return resp;
        }


        public TxnResp transactionInquiry(double originalTransRefId)
        {
            channelServiceImport request = new channelServiceImport();
            serviceInputParamWrapper transWrapper = new serviceInputParamWrapper();
            channelRequestWrapper requestWrapper = new channelRequestWrapper();
            serviceInputParamWrapperEntry[] importParams = new serviceInputParamWrapperEntry[1];
            serviceInputParamWrapperEntry item = new serviceInputParamWrapperEntry
            {
                key = "IN_UNIQUE_REQUEST_REFERENCE",
                value = originalTransRefId.ToString()
            };
            importParams[0] = item;

            transWrapper.importParams = importParams;
            requestWrapper.serviceCode = "MICROPAY_TRANSACTION_INQUIRY";
            string currentTimeMilliseconds = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds().ToString();
            requestWrapper.requestReference = currentTimeMilliseconds;
            request.authorization = getAuthourizeRequest();
            requestWrapper.serviceInputs = transWrapper;
            request.request = requestWrapper;
            TxnResp resp = new TxnResp();
            try
            {
                LOGGER.objectInfo(request);
                channelServiceExport response = esbStub.ChannelService(request);
                LOGGER.objectInfo(response);
                resp.response = getRespMessage(response);
            }

            catch (Exception ex)
            {
                LOGGER.error(ex.ToString());
                resp = new TxnResp();
                string message = ex.Message + " during ESB Processing";
                if (ex.Message.Contains("timed out"))
                    message = "Reversal Request has Timeout at Centenary ESB, Process has been aborted";
                ResponseMessage respMessage = new ResponseMessage("-4300", message);
                resp.response = respMessage;
            }
            return resp;
        }

    }
}