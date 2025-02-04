using micropay_apis.ABModels;
using micropay_apis.APIModals;
using micropay_apis.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace micropay_apis.Utils
{
    public class PrinterUtil
    {
        public static string generateCorporateCashInReceipt(TxnData request, TxnResp response)
        {
            StringBuilder sb = new StringBuilder();
            response.chargeAmt = 0.0;
            StringBuilder receiptHeader = PrinterUtil.getReceiptHeader(sb);
            receiptHeader.AppendFormat(PrinterUtil.formartSingleLineString("Cash Deposit"));
            StringBuilder recieptCommonData = PrinterUtil.generateRecieptCommonData(receiptHeader, request, response);
            string itemName1 = "Cust Account";
            string referenceNo = request.referenceNo;
            recieptCommonData.AppendFormat(PrinterUtil.formatLineItem(itemName1, referenceNo));
            string itemName2 = "Bank Name";
            string bankName = request.bankName;
            recieptCommonData.AppendFormat(PrinterUtil.formatLineItem(itemName2, bankName));
            string itemName3 = "Depositor";
            string depositorName = request.depositorName;
            recieptCommonData.AppendFormat(PrinterUtil.formatLineItem(itemName3, depositorName));
            return PrinterUtil.getReceiptFooter(PrinterUtil.getCentenaryAddress(recieptCommonData)).ToString();
        }

        public static string generateCorporateCashOutReceipt(TxnData request, TxnResp response)
        {
            StringBuilder receiptHeader = PrinterUtil.getReceiptHeader(new StringBuilder());
            receiptHeader.AppendFormat(PrinterUtil.formartSingleLineString("Cash Withdraw"));
            StringBuilder recieptCommonData = PrinterUtil.generateRecieptCommonData(receiptHeader, request, response);
            string itemName1 = "Cust Account";
            string referenceNo = request.referenceNo;
            recieptCommonData.AppendFormat(PrinterUtil.formatLineItem(itemName1, referenceNo));
            string itemName2 = "Bank Name";
            string bankName = request.bankName;
            recieptCommonData.AppendFormat(PrinterUtil.formatLineItem(itemName2, bankName));
            return PrinterUtil.getReceiptFooter(PrinterUtil.getCentenaryAddress(recieptCommonData)).ToString();
        }

        public static string generateCorporateSchoolPayReceipt(TxnData request, TxnResp response)
        {
            StringBuilder sb = new StringBuilder();
            response.chargeAmt = request.surCharge;
            StringBuilder receiptHeader = PrinterUtil.getReceiptHeader(sb);
            receiptHeader.AppendFormat(PrinterUtil.formartSingleLineString("SchoolPay Payment"));
            StringBuilder recieptCommonData = PrinterUtil.generateRecieptCommonData(receiptHeader, request, response);
            string itemName1 = "School";
            string schoolName = request.schoolName;
            recieptCommonData.AppendFormat(PrinterUtil.formatLineItem(itemName1, schoolName, 7, 23));
            string itemName2 = "Account";
            string crAcctNo = request.crAcctNo;
            recieptCommonData.AppendFormat(PrinterUtil.formatLineItem(itemName2, crAcctNo));
            string itemName3 = "Student No";
            string referenceNo = request.referenceNo;
            recieptCommonData.AppendFormat(PrinterUtil.formatLineItem(itemName3, referenceNo));
            string itemName4 = "Class";
            string className = request.className;
            recieptCommonData.AppendFormat(PrinterUtil.formatLineItem(itemName4, className, 7, 23));
            string itemName5 = "Bank Name";
            string bankName = request.bankName;
            recieptCommonData.AppendFormat(PrinterUtil.formatLineItem(itemName5, bankName));
            return PrinterUtil.getReceiptFooter(PrinterUtil.getCentenaryAddress(recieptCommonData)).ToString();
        }

         
        public static string generateSurePaySchoolReceipt(TxnData request, TxnResp response)
        {
            StringBuilder sb = new StringBuilder();
            response.chargeAmt = request.surCharge;
            StringBuilder receiptHeader = PrinterUtil.getReceiptHeader(sb);
            receiptHeader.AppendFormat(PrinterUtil.formartSingleLineString("School Fees Payment"));
            StringBuilder recieptCommonData = PrinterUtil.generateRecieptCommonData(receiptHeader, request, response);
            string itemName1 = "School";
            string schoolName = request.schoolName;
            recieptCommonData.AppendFormat(PrinterUtil.formatLineItem(itemName1, schoolName, 7, 23));
            string itemName2 = "Account";
            string crAcctNo = request.crAcctNo;
            recieptCommonData.AppendFormat(PrinterUtil.formatLineItem(itemName2, crAcctNo));
            string itemName3 = "Student A/C";
            string referenceNo = request.referenceNo;
            recieptCommonData.AppendFormat(PrinterUtil.formatLineItem(itemName3, referenceNo));
            string itemName4 = "Class";
            string className = request.className;
            recieptCommonData.AppendFormat(PrinterUtil.formatLineItem(itemName4, className, 7, 23));
            return PrinterUtil.getReceiptFooter(recieptCommonData).ToString();
        }


        public static string generateUmemeReceipt(
          TxnData request,
          TxnResp response,
          string packageType)
        {
            StringBuilder receiptHeader = PrinterUtil.getReceiptHeader(new StringBuilder());
            receiptHeader.AppendFormat(PrinterUtil.formartSingleLineString(packageType + " " + CONVERTER.toSentenceCase(request.customerType) + " Payment"));
            StringBuilder recieptCommonData = PrinterUtil.generateRecieptCommonData(receiptHeader, request, response);
            string itemName1 = "Customer Type";
            string customerType = request.customerType;
            recieptCommonData.AppendFormat(PrinterUtil.formatLineItem(itemName1, customerType));
            string itemName2 = "Meter No.";
            string referenceNo = request.referenceNo;
            recieptCommonData.AppendFormat(PrinterUtil.formatLineItem(itemName2, referenceNo));
            string itemName3 = "Token No.";
            string tokenValue = response.tokenValue;
            recieptCommonData.AppendFormat(PrinterUtil.formatLineItem(itemName3, tokenValue));
            string itemName4 = "Units.";
            string noOfUnits = response.noOfUnits;
            recieptCommonData.AppendFormat(PrinterUtil.formatLineItem(itemName4, noOfUnits));
            return PrinterUtil.getReceiptFooter(recieptCommonData).ToString();
        }

        public static string generateNWSCReceipt(TxnData request, TxnResp response)
        {
            StringBuilder receiptHeader = PrinterUtil.getReceiptHeader(new StringBuilder());
            receiptHeader.AppendFormat(PrinterUtil.formartSingleLineString("NWSC Payment"));
            StringBuilder recieptCommonData = PrinterUtil.generateRecieptCommonData(receiptHeader, request, response);
            string itemName1 = "Reference";
            string referenceNo = request.referenceNo;
            recieptCommonData.AppendFormat(PrinterUtil.formatLineItem(itemName1, referenceNo));
            string itemName2 = "Cust. Area";
            string customerArea = request.customerArea;
            recieptCommonData.AppendFormat(PrinterUtil.formatLineItem(itemName2, customerArea));
            string itemName3 = "Ext Trans ID.";
            string utilityRef = response.utilityRef;
            recieptCommonData.AppendFormat(PrinterUtil.formatLineItem(itemName3, utilityRef));
            return PrinterUtil.getReceiptFooter(recieptCommonData).ToString();
        }

        public static string generateTugendeReceipt(TxnData request, TxnResp response, string tvType)
        {
            StringBuilder receiptHeader = PrinterUtil.getReceiptHeader(new StringBuilder());
            receiptHeader.AppendFormat(PrinterUtil.formartSingleLineString(tvType + " Payment"));
            StringBuilder recieptCommonData = PrinterUtil.generateRecieptCommonData(receiptHeader, request, response);
            string itemName = "Reference";
            string referenceNo = request.referenceNo;
            recieptCommonData.AppendFormat(PrinterUtil.formatLineItem(itemName, referenceNo));
            return PrinterUtil.getReceiptFooter(recieptCommonData).ToString();
        }
         
        public static string generateURAReceipt(TxnData request, TxnResp response)
        {
            StringBuilder receiptHeader = PrinterUtil.getReceiptHeader(new StringBuilder());
            receiptHeader.AppendFormat(PrinterUtil.formartSingleLineString("URA Payment"));
            StringBuilder recieptCommonData = PrinterUtil.generateRecieptCommonData(receiptHeader, request, response, "URA");
            string itemName = "PRN";
            string referenceNo = request.referenceNo;
            recieptCommonData.AppendFormat(PrinterUtil.formatLineItem(itemName, referenceNo));
            return PrinterUtil.getReceiptFooter(recieptCommonData).ToString();
        }

        public static string generateTVReceipt(TxnData request, TxnResp response, string tvType)
        {
            StringBuilder receiptHeader = PrinterUtil.getReceiptHeader(new StringBuilder());
            receiptHeader.AppendFormat(PrinterUtil.formartSingleLineString(tvType + " Payment"));
            StringBuilder recieptCommonData = PrinterUtil.generateRecieptCommonData(receiptHeader, request, response);
            string itemName1 = "Reference";
            string referenceNo = request.referenceNo;
            recieptCommonData.AppendFormat(PrinterUtil.formatLineItem(itemName1, referenceNo));
            string itemName2 = "Package name";
            string packageName = request.packageName;
            recieptCommonData.AppendFormat(PrinterUtil.formatLineItem(itemName2, packageName));
            return PrinterUtil.getReceiptFooter(recieptCommonData).ToString();
        }

        public static string generateAirTimeReceipt(TxnData request, TxnResp response, string category)
        {
            StringBuilder receiptHeader = PrinterUtil.getReceiptHeader(new StringBuilder());
            receiptHeader.AppendFormat(PrinterUtil.formartSingleLineString(category + " Payment"));
            StringBuilder recieptCommonData = PrinterUtil.generateRecieptCommonData(receiptHeader, request, response);
            string itemName = "Customer Phone";
            string referenceNo = request.referenceNo;
            recieptCommonData.AppendFormat(PrinterUtil.formatLineItem(itemName, referenceNo));
            return PrinterUtil.getReceiptFooter(recieptCommonData).ToString();
        }

        public static string generateDataReceipt(TxnData request, TxnResp response, string category)
        {
            StringBuilder receiptHeader = PrinterUtil.getReceiptHeader(new StringBuilder());
            receiptHeader.AppendFormat(PrinterUtil.formartSingleLineString(category + " Payment"));
            StringBuilder recieptCommonData = PrinterUtil.generateRecieptCommonData(receiptHeader, request, response);
            string itemName = "Customer Phone";
            string referenceNo = request.referenceNo;
            recieptCommonData.AppendFormat(PrinterUtil.formatLineItem(itemName, referenceNo));
            return PrinterUtil.getReceiptFooter(recieptCommonData).ToString();
        }

        public static string generateCashOutReciept(TxnData request, TxnResp response, string category)
        {
            StringBuilder receiptHeader = PrinterUtil.getReceiptHeader(new StringBuilder());
            receiptHeader.AppendFormat(PrinterUtil.formartSingleLineString(category + " Payment"));
            StringBuilder recieptCommonData = PrinterUtil.generateRecieptCommonData(receiptHeader, request, response);
            string itemName = "Customer Phone";
            string referenceNo = request.referenceNo;
            recieptCommonData.AppendFormat(PrinterUtil.formatLineItem(itemName, referenceNo));
            return PrinterUtil.getReceiptFooter(recieptCommonData).ToString();
        }

        public static string generateCashInReceipt(TxnData request, TxnResp response)
        {
            StringBuilder receiptHeader = PrinterUtil.getReceiptHeader(new StringBuilder());
            receiptHeader.AppendFormat(PrinterUtil.formartSingleLineString("Cash Deposit"));
            return PrinterUtil.getReceiptFooter(PrinterUtil.generateRecieptCommonData(receiptHeader, request, response)).ToString();
        }

        public static string generateCashOutReceipt(TxnData request, TxnResp response)
        {
            StringBuilder receiptHeader = PrinterUtil.getReceiptHeader(new StringBuilder());
            receiptHeader.AppendFormat(PrinterUtil.formartSingleLineString("Cash Withdraw"));
            return PrinterUtil.getReceiptFooter(PrinterUtil.generateRecieptCommonData(receiptHeader, request, response)).ToString();
        }

        public static string generateMobileMoneyCashOutReceipt(
          TxnData request,
          TxnResp response,
          string mno)
        {
            StringBuilder receiptHeader = PrinterUtil.getReceiptHeader(new StringBuilder());
            receiptHeader.AppendFormat(PrinterUtil.formartSingleLineString(mno + " Cash Withdraw"));
            return PrinterUtil.getReceiptFooter(PrinterUtil.generateRecieptCommonData(receiptHeader, request, response)).ToString();
        }

        public static string generateMobileMoneyCashInReceipt(
          TxnData request,
          TxnResp response,
          string mno)
        {
            StringBuilder receiptHeader = PrinterUtil.getReceiptHeader(new StringBuilder());
            receiptHeader.AppendFormat(PrinterUtil.formartSingleLineString(mno + " Cash Deposit"));
            return PrinterUtil.getReceiptFooter(PrinterUtil.generateRecieptCommonData(receiptHeader, request, response)).ToString();
        }

        private static double getAmount(
          List<ChildTrans> transDetailsList,
          TxnData request,
          string amountType)
        {
            double amount = 0.0;
            for (int index1 = 0; index1 < transDetailsList.Count; ++index1)
            {
                if (transDetailsList[index1].amountType == amountType && transDetailsList[index1].sourceAcctNo == request.drAcctNo)
                {
                    amount = transDetailsList[index1].transAmt;
                    if (amountType == "EXCISE_DUTY" && amount > 0.0)
                    {
                        for (int index2 = 0; index2 < transDetailsList.Count; ++index2)
                        {
                            if (transDetailsList[index2].amountType == "MOBILE_MONEY_TAX" && transDetailsList[index2].sourceAcctNo == request.drAcctNo)
                            {
                                amount += transDetailsList[index2].transAmt;
                                break;
                            }
                        }
                        break;
                    }
                    break;
                }
            }
            return amount;
        }

        private static StringBuilder getReceiptHeader(StringBuilder sb)
        {
            sb.AppendFormat(PrinterUtil.formartSingleLineString("--------------------------------"));
            sb.AppendFormat("\n");
            return sb;
        }
         
        private static StringBuilder generateRecieptCommonData(
          StringBuilder sb,
          TxnData request,
          TxnResp response, string receiptType = null)
        {
            OutletAuthentication authRequest = request.authRequest;
            if (response.receiptNo == null)
                response.receiptNo = DateTime.Now.ToString("yyyyMMddHHmmss");
            double num = request.transAmt + response.chargeAmt;
            string itemName1 = "Reciept #";
            string receiptNo = response.receiptNo;
            sb.AppendFormat(PrinterUtil.formatLineItem(itemName1, receiptNo));
            string itemName2 = "Agent Name";
            string itemValue1 = authRequest.entityName == null ? "" : authRequest.entityName.Trim();
            sb.AppendFormat(PrinterUtil.formatLineItem(itemName2, itemValue1));
            string itemName3 = "Agent Code";
            string itemValue2 = authRequest.outletCode == null ? "" : authRequest.outletCode.Trim();
            sb.AppendFormat(PrinterUtil.formatLineItem(itemName3, itemValue2));
            string itemName4 = "Agent Phone";
            string itemValue3 = authRequest.phoneNo == null ? "" : authRequest.phoneNo.Trim();
            sb.AppendFormat(PrinterUtil.formatLineItem(itemName4, itemValue3));
            string itemName5 = "Trans Date";
            string itemValue4 = DateTime.Now.ToString("dd-MMM-yyyy HH:mm:ss");
            sb.AppendFormat(PrinterUtil.formatLineItem(itemName5, itemValue4));
            sb.AppendFormat(PrinterUtil.formartSingleLineString("--------------------------------"));

            if (receiptType != null && receiptType == "URA")
            {
                sb.AppendFormat(PrinterUtil.formartSingleLineString("DIAMOND TRUST BANK LIMITED"));
                sb.AppendFormat(PrinterUtil.formartSingleLineString("URA BILL PAYMENT"));
            }
            sb.AppendFormat(PrinterUtil.formartSingleLineString("--------------------------------"));
            string itemName6 = "Trans Amount";
            string itemValue5 = request.transAmt.ToString("N2") + " UGX";
            sb.AppendFormat(PrinterUtil.formatLineItem(itemName6, itemValue5));
            string itemName7 = "Charge";
            double amount = PrinterUtil.getAmount(response.transItems, request, "CHARGE");
            string itemValue6 = amount.ToString("N2") + " UGX";
            sb.AppendFormat(PrinterUtil.formatLineItem(itemName7, itemValue6));
            string itemName8 = "Tax";
            amount = PrinterUtil.getAmount(response.transItems, request, "EXCISE_DUTY");
            string itemValue7 = amount.ToString("N2") + " UGX";
            sb.AppendFormat(PrinterUtil.formatLineItem(itemName8, itemValue7));
            string itemName9 = "Total Amount";
            string itemValue8 = num.ToString("N2") + " UGX";
            sb.AppendFormat(PrinterUtil.formatLineItem(itemName9, itemValue8));
            sb.AppendFormat(PrinterUtil.formartSingleLineString(AmountToWord.convertNumberToWords(request.transAmt)));
            string itemName10 = "Trans ID";
            string transId = response.transId;
            sb.AppendFormat(PrinterUtil.formatLineItem(itemName10, transId));
            string itemName11 = "Customer Name";
            string customerName = request.customerName;
            sb.AppendFormat(PrinterUtil.formatLineItem(itemName11, customerName));
            return sb;
        }

        private static StringBuilder getCentenaryAddress(StringBuilder sb)
        {
            sb.AppendFormat(PrinterUtil.formartSingleLineString("--------------------------------"));
            string itemName1 = "Toll Free:";
            string itemValue1 = "0800 200 555";
            sb.AppendFormat(PrinterUtil.formatLineItem(itemName1, itemValue1));
            string itemName2 = "Email:";
            string itemValue2 = "info@centenarybank.co.ug";
            sb.AppendFormat(PrinterUtil.formatLineItem(itemName2, itemValue2, 6, 24));
            string itemName3 = "Web:";
            string itemValue3 = "www.centenarybank.co.ug";
            sb.AppendFormat(PrinterUtil.formatLineItem(itemName3, itemValue3, 6, 24));
            return sb;
        }

        private static StringBuilder getReceiptFooter(StringBuilder sb)
        {
            sb.AppendFormat(PrinterUtil.formartSingleLineString("--------------------------------"));
            sb.AppendFormat(PrinterUtil.formartSingleLineString("Thank you for coming & We look"));
            sb.AppendFormat(PrinterUtil.formartSingleLineString("forward to serving you again"));
            sb.AppendFormat(PrinterUtil.formartSingleLineString("--------------------------------"));
            sb.AppendFormat(PrinterUtil.formartSingleLineString("Technology by Micropay (U) Ltd"));
            return sb;
        }

        private static List<string> split(string myString, int chunkSize)
        {
            List<string> stringList = new List<string>();
            for (int startIndex = 0; startIndex < myString.Length; startIndex += chunkSize)
            {
                int length = Math.Min(chunkSize, myString.Length - startIndex);
                string str = myString.Substring(startIndex, length);
                stringList.Add(str);
            }
            return stringList;
        }

        private static string formartSingleLineString(string itemName)
        {
            string str1 = (string)null;
            int chunkSize = 32;
            string format = "{0}";
            if (itemName.Length > chunkSize)
            {
                IEnumerable<string> strings = (IEnumerable<string>)PrinterUtil.split(itemName, chunkSize);
                int num = 0;
                foreach (string str2 in strings)
                {
                    str1 = str1 + string.Format(format, (object)str2.PadRight(str2.Length)) + "\n";
                    ++num;
                }
            }
            else
                str1 = string.Format(format, (object)itemName.PadRight(itemName.Length)) + "\n";
            return str1;
        }

        private static string formatLineItem(
          string itemName,
          string itemValue,
          int itemNameLength = 15,
          int itemValueLength = 15)
        {
            itemValue = itemValue == null ? "" : itemValue;
            string str1 = (string)null;
            bool flag = false;
            string format = "{0}  {1}";
            if (itemValue.Length > itemValueLength)
            {
                IEnumerable<string> strings = (IEnumerable<string>)itemValue.Split(' ');
                foreach (string str2 in strings)
                {
                    if (str2.Length > itemValueLength)
                    {
                        flag = true;
                        break;
                    }
                }
                if (flag)
                    strings = (IEnumerable<string>)PrinterUtil.split(itemValue, itemValueLength);
                int num = 0;
                foreach (string input in strings)
                {
                    str1 = str1 + string.Format(format, (object)PrinterUtil.ensureLength(itemName = num == 0 ? itemName : "", itemNameLength, true), (object)PrinterUtil.ensureLength(input, itemValueLength, false)) + "\n";
                    ++num;
                }
            }
            else
                str1 = string.Format(format, (object)PrinterUtil.ensureLength(itemName, itemNameLength, true), (object)PrinterUtil.ensureLength(itemValue, itemValueLength, false)) + "\n";
            return str1;
        }

        private static string ensureLength(string input, int requiredLength, bool padRight)
        {
            if (input.Length > requiredLength)
                return input.Substring(0, requiredLength);
            if (input.Length == requiredLength)
                return input;
            return padRight ? input.PadRight(requiredLength) : input.PadLeft(requiredLength);
        }

         
        public static ABCReceiptData ParseReceipt(string printData)
        {
            if (string.IsNullOrEmpty(printData)) return new ABCReceiptData();

            var lines = printData.Split(new[] { '\n' }, StringSplitOptions.RemoveEmptyEntries).Select(line => line.Trim()).ToArray();
            var receipt = new ABCReceiptData
            {
                ReceiptNumber = getLineValue(lines, "Receipt Number"),
                Date = getLineValue(lines, "Date"),
                OutletName = getLineValue(lines, "Outlet Name"),
                OutletCode = getLineValue(lines, "Outlet Code"),
                OutletPhone = getLineValue(lines, "Outlet Phone"),
                Amount = getLineValue(lines, "Amount"),
                AccountNo = getLineValue(lines, "Account No"),
                CustName = getLineValue(lines, "Cust Name"),
                TransId = getLineValue(lines, "Trans ID"),
                TransCharge = getLineValue(lines, "Trans Charge"),
            };
            string custName = receipt.CustName ?? receipt.CustName.Replace("\n", "").Replace("\r", "").Trim();
            custName = custName.Replace("Name", "").Trim();
            receipt.CustName = custName;
            return receipt;
        }

        static string getLineValue(string[] lines, string key)
        {
            return lines.FirstOrDefault(line => line.StartsWith(key))?.Split(new[] { ' ' }, 2).LastOrDefault().Trim();
        }

    }
}