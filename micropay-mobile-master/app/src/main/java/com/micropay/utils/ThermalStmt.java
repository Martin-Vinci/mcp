package com.micropay.utils;

import android.content.Context;
import android.os.AsyncTask;
import androidx.appcompat.app.AlertDialog;

import com.micropay.popups.DialogUtils;
import com.micropay.micropay.MicropayMobile;
import com.micropay.micropay.R;
import com.micropay.models.Transaction;
import com.prowesspride.api.Printer_GEN;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;

public class ThermalStmt extends AsyncTask<String, Void, Integer> {

    private Printer_GEN prnGen;
    private AlertDialog alertDialog;
    private Context context;
    private BluetoothComm bluetoothComm;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Transaction transaction;

    public ThermalStmt(Context context, Transaction transaction) {
        this.context = context;
        this.transaction = transaction;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        alertDialog = DialogUtils.showProgressDialog(context, "Printing...", false);
    }

    @Override
    protected Integer doInBackground(String... voids) {
        int response = 0;
        try {
            if (!MicropayMobile.isThermalActivated) {
                return BluetoothComm.DEVICE_NOTCONNECTED;
            }
            if (null == bluetoothComm) {
                bluetoothComm = new BluetoothComm(voids[0]);
                if (bluetoothComm.openRFChannel()) {
                    inputStream = BluetoothComm.misIn;
                    outputStream = BluetoothComm.mosOut;
                    prnGen = new Printer_GEN(MicropayMobile.setup, outputStream, inputStream);
                    response = printData();
                }
            } else {
                inputStream = BluetoothComm.misIn;
                outputStream = BluetoothComm.mosOut;
                prnGen = new Printer_GEN(MicropayMobile.setup, outputStream, inputStream);
                response = printData();
            }
        } catch (Exception e) {
            e.printStackTrace();
            response = BluetoothComm.DEVICE_NOTCONNECTED;
        }
        return response;
    }

    @Override
    protected void onCancelled() {
        closeRFChannel();
        super.onCancelled();
    }

