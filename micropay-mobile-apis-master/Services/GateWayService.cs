using micropay_apis.APIModals;
using micropay_apis.Models;
using micropay_apis.Remote;
using micropay_apis.Utils;
using System;
using System.Collections.Generic;


namespace micropay_apis.Services
{
    public class GateWayService
    {
        private MediumsService mediumsService;
        private eQuiWebService equiWebService;
        private CoporateAgencyService coporateAgencyService;
        private DateTime start;
        private DateTime end;
        private TimeSpan timeDifference;
        private int difference_Miliseconds;

        private TxnResp processBillPayments(TxnData request, TxnResp transData)
        {
            TxnResp txnResp = new TxnResp();
            request.externalTransId = transData.transId.ToString();
            ExternalResponse externalResponse1 = new ExternalResponse();
            if (request.tranCode == TRANCODES.AIRTEL_AIRTIME_CUSTOMER || request.tranCode == TRANCODES.AIRTEL_AIRTIME_AGENT)
            {
                MobileTopUpResponse mobileTopUpResponse = StubClientService.pretupService.airtelAirtimeRecharge(new MobileTopUpRequest()
                {
                    EXTCODE = request.authRequest.outletCode,
                    EXTREFNUM = transData.transId.ToString(),
                    MSISDN2 = request.referenceNo,
                    AMOUNT = request.transAmt
                }, request, transData.transId);
                externalResponse1.responseCode = mobileTopUpResponse.responseStatus.responseCode;
                externalResponse1.responseMessage = mobileTopUpResponse.responseStatus.responseMessage;
                externalResponse1.transRefNo = mobileTopUpResponse.TXNID;
                txnResp.response = new ResponseMessage(externalResponse1.responseCode, externalResponse1.responseMessage);
            }

            else if (request.tranCode == TRANCODES.AIRTEL_DATA_CUSTOMER
                || request.tranCode == TRANCODES.AIRTEL_DATA_AGENT
                || request.tranCode == TRANCODES.AIRTEL_VOICE_CUSTOMER
                || request.tranCode == TRANCODES.AIRTEL_VOICE_AGENT)
            {
                MobileTopUpResponse mobileTopUpResponse = StubClientService.pretupService.airtelDataRecharge(new MobileTopUpRequest()
                {
                    EXTCODE = request.authRequest.outletCode,
                    EXTREFNUM = transData.transId.ToString(),
                    billerCode = request.billerCode,
                    MSISDN2 = request.referenceNo,
                    AMOUNT = request.transAmt,
                    SELECTOR = request.paymentCode
                }, request, transData.transId);
                externalResponse1.responseCode = mobileTopUpResponse.responseStatus.responseCode;
                externalResponse1.responseMessage = mobileTopUpResponse.responseStatus.responseMessage;
                externalResponse1.transRefNo = mobileTopUpResponse.TXNID;
                txnResp.response = new ResponseMessage(externalResponse1.responseCode, externalResponse1.responseMessage);
            }
            else if (request.tranCode == TRANCODES.MTN_DATA_CUSTOMER
                || request.tranCode == TRANCODES.MTN_DATA_AGENT
                || request.tranCode == TRANCODES.MTN_VOICE_AGENT
                || request.tranCode == TRANCODES.MTN_VOICE_CUSTOMER)
            {
                MTNDataTopupResponse dataTopupResponse = StubClientService.mtnServices.mtnDataRecharge(new MTNDataTopupRequest()
                {
                    retry = false,
                    txnRef = transData.transId.ToString(),
                    billerCode = request.billerCode,
                    phoneNumber = request.referenceNo,
                    subscriptionId = request.paymentCode
                }, request, transData.transId);
                externalResponse1.responseCode = dataTopupResponse.responseStatus.responseCode;
                externalResponse1.responseMessage = dataTopupResponse.responseStatus.responseMessage;
                externalResponse1.transRefNo = transData.transId.ToString();
                txnResp.response = new ResponseMessage(externalResponse1.responseCode, externalResponse1.responseMessage);
            }
            else if (request.tranCode == TRANCODES.MTN_AIRTIME_CUSTOMER || request.tranCode == TRANCODES.MTN_AIRTIME_AGENT)
            {
                MTNTopupResponse mtnTopupResponse = StubClientService.mtnServices.mtnAirtimeRecharge(new MTNTopupRequest()
                {
                    retry = false,
                    txnRef = transData.transId.ToString(),
                    mobilePhone = request.referenceNo,
                    amount = request.transAmt
                }, request, transData.transId);
                externalResponse1.responseCode = mtnTopupResponse.responseStatus.responseCode;
                externalResponse1.responseMessage = mtnTopupResponse.responseStatus.responseMessage;
                externalResponse1.transRefNo = mtnTopupResponse.agenttransno;
                txnResp.response = new ResponseMessage(externalResponse1.responseCode, externalResponse1.responseMessage);
            }
            else if (request.tranCode == TRANCODES.NWSC_AGENT || request.tranCode == TRANCODES.NWSC_CUSTOMER)
            {
                request.externalTransId = transData.transId.ToString();
                request.paymentCode = "NWSC";
                txnResp = StubClientService.pegasusService.postTrans(request, transData.transId);
            }
            else if (request.tranCode == TRANCODES.UMEME_COLLECTION_AGENT || request.tranCode == TRANCODES.UMEME_COLLECTION_CUSTOMER)
            {
                request.externalTransId = transData.transId.ToString();
                request.paymentCode = "UMEME";
                txnResp = StubClientService.pegasusService.postTrans(request, transData.transId);
            }
            else if (request.tranCode == TRANCODES.GOTV_AGENT ||
                request.tranCode == TRANCODES.GOTV_CUSTOMER ||
                request.tranCode == TRANCODES.DSTV_AGENT ||
                request.tranCode == TRANCODES.DSTV_CUSTOMER ||
                request.tranCode == TRANCODES.STARTIMES_AGENT ||
                request.tranCode == TRANCODES.STARTIMES_CUSTOMER ||
                request.tranCode == TRANCODES.ZUKU_AGENT ||
                request.tranCode == TRANCODES.ZUKU_CUSTOMER ||
                request.tranCode == TRANCODES.AZAM_AGENT ||
                request.tranCode == TRANCODES.AZAM_CUSTOMER ||
                request.tranCode == TRANCODES.MTN_CASH_DEPOSIT ||
                request.tranCode == TRANCODES.MTN_CASH_DEPOSIT_CUSTOMER ||
                request.tranCode == TRANCODES.MTN_CASH_WITHDRAW ||
                request.tranCode == TRANCODES.WENRECO_CUSTOMER ||
                request.tranCode == TRANCODES.WENRECO_AGENT ||
                request.tranCode == TRANCODES.TUGENDE_AGENT ||
                request.tranCode == TRANCODES.TUGENDE_CUSTOMER ||
                request.tranCode == TRANCODES.UTL_CUSTOMER ||
                request.tranCode == TRANCODES.UTL_AGENT ||
                request.tranCode == TRANCODES.ROKE_CUSTOMER ||
                request.tranCode == TRANCODES.ROKE_AGENT)
                txnResp = StubClientService.interswitchService.sendAdviceRequest(request, transData.transId);

            else if (request.tranCode == TRANCODES.URA_CUSTOMER || request.tranCode == TRANCODES.URA_AGENT)
                txnResp = StubClientService.dtbService.postPRN(request, transData.transId);

            else if (request.tranCode == TRANCODES.AIRTEL_CASH_WITHDRAW)
                txnResp = StubClientService.airtelMoneyService.cashOut(request, transData.transId);

            else if (request.tranCode == TRANCODES.AIRTEL_CASH_DEPOSIT || request.tranCode == TRANCODES.AIRTEL_CASH_DEPOSIT_CUSTOMER)
                txnResp = StubClientService.airtelMoneyService.cashIn(request, transData.transId);

            else if (request.tranCode == TRANCODES.SUREPAY_SCHOOL_FEES_AGENT || request.tranCode == TRANCODES.SUREPAY_SCHOOL_FEES_CUSTOMER)
                txnResp = StubClientService.surepayService.makePayment(request, transData.transId);

            else if (request.tranCode == TRANCODES.LYCA_AIRTIME_AGENT || request.tranCode == TRANCODES.LYCA_AIRTIME_CUSTOMER)
            {
                ExternalResponse externalResponse2 = StubClientService.lycaMobileService.purchaseAirtime(request, transData.transId);
                txnResp.response = new ResponseMessage(externalResponse2.responseCode, externalResponse2.responseMessage);
                txnResp.utilityRef = externalResponse2.transRefNo;
            }
            else if (request.tranCode == TRANCODES.LYCA_DATA_AGENT || request.tranCode == TRANCODES.LYCA_DATA_CUSTOMER)
            {
                ExternalResponse externalResponse3 = StubClientService.lycaMobileService.purchaseData(request, transData.transId);
                txnResp.response = new ResponseMessage(externalResponse3.responseCode, externalResponse3.responseMessage);
                txnResp.utilityRef = externalResponse3.transRefNo;
            }
            //else if (request.tranCode == TRANCODES.CENTENARY_CASH_DEPOSIT)
            //{
            //    ExternalResponse externalResponse4 = StubClientService.airtelMoneyService.cashIn(request, transData.transId);
            //    txnResp.response = new ResponseMessage(externalResponse4.responseCode, externalResponse4.responseMessage);
            //    txnResp.utilityRef = externalResponse4.transRefNo;
            //}
            //else if (request.tranCode == TRANCODES.CENTENARY_CASH_WITHDRAW)
            //{
            //    ExternalResponse externalResponse5 = StubClientService.airtelMoneyService.cashOut(request, transData.transId);
            //    txnResp.response = new ResponseMessage(externalResponse5.responseCode, externalResponse5.responseMessage);
            //    txnResp.utilityRef = externalResponse5.transRefNo;
            //}
            else
                txnResp.response = MESSAGES.getSuccessMessage();
            return txnResp;
        }


