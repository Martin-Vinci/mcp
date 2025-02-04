

namespace micropay_apis.Models
{
    public class InterswitchRegistrationResp
    {
		public static long serialVersionUID = 6595929533116515317L;
		public string firstname{ get; set; }
		public string lastname{ get; set; }
		public string username{ get; set; }
		public string name{ get; set; }
		public string contact{ get; set; }
		public string authToken{ get; set; }
		public string merchantId{ get; set; }
		public string userId{ get; set; }
		public string terminalId{ get; set; }
		public bool active{ get; set; }
		public string location{ get; set; }
		public string operatorName{ get; set; }
		public string clientSecret{ get; set; }
		public string serverSessionPublicKey{ get; set; }
		public bool requiresOtp{ get; set; }
		public string tpk{ get; set; }
		public string tpkIV{ get; set; }
		public string currencySymbol{ get; set; }
		public string currencyCode{ get; set; }
	}
}
