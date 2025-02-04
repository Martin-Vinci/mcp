using System;
using System.Collections.Generic;
using System.Data;
using System.Data.SqlClient;
using System.Linq;
using micropay_apis.Models;
using micropay_apis.Services;
using micropay_apis.Utils;
using Npgsql;
using NpgsqlTypes;
using Orbitlite.DataAccess;

namespace micropay_apis.DataAccess
{
	public class PinsDAO
	{
		DateTime start; //Start time
		DateTime end;   //End time
		TimeSpan timeDifference; //Time span between start and end = the time span needed to execute your method
		int difference_Miliseconds;
		private string generateAgentPIN()
		{
			// Generate PIN Code
			string pinNo = "";
			char[] charArr = "0123456789".ToCharArray();
			string strrandom = string.Empty;
			Random objran = new Random();
			int noofcharacters = 4;
			for (int i = 0; i < noofcharacters; i++)
			{
				//It will not allow Repetation of Characters
				int pos = objran.Next(1, charArr.Length);
				if (!strrandom.Contains(charArr.GetValue(pos).ToString()))
					strrandom += charArr.GetValue(pos);
				else
					i--;
			}
			pinNo = strrandom;
			return pinNo;
		} 

		public CICustomerResp generateCustomerPin(string mobilePhone, string userName, NpgsqlConnection sqlConn, NpgsqlTransaction sqlTran)
		{
			string encryptedPin = "";
			ResponseMessage message = new ResponseMessage();
			CICustomerResp customerResponse = new CICustomerResp();
			CICustomerResp pinResponseData = new CICustomerResp();
			// Insert the Session into the database

			string plainPin = generateAgentPIN();
			string pinToHash = plainPin + "~" + mobilePhone.Trim();
			encryptedPin = ENCRYPTER.encryptWithTripleDES(pinToHash);

			NpgsqlCommand cmd = new NpgsqlCommand();
			cmd.CommandText = "Nep_Insert_PinNo";
			cmd.Connection = sqlConn;
			cmd.Transaction = sqlTran;
			cmd.CommandType = CommandType.StoredProcedure;
			cmd.Parameters.Add("@psPinNo", NpgsqlDbType.Varchar, 100).Value = encryptedPin.Trim();
			cmd.Parameters.Add("@psActivationCode", NpgsqlDbType.Varchar, 30).Value = DBNull.Value;
			cmd.Parameters.Add("@psPhoneNo", NpgsqlDbType.Varchar, 40).Value = mobilePhone.Trim();
			cmd.Parameters.Add("@psStatus", NpgsqlDbType.Varchar, 40).Value = "Pending";
			cmd.Parameters.Add("@pnExpiryPrd", NpgsqlDbType.Integer).Value = 6;
			cmd.Parameters.Add("@psExpiryPrdType", NpgsqlDbType.Varchar, 12).Value = "HOUR";

			NpgsqlParameter expiryDate = new NpgsqlParameter("@psExpiryDt", NpgsqlDbType.Varchar, 30);
			expiryDate.Direction = ParameterDirection.Output;
			cmd.Parameters.Add(expiryDate);

			NpgsqlParameter returnCode = new NpgsqlParameter("@pnReturnCode", NpgsqlDbType.Integer);
			returnCode.Direction = ParameterDirection.Output;
			cmd.Parameters.Add(returnCode);

			NpgsqlParameter errorDescr = new NpgsqlParameter("@psErrorDescr", NpgsqlDbType.Varchar, 100);
			errorDescr.Direction = ParameterDirection.Output;
			cmd.Parameters.Add(errorDescr);

			NpgsqlParameter maxTrialTimes = new NpgsqlParameter("@psMaxTrialTimes", NpgsqlDbType.Integer);
			maxTrialTimes.Direction = ParameterDirection.Output;
			cmd.Parameters.Add(maxTrialTimes);
			NpgsqlParameter pinChangeFlag = new NpgsqlParameter("@psPinChangeFlag", NpgsqlDbType.Char, 1);
			pinChangeFlag.Direction = ParameterDirection.Output;
			cmd.Parameters.Add(pinChangeFlag);
			NpgsqlParameter firstPinGenerated = new NpgsqlParameter("@psFirstPinGenerated", NpgsqlDbType.Char, 1);
			firstPinGenerated.Direction = ParameterDirection.Output;
			cmd.Parameters.Add(firstPinGenerated);
			NpgsqlParameter pinLocked = new NpgsqlParameter("@psPinLocked", NpgsqlDbType.Char, 1);
			pinLocked.Direction = ParameterDirection.Output;
			cmd.Parameters.Add(pinLocked);
			cmd.Parameters.Add("@psCreatedBy", NpgsqlDbType.Varchar, 20).Value = userName;
			DBUtility.processRecord(cmd);
			if (returnCode.Value.ToString() != "0")
			{
				message.responseCode = returnCode.Value.ToString();
				message.responseMessage = errorDescr.Value.ToString();
				throw new MediumsException(message);
			}
			string expiryDt = expiryDate.Value.ToString();
			pinResponseData = customerResponse;
			string messageBody = "Centenary: Your Cente Agent PIN is " + plainPin.Trim() + ". Expiry date: " + expiryDt;
			pinResponseData.lockedFlag = pinLocked.Value.ToString() == "Y" ? true : false;
			pinResponseData.pinChangeFlag = pinChangeFlag.Value.ToString() == "Y" ? true : false;
			pinResponseData.firstPinGenerated = firstPinGenerated.Value.ToString() == "Y" ? true : false;
			CISMSRequest smsRequest = new CISMSRequest();
			smsRequest.phoneNo = mobilePhone;
			smsRequest.messageText = messageBody;
			string smsResp = new SMSService().sendSMS(smsRequest);
			new SMSDAO().saveSMS(messageBody, mobilePhone, smsResp, sqlConn, sqlTran);
			return pinResponseData;
		}

