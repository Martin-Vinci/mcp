using System;
using System.Collections.Generic;
using System.Net;
using System.Security.Cryptography;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using System.Threading;
using micropay_apis.Models;
using micropay_apis.Services;
using micropay_apis.ug.co.pegasus.test;
using micropay_apis.Utils;

namespace micropay_apis.Remote
{
	public class PegasusService
	{
		readonly PegPay pegPay = new PegPay();		
		DateTime start; //Start time
		DateTime end;   //End time
		TimeSpan timeDifference; //Time span between start and end = the time span needed to execute your method
		int difference_Miliseconds;
		public PegasusService()
		{
			ServicePointManager.ServerCertificateValidationCallback = delegate { return true; };
			pegPay.Timeout = 180000;
        }

		private ResponseMessage getPegasusResponseMessage(Response resp)
		{
			ResponseMessage response = new ResponseMessage();
			response.responseCode = resp.ResponseField6 == "1000" ? "0" : resp.ResponseField6;
			response.responseMessage = resp.ResponseField7;
			return response;
		}

		private string encryptWithDigitalSignature(string dataToSign)
		{
			{
				X509Certificate2 cert = new X509Certificate2(PROPERTIES.PEGASUS_PRIVATE_CERT_PATH, PROPERTIES.PEGASUS_PRIVATE_CERT_PWD);
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

		public TxnResp postTrans(TxnData request, string mcpTransId)
		{
			TransactionRequest billerRequest = new TransactionRequest();
			TxnResp response = new TxnResp();
			string tellerCode;
			long? notifId = null;

            try
			{
				tellerCode = "MICROPAY";
				billerRequest.PostField1 = request.referenceNo;
				billerRequest.PostField2 = request.customerName;
				billerRequest.PostField3 = request.customerArea;
				billerRequest.PostField4 = request.paymentCode;
				billerRequest.PostField5 = DateTime.Now.ToString("dd/MM/yyyy");
				billerRequest.PostField7 = request.transAmt.ToString();
				billerRequest.PostField8 = "CASH"; // CASH, TRANSFER etc
				billerRequest.PostField9 = PROPERTIES.PEGASUS_VENDOR_USERNAME; // VendorCode(REPLACE);
				billerRequest.PostField10 = PROPERTIES.PEGASUS_VENDOR_PASSWORD; // Password(REPLACE);
				billerRequest.PostField11 = CONVERTER.formatPhoneNumber(request.depositorPhoneNo); //Customer Tel;
				billerRequest.PostField12 = "0"; //Reversal;
												 //request.PostField13 = TranIdToBeReversed;
				billerRequest.PostField14 = tellerCode; //Teller;
				billerRequest.PostField15 = "0"; // Offline;
				billerRequest.PostField18 = request.description; // Narration;
																 //request.PostField19 = Email;
				billerRequest.PostField20 = request.externalTransId; //Vendor Transaction Id;
				billerRequest.PostField21 = request.customerType;
				string stringToHash = billerRequest.PostField1 + billerRequest.PostField2 + billerRequest.PostField11 + billerRequest.PostField20 + billerRequest.PostField9
				+ billerRequest.PostField10 + billerRequest.PostField5 + billerRequest.PostField14 + billerRequest.PostField7 + billerRequest.PostField18 + billerRequest.PostField8;
				LOGGER.info(stringToHash);
				//string computeHashRequest = DIGITAL_SIGNATURE.sha1(stringToHash);
				billerRequest.PostField16 = encryptWithDigitalSignature(stringToHash);

				LOGGER.objectInfo(billerRequest);              

				notifId = StubClientService.mediumsService.logBillNotification(request, request.paymentCode, mcpTransId.ToString(), null, mcpTransId.ToString());
				start = DateTime.Now; //Start time
				Response utilResp = pegPay.PrepaidVendorPostTransaction(billerRequest);
				end = DateTime.Now;   //End time
				timeDifference = end - start; //Time span between start and end = the time span needed to execute your method
				difference_Miliseconds = (int)timeDifference.TotalMilliseconds; //Gives you the time needed to execute the method in miliseconds (= time between start and end in miliseconds)
				LOGGER.objectInfo(utilResp);
				LOGGER.info("============== PEGASUS " + request.paymentCode + " POSTING DURATION: " + difference_Miliseconds + "ms: REFERENCE NO: " + billerRequest.PostField1);
				ResponseMessage responseMessage = new ResponseMessage();
				responseMessage.responseCode = utilResp.ResponseField6 == "1000" ? "0" : utilResp.ResponseField6;
				responseMessage.responseMessage = utilResp.ResponseField7;
				response.response = responseMessage;
				//if (request.paymentCode != "UMEME")
				//{
				//	if (notifId != null)
				//	{
				//		string status = responseMessage.responseCode == "0" ? "NOTIFIED" : "FAILED";
				//		mediumsService.updateBillNotificationStatus(notifId, status, responseMessage.responseMessage, response.utilityRef, difference_Miliseconds, null);
				//	}
				//}


				if (responseMessage.responseCode != "0")
					return response;

				//if (request.paymentCode != "UMEME")
				//	return getSuccessResponse(utilResp);

				while (true)
				{
					LOGGER.info("==============  " + request.paymentCode + "  POSTED WITH PENDING STATUS ENTERING SLEEP MODE " + DateTime.Now.ToString("dd-MM-yyyy HH:mm:ss"));
					int milliseconds = 10000;
					Thread.Sleep(milliseconds);
					LOGGER.info("==============  " + request.paymentCode + "  CHECK STATUS HAS WOKEN UP AT " + DateTime.Now.ToString("dd-MM-yyyy HH:mm:ss"));
					response = queryTransactionStatus(request.externalTransId, request.paymentCode);
					if (response.response.responseMessage.ToUpper().Contains("PENDING"))
						continue;

					if (notifId != null)
					{
						string status = response.response.responseCode == "0" ? "NOTIFIED" : "FAILED";
                        StubClientService.mediumsService.updateBillNotificationStatus(notifId, status, response.response.responseMessage, response.utilityRef, difference_Miliseconds, null);
					}
					break;
				}
				response.receiptNo = utilResp.ResponseField8;
				return response;
			}
			catch (Exception e)
			{
				LOGGER.error(e.ToString());
				response.response = new ResponseMessage("-99", e.Message + " at Pegasus");

                if (notifId != null)
                {
                    StubClientService.mediumsService.updateBillNotificationStatus(notifId, "FAILED", response.response.responseMessage, response.utilityRef, difference_Miliseconds, null);
                }
            }
            finally 
            {
                ResponseMessage response2 = response.response;
                if (response2.responseMessage.ToLower().Contains("insufficient"))
                    response2.responseMessage = "@Biller|We are unable to process your request right now. Please contact Micropay for support";
                response.response = response2;
            }
            return response;
		}
		 
		public CIBillValidationResponse queryUMEMECustomerDetails(CIBillValidationRequest gwRequest)
		{
			QueryRequest request = new QueryRequest();
			request.QueryField1 = gwRequest.referenceNo; 
			request.QueryField2 = gwRequest.customerType; // {PREPAID, POSTPAID}
			request.QueryField3 = gwRequest.customerArea;
			request.QueryField4 = gwRequest.paymentCode; // NWSC,UMEME etc
			request.QueryField5 = PROPERTIES.PEGASUS_VENDOR_USERNAME;
			request.QueryField6 = PROPERTIES.PEGASUS_VENDOR_PASSWORD;
			LOGGER.objectInfo(request);                //request.PostField21 = Customer Type;
			start = DateTime.Now; //Start time
			Response response = pegPay.QueryCustomerDetails(request);
			end = DateTime.Now;   //End time
			timeDifference = end - start; //Time span between start and end = the time span needed to execute your method
			difference_Miliseconds = (int)timeDifference.TotalMilliseconds; //Gives you the time needed to execute the method in miliseconds (= time between start and end in miliseconds)
			LOGGER.info("==============" + gwRequest.paymentCode + " QUERY DETAILS DURATION: " + difference_Miliseconds + "ms: SOURCE ACCOUNT: " + request.QueryField1);
			LOGGER.objectInfo(response);
			CIBillValidationResponse utilityResponse = new CIBillValidationResponse();

			utilityResponse.response = getPegasusResponseMessage(response);
			if (utilityResponse.response.responseCode != "0")
				return utilityResponse;

			utilityResponse.customerRef = response.ResponseField1;
			utilityResponse.customerName = response.ResponseField2;
			utilityResponse.area = response.ResponseField3;
			utilityResponse.outStandingBal = CONVERTER.toDouble(response.ResponseField4);
			return utilityResponse;
		}
		 
		public CIBillValidationResponse queryNWSCCustomerDetails(CIBillValidationRequest gwRequest)
		{
			QueryRequest request = new QueryRequest
			{
				QueryField1 = gwRequest.referenceNo,
				QueryField2 = gwRequest.customerArea,// {PREPAID, POSTPAID}
				QueryField3 = gwRequest.customerArea,
				QueryField4 = gwRequest.paymentCode, // NWSC,UMEME etc
				QueryField5 = PROPERTIES.PEGASUS_VENDOR_USERNAME,
				QueryField6 = PROPERTIES.PEGASUS_VENDOR_PASSWORD
			};
			LOGGER.objectInfo(request);                //request.PostField21 = Customer Type;
			start = DateTime.Now; //Start time
			Response response = pegPay.QueryCustomerDetails(request);
			end = DateTime.Now;   //End time
			timeDifference = end - start; //Time span between start and end = the time span needed to execute your method
			difference_Miliseconds = (int)timeDifference.TotalMilliseconds; //Gives you the time needed to execute the method in miliseconds (= time between start and end in miliseconds)
			LOGGER.info("==============" + gwRequest.paymentCode + " QUERY DETAILS DURATION: " + difference_Miliseconds + "ms: SOURCE ACCOUNT: " + request.QueryField1);
			LOGGER.objectInfo(response);
			CIBillValidationResponse utilityResponse = new CIBillValidationResponse();

			utilityResponse.response = getPegasusResponseMessage(response);
			if (utilityResponse.response.responseCode != "0")
				return utilityResponse;

			utilityResponse.customerRef = response.ResponseField1;
			utilityResponse.customerName = response.ResponseField2;
			utilityResponse.area = response.ResponseField3;
			utilityResponse.outStandingBal = CONVERTER.toDouble(response.ResponseField4);
			return utilityResponse;
		}


		public TxnResp queryTransactionStatus(string originTransId, string utilityCode)
		{
			QueryRequest request = new QueryRequest();
			request.QueryField4 = utilityCode;
			request.QueryField5 = PROPERTIES.PEGASUS_VENDOR_USERNAME;
			request.QueryField6 = PROPERTIES.PEGASUS_VENDOR_PASSWORD;
			request.QueryField10 = originTransId;
			LOGGER.objectInfo(request);                //request.PostField21 = Customer Type;
			start = DateTime.Now; //Start time
			Response utilResp = pegPay.GetTransactionDetails(request);
			end = DateTime.Now;   //End time
			timeDifference = end - start; //Time span between start and end = the time span needed to execute your method
			difference_Miliseconds = (int)timeDifference.TotalMilliseconds; //Gives you the time needed to execute the method in miliseconds (= time between start and end in miliseconds)
			LOGGER.objectInfo(utilResp);
			LOGGER.info("============== PEGPAY GET TRANSACTION STATUS DURATION: " + difference_Miliseconds + "ms: SOURCE ACCOUNT: " + request.QueryField6);

			ResponseMessage responseMessage = new ResponseMessage();
			responseMessage.responseCode = utilResp.ResponseField6 == "0" ? "0" : utilResp.ResponseField6;
			responseMessage.responseMessage = utilResp.ResponseField7;

			if (responseMessage.responseCode != "0")
				responseMessage.responseMessage = "ERROR: " + responseMessage.responseMessage + " AT " + utilityCode.ToUpper();

			TxnResp response = new TxnResp
			{
				response = responseMessage
			};

			//if (response.response.responseCode != "0")
			//	return response;

			response.bankRef = utilResp.ResponseField4;
			response.utilityRef = utilResp.ResponseField8;
			response.noOfUnits = utilResp.ResponseField9 + " " + "kWh";
			response.serviceFee = utilResp.ResponseField14;
			response.payAccount = utilResp.ResponseField15;
			response.debtRecovery = utilResp.ResponseField16;
			response.receiptNo = utilResp.ResponseField17;
			response.forexAdjustment = utilResp.ResponseField18;
			response.fuelAdjustment = utilResp.ResponseField19;
			response.inflationAdjustment = utilResp.ResponseField20;
			response.purchaseBreak = utilResp.ResponseField21;
			response.vat = utilResp.ResponseField22;
			response.tokenValue = utilResp.ResponseField8;
			return response;
		}


		private TxnResp getSuccessResponse(Response utilResp)
		{
			TxnResp response = new TxnResp
			{
				bankRef = utilResp.ResponseField4,
				utilityRef = utilResp.ResponseField8,
				noOfUnits = utilResp.ResponseField13,
				serviceFee = utilResp.ResponseField14,
				payAccount = utilResp.ResponseField15,
				debtRecovery = utilResp.ResponseField16,
				receiptNo = utilResp.ResponseField17,
				forexAdjustment = utilResp.ResponseField18,
				fuelAdjustment = utilResp.ResponseField19,
				inflationAdjustment = utilResp.ResponseField20,
				purchaseBreak = utilResp.ResponseField21,
				vat = utilResp.ResponseField22,
				tokenValue = utilResp.ResponseField23
			};

			response.response = getPegasusResponseMessage(utilResp);
			return response;
		}
	}
}