        public SignUpResponse customerEnrollment(SignUpRequest request)
        {
            this.mediumsService = new MediumsService();
            SignUpResponse signUpResponse = this.mediumsService.customerEnrollment(request);
            signUpResponse.acctType = "CUSTOMER";
            signUpResponse.branchName = "Head Office";
            signUpResponse.response = MESSAGES.getSuccessMessage();
            return signUpResponse;
        }


        public TxnResp fundsTransfer(TxnData request)
        {
            this.mediumsService = new MediumsService();
            this.equiWebService = new eQuiWebService();
            this.start = DateTime.Now;
            bool isTransactionPended = false;
            TxnResp txnResp1;
            //if (request.tranCode == TRANCODES.AIRTEL_CASH_WITHDRAW)
            //    txnResp1 = StubClientService.airtelMoneyService.postAirtelWithdraw(request);
            if (request.tranCode == TRANCODES.MTN_CASH_WITHDRAW
                || request.tranCode == TRANCODES.AIRTEL_CASH_WITHDRAW)
            {
                isTransactionPended = true;
                txnResp1 = mediumsService.logTransaction(request);
            }
            else
                txnResp1 = mediumsService.fundsTransfer(request);

            this.end = DateTime.Now;
            this.timeDifference = this.end - this.start;
            this.difference_Miliseconds = (int)this.timeDifference.TotalMilliseconds;
            LOGGER.info("============== MEDIUMS PROCESSING DURATION: " + this.difference_Miliseconds.ToString() + "ms: SOURCE ACCOUNT: " + request.drAcctNo);
            if (txnResp1.response.responseCode != "0")
                throw new MediumsException(txnResp1.response);

            TxnResp txnResp2 = this.processBillPayments(request, txnResp1);
            if (txnResp2.response.responseCode != "0")
            {
                if (!isTransactionPended)
                {
                    this.mediumsService.reverseTrans(new CIReversalRequest()
                    {
                        authRequest = request.authRequest,
                        transId = txnResp1.transId,
                        reason = txnResp2.response.responseMessage
                    });
                }
                return txnResp2;
            }

            if (isTransactionPended)
            {
                request.originTransId = long.Parse(txnResp1.transId);
                txnResp1 = mediumsService.completeTransaction(request);
            }

            MessageService messageService = new MessageService();
            txnResp1.tokenValue = txnResp2.tokenValue;
            txnResp1.receiptNo = txnResp2.receiptNo;
            txnResp1.noOfUnits = txnResp2.noOfUnits;
            messageService.sendFinancialSMS(request, txnResp1);
            string str = (string)null; 
            if (request.tranCode == TRANCODES.UMEME_COLLECTION_AGENT || request.tranCode == TRANCODES.UMEME_COLLECTION_CUSTOMER)
                str = PrinterUtil.generateUmemeReceipt(request, txnResp1, "UMEME");
            else if (request.tranCode == TRANCODES.NWSC_AGENT || request.tranCode == TRANCODES.NWSC_CUSTOMER)
                str = PrinterUtil.generateNWSCReceipt(request, txnResp1);
            else if (request.tranCode == TRANCODES.DSTV_AGENT || request.tranCode == TRANCODES.DSTV_CUSTOMER)
                str = PrinterUtil.generateTVReceipt(request, txnResp1, "DSTV");
            else if (request.tranCode == TRANCODES.GOTV_AGENT || request.tranCode == TRANCODES.GOTV_CUSTOMER)
                str = PrinterUtil.generateTVReceipt(request, txnResp1, "GOTV");
            else if (request.tranCode == TRANCODES.STARTIMES_AGENT || request.tranCode == TRANCODES.STARTIMES_CUSTOMER)
                str = PrinterUtil.generateTVReceipt(request, txnResp1, "STARTIMES");
            else if (request.tranCode == TRANCODES.ZUKU_AGENT || request.tranCode == TRANCODES.ZUKU_CUSTOMER)
                str = PrinterUtil.generateTVReceipt(request, txnResp1, "ZUKU");
            else if (request.tranCode == TRANCODES.AZAM_AGENT || request.tranCode == TRANCODES.AZAM_CUSTOMER)
                str = PrinterUtil.generateTVReceipt(request, txnResp1, "AZAM");
            else if (request.tranCode == TRANCODES.MTN_AIRTIME_AGENT || request.tranCode == TRANCODES.MTN_AIRTIME_CUSTOMER)
                str = PrinterUtil.generateAirTimeReceipt(request, txnResp1, "MTN Airtime");
            else if (request.tranCode == TRANCODES.AIRTEL_AIRTIME_AGENT || request.tranCode == TRANCODES.AIRTEL_AIRTIME_CUSTOMER)
                str = PrinterUtil.generateAirTimeReceipt(request, txnResp1, "Airtel Airtime");
            else if (request.tranCode == TRANCODES.MTN_DATA_AGENT || request.tranCode == TRANCODES.MTN_DATA_CUSTOMER)
                str = PrinterUtil.generateDataReceipt(request, txnResp1, "MTN Data");
            else if (request.tranCode == TRANCODES.AIRTEL_DATA_AGENT || request.tranCode == TRANCODES.AIRTEL_DATA_CUSTOMER)
                str = PrinterUtil.generateDataReceipt(request, txnResp1, "Airtel Data");
            else if (request.tranCode == TRANCODES.LYCA_DATA_AGENT || request.tranCode == TRANCODES.LYCA_AIRTIME_CUSTOMER)
                str = PrinterUtil.generateDataReceipt(request, txnResp1, "Lyka Data");
            else if (request.tranCode == TRANCODES.CASH_DEPOSIT)
                str = PrinterUtil.generateCashInReceipt(request, txnResp1);
            else if (request.tranCode == TRANCODES.CASH_WITHDRAW)
                str = PrinterUtil.generateCashOutReceipt(request, txnResp1);
            else if (request.tranCode == TRANCODES.MTN_CASH_DEPOSIT || request.tranCode == TRANCODES.MTN_CASH_DEPOSIT_CUSTOMER)
                str = PrinterUtil.generateMobileMoneyCashInReceipt(request, txnResp1, "MTN");
            else if (request.tranCode == TRANCODES.AIRTEL_CASH_DEPOSIT || request.tranCode == TRANCODES.AIRTEL_CASH_DEPOSIT_CUSTOMER)
                str = PrinterUtil.generateMobileMoneyCashInReceipt(request, txnResp1, "Airtel");
            else if (request.tranCode == TRANCODES.MTN_CASH_WITHDRAW)
                str = PrinterUtil.generateMobileMoneyCashOutReceipt(request, txnResp1, "MTN");
            else if (request.tranCode == TRANCODES.AIRTEL_CASH_WITHDRAW)
                str = PrinterUtil.generateMobileMoneyCashOutReceipt(request, txnResp1, "Airtel");
            else if (request.tranCode == TRANCODES.WENRECO_AGENT || request.tranCode == TRANCODES.WENRECO_CUSTOMER)
                str = PrinterUtil.generateUmemeReceipt(request, txnResp1, "WENRECO");
            else if (request.tranCode == TRANCODES.TUGENDE_AGENT || request.tranCode == TRANCODES.TUGENDE_CUSTOMER)
                str = PrinterUtil.generateTugendeReceipt(request, txnResp1, "TUGENDE");
            else if (request.tranCode == TRANCODES.URA_AGENT || request.tranCode == TRANCODES.URA_CUSTOMER)
                str = PrinterUtil.generateURAReceipt(request, txnResp1);
            else if (request.tranCode == TRANCODES.SUREPAY_SCHOOL_FEES_AGENT || request.tranCode == TRANCODES.SUREPAY_SCHOOL_FEES_AGENT)
                str = PrinterUtil.generateSurePaySchoolReceipt(request, txnResp1);
            if (str != null)
            {
                txnResp1.printData = str;
                this.mediumsService.saveReceipt(new IssuedReceipt()
                {
                    authRequest = request.authRequest,
                    txnId = txnResp1.transId,
                    phoneNo = request.authRequest.phoneNo,
                    receiptData = txnResp1.printData
                });
            }
            return txnResp1;
        }

