namespace micropay_apis.APIModals
{
    public class SurePayRequest
    {
        public string accountNumber { get; set; }
        public string accountName { get; set; }
        public string accountType { get; set; }
        public string accountCategory { get; set; }
        public string accountProvider { get; set; }
        public string bankCode { get; set; }
        public string password { get; set; }
        public string tranAmount { get; set; }
        public string tranType { get; set; }
        public string tranCategory { get; set; }
        public string channel { get; set; }
        public string currency { get; set; }
        public string paymentDate { get; set; }
        public string tranSignature { get; set; }
        public string transactionId { get; set; }
        public string narration { get; set; }
    }
}
