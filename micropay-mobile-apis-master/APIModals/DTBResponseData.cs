
namespace micropay_apis.APIModals
{
    public class DTBResponseData
    {
        public string UraStatusCode { get; set; }
        public string UraStatusDesc { get; set; }
        public double? UraAmount { get; set; }
        public string UraPaymentRegDt { get; set; }
        public string UraTin { get; set; }
        public string UraExpiryDt { get; set; }
        public string UraTaxpayerName { get; set; }
        public string BankReceiptRef { get; set; }
        public string Trn { get; set; }
        public string Result { get; set; }
        public string ResultDesc { get; set; }
    }
}
