
namespace micropay_apis.Models
{
    public class InterswitchCompleteClientRegistration : InterswitchClientTerminalRequest
    {
        public string otp;
        public string password;
        public string transactionReference;
    }
}
