using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using micropay_apis.Models;

namespace micropay_apis.Utils
{
	public class YOXMLHandler
	{
		public static string getMiniStatementString(CIStatementRequest request)
		{
			string xml_data = "<?xml version='1.0' encoding='UTF-8'?>"
				+ "<AutoCreate>"
				+ "<Request>"
				+ "<APIUsername>" + PROPERTIES.YO_UGANDA_API_USERNAME + "</APIUsername >"
				+ "<APIPassword>" + PROPERTIES.YO_UGANDA_API_PASSWORD + "</APIPassword>"
				+ "<Method>acgetministatement</Method>"
				+ "<StartDate>" + request.fromDate.Value.ToString("yyyy-MM-dd hh:mm:ss") + "</StartDate>"
				+ "<EndDate>" + request.toDate.Value.ToString("yyyy-MM-dd hh:mm:ss") + "</EndDate>"
				+ "<TransactionStatus>SUCCEEDED</TransactionStatus>"
				+ "<TransactionEntryDesignation>TRANSACTION</TransactionEntryDesignation>"
				+ "<ResultSetLimit>50</ResultSetLimit>"
				+ "</Request>"
				+ "</AutoCreate>";
			return xml_data;
		}
		public static string getBalanceRequestString()
		{
			string xml_data = "<?xml version='1.0' encoding='UTF-8'?>"
				+ "<AutoCreate>"
				+ "<Request>"
				+ "<APIUsername>" + PROPERTIES.YO_UGANDA_API_USERNAME + "</APIUsername >"
				+ "<APIPassword>" + PROPERTIES.YO_UGANDA_API_PASSWORD + "</APIPassword>"
				+ "<Method>acacctbalance</Method>"
				+ "</Request>"
				+ "</AutoCreate>";
			return xml_data;
		}
		public static string getWithdrawXMLString(YoCashRequest request)
		{
			string xml_data = "<?xml version='1.0' encoding='UTF-8'?>"
				+ "<AutoCreate>"
				+ " <Request>"
				+ " <APIUsername>" + PROPERTIES.YO_UGANDA_API_USERNAME + "</APIUsername>"
				+ " <APIPassword>" + PROPERTIES.YO_UGANDA_API_PASSWORD + "</APIPassword>"
				+ " <Method>acwithdrawfunds</Method>"
				+ " <Amount>" + request.transAmt + "</Amount>"
				+ " <Account>" + request.phoneNo + "</Account>"
				+ " <Narrative>" + request.description + "</Narrative>"
				+ " </Request>"
				+ "</AutoCreate>";
			return xml_data;
		}


		public static string getDepositXMLString(YoCashRequest request)
		{
			string xml_data = "<?xml version='1.0' encoding='UTF-8'?>"
				+ "<AutoCreate>"
				+ " <Request>"
				+ " <APIUsername>" + PROPERTIES.YO_UGANDA_API_USERNAME + "</APIUsername>"
				+ " <APIPassword>" + PROPERTIES.YO_UGANDA_API_PASSWORD + "</APIPassword>"
				+ " <Method>acdepositfunds</Method>"
				+ " <NonBlocking>TRUE</NonBlocking>"
				+ " <Amount>" + request.transAmt + "</Amount>"
				+ " <Account>" + request.phoneNo + "</Account>"
				+ " <Narrative>" + request.description + "</Narrative>"
				+ " </Request>"
				+ "</AutoCreate>";
			return xml_data;
		}


	}
}