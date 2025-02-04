public class InterswitchPaymentResponse
{
        public string transactionReference { get; set; }
        public string biller { get; set; }
        public string customerId { get; set; }
        public string customerName { get; set; }
        public string paymentItem { get; set; }
        public string narration { get; set; }
        public double amount { get; set; }
        public double totalAmount { get; set; }
        public string collectionsAccountNumber { get; set; }
        public double surcharge { get; set; }
        public double excise { get; set; }
        public double balance { get; set; }
        public string balanceNarration { get; set; }
        public string balanceType { get; set; }
        public bool displayBalance { get; set; }
        public string alternateCustomerId { get; set; }
        public string address { get; set; }
        public string thirdPartyCode { get; set; }
        public string additionalData { get; set; }
        public string retrievalReference { get; set; }
        public bool amountFixed { get; set; }
        public bool feeInclusiveInAmount { get; set; }
    }


    public class ApiResponse<T>
    {
        public string responseCode { get; set; }
        public string responseMessage { get; set; }
        public string requestReference { get; set; }
        public T response { get; set; }
    }