        public MTNTopupResponse mtnAirtimeRecharge(MTNTopupRequest mtnAirtimeRequest)
        {
            this.mediumsService = new MediumsService();
            this.equiWebService = new eQuiWebService();
            TxnData txnData = new TxnData();
            txnData.transAmt = mtnAirtimeRequest.amount;
            txnData.currency = mtnAirtimeRequest.currencyCode;
            txnData.description = "MTN Airtime-" + mtnAirtimeRequest.comments;
            txnData.tranCode = CONVERTER.getBillerServiceCode(mtnAirtimeRequest.sourceCode, "MTN");
            txnData.drAcctNo = CONVERTER.getCorporateDrAcct(mtnAirtimeRequest.sourceCode, "MTN");
            txnData.referenceNo = mtnAirtimeRequest.mobilePhone;
            txnData.authRequest = new OutletAuthentication()
            {
                channelCode = mtnAirtimeRequest.sourceCode,
                outletCode = "EXTERNAL",
                phoneNo = mtnAirtimeRequest.mobilePhone,
                vCode = mtnAirtimeRequest.apiUserName,
                vPassword = mtnAirtimeRequest.apiUserPassword
            };
            this.start = DateTime.Now;
            TxnResp txnResp = this.mediumsService.fundsTransfer(txnData);
            this.end = DateTime.Now;
            this.timeDifference = this.end - this.start;
            this.difference_Miliseconds = (int)this.timeDifference.TotalMilliseconds;
            LOGGER.info("============== MEDIUMS PROCESSING DURATION: " + this.difference_Miliseconds.ToString() + "ms: SOURCE ACCOUNT: " + txnData.drAcctNo);
            if (txnResp.response.responseCode != "0")
                throw new MediumsException(txnResp.response);
            MTNTopupResponse mtnTopupResponse = StubClientService.mtnServices.mtnAirtimeRecharge(mtnAirtimeRequest, txnData, txnResp.transId);
            int num = mtnTopupResponse.responseStatus.responseCode == "0" ? 1 : 0;
            return mtnTopupResponse;
        }

