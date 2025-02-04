using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace micropay_apis.Models
{
    public class ReturnObject
    {
        public bool? lockedFlag { get; set; }
        public string accountNo { get; set; }
        public string acctTitle { get; set; }
        public double? chargedAmount { get; set; }
        public bool? pinChangeFlag { get; set; }
        public bool firstPinGenerated { get; set; }
        public string phoneNo { get; set; }
        public CustomerData customerData { get; set; }
        public string lastName { get; set; }

        public string studentFullName { get; set; }
        public ChargesData chargesData { get; set; }
        public string outstandingAmount { get; set; }
        public string returnMessage { get; set; }
        public string dateOfBirth { get; set; } 
        public string schoolAcctNo { get; set; }
        public string studentClass { get; set; }
        public string firstName { get; set; }
        public int? returnCode { get; set; }
        public string bankId { get; set; }
        public string paymentCode { get; set; }
        public string secondaryProgressIndicator { get; set; }
        public string registrationNumber { get; set; }
        public bool? allowPartPayments { get; set; }
        public string middleName { get; set; }
        public string schoolName { get; set; }
        public string schoolCode { get; set; }

        public string expiryDate { get; set; }
        public string amount { get; set; }
        public string customerName { get; set; }
        public string codeStatus { get; set; }
    }


    public class ChargesData
    {
        public double? totalCharge;
        public double? wht;
        public double? charge;
        public BillPayRespCode response;
        public double? commission;
        public double? exciseDuty;
    }
     
    public class BillPayRespCode
    {
        public string responseMessage;
        public string responseCode;
    }


    public class CustomerData
    {
        public string lastName;
        public string outstandingAmount;
        public string returnMessage;
        public string dateOfBirth;
        public string studentClass;
        public string firstName;
        public int returnCode;
        public string bankId;
        public string paymentCode;
        public string registrationNumber;
        public bool allowPartPayments;
        public string middleName;
        public string schoolName;
        public string schoolCode;
    }


    public class CoporateAgencyResponse
    {
        public CoporateAgencyResponse(int? returnCode, string returnMessage) {
            this.returnCode = returnCode;
            this.returnMessage = returnMessage;
        }

        public CoporateAgencyResponse() { }

        public int? clientCommand { get; set; }
        public string progressIndicator { get; set; }
        public int? returnCode { get; set; }
        public string returnMessage { get; set; }
        public string printData { get; set; }
        public double? drAcctBal { get; set; }
        public string transId { get; set; }
        public ReturnObject returnObject { get; set; }
        public string ussdResponse { get; set; }
    }


}