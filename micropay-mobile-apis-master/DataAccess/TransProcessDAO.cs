using System;
using System.Collections.Generic;
using System.Data;
using micropay_apis.Models;
using micropay_apis.Utils;
using Npgsql;


namespace micropay_apis.DataAccess
{
	public class TransProcessDAO
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


		public double saveAndGenerateTransRef(TxnData request, EntityAmount entityAmount, Authentication authRequest,
			NpgsqlConnection sqlConn, NpgsqlTransaction sqlTran)
		{
			NpgsqlCommand cmd = new NpgsqlCommand();
			double txnRef = generateTxnRef("biller_ref_biller_ref_id_seq", sqlConn);
			cmd.CommandText = "INSERT INTO Nep_Base_Txns(CrAcct,DrAcct,Amount,Currency,UserName,TransCode,description,TxnRef,Success,Trans_Date,Util_Posted,Reversal,Profits_Ref,Reason,Depositor_Phone,"
				+ "Depositor_Name,Agent_Commission,Net_Charge,Excise_Duty,Withhold_Tax,Dr_Name,Cr_Name,System_Date,External_Bank_Code,external_acct,"
				+ "initiator_phone_no,misc_value_1,misc_value_2,misc_value_3,misc_value_4,misc_value_5,misc_value_6,misc_value_7,misc_value_8,misc_value_9,"
				+ "misc_value_10) VALUES(@CrAcct, @DrAcct, @Amount, @Currency, @UserName, @TransCode, @description, @TxnRef, @Success, @Trans_Date, @Util_Posted, @Reversal,"
				+ "@Profits_Ref, @Reason, @Depositor_Phone, @Depositor_Name, @Agent_Commission, @Net_Charge, @Excise_Duty, @Withhold_Tax, @Dr_Name, @Cr_Name, @System_Date,"
				+ "@External_Bank_Code, @external_acct, @initiator_phone_no, @misc_value_1, @misc_value_2, @misc_value_3, @misc_value_4, @misc_value_5, @misc_value_6,"
				+ "@misc_value_7, @misc_value_8, @misc_value_9, @misc_value_10)";
			cmd.CommandType = CommandType.Text;
			cmd.Connection = sqlConn;
			cmd.Transaction = sqlTran;
			cmd.Parameters.AddWithValue("@CrAcct", request.crAcctNo);
			cmd.Parameters.AddWithValue("@DrAcct", request.drAcctNo);
			cmd.Parameters.AddWithValue("@Amount", request.transAmt);
			cmd.Parameters.AddWithValue("@Currency", request.currency);
			cmd.Parameters.AddWithValue("@UserName", authRequest.outletCode);
			cmd.Parameters.AddWithValue("@TransCode", request.tranCode);
			cmd.Parameters.AddWithValue("@description", request.description);
			cmd.Parameters.AddWithValue("@TxnRef", txnRef);
			cmd.Parameters.AddWithValue("@Success", "N");
			cmd.Parameters.AddWithValue("@Util_Posted", "N");
			cmd.Parameters.AddWithValue("@Reversal", "N");
			cmd.Parameters.AddWithValue("@Profits_Ref", DBNull.Value);
			cmd.Parameters.AddWithValue("@Reason", DBNull.Value);
			cmd.Parameters.AddWithValue("@Depositor_Phone", request.depositorPhoneNo ?? (object)DBNull.Value);
			cmd.Parameters.AddWithValue("@Depositor_Name", request.depositorName ?? (object)DBNull.Value);
			cmd.Parameters.AddWithValue("@Agent_Commission", entityAmount.commission);
			cmd.Parameters.AddWithValue("@Net_Charge", entityAmount.netCharge);
			cmd.Parameters.AddWithValue("@Excise_Duty", entityAmount.exciseDuty);
			cmd.Parameters.AddWithValue("@Withhold_Tax", entityAmount.withHoldTax);
			cmd.Parameters.AddWithValue("@Dr_Name", DBNull.Value);
			cmd.Parameters.AddWithValue("@Cr_Name", DBNull.Value);
			cmd.Parameters.AddWithValue("@System_Date", DateTime.Now);
			cmd.Parameters.AddWithValue("@External_Bank_Code", DBNull.Value);
			cmd.Parameters.AddWithValue("@external_acct", DBNull.Value);
			cmd.Parameters.AddWithValue("@initiator_phone_no", authRequest.phoneNo);
			cmd.Parameters.AddWithValue("@misc_value_1", DBNull.Value);
			cmd.Parameters.AddWithValue("@misc_value_2", DBNull.Value);
			cmd.Parameters.AddWithValue("@misc_value_3", DBNull.Value);
			cmd.Parameters.AddWithValue("@misc_value_4", DBNull.Value);
			cmd.Parameters.AddWithValue("@misc_value_5", DBNull.Value);
			cmd.Parameters.AddWithValue("@misc_value_6", DBNull.Value);
			cmd.Parameters.AddWithValue("@misc_value_7", DBNull.Value);
			cmd.Parameters.AddWithValue("@misc_value_8", DBNull.Value);
			cmd.Parameters.AddWithValue("@misc_value_9", DBNull.Value);
			cmd.Parameters.AddWithValue("@misc_value_10", DBNull.Value);
			DBUtility.createRecord(cmd);
			return txnRef;
		}


