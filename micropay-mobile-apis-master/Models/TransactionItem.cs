
namespace micropay_apis.Models
{
    public class TransactionItem
	{ 
        public string transDescr { set; get; }
        public string crAcctNo;
        public string drAcctNo;
        public double amount;
        public long itemNo; 
        public double mainTransId { set; get; }
        public string entryType;
        public string crAcctType;
        public string payeeName;
        public string currency;
        public string drAcctType;
        public string userCode;
        public string phoneNo;
        public string senderBankCode;
        public string receipientBankCode;
    }
}