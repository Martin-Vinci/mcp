namespace micropay_apis.APIModals
{
    public class SurePayTxnStatusCheckResp
    {
        public string accountNumber { get; set; }
        public string accountName { get; set; }
        public string accountType { get; set; }
        public string bankCode { get; set; }
        public string transactionId { get; set; }
        public string gatewayReference { get; set; }
        public string currency { get; set; }
        public string transactionStatus { get; set; }
        public string transactionDescription { get; set; }
        public string tranAmount { get; set; }
        public string statusCode { get; set; }
        public string statusDescription { get; set; }

    }
}
