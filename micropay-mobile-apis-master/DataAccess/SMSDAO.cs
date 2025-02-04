using System;
using System.Data;
using Npgsql;

namespace Orbitlite.DataAccess
{
	public class SMSDAO
	{ 
		public void saveSMS(string messageBody, string mobilePhone, string messageResponse, NpgsqlConnection sqlConn, NpgsqlTransaction sqlTrans)
		{
			NpgsqlCommand cmd = null;
			cmd = new NpgsqlCommand();
			String query = "INSERT INTO Nep_Sent_Messages(PhoneNo,MessageBody,Create_Date,MessageResponse)VALUES('" + mobilePhone + "','" + messageBody + "',GETDATE(),'" + messageResponse + "')";
			cmd.CommandText = query;
			cmd.CommandType = CommandType.Text;			
			cmd.Connection = sqlConn;
			cmd.Transaction = sqlTrans;
			DBUtility.createRecord(cmd);
		}
	}
}