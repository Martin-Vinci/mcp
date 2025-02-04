package com.micropay.micropay;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.micropay.adaptor.MenuAdaptor;
import com.micropay.api.CacheUtil;
import com.micropay.api.Constants;
import com.micropay.api.DisplayUtils;
import com.micropay.api.GridSpaceDeco;
import com.micropay.api.ItemClickListener;
import com.micropay.api.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CustomerHomeActivity extends ParentActivity implements NavigationView.OnNavigationItemSelectedListener,
        ItemClickListener {


    private final String TAG = AgentHomeActivity.class.getSimpleName();
    private AlertDialog alertDialog;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;
    private ProgressDialog dialog;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private TextView username;
    private CacheUtil cacheUtil;

    private JSONArray tcInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_home);
        NetworkUtil.handleSSLHandshake();
        // Install the application crash handler
        AppCrashHandler.installHandler(getApplicationContext());

        cacheUtil = new CacheUtil(getApplicationContext());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.itemlist);
        swipeContainer = findViewById(R.id.menu_swipe);
        username = findViewById(R.id.username);

        initCollapsingToolbar();
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = (TextView) headerView.findViewById(R.id.profileUName);
        navUsername.setText(cacheUtil.getString("customerName"));
        TextView profilePhone = (TextView) headerView.findViewById(R.id.profilePhone);
        profilePhone.setText(cacheUtil.getString("registeredPhone"));
        TextView outletCode = (TextView) headerView.findViewById(R.id.outletCode);
        outletCode.setVisibility(View.GONE);
        //outletCode.setText("Customer Code: " + cacheUtil.getString("outletCode"));

        if (!cacheUtil.getBoolean("builtin_printer", false))
