using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using micropay_apis.Utils;
using Npgsql;

namespace micropay_apis.DataAccess
{
	public class CommonDAO
	{
		public long generateTxnRef(string sequenceName, NpgsqlConnection conn)
		{
			NpgsqlCommand cmd = new NpgsqlCommand
			{
				CommandText = "SELECT nextval('" + PROPERTIES.SCHEMA_NAME + "." + sequenceName + "')",
				Connection = conn
			};
			long response = DBUtility.findLong(cmd);
			return response;
		}
	}
}