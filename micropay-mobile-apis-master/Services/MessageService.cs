using System;
using micropay_apis.Models;
using micropay_apis.Remote;
using micropay_apis.Utils;

namespace micropay_apis.Services
{
    public class MessageService
    {
        private MediumsService mediumsService = new MediumsService();

        private string trim(string value)
        {
            try
            {
                return value.Trim();
            }
            catch (Exception ex)
            {
                return (string)null;
            }
        }

        public void sendFinancialSMS(TxnData request, TxnResp txnResp)
        {
            string str1 = (string)null;
            string mobilePhone1 = (string)null;
            string str2 = (string)null;
            string mobilePhone2 = (string)null;
            bool deliverSMS = false;
            try
            {
                if (request.tranCode == TRANCODES.CASH_DEPOSIT)
                {
                    str1 = SMS_TEMPLATE.CUSTOMER_CASHIN_SUCCESS;
                    mobilePhone1 = request.customerPhoneNo;
                    deliverSMS = true;
                    str2 = SMS_TEMPLATE.OUTLET_CASHIN_SUCCESS;
                    mobilePhone2 = request.authRequest.phoneNo;
                }
                if (request.tranCode == TRANCODES.CASH_WITHDRAW)
                {
                    str1 = SMS_TEMPLATE.CUSTOMER_CASHOUT_SUCCESS;
                    mobilePhone1 = request.customerPhoneNo;
                    deliverSMS = true;
                    str2 = SMS_TEMPLATE.OUTLET_CASHOUT_SUCCESS;
                    mobilePhone2 = request.authRequest.phoneNo;
                }
                if (request.tranCode == TRANCODES.SUPER_AGENT_TO_OUTLET)
                {
                    str1 = SMS_TEMPLATE.CUSTOMER_CASHOUT_SUCCESS;
                    mobilePhone1 = request.customerPhoneNo;
                    deliverSMS = false;
                    str2 = SMS_TEMPLATE.OUTLET_CASHOUT_SUCCESS;
                    mobilePhone2 = request.authRequest.phoneNo;
                }
                if (request.tranCode == TRANCODES.OUTLET_TO_SUPER_AGENT)
                {
                    str1 = SMS_TEMPLATE.CUSTOMER_CASHOUT_SUCCESS;
                    mobilePhone1 = request.customerPhoneNo;
                    deliverSMS = true;
                    str2 = SMS_TEMPLATE.CUSTOMER_CASHOUT_SUCCESS;
                    mobilePhone2 = request.authRequest.phoneNo;
                }
                if (request.tranCode == TRANCODES.OUTLET_TO_OUTLET)
                {
                    str1 = SMS_TEMPLATE.CUSTOMER_CASHOUT_SUCCESS;
                    mobilePhone1 = request.customerPhoneNo;
                    mobilePhone2 = request.authRequest.phoneNo;
                    deliverSMS = true;
                }
                if (request.tranCode == TRANCODES.SUPER_AGENT_TRANSFER)
                {
                    str1 = SMS_TEMPLATE.CUSTOMER_CASHOUT_SUCCESS;
                    mobilePhone1 = request.customerPhoneNo;
                    mobilePhone2 = request.authRequest.phoneNo;
                    deliverSMS = true;
                }
                if (request.tranCode == TRANCODES.NWSC_CUSTOMER)
                {
                    str1 = SMS_TEMPLATE.CUSTOMER_NWSC_SUCCESS;
                    mobilePhone1 = request.authRequest.phoneNo;
                    mobilePhone2 = request.authRequest.phoneNo;
                }
                if (request.tranCode == TRANCODES.UMEME_COLLECTION_CUSTOMER)
                {
                    str1 = SMS_TEMPLATE.CUSTOMER_UMEME_SUCCESS;
                    mobilePhone1 = request.authRequest.phoneNo;
                    mobilePhone2 = request.authRequest.phoneNo;
                }
                if (request.tranCode == TRANCODES.UMEME_COLLECTION_AGENT)
                {
                    str1 = SMS_TEMPLATE.CUSTOMER_UMEME_SUCCESS;
                    mobilePhone1 = request.authRequest.phoneNo;
                    mobilePhone2 = request.authRequest.phoneNo;
                }
                if (request.tranCode == TRANCODES.MTN_CASH_DEPOSIT_CUSTOMER)
                {
                    str1 = SMS_TEMPLATE.MTN_CASHIN_CUSTOMER;
                    mobilePhone1 = request.authRequest.phoneNo;
                }
                if (request.tranCode == TRANCODES.AIRTEL_CASH_DEPOSIT_CUSTOMER)
                {
                    str1 = SMS_TEMPLATE.AIRTEL_CASHIN_CUSTOMER;
                    mobilePhone1 = request.authRequest.phoneNo;
                }
                if (request.tranCode == TRANCODES.WENRECO_CUSTOMER)
                {
                    str1 = SMS_TEMPLATE.WENRECO_CUSTOMER;
                    mobilePhone1 = request.authRequest.phoneNo;
                    mobilePhone2 = request.authRequest.phoneNo;
                }
                if (request.tranCode == TRANCODES.TUGENDE_CUSTOMER)
                {
                    str1 = SMS_TEMPLATE.TUGENDE_CUSTOMER;
                    mobilePhone1 = request.authRequest.phoneNo;
                }
                if (request.tranCode == TRANCODES.MTN_CASH_DEPOSIT)
                {
                    str2 = SMS_TEMPLATE.MTN_CASHIN_AGENT;
                    mobilePhone2 = request.authRequest.phoneNo;
                }
                if (request.tranCode == TRANCODES.MTN_CASH_WITHDRAW)
                {
                    str2 = SMS_TEMPLATE.MTN_CASHOUT_AGENT;
                    mobilePhone2 = request.authRequest.phoneNo;
                }
                if (request.tranCode == TRANCODES.AIRTEL_CASH_WITHDRAW)
                {
                    str2 = SMS_TEMPLATE.AIRTEL_CASHOUT_AGENT;
                    mobilePhone2 = request.authRequest.phoneNo;
                }
                if (request.tranCode == TRANCODES.WENRECO_AGENT)
                {
                    str1 = SMS_TEMPLATE.WENRECO_CUSTOMER;
                    mobilePhone1 = request.customerPhoneNo;
                    str2 = SMS_TEMPLATE.WENRECO_AGENT;
                    mobilePhone2 = request.authRequest.phoneNo;
                }
                if (request.tranCode == TRANCODES.TUGENDE_AGENT)
                {
                    str1 = SMS_TEMPLATE.TUGENDE_CUSTOMER;
                    mobilePhone1 = request.customerPhoneNo;
                    str2 = SMS_TEMPLATE.TUGENDE_AGENT;
                    mobilePhone2 = request.authRequest.phoneNo;
                }
                if (str1 != null)
                {
                    string messageText = str1.Replace("{CustomerName}", request.customerName).Replace("{Amount}", string.Format("{0:n}", (object)request.transAmt)).Replace("{CustomerPhone}", request.customerPhoneNo).Replace("{CustomerName}", request.customerName).Replace("{DrAcctNo}", request.drAcctNo).Replace("{TransId}", txnResp.transId).Replace("{Reason}", request.description).Replace("{BillerRef}", request.referenceNo).Replace("{DrOutletCode}", request.authRequest.outletCode).Replace("{DrOutletName}", request.authRequest.outletCode).Replace("{DrAcctBal}", string.Format("{0:n}", (object)txnResp.drAcctBal)).Replace("{CrAcctBal}", string.Format("{0:n}", (object)txnResp.crAcctBal)).Replace("{Charge}", string.Format("{0:n}", (object)txnResp.chargeAmt)).Replace("{TotalAmount}", string.Format("{0:n}", (object)(request.transAmt + txnResp.chargeAmt))).Replace("{TransDate}", DateTime.Now.ToString("dd/MMM/yyyy HH:mm:ss")).Replace("{ReferenceNo}", this.trim(request.referenceNo)).Replace("{TokenNo}", this.trim(txnResp.tokenValue)).Replace("{NoOfUnits}", this.trim(txnResp.noOfUnits));
                    this.mediumsService.sendSMS(mobilePhone1, messageText, deliverSMS);
                }
                if (str2 == null)
                    return;
                string messageText1 = str2.Replace("{Amount}", string.Format("{0:n}", (object)request.transAmt)).Replace("{CustomerPhone}", request.customerPhoneNo).Replace("{CustomerName}", request.customerName).Replace("{DrAcctNo}", request.drAcctNo).Replace("{TransId}", txnResp.transId).Replace("{Reason}", request.description).Replace("{BillerRef}", request.referenceNo).Replace("{DrOutletCode}", request.authRequest.outletCode).Replace("{DrOutletName}", request.authRequest.outletCode).Replace("{DrAcctBal}", string.Format("{0:n}", (object)txnResp.drAcctBal)).Replace("{CrAcctBal}", string.Format("{0:n}", (object)txnResp.crAcctBal)).Replace("{Charge}", string.Format("{0:n}", (object)txnResp.chargeAmt)).Replace("{TotalAmount}", string.Format("{0:n}", (object)(request.transAmt + txnResp.chargeAmt))).Replace("{ReferenceNo}", this.trim(request.referenceNo)).Replace("{TokenNo}", this.trim(txnResp.tokenValue)).Replace("{NoOfUnits}", this.trim(txnResp.noOfUnits)).Replace("{TransDate}", DateTime.Now.ToString("dd/MMM/yyyy HH:mm:ss"));
                this.mediumsService.sendSMS(mobilePhone2, messageText1, deliverSMS);
            }
            catch (Exception ex)
            {
                LOGGER.error(ex.ToString());
            }
        }

