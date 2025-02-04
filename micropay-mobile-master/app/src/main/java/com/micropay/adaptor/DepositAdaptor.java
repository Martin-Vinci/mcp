package com.micropay.adaptor;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.micropay.api.CacheUtil;
import com.micropay.api.Constants;
import com.micropay.api.NetworkUtil;
import com.micropay.popups.DialogUtils;
import com.micropay.micropay.R;
import com.micropay.fragments.transactions.TXNCashDeposit;
import com.micropay.fragments.transactions.TXNCashTransfer;
import com.micropay.fragments.transactions.TXNCashWithdraw;
import com.micropay.fragments.RequestChequeBook;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by developer on 9/20/18.
 */

public class DepositAdaptor extends RecyclerView.Adapter<DepositAdaptor.ViewHolder> {

    private JSONArray accountList;
    private Activity mCtx;
    private String specifiedKey;
    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;

    public DepositAdaptor(JSONArray list, Activity mCtx, String specifiedKey) {
        this.accountList = list;
        this.mCtx = mCtx;
        this.specifiedKey = specifiedKey;
        cacheUtil = new CacheUtil(mCtx);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.acct_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final JSONObject jsonObject = accountList.optJSONObject(position);
        holder.product.setText(jsonObject.optString("PROD_NAME"));
        holder.acct_title.setText(jsonObject.optString("ACCT_NM"));
        holder.branch.setText(jsonObject.optString("BRANCH_NAME"));
        holder.acct_no.setText(jsonObject.optString("ACCT_NO"));
        holder.card_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = null;
                Bundle bundle = new Bundle();
                switch (specifiedKey) {
                    case "Enquiry":
                        accountNumber = jsonObject.optString("ACCT_NO");
                        customerNo = jsonObject.optString("CUST_NO");
                        showConfirmRequestDialog(accountNumber);
                        break;
                    //Cheque Request
                    case "Cheque":
                        fragment = new RequestChequeBook();
                        bundle.putString("ACCT_NM", jsonObject.optString("ACCT_NM"));
                        bundle.putString("ACCT_NO", jsonObject.optString("ACCT_NO"));
                        bundle.putLong("PROD_ID", jsonObject.optLong("PROD_ID"));
                        bundle.putLong("CRNCY_ID", jsonObject.optLong("CRNCY_ID"));
                        bundle.putLong("PRIMARY_CUST_ID", jsonObject.optLong("PRIMARY_CUST_ID"));
                        bundle.putString("CUST_NO", jsonObject.optString("CUST_NO"));
                        bundle.putBoolean(Constants.KEY, false);
                        break;
                    case "Deposit":
                        fragment = new TXNCashDeposit();
                        bundle.putString("CUST_NM", jsonObject.optString("ACCT_NM"));
                        bundle.putString("CUST_NO", jsonObject.optString("CUST_NO"));
                        bundle.putString("ACCT_NO", jsonObject.optString("ACCT_NO"));
                        bundle.putString("CRNCY_SYM", jsonObject.optString("CRNCY_SYM"));
                        break;
                    case "Withdraw":
                        fragment = new TXNCashWithdraw();
                        bundle.putString("CUST_NM", jsonObject.optString("ACCT_NM"));
                        bundle.putString("CUST_NO", jsonObject.optString("CUST_NO"));
                        bundle.putString("ACCT_NO", jsonObject.optString("ACCT_NO"));
                        bundle.putString("CRNCY_SYM", jsonObject.optString("CRNCY_SYM"));
                        break;
                    case "Transfer":
                        fragment = new TXNCashTransfer();
                        bundle.putString("CUST_NM", jsonObject.optString("ACCT_NM"));
                        bundle.putString("CUST_NO", jsonObject.optString("CUST_NO"));
                        bundle.putString("ACCT_NO", jsonObject.optString("ACCT_NO"));
                        bundle.putString("CRNCY_SYM", jsonObject.optString("CRNCY_SYM"));
                        break;
                    case "AgentTransfer":
                        fragment = new TXNCashTransfer();
                        bundle.putString("CUST_NM", jsonObject.optString("ACCT_NM"));
                        bundle.putString("CUST_NO", jsonObject.optString("CUST_NO"));
                        bundle.putString("ACCT_NO", jsonObject.optString("ACCT_NO"));
                        bundle.putString("CRNCY_SYM", jsonObject.optString("CRNCY_SYM"));
                        bundle.putString(Constants.KEY, specifiedKey);
                        break;
                }
                if (mCtx != null && !mCtx.isFinishing() && fragment != null) {
                    fragment.setArguments(bundle);
                    ((FragmentActivity) mCtx).getSupportFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.container, fragment).addToBackStack(null)
                            .commit();
                }
            }
        });
        holder.app_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NetworkUtil.copyToClip(mCtx, "Account Number Copied to Clip", jsonObject.optString("ACCT_NO"));
            }
        });
    }

    private void placeAccountBalanceRequest(String chargeAccount) {
        try {
            JSONObject outletCredentials = NetworkUtil.getBaseRequest(mCtx);
            if (cacheUtil.getBoolean("isTeller", false)) {
                outletCredentials
                        .put("agentNo", cacheUtil.getString("userLoginId"))
                        .put("agentType", cacheUtil.getString("agentType"));
            } else {
                outletCredentials.put("agentAcctNo", cacheUtil.getString("OUTLET_FLOAT_ACCT"))
                        .put("agentType", cacheUtil.getString("agentType"))
                        .put("agentNo", cacheUtil.getString("AGENT_NO"));
            }

            JSONObject requestObject = new JSONObject();
            requestObject.put("outletCredentials", outletCredentials)
                    .put("accountNumber1", accountNumber)
                    .put("chargeAccount", chargeAccount)
                    .put("customerNo", customerNo);

            if (NetworkUtil.isNetworkAvailable(mCtx.getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/depositAccountEnquiry", requestObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null) {
                                    if ("00".equalsIgnoreCase(response.optString("responseCode"))) {
                                        showAlertDialog(response.optString("responseTxt"), true);
                                    } else {
                                        showAlertDialog(response.optString("responseTxt"), false);
                                    }
                                } else
                                    showAlertDialog("An error occurred while processing your request.", false);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (alertDialog != null && alertDialog.isShowing())
                            alertDialog.cancel();
                        showAlertDialog(NetworkUtil.getErrorDesc(error), false);
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Authorization", "Basic " + Constants.getRawBasicData());
                        headers.put("sessionId", cacheUtil.getString("sessionId"));
                        return headers;
                    }
                };
                jsonObjectRequest.setTag(TAG);
                jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                        0,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                NetworkUtil.getInstance(mCtx.getApplicationContext()).addToRequestQueue(jsonObjectRequest);
                alertDialog = DialogUtils.showProgressDialog(mCtx, "Requesting...", false);
            } else {
                Toast.makeText(mCtx, "Cannot connect to server. No network available", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showAlertDialog(String body, final boolean exit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mCtx, R.style.dialogTheme);
        builder.setMessage(body)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                                if (exit)
                                    mCtx.finish();
                            }
                        }).setCancelable(false);

        if (alertDialog != null && !alertDialog.isShowing())
            alertDialog.dismiss();
        alertDialog = builder.create();
        alertDialog.show();
    }


    @Override
    public int getItemCount() {
        return accountList.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView acct_no, product, acct_title, branch;
        public ImageButton app_menu;
        public CardView card_view;

        public ViewHolder(View itemView) {
            super(itemView);
            product = itemView.findViewById(R.id.product);
            acct_no = itemView.findViewById(R.id.acct_no);
            acct_title = itemView.findViewById(R.id.acct_title);
            branch = itemView.findViewById(R.id.branch);
            app_menu = itemView.findViewById(R.id.app_menu);
            card_view = itemView.findViewById(R.id.card_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
        }
    }

    public void showConfirmRequestDialog(final String accountNumber) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mCtx, R.style.dialogTheme);
        builder.setMessage("An SMS with balance on account " + accountNumber
                + " will be sent to the customer's registered phone number.")
                .setPositiveButton("CONTINUE",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                                loadCustomerChargeAccounts(accountNumber);
                            }
                        }).setNegativeButton("CANCEL",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        arg0.dismiss();
                    }
                }).setCancelable(false);
        if (alertDialog != null && !alertDialog.isShowing())
            alertDialog.dismiss();
        alertDialog = builder.create();
        alertDialog.show();
    }

    private String accountNumber, customerNo;

    private void loadCustomerChargeAccounts(String accountNumber) {
        try {
            if (NetworkUtil.isNetworkAvailable(mCtx.getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/getCustomerChargeAccounts", new JSONObject()
                        .put("outletCredentials", NetworkUtil.getBaseRequest(mCtx))
                        .put("activity", TAG).put("accountNo", accountNumber),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null) {
                                    if ("00".equalsIgnoreCase(response.optString("responseCode"))) {
                                        updateChargeAccountList(response);
                                    } else {
                                        showBasicAlertDialog(response.optString("responseTxt"));
                                    }
                                } else
                                    showBasicAlertDialog(mCtx.getString(R.string.resp_timeout));
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (alertDialog != null && alertDialog.isShowing())
                            alertDialog.cancel();
                        showBasicAlertDialog(NetworkUtil.getErrorDesc(error));
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Authorization", "Basic " + Constants.getRawBasicData());
                        headers.put("sessionId", cacheUtil.getString("sessionId"));
                        return headers;
                    }
                };
                jsonObjectRequest.setTag(TAG);
                NetworkUtil.getInstance(mCtx).addToRequestQueue(jsonObjectRequest);
                alertDialog = DialogUtils.showProgressDialog(mCtx, "Authorizing.", false);
            } else {
                showBasicAlertDialog(mCtx.getResources().getString(R.string.network_error));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void updateChargeAccountList(JSONObject response) {
        if (response == null || response.length() == 0) {
            showBasicAlertDialog("Unfortunately, the customer does not have any valid accounts to charge");
            return;
        }

        JSONArray account_list = response.optJSONArray("account_list");
        if (account_list == null || account_list.length() <= 0) {
            showBasicAlertDialog("Unable to fetch customer account list");
            return;
        }
        try {
            if (mCtx != null && !mCtx.isFinishing()) {
                final CharSequence options[] = new CharSequence[account_list.length()];
                for (int i = 0; i < account_list.length(); i++) {
                    JSONObject jsonObject = account_list.optJSONObject(i);
                    options[i] = jsonObject.optString("ACCT_NO");
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(mCtx, R.style.dialogTheme);
                builder.setCancelable(false);
                builder.setTitle("Customer Fee accounts");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        placeAccountBalanceRequest(options[which].toString());
                    }
                });
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialogObject = builder.create();
                ListView listView = alertDialogObject.getListView();
                listView.setDivider(new ColorDrawable(Color.BLACK)); // set color
                listView.setDividerHeight(2); // set height
                alertDialogObject.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void showBasicAlertDialog(final String body) {
        if (mCtx != null && !mCtx.isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mCtx, R.style.dialogTheme);
            builder.setMessage(body)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    arg0.dismiss();
                                }
                            }).setCancelable(false);
            renderDialog(builder);
        }
    }

    private void renderDialog(AlertDialog.Builder builder) {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        alertDialog = builder.create();
        NetworkUtil.doKeepDialog(alertDialog);
        if (!alertDialog.isShowing()) {
            alertDialog.show();
        }
    }

    private final String TAG = DepositAdaptor.class.getSimpleName();
}
