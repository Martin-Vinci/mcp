

namespace micropay_apis.Models
{
    public class InterswitchClientTerminalRequest
    {
        public string terminalId{ get; set; }
        public string appVersion{ get; set; }
        public string serialId{ get; set; }
        public string requestReference{ get; set; }
        public string gprsCoordinate{ get; set; }
    }
}
