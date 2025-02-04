namespace micropay_apis.APIModals
{
    public class SurePayResp
    {
        public string transactionId { get; set; }
        public string gatewayReference { get; set; }
        public string statusCode { get; set; }
        public string statusDesc { get; set; }
    }
}
