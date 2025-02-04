namespace micropay_apis.Models
{
    public class CIBillValidationRequest
	{
		public string referenceNo {  get; set; }
		public string customerType;
		public string mobilePhone {  get; set; }	
		public string customerArea;
		public double transAmt { get; set; } 
		public string withdrawCode { get; set; }

        public string emailAddress;
		public string billerCode;
		public string paymentCode;
		public string customerName;
		public string categoryCode;
        public string channelSource;
        public Authentication authRequest;
	}
}