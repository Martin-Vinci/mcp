package com.micropay.micropay;

/**
 * Created by micropay on 01/25/2021.
 */

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.micropay.api.CacheUtil;
import com.micropay.models.Receipt;
import com.micropay.utils.DATA_CONVERTER;
import com.micropay.utils.PrinterUtils;
import com.micropay.utils.UnicodeFormatter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class AppSettings extends AppCompatActivity {
    protected static final String TAG = "TAG";
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    TextView mPrint, mClose;
    private CheckBox enable_printing;
    private CacheUtil cacheUtil;
    BluetoothAdapter mBluetoothAdapter;
    private UUID applicationUUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ProgressDialog mBluetoothConnectProgressDialog;
    private BluetoothSocket mBluetoothSocket;
    BluetoothDevice mBluetoothDevice;

    @Override
    public void onCreate(Bundle mSavedInstanceState) {
        super.onCreate(mSavedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.fapp_settings);
        getSupportActionBar().setTitle("Settings");
        cacheUtil = new CacheUtil(getApplicationContext());
        //mScan = (TextView) findViewById(R.id.addPrinter);
        mPrint = (TextView) findViewById(R.id.mPrint);
        enable_printing = (CheckBox) findViewById(R.id.enable_printing);
        mClose = (TextView) findViewById(R.id.mClose);

        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.BLUETOOTH_CONNECT
                    },
                    1);
        }




        if ("CUSTOMER".equals(cacheUtil.getString("entityType"))) {
            //mScan.setVisibility(View.GONE);
            mPrint.setVisibility(View.GONE);
            enable_printing.setVisibility(View.GONE);
        }

        enable_printing.setChecked(cacheUtil.getBoolean("enable_printing", false));
        enable_printing.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                cacheUtil.putBoolean("enable_printing", checked);
            }
        });


        mPrint.setOnClickListener(mView -> {
            Thread t = new Thread() {
                public void run() {
                    try {

                        List<Receipt> RECEIPT = new ArrayList<>();
                        String transDate = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss").format(new Date());

                        Receipt data = new Receipt("Test Printing", null);
                        RECEIPT.add(data);
                        data = new Receipt("Receipt No:", DATA_CONVERTER.getReceiptNo());
                        RECEIPT.add(data);
                        data = new Receipt("Print Date:", transDate);
                        RECEIPT.add(data);

                        PrinterUtils printerUtils = new PrinterUtils(getApplication(), RECEIPT, cacheUtil);
                        AsyncTask<String, Void, Integer> execute = printerUtils.execute();
                        try {
                            execute.get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        Log.e("Main", "Exe ", e);
                    }
                }
            };
            t.start();
        });

        mClose.setOnClickListener(new View.OnClickListener() {
            public void onClick(View mView) {
                if ("OUTLET".equals(cacheUtil.getString("entityType"))) {
                    Intent intent = new Intent(AppSettings.this, AgentHomeActivity.class);
                    startActivity(intent);
                }
                if ("CUSTOMER".equals(cacheUtil.getString("entityType"))) {
                    Intent intent = new Intent(AppSettings.this, CustomerHomeActivity.class);
                    startActivity(intent);
                }
                if ("SUPER_AGENT".equals(cacheUtil.getString("entityType"))) {
                    Intent intent = new Intent(AppSettings.this, SuperAgentHomeActivity.class);
                    startActivity(intent);
                }
            }
        });


//        mDisc = (Button) findViewById(R.id.dis);
//        mDisc.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View mView) {
//                if (mBluetoothAdapter != null)
//                    mBluetoothAdapter.disable();
//            }
//        });

    }// onCreate

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        try {
            if (mBluetoothSocket != null)
                mBluetoothSocket.close();
        } catch (Exception e) {
            Log.e("Tag", "Exe ", e);
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (mBluetoothSocket != null)
                mBluetoothSocket.close();
        } catch (Exception e) {
            Log.e("Tag", "Exe ", e);
        }
        setResult(RESULT_CANCELED);
        finish();
    }


    private void closeSocket(BluetoothSocket nOpenSocket) {
        try {
            nOpenSocket.close();
            Log.d(TAG, "SocketClosed");
        } catch (IOException ex) {
            Log.d(TAG, "CouldNotCloseSocket");
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mBluetoothConnectProgressDialog.dismiss();
            if (msg.arg1 == 0)
                Toast.makeText(AppSettings.this, "Printer connected", Toast.LENGTH_LONG).show();
            if (msg.arg1 == 1)
                Toast.makeText(AppSettings.this, "Printer connection failed", Toast.LENGTH_LONG).show();
        }
    };

    public static byte intToByteArray(int value) {
        byte[] b = ByteBuffer.allocate(4).putInt(value).array();

        for (int k = 0; k < b.length; k++) {
            System.out.println("Selva  [" + k + "] = " + "0x"
                    + UnicodeFormatter.byteToHex(b[k]));
        }

        return b[3];
    }

    public byte[] sel(int val) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putInt(val);
        buffer.flip();
        return buffer.array();
    }

}