using System.Collections.Generic;

namespace micropay_apis.Models
{
    public class InterswitchPaymentItem
    {
        public string categoryid { get; set; }
        public int? billerid { get; set; }
        public bool isAmountFixed { get; set; }
        public int? paymentitemid { get; set; }
        public string paymentitemname { get; set; }
        public double? amount { get; set; }
        public string code { get; set; }
        public string currencyCode { get; set; }
        public string currencySymbol { get; set; }
        public string itemCurrencySymbol { get; set; }
        public string sortOrder { get; set; }
        public string pictureId { get; set; }
        public string paymentCode { get; set; }
    }

    public class PaymentResponse
    {
      public List<InterswitchPaymentItem> paymentitems { get; set; }
    }
}