        public MTNDataTopupResponse mtnDataTopup(MTNDataTopupRequest mtnAirtimeRequest)
        {
            this.mediumsService = new MediumsService();
            this.equiWebService = new eQuiWebService();
            MTNDataBundleInquiryBundle bundleInquiryBundle = StubClientService.mtnServices.dataBundleInquiry(mtnAirtimeRequest.phoneNumber, mtnAirtimeRequest.subscriptionId);
            if (bundleInquiryBundle.statusCode != "0000")
                throw new MediumsException(new ResponseMessage(bundleInquiryBundle.statusCode, bundleInquiryBundle.message));
            TxnData txnData = new TxnData();
            txnData.transAmt = bundleInquiryBundle.data.amount.Value;
            txnData.currency = mtnAirtimeRequest.currencyCode;
            txnData.description = "MTN Data Bundles-" + mtnAirtimeRequest.subscriptionName;
            txnData.tranCode = TRANCODES.CENTE_AGENT_MTN_AIRTIME;
            txnData.drAcctNo = CONVERTER.getCorporateDrAcct(mtnAirtimeRequest.sourceCode, "MTN");
            txnData.referenceNo = mtnAirtimeRequest.phoneNumber;
            txnData.authRequest = new OutletAuthentication()
            {
                channelCode = mtnAirtimeRequest.sourceCode,
                outletCode = "EXTERNAL",
                phoneNo = mtnAirtimeRequest.phoneNumber,
                vCode = mtnAirtimeRequest.apiUserName,
                vPassword = mtnAirtimeRequest.apiUserPassword
            };
            this.start = DateTime.Now;
            TxnResp txnResp = this.mediumsService.fundsTransfer(txnData);
            this.end = DateTime.Now;
            this.timeDifference = this.end - this.start;
            this.difference_Miliseconds = (int)this.timeDifference.TotalMilliseconds;
            LOGGER.info("============== MEDIUMS PROCESSING DURATION: " + this.difference_Miliseconds.ToString() + "ms: SOURCE ACCOUNT: " + txnData.drAcctNo);
            if (txnResp.response.responseCode != "0")
                throw new MediumsException(txnResp.response);
            MTNDataTopupResponse dataTopupResponse = StubClientService.mtnServices.mtnDataRecharge(mtnAirtimeRequest, txnData, txnResp.transId);
            int num = dataTopupResponse.responseStatus.responseCode == "0" ? 1 : 0;
            return dataTopupResponse;
        }

        public MTNDataTopupResponse mtnDataCheckStatus(MTNDataTopupRequest mtnAirtimeRequest)
        {
            this.mediumsService = new MediumsService();
            this.equiWebService = new eQuiWebService();
            TxnData txnData = new TxnData()
            {
                transAmt = 0.0,
                currency = mtnAirtimeRequest.currencyCode,
                description = mtnAirtimeRequest.subscriptionName,
                tranCode = TRANCODES.CENTE_AGENT_MTN_AIRTIME,
                drAcctNo = CONVERTER.getCorporateDrAcct(mtnAirtimeRequest.sourceCode, "MTN"),
                referenceNo = mtnAirtimeRequest.phoneNumber,
                authRequest = new OutletAuthentication()
                {
                    channelCode = mtnAirtimeRequest.sourceCode,
                    outletCode = "EXTERNAL",
                    phoneNo = mtnAirtimeRequest.phoneNumber,
                    vCode = mtnAirtimeRequest.apiUserName,
                    vPassword = mtnAirtimeRequest.apiUserPassword
                }
            };
            MTNDataTopupResponse dataTopupResponse = StubClientService.mtnServices.mtnDataCheckStatus(mtnAirtimeRequest);
            int num = dataTopupResponse.responseStatus.responseCode == "0" ? 1 : 0;
            return dataTopupResponse;
        }

