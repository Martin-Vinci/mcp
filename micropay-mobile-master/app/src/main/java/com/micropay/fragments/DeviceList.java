package com.micropay.fragments;

/**
 * Created by micropay on 01/25/2021.
 */

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.micropay.api.CacheUtil;
import com.micropay.api.NetworkUtil;
import com.micropay.popups.DialogUtils;
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.MicropayMobile;
import com.micropay.micropay.R;
import com.micropay.utils.BluetoothComm;
import com.prowesspride.api.Printer_GEN;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

public class DeviceList  extends Fragment {
    protected static final String TAG = "TAG";
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.device_list, container, false);

            getActivity().setResult(Activity.RESULT_CANCELED);
            mPairedDevicesArrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.device_name);

            ListView mPairedListView = (ListView) rootView.findViewById(R.id.paired_devices);
            mPairedListView.setAdapter(mPairedDevicesArrayAdapter);
            mPairedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView<?> parent, View mView, int position, long id) {
                    mBluetoothAdapter.cancelDiscovery();
                    String mDeviceInfo = ((TextView) mView).getText().toString();
                    String mDeviceAddress = mDeviceInfo.substring(mDeviceInfo.length() - 17);
                    Log.v(TAG, "Device_Address " + mDeviceAddress);

                    Bundle mBundle = new Bundle();
                    mBundle.putString("DeviceAddress", mDeviceAddress);
                    Intent mBackIntent = new Intent();
                    mBackIntent.putExtras(mBundle);
                    getActivity().setResult(Activity.RESULT_OK, mBackIntent);
                    getActivity().finish();
                }
            });

            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            Set<BluetoothDevice> mPairedDevices = mBluetoothAdapter.getBondedDevices();

            if (mPairedDevices.size() > 0) {
                rootView.findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
                for (BluetoothDevice mDevice : mPairedDevices) {
                    mPairedDevicesArrayAdapter.add(mDevice.getName() + "\n" + mDevice.getAddress());
                }
            } else {
                String mNoDevices = "None Paired";//getResources().getText(R.string.none_paired).toString();
                mPairedDevicesArrayAdapter.add(mNoDevices);
            }
            return rootView;
        }


        //    @Override
//    protected void onDestroy()
//    {
//        super.onDestroy();
//        if (mBluetoothAdapter != null)
//        {
//            mBluetoothAdapter.cancelDiscovery();
//        }
//    }

}