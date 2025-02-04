using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Text.RegularExpressions;
using System.Web;
using System.Xml;
using System.Xml.Serialization;
using micropay_apis.APIModals;
using micropay_apis.Models;
using Nancy.Json;

namespace micropay_apis.Utils
{
	public class CONVERTER
	{ 
        public static string objectToXMLString(object objectData)
        {
            string xmlString = "";
            try
            {
                if (objectData is SignUpRequest)
                {
                    SignUpRequest data = (SignUpRequest)objectData;
                    data.customerPhoto = null;
                    data.customerSign = null;
                    objectData = data;
                }

                XmlDocument xmlDoc = new XmlDocument();
                XmlSerializer xmlSerializer = new XmlSerializer(objectData.GetType());
                using (MemoryStream xmlStream = new MemoryStream())
                {
                    XmlSerializerNamespaces _namespace = new XmlSerializerNamespaces();
                    _namespace.Add("", "");
                    xmlSerializer.Serialize(xmlStream, objectData, _namespace);
                    xmlStream.Position = 0;
                    xmlDoc.Load(xmlStream);
                    xmlString = xmlDoc.InnerXml;
               }
            }
            catch (Exception ex)
            {
                LOGGER.error(ex.ToString());
            }
            return xmlString;
        } 
        public static string objectToJson(object objectData)
        {
            string jsonString = null; 
            try
            {
                JavaScriptSerializer jsonSerializer = new JavaScriptSerializer();
                jsonString = jsonSerializer.Serialize(objectData);
            }
            catch (Exception ex)
            {
                LOGGER.error(ex.ToString());
            }
            return jsonString;
        }
         
        public static XmlDocument toXML(string xmlData) {
            var regex = new Regex(@"[\s]+(?![^><]*(?:>|<\/))");
            string Replace1 = ">\\s+";
            string Replace2 = "\\s+<";
            xmlData = Regex.Replace(xmlData, Replace1, ">");
            xmlData = Regex.Replace(xmlData, Replace2, "<");
            xmlData = xmlData.Replace("< ", "<").Replace(" />", "/>");
            xmlData = xmlData.Replace("UTF - 8", "UTF-8");
            xmlData = regex.Replace(xmlData, "");
            XmlDocument respXML = new XmlDocument();
            respXML.PreserveWhitespace = false;
            xmlData = xmlData.Trim();
            respXML.LoadXml(xmlData);
            return respXML;
        }


        public static string toSentenceCase(string input)
        {
            if (string.IsNullOrEmpty(input))
                return input;

            TextInfo textInfo = new CultureInfo("en-US", false).TextInfo;
            string[] sentences = input.Split(new[] { '.' }, StringSplitOptions.RemoveEmptyEntries);

            for (int i = 0; i < sentences.Length; i++)
            {
                sentences[i] = sentences[i].Trim();
                if (!string.IsNullOrEmpty(sentences[i]))
                {
                    sentences[i] = textInfo.ToTitleCase(sentences[i]);
                    if (i < sentences.Length - 1)
                        sentences[i] += ".";
                }
            }

            return string.Join(" ", sentences);
        }


		public static double toDouble(object value)
		{
			double returnOut = 0;
			try
			{
				if (value is string)
					value = Convert.ToString(value).Replace(",", "");
				returnOut = Convert.ToDouble(value);
			}
			catch (Exception ex)
			{

			}
			return returnOut;
		}

		public static long toLong(string stringTest)
        {
            long returnOut = 0;
            try
            {
                returnOut = long.Parse(stringTest.Replace(",", ""));
            }
            catch (Exception ex)
            {

            }
            return returnOut;
        }

         
        public static int toInt(string stringTest)
        {
            int returnOut = 0;
            try
            {
                returnOut = Int32.Parse(stringTest.Replace(",", ""));
            }
            catch (Exception ex)
            {

            }
            return returnOut;
        }

        public static long? toNullableLong(string stringTest)
        {
            long? returnOut = null;
            try
            {
                returnOut = long.Parse(stringTest.Replace(",", ""));
            }
            catch (Exception ex)
            {

            }
            return returnOut;
        }

        public static double? toNullableDouble(string stringTest)
        {
            double? returnOut = null;
            try
            {
                returnOut = Double.Parse(stringTest.Replace(",", ""));
            }
            catch (Exception ex)
            {

            }
            return returnOut;
        }


        public static DateTime toDate(string stringTest)
        {
            DateTime returnOut = DateTime.Now;
            try
            {
                returnOut = DateTime.Parse(stringTest);
            }
            catch (Exception ex)
            {

            }
            return returnOut;
        }

        public static DateTime? toNullableDate(string stringTest)
        {
            DateTime? returnOut = null;
            try
            {
                returnOut = DateTime.Parse(stringTest.Trim());
            }
            catch (Exception ex)
            {

            }
            return returnOut;
        }

