using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace micropay_apis.APIModals
{
    public class SurePayValidationResp
    {
        public string accountNumber { get; set; }
        public string accountName { get; set; }
        public string accountType { get; set; }
        public string accountCategory { get; set; }
        public string accountProvider { get; set; }
        public string outstandingBalance { get; set; }
        public string statusCode { get; set; }
        public string statusDesc { get; set; }
        public object bankAccountNumber { get; set; }
        public object bankId { get; set; }
        public string addendum { get; set; }
        public object bankName { get; set; }
    }
}
