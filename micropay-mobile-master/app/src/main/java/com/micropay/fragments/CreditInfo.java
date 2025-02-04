package com.micropay.fragments;

/**
 * Created by micropay on 01/25/2021.
 */

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.micropay.api.CacheUtil;
import com.micropay.api.NetworkUtil;
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class CreditInfo extends Fragment {

    private final String TAG = CreditInfo.class.getSimpleName();
    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;


    private TextView account_no, appl_id, repayment_acct, loan_term,
            appl_date, loan_amount, loan_status, cust_nm, maturity_dt;

    private JSONObject loanInfo;
    private NumberFormat formatter = NumberFormat.getInstance(Locale.ENGLISH);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        View rootView = inflater.inflate(R.layout.floan_details, container, false);
        cacheUtil = new CacheUtil(getActivity().getApplicationContext());

        FragmentHandler loanActivity = (FragmentHandler) getActivity();
        loanActivity.setTitle("Loan Details");

        account_no = rootView.findViewById(R.id.account_no);
        loan_term = rootView.findViewById(R.id.loan_term);
        appl_id = rootView.findViewById(R.id.appl_id);
        repayment_acct = rootView.findViewById(R.id.repayment_acct);
        appl_date = rootView.findViewById(R.id.appl_date);
        loan_amount = rootView.findViewById(R.id.loan_amount);
        loan_status = rootView.findViewById(R.id.loan_status);
        cust_nm = rootView.findViewById(R.id.cust_nm);
        maturity_dt = rootView.findViewById(R.id.maturity_dt);

        try {
            Bundle arguments = getArguments();
            loanInfo = new JSONObject(arguments.getString("LOAN_INFO"));
            renderLoanDetails(loanInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        rootView.findViewById(R.id.repay_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRepaymentOption(loanInfo.optString("REPAY_ACCT"),
                        loanInfo.optString("CUST_NO"),
                        loanInfo.optString("PRIME_LIMIT_AMT"));
            }
        });

        setRetainInstance(true);
        return rootView;
    }

    private void showRepaymentOption(final String repay_acct, final String cust_no, final String amount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTheme);
        builder.setTitle("Continue to repayment window?");
        builder.setMessage("Would you like to proceed to deposit a repayment for this loan")
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                                processNextScreen(repay_acct, cust_no, amount);
                            }
                        }).setNegativeButton("CANCEL",
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

    private void processNextScreen(String repay_acct, String cust_no, String amount) {
        try {
            Bundle info = new Bundle();
            info.putString("REPAY_ACCT", repay_acct);
            info.putString("CUST_NO", cust_no);
            info.putString("REPAY_AMT", amount);
            Fragment fragment = new LoanRepayment();
            fragment.setArguments(info);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .replace(R.id.container, fragment)
                    .commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderLoanDetails(JSONObject loanInfo) {
        String term_code = mapToPeriod(loanInfo.optString("TERM_CD"));
        account_no.setText(loanInfo.optString("ACCT_NO"));
        appl_id.setText(loanInfo.optString("APPL_ID"));
        repayment_acct.setText(loanInfo.optString("REPAY_ACCT"));
        loan_term.setText(loanInfo.optString("TERM_VALUE") + " " + term_code);
        appl_date.setText(loanInfo.optString("START_DT"));
        loan_amount.setText(formatter.format(getAbs(loanInfo.optString("PRIME_LIMIT_AMT"))) + " " +
                loanInfo.optString("CRNCY_CD"));
        cust_nm.setText(loanInfo.optString("CUST_NM"));
        maturity_dt.setText(loanInfo.optString("MATURITY_DT"));
        loan_status.setText(loanInfo.optString("REF_DESC"));
    }


    private String mapToPeriod(String term_cd) {
        String term_code;
        switch (term_cd) {
            case "D":
                term_code = "Day(s)";
                break;
            case "W":
                term_code = "Week(s)";
                break;
            case "M":
                term_code = "Month(s)";
                break;
            default:
                term_code = "Quarterly(s)";
                break;
        }
        return term_code;
    }

    private BigDecimal getAbs(String defaultValue) {
        try {
            if (defaultValue != null || "null".equalsIgnoreCase(defaultValue)) {
                return new BigDecimal(defaultValue).abs();
            }
        } catch (NumberFormatException e) {
        }
        return BigDecimal.ZERO;
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
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        alertDialog = builder.create();
        if (alertDialog != null && !alertDialog.isShowing()) {
            alertDialog.show();
        }
    }

    @Override
    public void onDestroy() {
        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        NetworkUtil.getInstance(getActivity().getApplicationContext()).cancel(TAG);
    }

}