        public MTNTopupResponse mtnAirtimeCheckStatus(MTNTopupRequest mtnAirtimeRequest)
        {
            this.mediumsService = new MediumsService();
            this.equiWebService = new eQuiWebService();
            TxnData txnData = new TxnData()
            {
                transAmt = mtnAirtimeRequest.amount,
                currency = mtnAirtimeRequest.currencyCode,
                description = mtnAirtimeRequest.comments,
                tranCode = CONVERTER.getBillerServiceCode(mtnAirtimeRequest.sourceCode, "MTN"),
                drAcctNo = CONVERTER.getCorporateDrAcct(mtnAirtimeRequest.sourceCode, "MTN"),
                referenceNo = mtnAirtimeRequest.mobilePhone,
                authRequest = new OutletAuthentication()
                {
                    channelCode = mtnAirtimeRequest.sourceCode,
                    outletCode = "EXTERNAL",
                    phoneNo = mtnAirtimeRequest.mobilePhone,
                    vCode = mtnAirtimeRequest.apiUserName,
                    vPassword = mtnAirtimeRequest.apiUserPassword
                }
            };
            MTNTopupResponse mtnTopupResponse = StubClientService.mtnServices.checkAirtimeStatus(mtnAirtimeRequest);
            int num = mtnTopupResponse.responseStatus.responseCode == "0" ? 1 : 0;
            return mtnTopupResponse;
        }

        public MobileTopUpResponse airtelAirtimeRecharge(MobileTopUpRequest mtnAirtimeRequest)
        {
            this.mediumsService = new MediumsService();
            this.equiWebService = new eQuiWebService();
            TxnData txnData = new TxnData();
            txnData.transAmt = mtnAirtimeRequest.AMOUNT;
            txnData.currency = mtnAirtimeRequest.CURRENCY;
            txnData.description = mtnAirtimeRequest.COMMENT;
            txnData.tranCode = CONVERTER.getBillerServiceCode(mtnAirtimeRequest.SOURCECODE, "AIRTEL");
            txnData.drAcctNo = CONVERTER.getCorporateDrAcct(mtnAirtimeRequest.SOURCECODE, "AIRTEL");
            txnData.referenceNo = mtnAirtimeRequest.MSISDN2;
            txnData.authRequest = new OutletAuthentication()
            {
                channelCode = mtnAirtimeRequest.SOURCECODE,
                outletCode = "EXTERNAL",
                phoneNo = "256700000000",
                vCode = mtnAirtimeRequest.apiUserName,
                vPassword = mtnAirtimeRequest.apiUserPassword
            };
            this.start = DateTime.Now;
            TxnResp txnResp = this.mediumsService.fundsTransfer(txnData);
            this.end = DateTime.Now;
            this.timeDifference = this.end - this.start;
            this.difference_Miliseconds = (int)this.timeDifference.TotalMilliseconds;
            LOGGER.info("============== MEDIUMS PROCESSING DURATION: " + this.difference_Miliseconds.ToString() + "ms: SOURCE ACCOUNT: " + txnData.drAcctNo);
            if (txnResp.response.responseCode != "0")
                throw new MediumsException(txnResp.response);
            MobileTopUpResponse mobileTopUpResponse = StubClientService.pretupService.airtelAirtimeRecharge(mtnAirtimeRequest, txnData, txnResp.transId);
            int num = mobileTopUpResponse.responseStatus.responseCode == "0" ? 1 : 0;
            return mobileTopUpResponse;
        }

        public MobileTopUpResponse airtelAirtimeCheckStatus(MobileTopUpRequest mtnAirtimeRequest)
        {
            this.mediumsService = new MediumsService();
            this.equiWebService = new eQuiWebService();
            TxnData txnData = new TxnData()
            {
                transAmt = mtnAirtimeRequest.AMOUNT,
                currency = mtnAirtimeRequest.CURRENCY,
                description = mtnAirtimeRequest.COMMENT,
                tranCode = CONVERTER.getBillerServiceCode(mtnAirtimeRequest.SOURCECODE, "AIRTEL"),
                drAcctNo = CONVERTER.getCorporateDrAcct(mtnAirtimeRequest.SOURCECODE, "AIRTEL"),
                referenceNo = mtnAirtimeRequest.MSISDN2,
                authRequest = new OutletAuthentication()
                {
                    channelCode = mtnAirtimeRequest.SOURCECODE,
                    outletCode = "EXTERNAL",
                    phoneNo = "256700000000",
                    vCode = mtnAirtimeRequest.apiUserName,
                    vPassword = mtnAirtimeRequest.apiUserPassword
                }
            };
            MobileTopUpResponse mobileTopUpResponse = StubClientService.pretupService.checkAirtimeStatus(mtnAirtimeRequest);
            int num = mobileTopUpResponse.responseStatus.responseCode == "0" ? 1 : 0;
            return mobileTopUpResponse;
        }

        public MobileTopUpResponse airtelDataRecharge(MobileTopUpRequest mtnAirtimeRequest)
        {
            this.mediumsService = new MediumsService();
            this.equiWebService = new eQuiWebService();
            TxnData txnData = new TxnData();
            txnData.transAmt = mtnAirtimeRequest.AMOUNT;
            txnData.currency = mtnAirtimeRequest.CURRENCY;
            txnData.description = "Airtel Data Bundles-" + mtnAirtimeRequest.COMMENT;
            txnData.tranCode = TRANCODES.CENTE_AGENT_AIRTEL_AIRTIME;
            txnData.drAcctNo = CONVERTER.getCorporateDrAcct(mtnAirtimeRequest.SOURCECODE, "AIRTEL");
            txnData.referenceNo = mtnAirtimeRequest.MSISDN2;
            txnData.authRequest = new OutletAuthentication()
            {
                channelCode = mtnAirtimeRequest.SOURCECODE,
                outletCode = "EXTERNAL",
                phoneNo = "256700000000",
                vCode = mtnAirtimeRequest.apiUserName,
                vPassword = mtnAirtimeRequest.apiUserPassword
            };
            this.start = DateTime.Now;
            TxnResp txnResp = this.mediumsService.fundsTransfer(txnData);
            this.end = DateTime.Now;
            this.timeDifference = this.end - this.start;
            this.difference_Miliseconds = (int)this.timeDifference.TotalMilliseconds;
            LOGGER.info("============== MEDIUMS PROCESSING DURATION: " + this.difference_Miliseconds.ToString() + "ms: SOURCE ACCOUNT: " + txnData.drAcctNo);
            if (txnResp.response.responseCode != "0")
                throw new MediumsException(txnResp.response);
            MobileTopUpResponse mobileTopUpResponse = StubClientService.pretupService.airtelDataRecharge(mtnAirtimeRequest, txnData, txnResp.transId);
            int num = mobileTopUpResponse.responseStatus.responseCode == "0" ? 1 : 0;
            return mobileTopUpResponse;
        }

