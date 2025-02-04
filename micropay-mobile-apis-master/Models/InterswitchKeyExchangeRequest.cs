
namespace micropay_apis.Models
{
    public class InterswitchKeyExchangeRequest : InterswitchClientTerminalRequest
    {
        public string password;
        public string clientSessionPublicKey;
    }
}