		public long saveBillerNotification(TxnData request, NpgsqlConnection sqlConn)
		{
			NpgsqlCommand cmd = new NpgsqlCommand();
			long id = generateTxnRef("biller_ref_biller_ref_id_seq", sqlConn);
			cmd.CommandText = "INSERT INTO " + PROPERTIES.SCHEMA_NAME + ".biller_notif (biller_notif_id, amount, iso_code, posted_by, biller_code, trans_descr, status, trans_id, trans_date, reversal_flag, reversal_reason, reference_no) VALUES (@biller_notif_id, @amount, @iso_code, @posted_by, @biller_code, @trans_descr, @status, @trans_id, @trans_date, @reversal_flag, @reversal_reason, @reference_no)";
			cmd.CommandType = CommandType.Text;
			cmd.Connection = sqlConn;
			cmd.Parameters.AddWithValue("@biller_notif_id", id);
			cmd.Parameters.AddWithValue("@amount", request.transAmt);
			cmd.Parameters.AddWithValue("@iso_code", "UGX");
			cmd.Parameters.AddWithValue("@posted_by", request.authRequest.outletCode);
			cmd.Parameters.AddWithValue("@biller_code", request.paymentCode);
			cmd.Parameters.AddWithValue("@trans_descr", request.description);
			cmd.Parameters.AddWithValue("@status", "FAILED");
			cmd.Parameters.AddWithValue("@trans_id", DBNull.Value);
			cmd.Parameters.AddWithValue("@trans_date", DateTime.Now);
			cmd.Parameters.AddWithValue("@reversal_flag", "N");
			cmd.Parameters.AddWithValue("@reversal_reason", "N");
			cmd.Parameters.AddWithValue("@reference_no", request.referenceNo);
			DBUtility.createRecord(cmd);
			return id;
		}


		public void updateBillPaymentStatus(double notifyId, string status, NpgsqlConnection conn)
		{
			NpgsqlCommand cmd = new NpgsqlCommand
			{
				CommandText = "UPDATE " + PROPERTIES.SCHEMA_NAME + ".biller_notif set status = @status WHERE biller_notif_id = @nNotifyId",
				Connection = conn,
				CommandType = CommandType.Text
			};
			cmd.Parameters.AddWithValue("@status", status);
			cmd.Parameters.AddWithValue("@nNotifyId", notifyId);
			cmd.ExecuteNonQuery();
		}


		public void saveNotificationLog(TransLog request, NpgsqlConnection sqlConn)
		{
			NpgsqlCommand cmd = new NpgsqlCommand();
			cmd.CommandText = "INSERT INTO " + PROPERTIES.SCHEMA_NAME + ".biller_notif_log(biller_code, mobile_phone, channel_code, request_data, response_data, request_date, processing_duration)VALUES(@biller_code, @mobile_phone, @channel_code, @request_data, @response_data, @request_date, @processing_duration)";
			cmd.CommandType = CommandType.Text;
			cmd.Connection = sqlConn;
			cmd.Parameters.AddWithValue("@biller_code", request.biller_code);
			cmd.Parameters.AddWithValue("@mobile_phone", request.mobile_phone ?? (object)DBNull.Value);
			cmd.Parameters.AddWithValue("@channel_code", request.channel_code);
			cmd.Parameters.AddWithValue("@request_data", request.request_data ?? (object)DBNull.Value);
			cmd.Parameters.AddWithValue("@response_data", request.response_data ?? (object)DBNull.Value);
			cmd.Parameters.AddWithValue("@request_date", request.request_date);
			cmd.Parameters.AddWithValue("@processing_duration", request.processing_duration);
			DBUtility.createRecord(cmd);
		}


