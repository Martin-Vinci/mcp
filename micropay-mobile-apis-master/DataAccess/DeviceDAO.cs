using System.Data;
using micropay_apis.Models;
using Npgsql;

namespace micropay_apis.DataAccess
{
	public class DeviceDAO
	{
		public DataTable findEntityDevice(Authentication authRequest, NpgsqlConnection sqlConn, NpgsqlTransaction sqlTran)
		{
			string query = "select imei_no from rm_cust_device_info where item_id = " + authRequest.outletCode.Trim() + " And device_id =  '" + authRequest.deviceId + "'";
			NpgsqlCommand command = new NpgsqlCommand();
			command.Connection = sqlConn;
			command.Transaction = sqlTran;
			command.CommandText = query;
			return DBUtility.findDataTable(command);
		}
	}
}