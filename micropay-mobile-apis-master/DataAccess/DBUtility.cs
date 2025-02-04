using System;
using System.Data;
using System.Data.SqlClient;
using micropay_apis.Utils;
using Npgsql;
using NpgsqlTypes;

public class DBUtility
{
	private static string getCommandQuery(NpgsqlCommand NpgsqlCommand)
	{
		string query = NpgsqlCommand.CommandText;
		try
		{
			foreach (NpgsqlParameter sqlParameter in NpgsqlCommand.Parameters)
			{
				switch (sqlParameter.NpgsqlDbType)
				{
					case NpgsqlDbType.Bit:
						int boolToInt = (bool)sqlParameter.Value ? 1 : 0;
						query = query.Replace(sqlParameter.ParameterName, string.Format("{0}", (bool)sqlParameter.Value ? 1 : 0));
						break;
					case NpgsqlDbType.Integer:
						query = query.Replace(sqlParameter.ParameterName, string.Format("{0}", sqlParameter.Value));
						break;
					case NpgsqlDbType.Varchar:
						query = query.Replace(sqlParameter.ParameterName, string.Format("'{0}'", sqlParameter.Value));
						break;
					default:
						query = query.Replace(sqlParameter.ParameterName, string.Format("'{0}'", sqlParameter.Value));
						break;
				}
			}
		}
		catch (Exception ex)
		{
			LOGGER.error(ex.ToString());
		}
		return query;
	}

	public static string getCommandProcedure(NpgsqlCommand NpgsqlCommand)
	{
		string query = "";
		string outputParameters = " DECLARE ";
		bool isOutPutExists = false;
		try
		{
			foreach (NpgsqlParameter sqlParameter in NpgsqlCommand.Parameters)
			{

				switch (sqlParameter.Direction)
				{
					case ParameterDirection.Output:
						isOutPutExists = true;
						outputParameters += string.Format("{0}", sqlParameter.ParameterName + " " + sqlParameter.NpgsqlDbType) + ", ";
						break;
				}
			}
			if (isOutPutExists)
				query += outputParameters + Environment.NewLine;
			query += "EXEC " + NpgsqlCommand.CommandText + " ";

			foreach (NpgsqlParameter sqlParam in NpgsqlCommand.Parameters)
			{
				switch (sqlParam.NpgsqlDbType)
				{
					case NpgsqlDbType.Bit:
						int boolToInt = (bool)sqlParam.Value ? 1 : 0;
						query += string.Format("{0}", (bool)sqlParam.Value ? 1 : 0) + ", ";
						break;
					case NpgsqlDbType.Integer:
						query += string.Format("{0}", sqlParam.Direction == ParameterDirection.Output ? sqlParam.ParameterName + " OUTPUT" : (sqlParam.Value == DBNull.Value ? "NULL" : sqlParam.Value ?? "NULL")) + ", ";
						break;
					case NpgsqlDbType.Numeric:
						query += string.Format("{0}", sqlParam.Direction == ParameterDirection.Output ? sqlParam.ParameterName + " OUTPUT" : (sqlParam.Value == DBNull.Value ? "NULL" : sqlParam.Value ?? "NULL")) + ", ";
						break;
					case NpgsqlDbType.Varchar:
						query += string.Format("{0}", sqlParam.Direction == ParameterDirection.Output ? sqlParam.ParameterName + " OUTPUT" : (sqlParam.Value == DBNull.Value ? "NULL" : "'" + sqlParam.Value + "'" ?? "NULL")) + ", ";
						break;
					case NpgsqlDbType.Char:
						query += string.Format("{0}", sqlParam.Direction == ParameterDirection.Output ? sqlParam.ParameterName + " OUTPUT" : (sqlParam.Value == DBNull.Value ? "NULL" : "'" + sqlParam.Value + "'" ?? "NULL")) + ", ";
						break;
					case NpgsqlDbType.Date:
						query += string.Format("{0}", sqlParam.Direction == ParameterDirection.Output ? sqlParam.ParameterName + " OUTPUT" : (sqlParam.Value == DBNull.Value ? "NULL" : "'" + CONVERTER.toDate(sqlParam.Value.ToString()).ToString("yyyy-MM-dd") + "'" ?? "NULL")) + ", ";
						break;
					default:
						if (sqlParam.Direction != ParameterDirection.ReturnValue)
							query += string.Format("'{0}'", sqlParam.Direction == ParameterDirection.Output ? sqlParam.ParameterName + " OUTPUT" : sqlParam.Value) + ", ";
						break;
				}
			}

			if (isOutPutExists)
			{
				outputParameters = "SELECT ";
				foreach (NpgsqlParameter sqlParameter in NpgsqlCommand.Parameters)
				{
					switch (sqlParameter.Direction)
					{
						case ParameterDirection.Output:
							isOutPutExists = true;
							outputParameters += string.Format("{0}", sqlParameter.ParameterName) + ", ";
							break;
					}
				}
				query += Environment.NewLine + outputParameters;
			}
		}
		catch (Exception ex)
		{
			// Do Nothing
		}
		return query;
	}