        public MobileTopUpResponse checkAirtelDataStatus(MobileTopUpRequest mtnAirtimeRequest)
        {
            this.mediumsService = new MediumsService();
            this.equiWebService = new eQuiWebService();
            TxnData txnData = new TxnData()
            {
                transAmt = mtnAirtimeRequest.AMOUNT,
                currency = mtnAirtimeRequest.CURRENCY,
                description = mtnAirtimeRequest.COMMENT,
                tranCode = TRANCODES.CENTE_AGENT_AIRTEL_AIRTIME,
                drAcctNo = CONVERTER.getCorporateDrAcct(mtnAirtimeRequest.SOURCECODE, "AIRTEL"),
                referenceNo = mtnAirtimeRequest.MSISDN2,
                authRequest = new OutletAuthentication()
                {
                    channelCode = mtnAirtimeRequest.SOURCECODE,
                    outletCode = "EXTERNAL",
                    phoneNo = "256700000000",
                    vCode = mtnAirtimeRequest.apiUserName,
                    vPassword = mtnAirtimeRequest.apiUserPassword
                }
            };
            MobileTopUpResponse mobileTopUpResponse = StubClientService.pretupService.checkAirtelDataStatus(mtnAirtimeRequest);
            int num = mobileTopUpResponse.responseStatus.responseCode == "0" ? 1 : 0;
            return mobileTopUpResponse;
        }

        public ExtFinancialResponseData submitEscrowTransaction(ExtFinancialRequestData request)
        {
            ExtFinancialResponseData financialResponseData = new ExtFinancialResponseData();
            this.mediumsService = new MediumsService();
            TxnResp txnResp = this.mediumsService.submitEscrowTransaction(new TxnData()
            {
                transAmt = request.paymentAmount,
                externalTransId = request.externalRefNo,
                crAcctNo = request.primaryAcctNo,
                currency = "UGX",
                description = request.adviceNote
            });
            financialResponseData.returnCode = txnResp.response.responseCode;
            financialResponseData.returnMessage = txnResp.response.responseMessage;
            financialResponseData.transactionReference = Convert.ToString(txnResp.transId);
            return financialResponseData;
        }

        public ResponseMessage fundsTransfer(ExtFinancialRequestData request)
        {
            this.equiWebService = new eQuiWebService();
            TxnData txnData = new TxnData()
            {
                transAmt = request.paymentAmount,
                externalTransId = request.externalRefNo,
                crAcctNo = request.primaryAcctNo,
                drAcctNo = request.contraAcctNo,
                currency = "UGX",
                description = request.adviceNote,
                authRequest = new OutletAuthentication()
                {
                    outletCode = PROPERTIES.EQUIWEB_USER
                }
            };
            return this.equiWebService.fundsTransfer(request).response;
        }

        public ResponseMessage reverseTrans(CIReversalRequest request)
        {
            this.mediumsService = new MediumsService();
            this.mediumsService.reverseTrans(request);
            return MESSAGES.getSuccessMessage();
        }
          
        public ResponseMessage sendPaymentRequest(RequestPayment request)
        {
            this.mediumsService = new MediumsService();
            this.mediumsService.sendPaymentRequest(request);
            return MESSAGES.getSuccessMessage();
        } 

        public TxnResp reviewPaymentRequest(RequestPayment request)
        {
            this.mediumsService = new MediumsService();
            return mediumsService.reviewPaymentRequest(request);
        }

         
        public RequestPaymentData findPendingRequest(RequestPayment request)
        {
            this.mediumsService = new MediumsService();
            return mediumsService.findPendingRequest(request);
        }


        public CIStatementResponse doAccountFullStatement(CIStatementRequest request)
        {
            return StubClientService.mediumsService.doAccountFullStatement(request);
        }

        public List<BillerProductCategory> findProductCategoryByBillerCode(BillerProduct request)
        {
            this.mediumsService = new MediumsService();
            return this.mediumsService.findProductCategoryByBillerCode(request);
        }

        public List<BillerProduct> findBillerProductsByBiller(BillerProduct request)
        {
            this.mediumsService = new MediumsService();
            return this.mediumsService.findBillerProductsByBiller(request);
        }

        public BillerProduct findBillerProductsByBillerByProductId(int billerProdId)
        {
            this.mediumsService = new MediumsService();
            return this.mediumsService.findBillerProductsByBillerByProductId(billerProdId);
        }

        public CICustomerResp pinAuthentication(OutletAuthentication request)
        {
            this.mediumsService = new MediumsService();
            return this.mediumsService.pinAuthentication(request);
        }

        public ResponseMessage performDevicePairing(OutletAuthentication request)
        {
            this.mediumsService = new MediumsService();
            return this.mediumsService.performDevicePairing(request);
        }

        public ResponseMessage generateDeviceActivationCode(OutletAuthentication request)
        {
            this.mediumsService = new MediumsService();
            return this.mediumsService.generateDeviceActivationCode(request);
        }

        public CICustomerResp accountResponseByPhoneNo(OutletAuthentication request)
        {
            this.mediumsService = new MediumsService();
            return this.mediumsService.accountInquiryByPhoneNo(request);
        }

