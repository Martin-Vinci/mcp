using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace micropay_apis.Models
{
    public class InterswitchPaymentRequest
    {
        public string requestReference { get; set; }
        public double amount { get; set; }
        public string customerId { get; set; }
        public string phoneNumber { get; set; }
        public long paymentCode { get; set; }
        public string billerCode { get; set; }
        public string customerName { get; set; }
        public string sourceOfFunds { get; set; }
        public string narration { get; set; }
        public string depositorName { get; set; }
        public string location { get; set; }
        public string alternateCustomerId { get; set; }
        public string transactionCode { get; set; }
        public string customerToken { get; set; }
        public string additionalData { get; set; }
        public string collectionsAccountNumber { get; set; }
        public string pin { get; set; }
        public string otp { get; set; }
        public string currencyCode { get; set; }
    }
}
