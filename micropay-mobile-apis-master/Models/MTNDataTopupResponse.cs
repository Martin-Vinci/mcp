using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace micropay_apis.Models
{
	public class MTNDataTopupResponse
	{
		public string statusCode;
		public string subscriptionId;
		public string subscriptionProviderId;
		public string subscriptionName;
		public string subscriptionDescription;
		public string subscriptionStatus;
		public string subscriptionType;
		public string subscriptionLength;
		public string registrationChannel;
		public string correlationId;
		public string startDate;
		public string endDate;
		public string email;
		public double? amountCharged;
		public string subscriptionPaymentSource;
		public string sendSMSNotification;
		public string beneficiaryId;
		public string autoRenew;
		public string transactionId;
		public string statusDescription;
		public ResponseMessage responseStatus;
	}
}