		public ChargeDetails findTCCharges(TxnData request, NpgsqlConnection conn, NpgsqlTransaction sqlTran)
		{
			ChargeDetails item = new ChargeDetails();
			string query;
			NpgsqlCommand cmd = new NpgsqlCommand
			{
				Connection = conn,
				Transaction = sqlTran
			};
			query = "Select charge_code,description,currency,charge_type,amt,Convert(varchar(10),effective_dt,103) effective_dt,transCode,status From Nep_ad_gb_cc where charge_code = @charge_code";
			query = query + " and charge_code = @charge_code";
			cmd.Parameters.AddWithValue("@charge_code", request.tranCode);
			cmd.CommandText = query;
			DataTable datatable = DBUtility.findDataTable(cmd);
			foreach (DataRow dr in datatable.Rows)
			{
				item = new ChargeDetails
				{
					chargeCode = CONVERTER.toLong(dr[0].ToString()),
					description = dr[1].ToString(),
					currency = dr[2].ToString(),
					chargeType = dr[3].ToString().Trim(),
					amt = CONVERTER.toDouble(dr[4].ToString()),
					effectiveDt = dr[5].ToString(),
					transCode = CONVERTER.toLong(dr[6].ToString()),
					status = dr[7].ToString()
				};
			}
			return item;
		}
		public ChargeTier findChargeTierData(TxnData request, NpgsqlConnection conn, NpgsqlTransaction sqlTran)
		{
			ChargeTier data = new ChargeTier();
			int i = 0;
			NpgsqlCommand cmd = new NpgsqlCommand();
			cmd.Connection = conn;
			cmd.Transaction = sqlTran;
			cmd.CommandText = "Select * from NEP_AD_GB_CC_TIER_X Where @pnTransAmt >= from_bal And  @pnTransAmt <= to_bal And charge_code = @nChargeCode";
			cmd.Parameters.AddWithValue("@nChargeCode", request.tranCode);
			cmd.Parameters.AddWithValue("@pnTransAmt", request.transAmt);
			DataTable datatable = DBUtility.findDataTable(cmd);
			foreach (DataRow dr in datatable.Rows)
			{
				data = new ChargeTier
				{
					serialNo = CONVERTER.toLong(dr["tier_id"].ToString()),
					fromAmount = CONVERTER.toDouble(dr["from_bal"].ToString()),
					toAmount = CONVERTER.toDouble(dr["to_bal"].ToString()),
					tierAmount = CONVERTER.toDouble(dr["CC_AMT"].ToString()),
				};
				i++;
			}
			return data;
		}
		public ChargeDetails findTCCommission(TxnData request, NpgsqlConnection conn, NpgsqlTransaction sqlTran)
		{
			ChargeDetails item = new ChargeDetails();
			string query;
			NpgsqlCommand cmd = new NpgsqlCommand
			{
				Connection = conn,
				Transaction = sqlTran
			};
			query = "Select Charge_code,description,currency,Charge_type,amt,Convert(varchar(10),effective_dt,103) effective_dt,transCode,status From Nep_ad_com_cc where charge_code = @charge_code";
			query = query + " and charge_code = @charge_code";
			cmd.Parameters.AddWithValue("@charge_code", request.tranCode);
			cmd.CommandText = query;
			DataTable datatable = DBUtility.findDataTable(cmd);
			foreach (DataRow dr in datatable.Rows)
			{
				item = new ChargeDetails
				{
					chargeCode = CONVERTER.toLong(dr[0].ToString()),
					description = dr[1].ToString(),
					currency = dr[2].ToString(),
					chargeType = dr[3].ToString().Trim(),
					amt = CONVERTER.toDouble(dr[4].ToString()),
					effectiveDt = dr[5].ToString(),
					transCode = CONVERTER.toLong(dr[6].ToString()),
					status = dr[7].ToString()
				};
			}
			return item;
		}
		public ChargeTier findCommissionTierData(TxnData request, NpgsqlConnection conn, NpgsqlTransaction sqlTran)
		{
			ChargeTier data = new ChargeTier();
			NpgsqlCommand cmd = new NpgsqlCommand
			{
				Connection = conn,
				Transaction = sqlTran,
				CommandText = "Select * from NEP_AD_GB_COM_TIER_X Where @pnTransAmt >= from_bal And  @pnTransAmt <= to_bal And charge_code = @nChargeCode"
			};
			cmd.Parameters.AddWithValue("@nChargeCode", request.tranCode);
			cmd.Parameters.AddWithValue("@pnTransAmt", request.transAmt);
			DataTable datatable = DBUtility.findDataTable(cmd);
			foreach (DataRow dr in datatable.Rows)
			{
				data = new ChargeTier
				{
					serialNo = CONVERTER.toLong(dr["tier_id"].ToString()),
					fromAmount = CONVERTER.toDouble(dr["from_bal"].ToString()),
					toAmount = CONVERTER.toDouble(dr["to_bal"].ToString()),
					tierAmount = CONVERTER.toDouble(dr["cc_amt"].ToString()),
					craftSilconAmt = CONVERTER.toDouble(dr["craft_silcon"].ToString())
				};
			}
			return data;
		}
		public void saveChildTrans(TransactionItem request, TxnData mainRqst, NpgsqlConnection sqlConn, NpgsqlTransaction sqlTran)
		{
			NpgsqlCommand cmd = new NpgsqlCommand();
			cmd.Transaction = sqlTran;
			cmd.CommandText = "Insert Into Nep_Util_Sf (CrAcctNo,CrAcctType,pnAmt,pnCCAmt,IsoCode,DrAcctNo,DrAcctType,TranType,TranDescr,CheckNo,UserID,SuperUserID,RoutingNo,PostError,UtilPostError,Postingdt,Offline,CustRef,Area,CustName,Supervised,EqxPosted,PropertyRef,UtilPosted,PhoneNo,Reversal,HistPtid,OrigBranchNo,StmtSent,JournalPtid,ChannelCode,ItemNo,CCPtid,ChgCode,TransBatchID,TransCode,EntryType)Values(@CrAcctNo,@CrAcctType,@pnAmt,@pnCCAmt,@IsoCode,@DrAcctNo,@DrAcctType,@TranType,@TranDescr,@CheckNo,@UserID,@SuperUserID,@RoutingNo,@PostError,@UtilPostError,@Postingdt,@Offline,@CustRef,@Area,@CustName,@Supervised,@qxPosted,@PropertyRef,@UtilPosted,@PhoneNo,@Reversal,@HistPtid,@OrigBranchNo,@StmtSent,@JournalPtid,@ChannelCode,@ItemNo,@CCPtid,@ChgCode,@TransBatchID,@TransCode,@EntryType)";
			cmd.CommandType = CommandType.Text;
			cmd.Connection = sqlConn;
			cmd.Parameters.AddWithValue("@CrAcctNo", request.crAcctNo);
			cmd.Parameters.AddWithValue("@CrAcctType", request.crAcctType);
			cmd.Parameters.AddWithValue("@pnAmt", request.amount);
			cmd.Parameters.AddWithValue("@pnCCAmt", DBNull.Value);
			cmd.Parameters.AddWithValue("@IsoCode", mainRqst.currency);
			cmd.Parameters.AddWithValue("@DrAcctNo", request.drAcctNo);
			cmd.Parameters.AddWithValue("@DrAcctType", request.drAcctType);
			cmd.Parameters.AddWithValue("@TranType", "Transfer");
			cmd.Parameters.AddWithValue("@TranDescr", request.transDescr);
			cmd.Parameters.AddWithValue("@CheckNo", DBNull.Value);
			cmd.Parameters.AddWithValue("@UserID", request.userCode);
			cmd.Parameters.AddWithValue("@SuperUserID", DBNull.Value);
			cmd.Parameters.AddWithValue("@RoutingNo", DBNull.Value);
			cmd.Parameters.AddWithValue("@PostError", "");
			cmd.Parameters.AddWithValue("@UtilPostError", DBNull.Value);
			cmd.Parameters.AddWithValue("@Postingdt", DateTime.Now);
			cmd.Parameters.AddWithValue("@Offline", "N");
			cmd.Parameters.AddWithValue("@CustRef", DBNull.Value);
			cmd.Parameters.AddWithValue("@Area", DBNull.Value);
			cmd.Parameters.AddWithValue("@CustName", DBNull.Value);
			cmd.Parameters.AddWithValue("@Supervised", DBNull.Value);
			cmd.Parameters.AddWithValue("@qxPosted", "N");
			cmd.Parameters.AddWithValue("@PropertyRef", DBNull.Value);
			cmd.Parameters.AddWithValue("@UtilPosted", request.entryType == "COMMISSION" ? "P" : "N");
			cmd.Parameters.AddWithValue("@PhoneNo", DBNull.Value);
			cmd.Parameters.AddWithValue("@Reversal", "N");
			cmd.Parameters.AddWithValue("@HistPtid", DBNull.Value);
			cmd.Parameters.AddWithValue("@OrigBranchNo", 85);
			cmd.Parameters.AddWithValue("@StmtSent", DBNull.Value);
			cmd.Parameters.AddWithValue("@JournalPtid", DBNull.Value);
			cmd.Parameters.AddWithValue("@ChannelCode", "LITE");
			cmd.Parameters.AddWithValue("@ItemNo", request.itemNo);
			cmd.Parameters.AddWithValue("@CCPtid", DBNull.Value);
			cmd.Parameters.AddWithValue("@ChgCode", DBNull.Value);
			cmd.Parameters.AddWithValue("@TransBatchID", request.mainTransId);
			cmd.Parameters.AddWithValue("@TransCode", mainRqst.tranCode);
			cmd.Parameters.AddWithValue("@EntryType", request.entryType);
			DBUtility.createRecord(cmd);
		}
		public List<TransactionCodePolicy> findAgentTransactionCodePolicy(TxnData request, NpgsqlConnection sqlConn, NpgsqlTransaction sqlTran)
		{
			DataTable datatable;
			List<TransactionCodePolicy> literals = new List<TransactionCodePolicy>();
			TransactionCodePolicy item;
			NpgsqlCommand cmd = new NpgsqlCommand();
			cmd.Connection = sqlConn;
			cmd.Transaction = sqlTran;
			string query = "select * from agent_trans_code_policy where trans_code = @pnTranCode";
			cmd.Parameters.AddWithValue("@pnTranCode", request.tranCode);
			query = query + " order by posting_priority";
			cmd.CommandText = query;
			datatable = new DataTable();
			datatable = DBUtility.findDataTable(cmd);
			foreach (DataRow dr in datatable.Rows)
			{
				item = new TransactionCodePolicy();
				item.transCode = CONVERTER.toLong(dr["trans_code"].ToString());
				item.postingPriority = CONVERTER.toLong(dr["posting_priority"].ToString());
				item.sourceAcctPolicy = dr["source_acct"].ToString();
				item.destinationAcctPolicy = dr["destination_acct"].ToString();
				item.amountType = dr["amount_type"].ToString();
				item.transCategory = dr["trans_category"].ToString();
				item.transAmtBankShare = CONVERTER.toDouble(dr["tran_amt_bank_share"].ToString());
				item.transAmtVendorShare = CONVERTER.toDouble(dr["tran_amt_vendor_share"].ToString());
				literals.Add(item);
			}
			return literals;
		}
		public string findControlParameterValue(string paramCode, NpgsqlConnection sqlConn, NpgsqlTransaction sqlTran)
		{
			NpgsqlCommand cmd = new NpgsqlCommand
			{
				Connection = sqlConn,
				Transaction = sqlTran,
				CommandText = "SELECT Param_Value FROM ctrl_Parameter WHERE Param_Cd = '" + paramCode + "'",
				CommandType = CommandType.Text
			};
			string response = DBUtility.findString(cmd, sqlTran);
			return response;
		}
		public string findAgentCommissionAccount(string outletCode, NpgsqlConnection sqlConn, NpgsqlTransaction sqlTran)
		{
			NpgsqlCommand cmd = new NpgsqlCommand
			{
				Connection = sqlConn,
				Transaction = sqlTran,
				CommandText = "Select B.cbs_acct_no from Nep_Agency_Users A, dp_acct B, Nep_Agency_Ref C where B.rim_no = C.Agency_No And  C.Agent_Code = A.agent_id And B.Status = 'Active' And A.User_Id = @psUserID And B.class_code = (Select convert(int,param_value) from ctrl_parameter where param_cd = 'S04')",
				CommandType = CommandType.Text
			};
			cmd.Parameters.AddWithValue("@psUserID", outletCode);
			string response = DBUtility.findString(cmd, sqlTran);
			return response;
		}
		public bool isUserValid(string outletCode, NpgsqlConnection sqlConn, NpgsqlTransaction sqlTran)
		{
			NpgsqlCommand cmd = new NpgsqlCommand
			{
				Connection = sqlConn,
				Transaction = sqlTran,
				CommandText = "Select 1 from Nep_agency_users where user_id = @psUserID and Status = 'Active'",
				CommandType = CommandType.Text
			};
			cmd.Parameters.AddWithValue("@psUserID", outletCode);
			string response = DBUtility.findString(cmd, sqlTran);
			if (response != null)
				return true;
			else
			{
				cmd = new NpgsqlCommand
				{
					Connection = sqlConn,
					Transaction = sqlTran,
					CommandText = "Select employee_id from ad_gb_rsm Where user_name = @sUserID and status = 'Active'",
					CommandType = CommandType.Text
				};
				cmd.Parameters.AddWithValue("@psUserID", outletCode);
				response = DBUtility.findString(cmd, sqlTran);
				if (response != null)
					return true;
				else
					return false;
			}
		}


