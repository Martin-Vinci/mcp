namespace micropay_apis.Models
{
    public class InterswitchClientRegistrationDetails : InterswitchClientTerminalRequest
	{
		public string name{ get; set; }
		public string phoneNumber{ get; set; }
		public string nin{ get; set; }
		public string gender{ get; set; }
		public string emailAddress{ get; set; }
		public string ownerPhoneNumber{ get; set; }
		public string publicKey{ get; set; }
		public string clientSessionPublicKey{ get; set; }
	}
}
