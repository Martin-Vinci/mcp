using micropay_apis.APIModals;
using micropay_apis.Models;
using System;
using System.Collections.Generic;

public class RequestPayment
{ 
    public string action { get; set; }
    public decimal? amount { get; set; }
    public string customerName { get; set; }
    public string fromPhone { get; set; }
    public int? requestId { get; set; }
    public string requesterPhone { get; set; }
    public string requesterReason { get; set; }
    public string createDate { get; set; }
    public string status { get; set; }
    public OutletAuthentication authRequest { get; set; }
}

public class RequestPaymentData 
{
    public List<RequestPayment> data;
    public ResponseMessage response;
}
