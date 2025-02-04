using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace micropay_apis.APIModals
{
    public class SurePayValidationReqst
    {
        public string accountNumber { get; set; }
        public string bankCode { get; set; }
        public string password { get; set; }
        public string accountType { get; set; }
        public string accountCategory { get; set; }
    }
}
