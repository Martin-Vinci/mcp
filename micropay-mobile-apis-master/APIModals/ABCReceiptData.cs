using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace micropay_apis.APIModals
{
    public class ABCReceiptData
    {
        public string ReceiptNumber { get; set; }
        public string Date { get; set; }
        public string OutletName { get; set; }
        public string OutletCode { get; set; }
        public string OutletPhone { get; set; }
        public string PaymentStatus { get; set; }
        public string Amount { get; set; }
        public string AmountText { get; set; }
        public string AccountNo { get; set; }
        public string CustName { get; set; }
        public string By { get; set; }
        public string TransId { get; set; }
        public string TransCharge { get; set; }
        public string Footer { get; set; }
    }
}
