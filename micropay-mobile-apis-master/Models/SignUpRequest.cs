using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using micropay_apis.APIModals;

namespace micropay_apis.Models
{
	public class SignUpRequest
	{
		public string firstName;
		public string surName;
		public string middleName;
		public string boxNumber;
		public DateTime dateOfBirth;
		public string mobilePhone;
		public string gender;
		public int idType;
		public string idNumber;
		public DateTime? idIssueDate;
		public DateTime? idExpiryDate;
		public int? titleId;
		public string town;
		public short noOfChildren;
		public int nationality;
		public string accountNo;
		public string acctType;
		public string maritalStatus;
		public string fatherName;
		public string motherName;
		public string emailAddress;
		public string addressLine1;
		public string addressLine2;
		public string addressRegion;
		public string authImsi;
		public string authImei;
		public byte[] customerPhoto;
		public byte[] customerSign;
		public OutletAuthentication authRequest;
	}
}