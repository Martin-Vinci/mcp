package com.micropay.fragments;

/**
 * Created by micropay on 01/25/2021.
 */

import android.content.DialogInterface;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputEditText;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.micropay.api.CacheUtil;
import com.micropay.api.NetworkUtil;
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.R;

public class CustomerSearch extends Fragment {

    private final String TAG = CustomerSearch.class.getSimpleName();
    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;
    private TextInputEditText id_no, cust_nm, account_no;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        View rootView = inflater.inflate(R.layout.fcust_search, container, false);
        cacheUtil = new CacheUtil(getActivity().getApplicationContext());

        FragmentHandler loanActivity = (FragmentHandler) getActivity();
        loanActivity.setTitle("Search Customer");

        id_no = rootView.findViewById(R.id.id_no);
        cust_nm = rootView.findViewById(R.id.cust_nm);
        account_no = rootView.findViewById(R.id.account_no);

        rootView.findViewById(R.id.search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateEntries();
            }
        });

        setRetainInstance(true);
        return rootView;
    }

    private void validateEntries() {
        NetworkUtil.hideSoftKeyboard(getActivity());
        if (TextUtils.isEmpty(id_no.getText()) && TextUtils.isEmpty(cust_nm.getText())
                && TextUtils.isEmpty(account_no.getText())) {
            showAlertDialog("", "You need to specify at least one search criteria");
            cust_nm.requestFocus();
            return;
        }
        if (!NetworkUtil.isNetworkAvailable(getActivity().getApplicationContext())) {
            showAlertDialog("Connection Unavailable", "Kindly check your network connection and try again");
            return;
        }
        cacheUtil.putString("customer_nm", cust_nm.getText().toString());
        cacheUtil.putString("account_no", account_no.getText().toString());
        cacheUtil.putString("id_no", id_no.getText().toString());
        continueToSearchResultsPage();
    }

    public void showAlertDialog(String title, String body) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTheme);
        if (title != null)
            builder.setTitle(title);
        builder.setMessage(body)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                            }
                        }).setCancelable(false);
        alertDialog = builder.create();
        if (alertDialog != null && !alertDialog.isShowing()) {
            alertDialog.show();
        }
    }

    private void continueToSearchResultsPage() {
        getActivity().getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.container, new CustomerSearchResults()).addToBackStack(null)
                .commit();
    }

    @Override
    public void onStop() {
        super.onStop();
        NetworkUtil.getInstance(getActivity().getApplicationContext()).cancel(TAG);
    }

}