        public void sendCustomerInititationMessages(CIOTPRequestData request, CIOTPResponse response)
        {
            string initiationSuccess = SMS_TEMPLATE.CUSTOMER_CASHOUT_INITIATION_SUCCESS;
            try
            {
                string messageText = initiationSuccess.Replace("{CustomerName}", this.trim(request.authRequest.entityName)).Replace("{Amount}", string.Format("{0:n}", (object)request.amount)).Replace("{OutletCode}", this.trim(request.withdrawOutletCode)).Replace("{OutletName}", this.trim(request.outletName)).Replace("{WithdrawCode}", this.trim(response.withdrawCode)).Replace("{ExpiryDate}", this.trim(response.expiryDate));
                this.mediumsService.sendSMS(request.authRequest.phoneNo, messageText, true);
            }
            catch (Exception ex)
            {
                LOGGER.error(ex.ToString());
            }
        }

        public void sendUMEMETokenMessages(TxnData request, TxnResp response)
        {
            string customerUmemeSuccess = SMS_TEMPLATE.CUSTOMER_UMEME_SUCCESS;
            try
            {
                string messageText = customerUmemeSuccess.Replace("{ReferenceNo}", this.trim(request.authRequest.entityName)).Replace("{Amount}", string.Format("{0:n}", (object)request.transAmt)).Replace("{TokenNo}", this.trim(response.noOfUnits));
                this.mediumsService.sendSMS(request.authRequest.phoneNo, messageText, false);
            }
            catch (Exception ex)
            {
                LOGGER.error(ex.ToString());
            }
        }

        public void sendVoucherInitiationMessage(CIOTPRequestData request, CIOTPResponse response)
        {
            string initiationSuccess = SMS_TEMPLATE.CUSTOMER_CASHOUT_INITIATION_SUCCESS;
            try
            {
                string messageText = initiationSuccess.Replace("{CustomerName}", this.trim(request.authRequest.entityName)).Replace("{Amount}", string.Format("{0:n}", (object)request.amount)).Replace("{OutletCode}", this.trim(request.withdrawOutletCode)).Replace("{OutletName}", this.trim(request.outletName)).Replace("{WithdrawCode}", this.trim(response.withdrawCode)).Replace("{ExpiryDate}", this.trim(response.expiryDate));
                this.mediumsService.sendSMS(request.authRequest.phoneNo, messageText, true);
            }
            catch (Exception ex)
            {
                LOGGER.error(ex.ToString());
            }
        }
    }
}