		public DataTable findPin(string phoneNo, string pinNo, NpgsqlConnection sqlConn, NpgsqlTransaction sqlTran)
		{
			start = DateTime.Now; //Start time		
			NpgsqlCommand cmd = new NpgsqlCommand
			{
				Connection = sqlConn,
				Transaction = sqlTran,
				CommandType = CommandType.Text
			};
			string strQuery = "Select Activation_Code ,Pin_No ,Convert(Varchar(8),expiry_date,108) expiry_time, Convert(Varchar(8),expiry_date,103) expiry_date ,status ,isnull(max_trial_times,0) max_trial_times ,pin_change_flag ,first_Pin_Generated ,pin_Locked, DATEDIFF(MINUTE, GETDATE(), expiry_date) MinutesToExpiry, imei_number,device_id FROM Nep_Enroll_PIN where phone_no = '" + phoneNo + "'";
			if (pinNo != null)
			{
				string encryptedPin = pinNo + "~" + phoneNo;
				pinNo = ENCRYPTER.encryptWithTripleDES(encryptedPin);
				strQuery = strQuery + " and Pin_No = '" + pinNo.Trim() + "'";
			}
			cmd.CommandText = strQuery;
			DataTable dt = DBUtility.findDataTable(cmd);
			end = DateTime.Now;   //End time
			timeDifference = end - start; //Time span between start and end = the time span needed to execute your method
			difference_Miliseconds = (int)timeDifference.TotalMilliseconds; //Gives you the time needed to execute the method in miliseconds (= time between start and end in miliseconds)
			LOGGER.info("============== Pin details fetch duration : " + difference_Miliseconds + "ms: Phone Number: " + phoneNo);
			return dt;
		}
		public void resetPINAttempts(string phoneNo, NpgsqlConnection sqlConn, NpgsqlTransaction sqlTran)
		{
			NpgsqlCommand cmd = new NpgsqlCommand
			{
				Connection = sqlConn,
				Transaction = sqlTran,
				CommandType = CommandType.Text
			};
			cmd.CommandText = "Update Nep_Enroll_PIN set max_trial_times = 0 Where phone_no = '" + phoneNo.Trim() + "'";
			cmd.CommandType = CommandType.Text;
			DBUtility.createRecord(cmd);
		}
		public bool isPinLocked(string phoneNo, NpgsqlConnection sqlConn, NpgsqlTransaction sqlTran)
		{
			bool pinLocked = false;
			long maxTrials = 0;

			NpgsqlCommand cmd = new NpgsqlCommand
			{
				Connection = sqlConn,
				Transaction = sqlTran,
				CommandType = CommandType.Text,
				CommandText = "Select isnull(max_trial_times,0) max_trial_times from Nep_Enroll_PIN Where phone_no = '" + phoneNo.Trim() + "'"
			};
			DataTable dt = new DataTable();
			using (NpgsqlDataAdapter dataAdaptor = new NpgsqlDataAdapter(cmd))
			{
				dataAdaptor.Fill(dt);
			}
			if (dt.Rows.Count == 1)
				maxTrials = long.Parse(dt.Rows[0][0].ToString());

			if (maxTrials <= 3)
			{
				cmd = new NpgsqlCommand
				{
					Connection = sqlConn,
					Transaction = sqlTran,
					CommandType = CommandType.Text,
					CommandText = "Update Nep_Enroll_PIN set max_trial_times = max_trial_times + 1 Where phone_no = '" + phoneNo.Trim() + "'"
				};
				DBUtility.createRecord(cmd);
			}
			else if (maxTrials >= 4)
			{
				cmd = new NpgsqlCommand
				{
					Connection = sqlConn,
					Transaction = sqlTran,
					CommandType = CommandType.Text,
					CommandText = "Update Nep_Enroll_PIN set max_trial_times = max_trial_times + 1, pin_Locked = 'Y' Where phone_no = '" + phoneNo.Trim() + "'"
				};
				DBUtility.createRecord(cmd);
				pinLocked = true;
			}
			return pinLocked;
		}
		public void changePIN(string newPin, string phoneNo, NpgsqlConnection sqlConn, NpgsqlTransaction sqlTran)
		{
			string encryptedPIN = newPin + "~" + phoneNo;
			encryptedPIN = ENCRYPTER.encryptWithTripleDES(encryptedPIN);
			NpgsqlCommand cmd = new NpgsqlCommand();
			cmd.CommandText = "Nep_Update_PIN";
			cmd.CommandType = CommandType.StoredProcedure;

			cmd.Parameters.Add("@psPinNo", NpgsqlDbType.Varchar, 100).Value = encryptedPIN;
			cmd.Parameters.Add("@psPhoneNo", NpgsqlDbType.Varchar, 12).Value = phoneNo;

			NpgsqlParameter returnCode = new NpgsqlParameter("@pnReturnCode", NpgsqlDbType.Integer);
			returnCode.Direction = ParameterDirection.Output;
			cmd.Parameters.Add(returnCode);

			NpgsqlParameter errorDescr = new NpgsqlParameter("@psErrorDescr", NpgsqlDbType.Varchar, 30);
			errorDescr.Direction = ParameterDirection.Output;
			cmd.Parameters.Add(errorDescr);
			cmd.Connection = sqlConn;
			cmd.Transaction = sqlTran;
			DBUtility.createRecord(cmd);
		}


	}
}