		public void updatePostedTrans(double transId, string externalReference, NpgsqlConnection conn)
		{

			NpgsqlCommand cmd = new NpgsqlCommand
			{
				CommandText = "Update Nep_Base_Txns set Util_Posted = 'Y', Success = 'Y', Reversal = 'N', Profits_Ref = @sProfitRef Where TxnRef = @pnHistPtid",
				Connection = conn,
				CommandType = CommandType.Text
			};
			cmd.Parameters.AddWithValue("@sProfitRef", externalReference);
			cmd.Parameters.AddWithValue("@pnHistPtid", transId);
			cmd.ExecuteNonQuery();
		}
		public void updateAccumulatedTransAmount(TxnData request, NpgsqlConnection sqlConn)
		{
			string acctNo;
			switch (request.tranCode)
			{
				case 101:
					acctNo = request.drAcctNo;
					break;
				case 111:
					acctNo = request.drAcctNo;
					break;
				case 157:
					acctNo = request.drAcctNo;
					break;
				case 158:
					acctNo = request.drAcctNo;
					break;
				default:
					return;
			}
			NpgsqlCommand cmd = new NpgsqlCommand
			{
				CommandText = "UPDATE dp_acct_cum_trans_amount   SET amount = amount + @nAmount, last_trans_date = GETDATE() WHERE trans_code = @nTranCode and acct_no = @sAcctNo",
				CommandType = CommandType.Text,
				Connection = sqlConn,
			};
			cmd.Parameters.AddWithValue("@sAcctNo", acctNo);
			cmd.Parameters.AddWithValue("@nTranCode", request.tranCode);
			cmd.Parameters.AddWithValue("@nAmount", request.transAmt);
			DBUtility.createRecord(cmd);
		}
		public void createAccumulatedTransAmount(TxnData request, NpgsqlConnection sqlConn)
		{
			string acctNo;
			switch (request.tranCode)
			{
				case 101:
					acctNo = request.drAcctNo;
					break;
				case 111:
					acctNo = request.drAcctNo;
					break;
				case 157:
					acctNo = request.drAcctNo;
					break;
				case 158:
					acctNo = request.drAcctNo;
					break;
				default:
					return;
			}

			NpgsqlCommand cmd = new NpgsqlCommand
			{
				CommandText = "INSERT INTO dp_acct_cum_trans_amount (trans_code ,acct_no ,amount ,last_trans_date) VALUES (@nTranCode , @sAcctNo ,@nAmount ,@nLastTransDate)",
				CommandType = CommandType.Text,
				Connection = sqlConn,
			};
			cmd.Parameters.AddWithValue("@sAcctNo", acctNo);
			cmd.Parameters.AddWithValue("@nTranCode", request.tranCode);
			cmd.Parameters.AddWithValue("@nAmount", request.transAmt);
			cmd.Parameters.AddWithValue("@nLastTransDate", DateTime.Now);
			DBUtility.createRecord(cmd);
		}
		public bool isCummulativeTransAmountExists(TxnData request, NpgsqlConnection sqlConn)
		{
			string acctNo;
			switch (request.tranCode)
			{
				case 101:
					acctNo = request.drAcctNo;
					break;
				case 111:
					acctNo = request.drAcctNo;
					break;
				case 157:
					acctNo = request.drAcctNo;
					break;
				case 158:
					acctNo = request.drAcctNo;
					break;
				default:
					return true;
			}

			NpgsqlCommand cmd = new NpgsqlCommand
			{
				CommandText = "select count(*) from dp_acct_cum_trans_amount Where trans_code = @nTranCode and acct_no = @sAcctNo",
				CommandType = CommandType.Text,
				Connection = sqlConn,
			};
			cmd.Parameters.AddWithValue("@sAcctNo", acctNo);
			cmd.Parameters.AddWithValue("@nTranCode", request.tranCode);
			cmd.Parameters.AddWithValue("@nAmount", request.transAmt);
			int count = DBUtility.findInteger(cmd);
			return count > 0;
		}