        public CIAccountResponse accountResponseByAccountNo(AccountRequest request)
        {
            this.equiWebService = new eQuiWebService();
            return this.equiWebService.accountInquiryByAccountNo(request);
        }

        public CIAccountResponse lycaMobile(TxnData request)
        {
            this.equiWebService = new eQuiWebService();
            return StubClientService.lycaMobileService.customerResponseByphone(request);
        }

        public CIAccountBalanceResponse accountBalanceInquiry(AccountRequest request)
        {
            return StubClientService.mediumsService.doAccountBalance(request);
        }


        public CIChargeResponse findTransactionCharge(CIChargeRequest request)
        {
            this.mediumsService = new MediumsService();
            return this.mediumsService.findTransactionCharge(request);
        }

        public ResponseMessage changePIN(CIPINChangeRequest request)
        {
            this.mediumsService = new MediumsService();
            return this.mediumsService.changePIN(request);
        }

        public CIOutletResponse findOutletDetails(OutletAuthentication request)
        {
            this.mediumsService = new MediumsService();
            return this.mediumsService.findOutletDetails(request);
        }

        public CIOutletResponse findSuperAgentDetails(OutletAuthentication request)
        {
            this.mediumsService = new MediumsService();
            return this.mediumsService.findSuperAgentDetails(request);
        }

        public MessageRespData findMessages(OutletAuthentication request)
        {
            this.mediumsService = new MediumsService();
            return this.mediumsService.findMessages(request);
        }

        public IssuedReceiptData findIssuedReceipt(IssuedReceipt request)
        {
            this.mediumsService = new MediumsService();
            return this.mediumsService.findIssuedReceipt(request);
        }

        public CIVoucherResponse voucherPurchase(CIVoucherRequest request)
        {
            this.mediumsService = new MediumsService();
            request.authRequest.outletCode = "EXTERNAL";
            CIVoucherResponse ciVoucherResponse = this.mediumsService.voucherPurchase(request);
            this.start = DateTime.Now;
            if (ciVoucherResponse.response.responseCode != "0")
                throw new MediumsException(ciVoucherResponse.response);
            return ciVoucherResponse;
        }

        public TxnResp voucherRedeem(CIVoucherRequest request)
        {
            this.mediumsService = new MediumsService();
            TxnResp txnResp = this.mediumsService.voucherRedeem(request);
            this.start = DateTime.Now;
            if (txnResp.response.responseCode != "0")
                throw new MediumsException(txnResp.response);
            TxnData txnData = new TxnData()
            {
                transAmt = request.transAmt,
                currency = "UGX",
                description = request.description,
                drAcctNo = "",
                crAcctNo = request.outletAcctNo,
                referenceNo = Convert.ToString(txnResp.transId),
                authRequest = request.authRequest
            };
            return txnResp;
        }

        public CIVoucherResponse findVoucherDetails(CIVoucherRequest request)
        {
            this.mediumsService = new MediumsService();
            return this.mediumsService.findVoucherDetails(request);
        }

        public CIOTPResponse initiateCashWithdraw(CIOTPRequestData request)
        {
            this.mediumsService = new MediumsService();
            CIOTPResponse response = this.mediumsService.initiateCashWithdraw(request);
            if (response.response.responseCode == "0")
                new MessageService().sendCustomerInititationMessages(request, response);
            return response;
        }

        public CIOTPResponse initiateOutletCashWithdraw(CIOTPRequestData request)
        {
            this.mediumsService = new MediumsService();
            CIOTPResponse response = this.mediumsService.initiateOutletCashWithdraw(request);
            if (response.response.responseCode == "0")
                new MessageService().sendCustomerInititationMessages(request, response);
            return response;
        }

        public CIOTPResponse withdrawCodeInquiry(CIOTPRequestData request)
        {
            this.mediumsService = new MediumsService();
            return this.mediumsService.customerWithdrawCodeInquiry(request);
        }

        public CIOTPResponse outletWithdrawCodeInquiry(CIOTPRequestData request)
        {
            this.mediumsService = new MediumsService();
            return this.mediumsService.outletWithdrawCodeInquiry(request);
        }

        public ResponseMessage pairCustomerDevice(OutletAuthentication request)
        {
            this.mediumsService = new MediumsService();
            return this.mediumsService.pairCustomerDevice(request);
        }

        public MTNDataBundleInquiryBundle dataBundleInquiry(MTNDataTopupRequest request)
        {
            this.mediumsService = new MediumsService();
            return StubClientService.mtnServices.dataBundleInquiry(request.phoneNumber, request.subscriptionId);
        }

