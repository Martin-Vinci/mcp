namespace micropay_apis.Models
{
    public class AirtelMoneyResponse
    {
        public AirtelMoneyResponse()
        {
        }
        public AirtelMoneyResponse(Status status, ResponseData data)
        {
            this.data = data;
            this.status = status;
        }
        public ResponseData data;
        public Status status;
    }
      
    public class ResponseData 
    {
        public string message;
        public string id;
        public string status;
        public AirtelTxnRef transaction;
    }   

    public class Status
    {
        public string code;
        public string message;
        public string response_code;
        public bool? success;
    }
      
    public class AirtelTxnRef
    {
        public string airtel_money_id;
        public string id;
        public string status;
    }


}
