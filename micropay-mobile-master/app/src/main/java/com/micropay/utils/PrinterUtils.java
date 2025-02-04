package com.micropay.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import androidx.appcompat.app.AlertDialog;

import com.micropay.api.CacheUtil;
import com.micropay.micropay.Utils;
import com.micropay.models.Receipt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PrinterUtils extends AsyncTask<String, Void, Integer> {
    private Context context;
    private List<Receipt> transaction;
    private static final int LINE_BYTE_SIZE = 32;

    private BluetoothComm bluetoothComm;
    private OutputStream outputStream;
    BluetoothAdapter mBluetoothAdapter;
    private UUID applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket mBluetoothSocket;
    BluetoothDevice mBluetoothDevice;
    CacheUtil cacheUtil;

    public PrinterUtils(Context context, List<Receipt> transaction, CacheUtil cacheUtil) {
        this.context = context;
        this.transaction = transaction;
        this.cacheUtil = cacheUtil;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //alertDialog = DialogUtils.showProgressDialog(context, "Printing...", false);
    }

    private boolean connect() {
        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(applicationUUID);
            mBluetoothAdapter.cancelDiscovery();
            if (!mBluetoothSocket.isConnected())
                mBluetoothSocket.connect();
            return true;
        } catch (IOException eConnectException) {
            eConnectException.printStackTrace();
            try {
                mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(applicationUUID);
                mBluetoothAdapter.cancelDiscovery();
                mBluetoothSocket.connect();
                return true;
            } catch (Exception e2) {
                e2.printStackTrace();
                return false;
            }
        }
    }


    @Override
    protected Integer doInBackground(String... voids) {
        int response = 0;
        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {
                if (device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.IMAGING) {
                    mBluetoothDevice = device;
                    if (connect())
                        break;
                }
            }
            //mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(cacheUtil.getString("bluetooth_address"));
            try {
                response = printData();
            } catch (Exception e) {
                e.printStackTrace();
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

    public void addBankLogo() {
        try {
            InputStream is = context.getAssets().open("white_logo.png");
            Bitmap bmp = BitmapFactory.decodeStream(is);
            if (bmp != null) {
                byte[] command = Utils.decodeBitmap(bmp);
                outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                outputStream.write(command);
                outputStream.write(PrinterCommands.FEED_LINE);
                bmp.recycle();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int printData() {
        int align = 1;
        try {
            outputStream = mBluetoothSocket.getOutputStream();
            printNewLine();
            printNewLine();
            printCustom("Micropay (U) Limited", 1, 1);
            addBankLogo();
            printCustom("--------------------------------", 0, 1);
            printNewLine();
            for (Receipt item : transaction) {
                if (item.getValue() == null)
                    align = 0;
                int length = (item.getKey() + item.getValue()).length();
                if (length > 28 && item.getValue() != null) {
                    String value = item.getValue();
                    int excessLength = length - 28;
                    int valueLength = item.getValue().length() - excessLength;
                    String value1 = value.substring(0, valueLength);
                    String value2 = value.substring(item.getValue().length() - excessLength);
                    printCustom(leftRightAlign(item.getKey(), value1.trim()), 0, align);
                    printNewLine();
                    printCustom(leftRightAlign(null, value2.trim()), 0, align);
                } else
                    printCustom(leftRightAlign(item.getKey(), item.getValue()), 0, align);
                printNewLine();
            }

            printCustom("--------------------------------", 0, 1);
            printCustom("Thank you for coming & We look", 0, 0);
            printCustom("forward to serving you again", 0, 0);
            printCustom("--------------------------------", 0, 1);
            printCustom("Technology by Micropay (U) Ltd", 0, 0);
            printEndLine();
            printEndLine();
            printEndLine();
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    @Override
    protected void onPostExecute(Integer iRetVal) {
//        if (alertDialog != null && alertDialog.isShowing())
//            alertDialog.dismiss();
//        ((FragmentActivity) context).finish();
        super.onPostExecute(iRetVal);
    }

    public void closeRFChannel() {
        if (null != this.bluetoothComm) {
            this.bluetoothComm.closeConn();
            this.bluetoothComm = null;
        }

    }

    private static int getBytesLength(String msg) {
        return msg.getBytes(Charset.forName("GB2312")).length;
    }

    private String leftRightAlign(String leftText, String rightText) {
        if (rightText == null)
            rightText = "";
        if (leftText == null)
            leftText = "";
        StringBuilder sb = new StringBuilder();
        int leftTextLength = getBytesLength(leftText);
        int rightTextLength = getBytesLength(rightText);
        sb.append(leftText);

        // Calculate the space between the text on both sides
        int marginBetweenMiddleAndRight = LINE_BYTE_SIZE - leftTextLength - rightTextLength;

        for (int i = 0; i < marginBetweenMiddleAndRight; i++) {
            sb.append(" ");
        }
        sb.append(rightText);
        return sb.toString();
    }

    private void printNewLine() {
        try {
            outputStream.write(PrinterCommands.SET_LINE_SPACING_24);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printEndLine() {
        try {
            outputStream.write(PrinterCommands.FEED_LINE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printText(String text) {
        try {
            byte[] data = text.getBytes("gbk");
            outputStream.write(data, 0, data.length);
            outputStream.flush();
        } catch (IOException e) {
            //Toast.makeText(this.context, "Failed to send!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    private void printCustom(String msg, int size, int align) {
        //Print config "mode"
        byte[] cc = new byte[]{0x1B, 0x21, 0x03};  // 0- normal size text
        byte[] bb = new byte[]{0x1B, 0x21, 0x08};  // 1- only bold text
        byte[] bb2 = new byte[]{0x1B, 0x21, 0x20}; // 2- bold with medium text
        byte[] bb3 = new byte[]{0x1B, 0x21, 0x10}; // 3- bold with large text
        try {
            switch (size) {
                case 0:
                    outputStream.write(cc);
                    break;
                case 1:
                    outputStream.write(bb);
                    break;
                case 2:
                    outputStream.write(bb2);
                    break;
                case 3:
                    outputStream.write(bb3);
                    break;
            }

            switch (align) {
                case 0:
                    //left align
                    outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
                    break;
                case 1:
                    //center align
                    outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                    break;
                case 2:
                    //right align
                    outputStream.write(PrinterCommands.ESC_ALIGN_RIGHT);
                    break;
            }
            outputStream.write(msg.getBytes());
            outputStream.write(PrinterCommands.LF);
            //outputStream.write(cc);
            //printNewLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