        private static bool isNullEmpty(string str)
        {
            // check if string is null
            if (str == null)
            {
                return true;
            }

            // check if string is empty
            else if (str == "")
            {
                return true;
            }
            else
            {
                return false;
            }
        }


        public static string formatPhoneNumber(string phoneNo)
        {
            string response;
            if (isNullEmpty(phoneNo))
                throw new MediumsException(
                    new ResponseMessage("-99","Invalid mobile phone number, format should be 256XXXXXXXXX"));
           
            phoneNo = phoneNo.Trim();
            string phoneCode = phoneNo.Substring(0, 2);
            if (phoneCode == "07" || phoneCode == "03")   //Check whether Starting didgits are fine, Proceed and check the length
            {
                if (phoneNo.Length == 10)
                {
                    response = "256" + phoneNo.Substring(1);
                    return response;
                }
                else
                    throw new MediumsException(
                      new ResponseMessage("-99", "Invalid mobile phone number, format should be 256XXXXXXXXX"));
            }
            else if (phoneCode.Equals("25"))   //Check whether Starting didgits are fine, Proceed and check the length
            {
                if (phoneNo.Length == 12)
                    return phoneNo;
                else
                    throw new MediumsException(
                       new ResponseMessage("-99", "Invalid mobile phone number, format should be 256XXXXXXXXX"));
            }
            else
            {
                throw new MediumsException(
                      new ResponseMessage("-99", "Invalid mobile phone number, format should be 256XXXXXXXXX"));
            }
        }

        public static string getXMLNode(XmlDocument requestXML, string path)
        {
            try
            {
                return requestXML.SelectSingleNode(path).InnerText;
            }
            catch (Exception e)
            {
                LOGGER.error(e.ToString());
                return null;
            }
        }
         

        public static ResponseMessage getBillerCode(int tranCode) {
            ResponseMessage response = null;
            if (tranCode == TRANCODES.GOTV_CUSTOMER || tranCode == TRANCODES.GOTV_AGENT)
                response = new ResponseMessage("GOTV", "GTV Purcharse");
            else if (tranCode == TRANCODES.DSTV_CUSTOMER || tranCode == TRANCODES.DSTV_AGENT)
                response = new ResponseMessage("DSTV", "DSTV Purchase");
            else if (tranCode == TRANCODES.ZUKU_CUSTOMER || tranCode == TRANCODES.ZUKU_AGENT)
                response = new ResponseMessage("ZUKU", "ZUKU Purchase");
            else if (tranCode == TRANCODES.STARTIMES_CUSTOMER || tranCode == TRANCODES.STARTIMES_AGENT)
                response = new ResponseMessage("STARTIMES", "StartTimes Purchase");
            else if (tranCode == TRANCODES.AZAM_CUSTOMER || tranCode == TRANCODES.AZAM_AGENT)
                response = new ResponseMessage("AZAM", "AZAM TV Purchase");
            else if (tranCode == TRANCODES.LYCA_AIRTIME_AGENT || tranCode == TRANCODES.LYCA_AIRTIME_CUSTOMER)
                response = new ResponseMessage("LYCA_AIRTIME", "LycaMobile Airtime Purchase");
            else if (tranCode == TRANCODES.LYCA_DATA_AGENT || tranCode == TRANCODES.LYCA_DATA_CUSTOMER)
                response = new ResponseMessage("LYCA_DATA", "LycaData Purchase");
            else if (tranCode == TRANCODES.AIRTEL_DATA_CUSTOMER || tranCode == TRANCODES.AIRTEL_DATA_AGENT)
                response = new ResponseMessage("AIRTEL_DATA", "AirtelData Purchase");
            else if (tranCode == TRANCODES.MTN_CASH_DEPOSIT || tranCode == TRANCODES.MTN_CASH_DEPOSIT_CUSTOMER)
                response = new ResponseMessage("MTN_MOBILE_MONEY", "MTN MM Cash Deposit");
            else if (tranCode == TRANCODES.MTN_CASH_WITHDRAW)
                response = new ResponseMessage("MTN_MOBILE_MONEY", "MTN MM Cash Withdraw");
            else if (tranCode == TRANCODES.AIRTEL_CASH_DEPOSIT || tranCode == TRANCODES.AIRTEL_CASH_DEPOSIT_CUSTOMER)
                response = new ResponseMessage("AIRTEL_MONEY", "Airtel Money Cash Deposit");
            else if (tranCode == TRANCODES.AIRTEL_CASH_WITHDRAW)
                response = new ResponseMessage("AIRTEL_MONEY", "Airtel Money Cash Withdraw");
            else if (tranCode == TRANCODES.WENRECO_AGENT || tranCode == TRANCODES.WENRECO_CUSTOMER)
                response = new ResponseMessage("WENRECO", "WENRECO Payment");
            else if (tranCode == TRANCODES.TUDENDE_AGENT || tranCode == TRANCODES.TUDENDE_CUSTOMER)
                response = new ResponseMessage("TUGENDE", "Tugende Payment");
            else if (tranCode == TRANCODES.URA_AGENT || tranCode == TRANCODES.URA_CUSTOMER)
                response = new ResponseMessage("URA", "URA Tax Payment");
            else if (tranCode == TRANCODES.UTL_AGENT || tranCode == TRANCODES.UTL_CUSTOMER)
                response = new ResponseMessage("UTL_AIRTIME", "UTL Data Purchase");
            else if (tranCode == TRANCODES.ROKE_AGENT || tranCode == TRANCODES.ROKE_CUSTOMER)
                response = new ResponseMessage("ROKE_TELKOM", "Roke Data Purchase");
            else if (tranCode == TRANCODES.AIRTEL_VOICE_AGENT || tranCode == TRANCODES.AIRTEL_VOICE_CUSTOMER)
                response = new ResponseMessage("AIRTEL_VOICE", "Airtel Voice Purchase");
            else if (tranCode == TRANCODES.MTN_VOICE_AGENT || tranCode == TRANCODES.MTN_VOICE_CUSTOMER)
                response = new ResponseMessage("MTN_VOICE", "MTN Voice Purchase");
            else if (tranCode == TRANCODES.MTN_DATA_AGENT || tranCode == TRANCODES.MTN_DATA_CUSTOMER)
                response = new ResponseMessage("MTN_DATA", "MTN Data Purchase");
            else if (tranCode == TRANCODES.AIRTEL_DATA_AGENT || tranCode == TRANCODES.AIRTEL_DATA_CUSTOMER)
                response = new ResponseMessage("AIRTEL_DATA", "Airtel Data Purchase");
            else if (tranCode == TRANCODES.SUREPAY_SCHOOL_FEES_CUSTOMER || tranCode == TRANCODES.SUREPAY_SCHOOL_FEES_AGENT)
                response = new ResponseMessage("SUREPAY", "SurePay | School Fees Payment");


            else
            {
				throw new MediumsException(new ResponseMessage("-99", "Biller code for service [" + tranCode + "] is not mapped at the MCP Gateway"));
			}

            return response;

        }






