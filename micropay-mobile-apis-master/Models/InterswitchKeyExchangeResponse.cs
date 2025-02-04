
namespace micropay_apis.Models
{
    public class InterswitchKeyExchangeResponse
    {
        public string authToken;
        public string serverSessionPublicKey;
        public string expireTime;
        public bool requiresOtp;
        public string terminalKey;
    }
}
