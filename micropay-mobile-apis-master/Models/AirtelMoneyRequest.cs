
using System.Collections.Generic;

namespace micropay_apis.Models
{
    public class AdditionalInfo
    {
        public AdditionalInfo(string key, string value)
        {
            this.key = key;
            this.value = value;
        }

        public string key;
        public string value;
    }

    public class Subscriber
    {
        public string msisdn;
    }

    public class Transaction
    {
        public long amount;
        public string id;
    }


    public class AirtelMoneyRequest
    {
        public Subscriber subscriber;
        public Transaction transaction;
        public List<AdditionalInfo> additional_info;
        public long reference;
        public string pin;
    }
}