        public static string getCorporateDrAcct(string sourceCode, string productCode) {
            if (sourceCode == "TOTAL")
                return PROPERTIES.TOTAL_FUND_ACCOUNT;

            else {
                if (productCode == "AIRTEL")
                    return PROPERTIES.CENTE_AGENT_FUND_ACCOUNT_AIRTEL;
                if (productCode == "MTN")
                    return PROPERTIES.CENTE_AGENT_FUND_ACCOUNT_MTN;
                else
                    return PROPERTIES.CENTE_AGENT_FUND_ACCOUNT_MTN;
            }
        }
         
         
        public static int getBillerServiceCode(string sourceCode, string productCode)
        {
            if (sourceCode == "TOTAL")
            {
                if (productCode == "AIRTEL")
                    return TRANCODES.AIRTEL_AIRTIME_CUSTOMER;
                if (productCode == "MTN")
                    return TRANCODES.MTN_AIRTIME_CUSTOMER;
            }
            else
            {
                if (productCode == "AIRTEL")
                    return TRANCODES.CENTE_AGENT_AIRTEL_AIRTIME;
                if (productCode == "MTN")
                    return TRANCODES.CENTE_AGENT_MTN_AIRTIME;
            }
            return 0;
        }
        
        public static int getCorporateServiceCode(TxnData request) {
            OutletAuthentication authRequest = request.authRequest;
            if (authRequest.outletCode != null)
            {
                if (request.paymentCode == "CASH_DEPOSIT_COMPLETION")
                    return TRANCODES.CENTENARY_CASH_DEPOSIT;
                else if (request.paymentCode == "CASH_WITHDRAW_COMPLETION")
                    return TRANCODES.CENTENARY_CASH_WITHDRAW;
                else if (request.paymentCode == "CONFIRM_BILL_PAYMENT")
                    return TRANCODES.CENTENARY_SCHOOL_PAY_AGENT;
                else
                    return 0;
            }
            else
            {
                if (request.paymentCode == "CASH_DEPOSIT_COMPLETION")
                    return TRANCODES.CENTENARY_CASH_DEPOSIT_CUSTOMER;
                else if (request.paymentCode == "CONFIRM_BILL_PAYMENT")
                    return TRANCODES.CENTENARY_SCHOOL_PAY_CUSTOMER;
                else
                    return 0;
            }
        }


















    }
}