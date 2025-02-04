package com.micropay.utils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.util.Log;

import com.prowesspride.api.Printer_GEN;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


@SuppressLint("NewApi")
public class BluetoothComm {
    /**
     * Service UUID
     */
    public final static String UUID_STR = "00001101-0000-1000-8000-00805F9B34FB";
    /**
     * Bluetooth address code
     */
    private String msMAC;
    /**
     * Bluetooth connection status
     */
    private boolean mbConectOk = false;

    /* Get Default Adapter */
    private BluetoothAdapter mBT = BluetoothAdapter.getDefaultAdapter();
    /**
     * Bluetooth serial port connection object
     */
    private BluetoothSocket mbsSocket = null;
    /**
     * Input stream object
     */
    public static InputStream misIn = null;
    /**
     * Output stream object
     */
    public static OutputStream mosOut = null;
    /**
     * Constant: The current Adnroid SDK version number
     */
    private static final int SDK_VER;

    static {
        SDK_VER = Build.VERSION.SDK_INT;
    }

    ;

    /**
     * Constructor
     *
     * @param sMAC Bluetooth device MAC address required to connect
     */
    public BluetoothComm(String sMAC) {
        this.msMAC = sMAC;
    }

    /**
     * Disconnect the Bluetooth device connection
     *
     * @return void
     */
    public void closeConn() {
        if (this.mbConectOk) {
            try {
                if (null != this.misIn)
                    this.misIn.close();
                if (null != this.mosOut)
                    this.mosOut.close();
                if (null != this.mbsSocket)
                    this.mbsSocket.close();
                this.mbConectOk = false;//Mark the connection has been closed
            } catch (IOException e) {
                //Any part of the error, will be forced to close socket connection
                this.misIn = null;
                this.mosOut = null;
                this.mbsSocket = null;
                this.mbConectOk = false;//Mark the connection has been closed
            }
        }
        Log.e(TAG, " Closed connection");
    }

    private static final String TAG = "Prowess BT Comm";

    final public boolean openRFChannel() {
        if (!mBT.isEnabled())
            return false;
        //If a connection already exists, disconnect
        if (mbConectOk)
            this.closeConn();
        /*Start Connecting a Bluetooth device*/
        final BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(this.msMAC);
        final UUID uuidComm = UUID.fromString(UUID_STR);
        try {
            this.mbsSocket = device.createRfcommSocketToServiceRecord(uuidComm);
            Thread.sleep(2000);
            this.mbsSocket.connect();
            Thread.sleep(2000);
            this.mosOut = this.mbsSocket.getOutputStream();//Get global output stream object
            this.misIn = this.mbsSocket.getInputStream(); //Get global streaming input object
            this.mbConectOk = true; //Device is connected successfullyLog.e(TAG, ">>>>>>           Try 1 Over  ................!");
        } catch (Exception e) {
            try {
                Thread.sleep(2000);
                //this.mbsSocket = device.createRfcommSocketToServiceRecord(uuidComm);
                this.mbsSocket = device.createInsecureRfcommSocketToServiceRecord(uuidComm);
                Thread.sleep(2000);
                this.mbsSocket.connect();
                Thread.sleep(2000);
                this.mosOut = this.mbsSocket.getOutputStream();//Get global output stream object
                this.misIn = this.mbsSocket.getInputStream(); //Get global streaming input object
                this.mbConectOk = true;
            } catch (IOException e1) {
                e1.printStackTrace();
                this.closeConn();//Disconnect
                return false;
            } catch (Exception ee) {
                ee.printStackTrace();
                this.closeConn();//Disconnect
                return false;
            }
            return true;
        }
        return true;
    }

    /**
     * If the communication device has been established
     *
     * @return Boolean true: communication has been established / false: communication lost
     */
    public boolean isConnect() {
        return this.mbConectOk;
    }


    public static final int DEVICE_NOTCONNECTED = -100;


    public static String getPrintCodeDesc(int iRetVal) {

        if (iRetVal == DEVICE_NOTCONNECTED) {
            return "Printer not connected";
        } else if (iRetVal == Printer_GEN.SUCCESS) {
            return "Printing Successful";
        } else if (iRetVal == Printer_GEN.PLATEN_OPEN) {
            return "Platen open";
        } else if (iRetVal == Printer_GEN.PAPER_OUT) {
            return "Paper out";
        } else if (iRetVal == Printer_GEN.IMPROPER_VOLTAGE) {
            return "Printer at improper voltage";
        } else if (iRetVal == Printer_GEN.FAILURE) {
            return "Print failed";
        } else if (iRetVal == Printer_GEN.PARAM_ERROR) {
            return "Parameter error";
        } else if (iRetVal == Printer_GEN.NO_RESPONSE) {
            return "No response from Pride device";
        } else if (iRetVal == Printer_GEN.DEMO_VERSION) {
            return "Library in demo version";
        } else if (iRetVal == Printer_GEN.INVALID_DEVICE_ID) {
            return "Connected  device is not authenticated.";
        } else if (iRetVal == Printer_GEN.NOT_ACTIVATED) {
            return "Library not activated";
        } else if (iRetVal == Printer_GEN.NOT_SUPPORTED) {
            return "Not Supported";
        } else {
            return "Unknown Response from Device";
        }
    }

    public static final int ALIGN_CENTER = 1;
    public static final int ALIGN_LEFT = 0;
    public static final int ALIGHT_RIGHT = 2;


    public static final int FONT_LARGE_BOLD_WIDTH = 24;
    public static final int FONT_LARGE_NORMAL_WIDTH = 24;
    public static final int FONT_SMALL_NORMAL_WIDTH = 42;
    public static final int FONT_SMALL_BOLD_WIDTH = 42;

    public static String formatString(String rawString, float paperWidth, int requiredPosition) {
        String formattedText = trimToSize(rawString);
        switch (requiredPosition) {
            case 0:
                // left position
                break;
            case 1:
                // middle position
                float midText = formattedText.length() / 2;
                float midPaper = paperWidth / 2;
                float midPaperLength = midPaper - midText;
                formattedText = new String(new char[(int) midPaperLength]).replace("\0", " ") + formattedText;
                break;
            case 2:
                // right position
                float chars = (paperWidth - formattedText.length());
                formattedText = new String(new char[(int) chars]).replace("\0", " ") + formattedText;
                break;
        }
        return formattedText;
    }

    public static String leftRightAlign(String str1, String str2, int paperLength) {
        StringBuilder stringBuilder = new StringBuilder(trimToSize(str1));
        int strLen = (trimToSize(str1) + trimToSize(str2)).length();
        if (strLen < paperLength) {
            int diff = paperLength - trimToSize(str2).length();
            stringBuilder.append(createSpaces(diff - trimToSize(str1).length()))
                    .append(trimToSize(str2));
        }
        return stringBuilder.toString();
    }

    public static String leftAlign(String str1, int paperLength) {
        StringBuilder stringBuilder = new StringBuilder(trimToSize(str1));
        int strLen = trimToSize(str1).length();
        if (strLen < paperLength) {
            stringBuilder.append(createSpaces(paperLength - strLen));
        }
        return stringBuilder.toString();
    }

    public static String printNewLine(int paperLength) {
        return createSpaces(paperLength);
    }

    private static String trimToSize(String rawString) {
        return rawString != null ? rawString.trim() : "";
    }

    private static String createSpaces(int length) {
        return new String(new char[length]).replace("\0", " ");
    }

    public static String createLine(int length) {
        return new String(new char[length]).replace("\0", "-");
    }


}