	public static void processRecord(NpgsqlCommand NpgsqlCommand)
	{
		LOGGER.info(getCommandProcedure(NpgsqlCommand));
		NpgsqlCommand.ExecuteNonQuery();
	}



	public static DataTable findDataTable(NpgsqlCommand NpgsqlCommand)
	{
		DataTable dtContainer = new DataTable();
		LOGGER.info(getCommandQuery(NpgsqlCommand));
		using (NpgsqlDataAdapter dataAdapter = new NpgsqlDataAdapter(NpgsqlCommand))
		{
			dataAdapter.Fill(dtContainer);
		}
		return dtContainer;
	}
	 
	public static DataTable findDataTable(NpgsqlCommand NpgsqlCommand, NpgsqlTransaction sqlTran)
	{
		DataTable dtContainer = new DataTable();
		LOGGER.info(getCommandQuery(NpgsqlCommand));
		NpgsqlCommand.Transaction = sqlTran;
		using (NpgsqlDataAdapter dataAdapter = new NpgsqlDataAdapter(NpgsqlCommand))
		{
			dataAdapter.Fill(dtContainer);
		}
		return dtContainer;
	}
	 
	public static string findString(NpgsqlCommand NpgsqlCommand)
	{

		DataTable dt = new DataTable();
		string response = null;
		NpgsqlCommand.CommandType = CommandType.Text;
		LOGGER.info(getCommandQuery(NpgsqlCommand));
		NpgsqlDataAdapter sda = new NpgsqlDataAdapter(NpgsqlCommand);
		sda.Fill(dt);
		foreach (DataRow dr in dt.Rows)
		{
			response = dr[0].ToString();
		}
		return response;
	}

	public static string findString(NpgsqlCommand NpgsqlCommand, NpgsqlTransaction sqlTran)
	{

		DataTable dt = new DataTable();
		string response = null;
		LOGGER.info(getCommandQuery(NpgsqlCommand));
		NpgsqlCommand.CommandType = CommandType.Text;
		NpgsqlCommand.Transaction = sqlTran;
		NpgsqlDataAdapter sda = new NpgsqlDataAdapter(NpgsqlCommand);
		sda.Fill(dt);
		foreach (DataRow dr in dt.Rows)
		{
			response = dr[0].ToString();
		}
		return response;
	}


	public static int findInteger(NpgsqlCommand NpgsqlCommand)
	{

		DataTable dt = new DataTable();
		int response = 0;

		NpgsqlCommand.CommandType = CommandType.Text;
		try
		{
			NpgsqlDataAdapter sda = new NpgsqlDataAdapter(NpgsqlCommand);
			sda.Fill(dt);
			foreach (DataRow dr in dt.Rows)
			{
				response = Convert.ToInt32(dr[0].ToString());
			}
		}
		catch (Exception ex)
		{
			LOGGER.error(getCommandQuery(NpgsqlCommand));
			LOGGER.error(ex.ToString());
		}
		return response;
	}


	public static long findLong(NpgsqlCommand NpgsqlCommand)
	{
		DataTable dt = new DataTable();
		long response = 0;
		LOGGER.info(getCommandQuery(NpgsqlCommand));
		NpgsqlCommand.CommandType = CommandType.Text;
		NpgsqlDataAdapter sda = new NpgsqlDataAdapter(NpgsqlCommand);
		sda.Fill(dt);
		foreach (DataRow dr in dt.Rows)
		{
			response = long.Parse(dr[0].ToString());
		}
		return response;
	}

	public static double findDouble(NpgsqlCommand NpgsqlCommand)
	{
		DataTable dt = new DataTable();
		double response = 0;
		LOGGER.info(getCommandQuery(NpgsqlCommand));
		NpgsqlCommand.CommandType = CommandType.Text;
		NpgsqlDataAdapter sda = new NpgsqlDataAdapter(NpgsqlCommand);
		sda.Fill(dt);
		foreach (DataRow dr in dt.Rows)
		{
			response = double.Parse(dr[0].ToString());
		}
		return response;
	}

	public static int createRecord(NpgsqlCommand NpgsqlCommand, NpgsqlTransaction sqlTran)
	{
		int response = 1;
		LOGGER.info(getCommandQuery(NpgsqlCommand));
		NpgsqlCommand.Transaction = sqlTran;
		NpgsqlCommand.ExecuteNonQuery();
		response = 0;
		return response;
	}

	public static int createRecord(NpgsqlCommand NpgsqlCommand)
	{
		int response = 1;
		LOGGER.info(getCommandQuery(NpgsqlCommand));
		NpgsqlCommand.ExecuteNonQuery();
		response = 0;
		return response;
	}






}
