namespace micropay_apis.Models
{
	public class MTNTopupRequest
	{
		public bool retry; 
		public string txnRef; 
		public string mobilePhone;
		public string currencyCode;
		public double amount;
		public string terminalid; 
		public string comments;
		public string sourceCode;
		public string apiUserName;
		public string apiUserPassword;
	}
}