//            try {
//                OrbitLiteApp.setup = new Setup();
//                OrbitLiteApp.isThermalActivated = OrbitLiteApp.setup.blActivateLibrary(this, R.raw.licencefull_pride_gen);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

            setupViews();
    }

    private void setupViews() {

        GridSpaceDeco gridSpacingItemDecoration;
        GridLayoutManager mLayoutManager;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mLayoutManager = new GridLayoutManager(this, 1);
            gridSpacingItemDecoration = new GridSpaceDeco(1, DisplayUtils.dpToPx(1, this), true);
        } else {
            mLayoutManager = new GridLayoutManager(this, 4);
            gridSpacingItemDecoration = new GridSpaceDeco(4, DisplayUtils.dpToPx(10, this), false);
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(gridSpacingItemDecoration);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setBackgroundColor(Color.parseColor("#831687"));
        recyclerView.setDrawingCacheBackgroundColor(Color.parseColor("#831687"));

        MenuAdaptor adapter = new MenuAdaptor(this, getCustomerMenu());
        recyclerView.setAdapter(adapter);
        adapter.setClickListener(this);
        swipeContainer.setOnRefreshListener(() -> swipeContainer.setRefreshing(false));
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_dark,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_red_dark);
    }

    private JSONArray getCustomerMenu() {
        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray.put(0, new JSONObject().put("label", "Mobile Money").put("sub_title", "Mobile Money")
                    .put("icon", R.drawable.mobile_money));
            jsonArray.put(1, new JSONObject().put("label", "Request Payment").put("sub_title", "Request Payment")
                    .put("icon", R.drawable.request_payment));
            jsonArray.put(2, new JSONObject().put("label", "Approve Payment").put("sub_title", "Approve Payment")
                    .put("icon", R.drawable.approve_payment));
            jsonArray.put(3, new JSONObject().put("label", "Services and Utilities").put("sub_title", "Services and Utilities")
                    .put("icon", R.drawable.utility_icon));
            jsonArray.put(4, new JSONObject().put("label", "Buy Airtime").put("sub_title", "Buy Airtime")
                    .put("icon", R.drawable.airtime));
            jsonArray.put(5, new JSONObject().put("label", "Data Bundles").put("sub_title", "Buy Data")
                    .put("icon", R.drawable.data_bundles));
            jsonArray.put(6, new JSONObject().put("label", "Voice Bundles").put("sub_title", "Buy Data")
                    .put("icon", R.drawable.voice_bundles));
            jsonArray.put(7, new JSONObject().put("label", "Banks").put("sub_title", "Centenary Bank")
                    .put("icon", R.drawable.banks));
            jsonArray.put(8, new JSONObject().put("label", "School Fees").put("sub_title", "School Fees")
                    .put("icon", R.drawable.school_fees));
            jsonArray.put(9, new JSONObject().put("label", "My Account").put("sub_title", "Send Money")
                    .put("icon", R.drawable.my_account));
//            if (isGooglePlayServicesAvailable())
//                jsonArray.put(7, new JSONObject().put("label", "Agent Locator")
//                        .put("icon", R.drawable.marker));
            jsonArray.put(10, new JSONObject().put("label", "Sign out")
                    .put("icon", R.drawable.sign_out));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    @Override
    public void onClick(View view, int position) {
        switch (position) {
            case 0:
                //Credit
                cacheUtil.putString(Constants.KEY, "MobileMoney");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(this, FragmentHandler.class),
                            ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                } else {
                    startActivity(new Intent(this, FragmentHandler.class));
                }
                break;
            case 1:
                //Customer
                cacheUtil.putString(Constants.KEY, "RequestPayment");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(this, FragmentHandler.class),
                            ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                } else {
                    startActivity(new Intent(this, FragmentHandler.class));
                }
                break;
            case 2:
                //Customer
                cacheUtil.putString(Constants.KEY, "ApprovePayment");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(this, FragmentHandler.class),
                            ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                } else {
                    startActivity(new Intent(this, FragmentHandler.class));
                }
                break;
            case 3:
                //Account
                cacheUtil.putString(Constants.KEY, "BillsMenuCustomer");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(this, FragmentHandler.class),
                            ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                } else {
                    startActivity(new Intent(this, FragmentHandler.class));
                }
                break;
            case 4:
                //Transaction
                cacheUtil.putString(Constants.KEY, "AirtimeMenus");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(this, FragmentHandler.class),
                            ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                } else {
                    startActivity(new Intent(this, FragmentHandler.class));
                }
                break;
            case 5:
                //Transaction
                cacheUtil.putString(Constants.KEY, "DataMenus");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(this, FragmentHandler.class),
                            ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                } else {
                    startActivity(new Intent(this, FragmentHandler.class));
                }
                break;
            case 6:
                //Transaction
                cacheUtil.putString(Constants.KEY, "VoiceMenus");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(this, FragmentHandler.class),
                            ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                } else {
                    startActivity(new Intent(this, FragmentHandler.class));
                }
                break;
            case 7:
                //Transaction
                cacheUtil.putString(Constants.KEY, "OtherBanks");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(this, FragmentHandler.class),
                            ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                } else {
                    startActivity(new Intent(this, FragmentHandler.class));
                }
                break;
            case 8:
                //Transaction
                cacheUtil.putString(Constants.KEY, "SchoolFees");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(this, FragmentHandler.class),
                            ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                } else {
                    startActivity(new Intent(this, FragmentHandler.class));
                }
                break;
            case 9:
                cacheUtil.putString(Constants.KEY, "MyAccount");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(this, FragmentHandler.class),
                            ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                } else {
                    startActivity(new Intent(this, FragmentHandler.class));
                }
                break;
            case 10:
                showLogoutPrompt();
                break;
            default:
                Toast.makeText(this, "This service is coming soon!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_inbox:
                cacheUtil.putString(Constants.KEY, "Inbox");
                startActivity(new Intent(this, FragmentHandler.class));
                break;
            case R.id.nav_settings:
                cacheUtil.putString(Constants.KEY, "Settings");
                startActivity(new Intent(this, FragmentHandler.class));
                break;
            case R.id.nav_receipts:
                cacheUtil.putString(Constants.KEY, "ViewReceipts");
                startActivity(new Intent(this, FragmentHandler.class));
                break;
            case R.id.nav_notification:
                cacheUtil.putString(Constants.KEY, "Notifications");
                startActivity(new Intent(this, FragmentHandler.class));
                break;
            default:
                showAlertDialog("This feature is only available in the next version!");
                break;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean isTcAllowed(long tcNo) {
        boolean isAllowed = false;
        for (int i = 0; i < tcInfo.length(); i++) {
            JSONObject jsonObject = tcInfo.optJSONObject(i);
            if (jsonObject.has("TC_NO") && jsonObject.optLong("TC_NO") == tcNo) {
                isAllowed = true;
                break;
            }
        }
        return isAllowed;
    }

    private JSONArray getTcInfo() {
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(cacheUtil.getString("tc_info"));
        } catch (JSONException e) {
            jsonArray = new JSONArray();
        }
        return jsonArray;
    }

    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.toolbar_layout);
        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = findViewById(R.id.app_bar);
        appBarLayout.setExpanded(true);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle("Micropay-Customer");
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    public void showAboutDialog(String title, String body) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.dialogTheme);
        builder.setTitle(title);
        builder.setMessage(
                Html.fromHtml("<font color='#1B5E20'>" + body + "</font>"))
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                            }
                        }).setCancelable(false);
        alertDialog = builder.create();
        if (!isFinishing()) {
            alertDialog.show();
        }
    }

    public void showAlertDialog(String body) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.dialogTheme);
        builder.setMessage(
                Html.fromHtml("<font color='#1B5E20'>" + body + "</font>"))
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                            }
                        }).setCancelable(false);
        alertDialog = builder.create();
        if (!isFinishing()) {
            alertDialog.show();
        }
    }

    protected void showLogoutPrompt() {
        alertDialog = new AlertDialog.Builder(this, R.style.dialogTheme)
                .setMessage(
                        Html.fromHtml("<font color='#1B5E20'>Are you sure you want to logout ?.</font>"))
                .setPositiveButton("LOGOUT",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                                clearAndStartNewActivity(LoginActivity.class);
                            }
                        }).setNegativeButton("CANCEL",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                            }
                        }).setCancelable(false).create();
        if (!isFinishing())
            alertDialog.show();
    }

    private void clearAndStartNewActivity(Class<?> clazz) {
        Intent intent = new Intent(getApplicationContext(), clazz);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void sendToWhatsApp(String message, String contact) {
        try {
            Intent smsIntent = new Intent(Intent.ACTION_VIEW);
            smsIntent.setType("vnd.android-dir/mms-sms");
            smsIntent.putExtra("address", contact);
            smsIntent.putExtra("sms_body", message);
            startActivity(smsIntent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            showAlternativeWhatsApp("Looks like you do not have any Messages App installed on your device");
        }
    }

    private void showAlternativeWhatsApp(String body) {
        alertDialog = new AlertDialog.Builder(this, R.style.dialogTheme)
                .setMessage(
                        Html.fromHtml("<font color='#1B5E20'>" + body + "</font>"))
                .setPositiveButton("OPEN GOOGLE PLAY",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                                NetworkUtil.openGooglePlay(CustomerHomeActivity.this, "com.whatsapp");
                            }
                        }).setNegativeButton("CANCEL",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                            }
                        }).setCancelable(false).create();
        if (!isFinishing() && !alertDialog.isShowing())
            alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_signout) {
            showLogoutPrompt();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            showLogoutPrompt();
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onPause() {
        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();
        if (swipeContainer != null)
            swipeContainer.setRefreshing(false);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();
        if (swipeContainer != null)
            swipeContainer.setRefreshing(false);
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
        NetworkUtil.getInstance(this).cancel(TAG);
    }


}
