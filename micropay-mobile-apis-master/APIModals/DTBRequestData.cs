using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace micropay_apis.APIModals
{
    public class DTBRequestData
    {
        public string UserId { get; set; }
        public string Password { get; set; }
        public string Method { get; set; }
        public string TerminalId { get; set; }
        public string AgentCode { get; set; }
        public string Trn { get; set; }
        public string ScenarioType { get; set; }
        public string Prn { get; set; }
        public PrnData PrnData { get; set; }
    }

    public class PrnData
    {
        public string Prn { get; set; }
        public double Amount { get; set; }
        public double? Charge { get; set; }
        public double? Tax { get; set; }
        public string TxtDate { get; set; }
        public string MobileNo { get; set; }
        public string AgentFloatAcct { get; set; }
    }

}
