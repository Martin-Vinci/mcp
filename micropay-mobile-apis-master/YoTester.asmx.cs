using System;
using System.Collections.Generic;
using System.Web.Services;
using micropay_apis.APIModals;
using micropay_apis.Models;
using micropay_apis.Remote;
using micropay_apis.Services;
using micropay_apis.Utils;
using Newtonsoft.Json;

namespace micropay_apis
{
	/// <summary>
	/// Summary description for YoTester
	/// </summary>
	[WebService(Namespace = "http://tempuri.org/")]
	[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
	[System.ComponentModel.ToolboxItem(false)]
	// To allow this Web Service to be called from script, using ASP.NET AJAX, uncomment the following line. 
	// [System.Web.Script.Services.ScriptService]
	public class YoTester : WebService 
	{  
		YoUgandaProcessor yo = new YoUgandaProcessor();
		InterswitchService interswitchService = new InterswitchService();
		PegasusService pegasusService = new PegasusService();
		GateWayService gateWayService = new GateWayService();
		LycaMobileService lycaMobileService = new LycaMobileService();



		[WebMethod]  
		public List<InterswitchPaymentItem> findPaymentItems(string billerId)
		{
			return interswitchService.findPaymentItems(billerId);
		}
		 

		[WebMethod] 
		public TxnResp reverseCenteTrustPosting(ReversalRequest request)
		{
			return gateWayService.reverseFromESB(request);
		}
		 

		[WebMethod]
		public TxnResp crdbPostTransaction(TransactionItem request)
		{
			return gateWayService.crdbPostTransaction(request);
		}


		[WebMethod]
		public TxnResp airtelCashOut(TxnData request)
		{
			try
			{
				OutletAuthentication authentication = request.authRequest;
				authentication.vPassword = "Cypres_4rtr##%JHFde";
				request.authRequest = authentication;
				return gateWayService.fundsTransfer(request);
			}catch(MediumsException e)
            {
				TxnResp response = new TxnResp();
				response.response = e.getErrorMessage();
				return response;
			}
		}
		 

		[WebMethod]
		public TxnResp airtelCashIn(TxnData request)
		{
			try
			{
				OutletAuthentication authentication = request.authRequest;
				authentication.vPassword = "Cypres_4rtr##%JHFde";
				request.authRequest = authentication;
				return gateWayService.fundsTransfer(request);
			}
			catch (MediumsException e)
			{
				TxnResp response = new TxnResp();
				response.response = e.getErrorMessage();
				return response;
			}
		}

		 
		[WebMethod]
		public TxnResp findAirtelTxnStatus(string reference)
		{
			return gateWayService.findAirtelTxnStatus(reference, "");
		}
		 

		[WebMethod]
		public AirtelMoneyResponse findBillers()
		{
            string json = @"
        {
            ""data"": {
                ""message"": ""You have deposited UGX 500 on 18-May-2024 18:09 Mobile Number: 0705387922 Trans ID: 104003325738. Your bal: UGX 8,500."",
                ""transaction"": {
                    ""airtel_money_id"": ""CASHIN_BCWQZ65Q69_534533"",
                    ""id"": ""534533""
                },
                ""status"": ""SUCCESS""
            },
            ""status"": {
                ""response_code"": ""DP01000001001"",
                ""code"": ""200"",
                ""success"": true,
                ""message"": ""SUCCESS""
            }
        }";

            // Deserialize JSON string to RootObject
            AirtelMoneyResponse rootObject = JsonConvert.DeserializeObject<AirtelMoneyResponse>(json);

            // Access properties
            Console.WriteLine("Data Message: " + rootObject.data.message);
            Console.WriteLine("Transaction Airtel Money ID: " + rootObject.data.transaction.airtel_money_id);
            Console.WriteLine("Transaction ID: " + rootObject.data.transaction.id);
            Console.WriteLine("Data Status: " + rootObject.data.status);
            Console.WriteLine("Response Code: " + rootObject.status.response_code);
            Console.WriteLine("Status Code: " + rootObject.status.code);
            Console.WriteLine("Success: " + rootObject.status.success);
            Console.WriteLine("Status Message: " + rootObject.status.message);
			return rootObject;// interswitchService.findBillers();
		}


		[WebMethod]
		public ExternalResponse purchaseLycaAirtime()
		{
			string xmlData = @"<?xml version=""1.0"" encoding=""UTF - 8""?>"
  + " < AMResponse>	"
  + "	<STATUS>FAILED</STATUS>"
  + "	 <DESCRIPTION>LOW ACCOUNT BALANCE </DESCRIPTION>"
  + "	 <STATUS_CODE>908</STATUS_CODE>"
  + "	 <Signature>f416a12d85bc75b3c475e1a702c90082</Signature>"
  + "</AMResponse>";	

			return lycaMobileService.getResponseMessage(CONVERTER.toXML(xmlData));
		}


		[WebMethod]
		public List<BillerProductCategory> findProductCategoryByBillerCode(string billerCode)
		{
			BillerProduct billerProduct = new BillerProduct();
			billerProduct.billerCode = billerCode;
			return gateWayService.findProductCategoryByBillerCode(billerProduct);
		}

		[WebMethod]
		public TxnResp queryUMEMETransactionStatus(string originTransId, string utilityCode)
		{
			string data = "\"-------------------------------\\nReceipt Number    2024070424514\\nDate           Thu, 04 Jul 2024\\n                       14:25:33\\nOutlet Name      MIREMBE MOBILE\\n                       AGENCIES\\nOutlet Code             3818001\\nOutlet Phone       256775581556\\n-------------------------------\\nPOSTBANK cash deposit received\\nAmount                6,000 UGX\\nSix Thousand Shillings Only\\nAccount No             ****0299\\nCust Name        AKINYI EVELYNE\\nBy                        JUNTA\\n                 \\u0028256775581556\\u0029\\nTrans ID               99141947\\nTrans Charge              0 UGX\\n-------------------------------\\nThank you for using Cente Agent\\nToll Free          0800 200 555\\nEmail: info@centenarybank.co.ug\\nWeb:    www.centenarybank.co.ug\\n  Centenary Bank, \\\"Our Bank\\\"   \\n-------------------------------\\n\"";
			ABCReceiptData receiptData = PrinterUtil.ParseReceipt(data);




            return pegasusService.queryTransactionStatus(originTransId, utilityCode);
		}

		private YoBalanceResp doBalanceInquiry()
		{
			return yo.doBalanceInquiry(new YoAcctBalRequest());
		}

		[WebMethod]
		private YoCashResponse doCashDeposit()
		{
			return yo.doCashIn(new YoCashRequest());
		}

		  
		[WebMethod]
		public TxnResp test2()
		{
		return	new MediumsService().test();
		}



		[WebMethod]
		public YoCashResponse doCashWithdraw()
		{
			YoCashRequest yoCashRequest = new YoCashRequest();
			yoCashRequest.transAmt = 10000;
			yoCashRequest.description = "NADDDDDDDD";
			yoCashRequest.phoneNo = "256774945464";
			return yo.doCashOut(yoCashRequest);
		}

		  
		[WebMethod]
		public TxnResp interswitchValidateCustomer(double amount)
		{
			CIBillValidationRequest request = new CIBillValidationRequest();
			request.mobilePhone = "0774945464";
			request.referenceNo = "4623017589";
			request.paymentCode = "215614";
			request.transAmt = amount;
			return new InterswitchService().validateCustomer(request);
		}

		 

		[WebMethod]
		private string smsTest()
		{
			CISMSRequest request = new CISMSRequest();
			request.phoneNo = "256772461049";
			request.messageText = "Testing SMS from Micropay Core Agency System";
			new SMSService().sendSMS(request);

			request.phoneNo = "256774945464";
			request.messageText = "Testing SMS from Micropay Core Agency System";
			return new SMSService().sendSMS(request);
		}


		[WebMethod]
		private ExternalResponse mtnAirtimeAPI()
		{
			TxnData request = new TxnData();
			request.description = "Testing"; // Narration;
			request.paymentCode = "RACT_UG_Data_63";
			request.description = "Airtime_Purchase";
			request.referenceNo = "0774945464";
			return null;
		}
		  

		[WebMethod]
		public CoporateAgencyResponse processCorporateAgentService(TxnData request)
		{
			LOGGER.objectInfo(request);
			if (request.authRequest == null)
				return new CoporateAgencyResponse(-99, "Authentication details is missing");
			if(request.authRequest.phoneNo == null)
				return new CoporateAgencyResponse(-99, "Device phone number is missing");
			return gateWayService.processCorporateAgentService(request);
		}
	}
}