		public void updateAccumulatedTransAmount(TxnData request, NpgsqlConnection sqlConn, NpgsqlTransaction sqlTran)
		{
			string acctNo;
			switch (request.tranCode)
			{
				case 101:
					acctNo = request.drAcctNo;
					break;
				case 111:
					acctNo = request.drAcctNo;
					break;
				case 157:
					acctNo = request.drAcctNo;
					break;
				case 158:
					acctNo = request.drAcctNo;
					break;
				default:
					return;
			}
			NpgsqlCommand cmd = new NpgsqlCommand
			{
				CommandText = "UPDATE dp_acct_cum_trans_amount   SET amount = amount + @nAmount, last_trans_date = GETDATE() WHERE trans_code = @nTranCode and acct_no = @sAcctNo",
				CommandType = CommandType.Text,
				Connection = sqlConn,
				Transaction = sqlTran
			};
			cmd.Parameters.AddWithValue("@sAcctNo", acctNo);
			cmd.Parameters.AddWithValue("@nTranCode", request.tranCode);
			cmd.Parameters.AddWithValue("@nAmount", request.transAmt);
			DBUtility.createRecord(cmd);
		}
		public void createAccumulatedTransAmount(TxnData request, NpgsqlConnection sqlConn, NpgsqlTransaction sqlTran)
		{
			string acctNo;
			switch (request.tranCode)
			{
				case 103:
					acctNo = request.drAcctNo;
					break;
				case 111:
					acctNo = request.drAcctNo;
					break;
				case 157:
					acctNo = request.drAcctNo;
					break;
				case 158:
					acctNo = request.drAcctNo;
					break;
				default:
					return;
			}

			NpgsqlCommand cmd = new NpgsqlCommand
			{
				CommandText = "INSERT INTO dp_acct_cum_trans_amount (trans_code ,acct_no ,amount ,last_trans_date) VALUES (@nTranCode , @sAcctNo ,@nAmount ,@nLastTransDate)",
				CommandType = CommandType.Text,
				Connection = sqlConn,
				Transaction = sqlTran
			};
			cmd.Parameters.AddWithValue("@sAcctNo", acctNo);
			cmd.Parameters.AddWithValue("@nTranCode", request.tranCode);
			cmd.Parameters.AddWithValue("@nAmount", request.transAmt);
			cmd.Parameters.AddWithValue("@nLastTransDate", DateTime.Now);
			DBUtility.createRecord(cmd);
		}
		public bool isCummulativeTransAmountExists(TxnData request, NpgsqlConnection sqlConn, NpgsqlTransaction sqlTran)
		{
			string acctNo;
			switch (request.tranCode)
			{
				case 103:
					acctNo = request.drAcctNo;
					break;
				case 111:
					acctNo = request.drAcctNo;
					break;
				case 157:
					acctNo = request.drAcctNo;
					break;
				case 158:
					acctNo = request.drAcctNo;
					break;
				default:
					return true;
			}

			NpgsqlCommand cmd = new NpgsqlCommand
			{
				CommandText = "select count(*) from dp_acct_cum_trans_amount Where trans_code = @nTranCode and acct_no = @sAcctNo",
				CommandType = CommandType.Text,
				Connection = sqlConn,
				Transaction = sqlTran
			};
			cmd.Parameters.AddWithValue("@sAcctNo", acctNo);
			cmd.Parameters.AddWithValue("@nTranCode", request.tranCode);
			cmd.Parameters.AddWithValue("@nAmount", request.transAmt);
			int count = DBUtility.findInteger(cmd);
			return count > 0;
		}
	}
}