    private int printData() {
        prnGen.iFlushBuf();
        try {

            prnGen.iGrayscalePrint(context, R.drawable.bank_logo);
            prnGen.iAddData(Printer_GEN.FONT_LARGE_BOLD,
                    BluetoothComm.formatString("MICROPAY (U) LTD", BluetoothComm.FONT_LARGE_BOLD_WIDTH,
                            BluetoothComm.ALIGN_CENTER));

            prnGen.iAddData(Printer_GEN.FONT_LARGE_BOLD, BluetoothComm.formatString(transaction.getTranType(),
                    BluetoothComm.FONT_LARGE_BOLD_WIDTH,
                    BluetoothComm.ALIGN_CENTER));


            if (transaction.getCustomer() != null)
                prnGen.iAddData(Printer_GEN.FONT_LARGE_NORMAL, BluetoothComm.formatString(transaction.getCustomer(),
                        BluetoothComm.FONT_LARGE_NORMAL_WIDTH,
                        BluetoothComm.ALIGN_CENTER));

            if (transaction.getAccount() != null)
                prnGen.iAddData(Printer_GEN.FONT_LARGE_NORMAL, BluetoothComm.formatString(transaction.getAccount(),
                        BluetoothComm.FONT_LARGE_NORMAL_WIDTH,
                        BluetoothComm.ALIGN_CENTER));

            if (transaction.getTranStatus() != null)
                prnGen.iAddData(Printer_GEN.FONT_LARGE_NORMAL, BluetoothComm.formatString(transaction.getTranStatus(),
                        BluetoothComm.FONT_LARGE_NORMAL_WIDTH,
                        BluetoothComm.ALIGN_CENTER));

            prnGen.iAddData(Printer_GEN.FONT_SMALL_NORMAL, BluetoothComm.printNewLine(BluetoothComm.FONT_SMALL_NORMAL_WIDTH));
            prnGen.iAddData(Printer_GEN.FONT_SMALL_NORMAL, BluetoothComm.createLine(BluetoothComm.FONT_SMALL_NORMAL_WIDTH));
            prnGen.iAddData(Printer_GEN.FONT_SMALL_NORMAL, BluetoothComm.printNewLine(BluetoothComm.FONT_SMALL_NORMAL_WIDTH));
            prnGen.iAddData(Printer_GEN.FONT_SMALL_NORMAL, BluetoothComm.printNewLine(BluetoothComm.FONT_SMALL_NORMAL_WIDTH));


            JSONObject jsonObject = null;
            for (int i = 0; i < 5; i++) {
                jsonObject = transaction.getJsonArray().optJSONObject(i);

                prnGen.iAddData(Printer_GEN.FONT_SMALL_NORMAL, BluetoothComm.leftRightAlign("RRN",
                        jsonObject.optString("TRAN_JOURNAL_ID"),
                        BluetoothComm.FONT_SMALL_NORMAL_WIDTH));

                prnGen.iAddData(Printer_GEN.FONT_SMALL_NORMAL, BluetoothComm.leftRightAlign("Date",
                        jsonObject.optString("TRAN_DT"),
                        BluetoothComm.FONT_SMALL_NORMAL_WIDTH));

                prnGen.iAddData(Printer_GEN.FONT_SMALL_NORMAL, BluetoothComm.leftRightAlign("Amount",
                        NumberUtils.formatNumber(jsonObject.optLong("AMOUNT")),
                        BluetoothComm.FONT_SMALL_NORMAL_WIDTH));

                String desc = jsonObject.optString("TRAN_DESC");
                if (desc != null && desc.length() > 32) {
                    prnGen.iAddData(Printer_GEN.FONT_SMALL_NORMAL, BluetoothComm.leftAlign(
                            desc,
                            BluetoothComm.FONT_SMALL_NORMAL_WIDTH));
                } else
                    prnGen.iAddData(Printer_GEN.FONT_SMALL_NORMAL, BluetoothComm.leftAlign(
                            desc.substring(0, 20),
                            BluetoothComm.FONT_SMALL_NORMAL_WIDTH));
            }
            if (jsonObject != null) {
                prnGen.iAddData(Printer_GEN.FONT_SMALL_NORMAL, BluetoothComm.leftRightAlign("Closing Bal",
                        NumberUtils.formatNumber(jsonObject.optLong("LEDGER_BAL")),
                        BluetoothComm.FONT_SMALL_NORMAL_WIDTH));
            }

            if (transaction.getAgent() != null)
                prnGen.iAddData(Printer_GEN.FONT_SMALL_NORMAL, BluetoothComm.leftRightAlign("Merchant", transaction.getAgent(),
                        BluetoothComm.FONT_SMALL_NORMAL_WIDTH));

            if (transaction.getOutlet() != null)
                prnGen.iAddData(Printer_GEN.FONT_SMALL_NORMAL, BluetoothComm.leftRightAlign("Outlet", transaction.getOutlet(),
                        BluetoothComm.FONT_SMALL_NORMAL_WIDTH));

            prnGen.iAddData(Printer_GEN.FONT_SMALL_NORMAL, BluetoothComm.printNewLine(BluetoothComm.FONT_SMALL_NORMAL_WIDTH));
            prnGen.iAddData(Printer_GEN.FONT_SMALL_NORMAL, BluetoothComm.printNewLine(BluetoothComm.FONT_SMALL_NORMAL_WIDTH));

            prnGen.iAddData(Printer_GEN.FONT_SMALL_NORMAL, BluetoothComm.formatString("Your Success, Our Success",
                    BluetoothComm.FONT_SMALL_NORMAL_WIDTH,
                    BluetoothComm.ALIGN_CENTER));

            prnGen.iAddData(Printer_GEN.FONT_SMALL_NORMAL, BluetoothComm.formatString("Thank you for banking with us!",
                    BluetoothComm.FONT_SMALL_NORMAL_WIDTH,
                    BluetoothComm.ALIGN_CENTER));

            prnGen.iAddData(Printer_GEN.FONT_SMALL_NORMAL, BluetoothComm.printNewLine(BluetoothComm.FONT_SMALL_NORMAL_WIDTH));
            prnGen.iAddData(Printer_GEN.FONT_SMALL_NORMAL, BluetoothComm.printNewLine(BluetoothComm.FONT_SMALL_NORMAL_WIDTH));
            prnGen.iAddData(Printer_GEN.FONT_SMALL_NORMAL, BluetoothComm.createLine(BluetoothComm.FONT_SMALL_NORMAL_WIDTH));

            prnGen.iAddData(Printer_GEN.FONT_SMALL_NORMAL, BluetoothComm.printNewLine(BluetoothComm.FONT_SMALL_NORMAL_WIDTH));
            prnGen.iAddData(Printer_GEN.FONT_SMALL_NORMAL, BluetoothComm.printNewLine(BluetoothComm.FONT_SMALL_NORMAL_WIDTH));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return prnGen.iStartPrinting(1);
    }

    @Override
    protected void onPostExecute(Integer iRetVal) {
        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();
        closeRFChannel();
        if (iRetVal != Printer_GEN.SUCCESS) {
            DialogUtils.showAlertDialog(context, null, BluetoothComm.getPrintCodeDesc(iRetVal), false);
        }
        super.onPostExecute(iRetVal);
    }

    public void closeRFChannel() {
        if (null != this.bluetoothComm) {
            this.bluetoothComm.closeConn();
            this.bluetoothComm = null;
        }
    }
}