        public CoporateAgencyResponse processCorporateAgentService(TxnData request)
        {
            this.coporateAgencyService = new CoporateAgencyService();
            if (request.paymentCode == null)
                return new CoporateAgencyResponse(new int?(-99), "Payment code is missing");
            if (request.paymentCode == "CASH_DEPOSIT_VALIDATION")
                return this.coporateAgencyService.cashDepositValidation(request);
            if (request.paymentCode == "ABC_CASHIN_VALIDATION")
            {
                CoporateAgencyResponse coporateAgencyResponse = this.coporateAgencyService.abcCashInValidation(request);
                ReturnObject returnObject = coporateAgencyResponse.returnObject ?? new ReturnObject();
                returnObject.acctTitle = returnObject.customerName ?? request.bankCode;
                coporateAgencyResponse.returnObject = returnObject;
                return coporateAgencyResponse;
            }
            if (request.paymentCode == "CASH_WITHDRAW_VALIDATION")
            {
                CoporateAgencyResponse coporateAgencyResponse = this.coporateAgencyService.cashWithdrawValidation(request);
                ReturnObject returnObject = coporateAgencyResponse.returnObject;
                if (returnObject != null)
                {
                    returnObject.acctTitle = returnObject.customerName;
                    coporateAgencyResponse.returnObject = returnObject;
                }
                return coporateAgencyResponse;
            }
            if (request.paymentCode == "VALIDATE_BILL_PAYMENT")
                return this.coporateAgencyService.billPaymentValidation(request);
            if (request.paymentCode == "INITIATE_BILL_PAYMENT")
            {
                CoporateAgencyResponse coporateAgencyResponse = this.coporateAgencyService.billPaymentInitiation(request);
                ReturnObject returnObject = coporateAgencyResponse.returnObject;
                if (returnObject != null)
                {
                    returnObject.studentFullName = returnObject.firstName + " " + returnObject.lastName;
                    returnObject.schoolAcctNo = returnObject.dateOfBirth;
                }
                return coporateAgencyResponse;
            }
            this.mediumsService = new MediumsService();
            this.start = DateTime.Now;
            TxnResp response = this.mediumsService.fundsTransfer(request);
            this.end = DateTime.Now;
            this.timeDifference = this.end - this.start;
            this.difference_Miliseconds = (int)this.timeDifference.TotalMilliseconds;
            LOGGER.info("============== MEDIUMS PROCESSING DURATION: " + this.difference_Miliseconds.ToString() + "ms: SOURCE ACCOUNT: " + request.drAcctNo);
            if (response.response.responseCode != "0")
                throw new MediumsException(response.response);

            CoporateAgencyResponse coporateAgencyResponse1;

            if (request.paymentCode == "CASH_DEPOSIT_COMPLETION")
                coporateAgencyResponse1 = this.coporateAgencyService.cashDepositConfirmation(request);
            else if (request.paymentCode == "CASH_WITHDRAW_COMPLETION")
                coporateAgencyResponse1 = this.coporateAgencyService.cashWithdrawCompletion(request);
            else if (request.paymentCode == "CONFIRM_BILL_PAYMENT")
                coporateAgencyResponse1 = this.coporateAgencyService.billPaymentConfirmation(request);
            else if (request.paymentCode == "ABC_CASHIN")
            {
                coporateAgencyResponse1 = this.coporateAgencyService.abcCashInConfirmation(request, response.transId);

            }
            else
                coporateAgencyResponse1 = new CoporateAgencyResponse(new int?(-99), "Invalid Payment code selected");

            if (coporateAgencyResponse1.returnCode.HasValue)
            {
                int? returnCode = coporateAgencyResponse1.returnCode;
                int num1 = 0;
                if (!(returnCode.GetValueOrDefault() == num1 & returnCode.HasValue))
                {
                    this.mediumsService.reverseTrans(new CIReversalRequest()
                    {
                        authRequest = request.authRequest,
                        transId = response.transId,
                        reason = coporateAgencyResponse1.returnMessage
                    });
                    return coporateAgencyResponse1;
                }
                response.receiptNo = DateTime.Now.ToString("ddMMyyyyHHmmss");
                request.bankName = request.bankName ?? "Centenary Bank";
                coporateAgencyResponse1.drAcctBal = response.drAcctBal;
                coporateAgencyResponse1.transId = Convert.ToString(response.transId);
                if (request.paymentCode == "CASH_DEPOSIT_COMPLETION")
                    coporateAgencyResponse1.printData = PrinterUtil.generateCorporateCashInReceipt(request, response);

                else if (request.paymentCode == "ABC_CASHIN")
                {
                    ABCReceiptData returnObject = PrinterUtil.ParseReceipt(coporateAgencyResponse1.printData);
                    request.customerName = returnObject.CustName;
                    coporateAgencyResponse1.printData = PrinterUtil.generateCorporateCashInReceipt(request, response);
                }
                else if (request.paymentCode == "CASH_WITHDRAW_COMPLETION")
                {
                    ReturnObject returnObject = coporateAgencyResponse1.returnObject;
                    TxnResp txnResp = response;
                    double? chargedAmount = returnObject.chargedAmount;
                    double num2;
                    if (chargedAmount.HasValue)
                    {
                        chargedAmount = returnObject.chargedAmount;
                        num2 = chargedAmount.Value;
                    }
                    else
                        num2 = 0.0;
                    txnResp.chargeAmt = num2;
                    coporateAgencyResponse1.printData = PrinterUtil.generateCorporateCashOutReceipt(request, response);
                }
                else if (request.paymentCode == "CONFIRM_BILL_PAYMENT" && request.billerCode == "SCHOOLPAY")
                    coporateAgencyResponse1.printData = PrinterUtil.generateCorporateSchoolPayReceipt(request, response);
                this.mediumsService.saveReceipt(new IssuedReceipt()
                {
                    authRequest = request.authRequest,
                    txnId = response.transId,
                    phoneNo = request.authRequest.phoneNo,
                    receiptData = coporateAgencyResponse1.printData
                });
            }
            coporateAgencyResponse1.returnMessage = removeBalanceInfo(coporateAgencyResponse1.returnMessage);
            return coporateAgencyResponse1;
        }


        static string removeBalanceInfo(string input)
        {
            if (input == null)
                return input;

            int balIndex = input.IndexOf("\nBal:");
            if (balIndex != -1)
            {
                return input.Substring(0, balIndex);
            }
            return input;
        }

        public TxnResp crdbPostTransaction(TransactionItem request)
        {
            this.mediumsService = new MediumsService();
            return StubClientService.centeESBService.postToESB(request);
        }

        public TxnResp reverseFromESB(ReversalRequest request)
        {
            this.mediumsService = new MediumsService();
            return StubClientService.centeESBService.reverseFromESB(request);
        }

        public TxnResp airtelCashOut(TxnData request)
        {
            string mcpTransId = DateTime.Now.ToString("yyMMddHHmmssfff");
            this.mediumsService = new MediumsService();
            return StubClientService.airtelMoneyService.cashOut(request, mcpTransId);
        }

        public TxnResp airtelCashIn(TxnData request)
        {
            string mcpTransId = DateTime.Now.ToString("yyMMddHHmmssfff");
            this.mediumsService = new MediumsService();
            return StubClientService.airtelMoneyService.cashIn(request, mcpTransId);
        }

        public TxnResp findAirtelTxnStatus(string reference, string apiName)
        {
            return StubClientService.airtelMoneyService.findAirtelTxnStatus(reference, apiName);
        }
    }
}
