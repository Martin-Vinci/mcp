package com.micropay.micropay;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.navigation.NavigationView;
import com.micropay.adaptor.MenuAdaptor;
import com.micropay.api.CacheUtil;
import com.micropay.api.Constants;
import com.micropay.api.DisplayUtils;
import com.micropay.api.GridSpaceDeco;
import com.micropay.api.ItemClickListener;
import com.micropay.api.NetworkUtil;
import com.micropay.micropay.AppCrashHandler;
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.LoginActivity;
import com.micropay.micropay.ParentActivity;
import com.micropay.micropay.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SuperAgentHomeActivity extends ParentActivity implements NavigationView.OnNavigationItemSelectedListener,
        ItemClickListener {
    private final String TAG = SuperAgentHomeActivity.class.getSimpleName();
    private AlertDialog alertDialog;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;
    private ProgressDialog dialog;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private TextView username;
    private CacheUtil cacheUtil;
    private boolean isPrinterConfigured = true;
    private boolean isPrintingEnabled = true;
    private static boolean isPrinterConnected = true;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        NetworkUtil.handleSSLHandshake();
        setContentView(R.layout.activity_home);
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

        isPrintingEnabled = cacheUtil.getBoolean("enable_printing", false);
        isPrinterConnected = cacheUtil.getBoolean("is_printer_connected", false);
        if (isPrintingEnabled) {
            if (cacheUtil.getString("bluetooth_address").trim().length() <= 0)
                isPrinterConfigured = false;
            else
                submitJob(startCheckBluetoothConnectivity);
        }

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = (TextView) headerView.findViewById(R.id.profileUName);
        navUsername.setText(cacheUtil.getString("customerName"));
        TextView profilePhone = (TextView) headerView.findViewById(R.id.profilePhone);
        profilePhone.setText(cacheUtil.getString("registeredPhone"));
        TextView outletCode = (TextView) headerView.findViewById(R.id.outletCode);
        outletCode.setText("Outlet Code: " + cacheUtil.getString("outletCode"));

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
            jsonArray.put(0, new JSONObject().put("label", "Outlet Cash In").put("sub_title", "Send Money")
                    .put("icon", R.drawable.cash_deposit));
            jsonArray.put(1, new JSONObject().put("label", "Outlet Cash Out").put("sub_title", "Send Money")
                    .put("icon", R.drawable.withdraw));
            jsonArray.put(2, new JSONObject().put("label", "Transfer to S.Agent").put("sub_title", "Send Money")
                    .put("icon", R.drawable.outlet_transfer));
            jsonArray.put(3, new JSONObject().put("label", "My Account").put("sub_title", "Send Money")
                    .put("icon", R.drawable.my_account));
            jsonArray.put(4, new JSONObject().put("label", "Sign out").put("sub_title", "Send Money")
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
                if(1==2) { // (isPrintingEnabled && !isPrinterConfigured) {
                    showToast(1, "Printer is not configured");
                    return;
                }
                if (isPrintingEnabled && isPrinterConfigured && !isPrinterConnected) {
                    showToast(1, "Bluetooth printer is not connected");
                    return;
                }
                cacheUtil.putString(Constants.KEY, "OutletCashIn");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(this, FragmentHandler.class),
                            ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                } else {
                    startActivity(new Intent(this, FragmentHandler.class));
                }
                break;
            case 1:
                if(1==2) { // (isPrintingEnabled && !isPrinterConfigured) {
                    showToast(1, "Printer is not configured");
                    return;
                }
                if (isPrintingEnabled && isPrinterConfigured && !isPrinterConnected) {
                    showToast(1, "Bluetooth printer is not connected");
                    return;
                }
                cacheUtil.putString(Constants.KEY, "OutletCashOut");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(this, FragmentHandler.class),
                            ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                } else {
                    startActivity(new Intent(this, FragmentHandler.class));
                }
                break;
            case 2:
                if(1==2) { // (isPrintingEnabled && !isPrinterConfigured) {
                    showToast(1, "Printer is not configured");
                    return;
                }
                if (isPrintingEnabled && isPrinterConfigured && !isPrinterConnected) {
                    showToast(1, "Bluetooth printer is not connected");
                    return;
                }
                cacheUtil.putString(Constants.KEY, "TransferToSuperAgent");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(this, FragmentHandler.class),
                            ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                } else {
                    startActivity(new Intent(this, FragmentHandler.class));
                }
                break;
            case 3:
                //contact us
                cacheUtil.putString(Constants.KEY, "MyAccount");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(this, FragmentHandler.class),
                            ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                } else {
                    startActivity(new Intent(this, FragmentHandler.class));
                }
                break;
            case 4:
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
                    collapsingToolbar.setTitle("Micropay-Super Agent");
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
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


    private Thread startCheckBluetoothConnectivity = new Thread(new Runnable() {
        @Override
        public void run() {
            BluetoothAdapter mBluetoothAdapter;
            BluetoothDevice mBluetoothDevice;
            BluetoothSocket mBluetoothSocket;
            UUID applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            try {
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(cacheUtil.getString("bluetooth_address"));
                try {
                    mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(applicationUUID);
                    mBluetoothAdapter.cancelDiscovery();
                    if (!mBluetoothSocket.isConnected())
                        mBluetoothSocket.connect();
                    isPrinterConnected = true;
                    cacheUtil.putBoolean("is_printer_connected", isPrinterConnected);
                    //Looper.prepare();
                    //showToast(2, "Bluetooth printer is now connected");
                    //Looper.loop();
                } catch (IOException eConnectException) {
                    eConnectException.printStackTrace();
                    try {
                        mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(applicationUUID);
                        mBluetoothAdapter.cancelDiscovery();
                        mBluetoothSocket.connect();
                        isPrinterConnected = true;
                        cacheUtil.putBoolean("is_printer_connected", isPrinterConnected);
                        //Looper.prepare();
                        //showToast(2, "Bluetooth printer is now connected");
                        //Looper.loop();
                    } catch (Exception e2) {
                        isPrinterConnected = false;
                        cacheUtil.putBoolean("is_printer_connected", isPrinterConnected);
                        e2.printStackTrace();
                        Looper.prepare();
                        showToast(3, "Bluetooth printer not connected");
                        Looper.loop();
                    }
                } finally {

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

    public void showToast(final int duration, final String text) {
        if (duration <= 0)
            return;

        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showToast(duration - 1, text);
            }
        }, 1000);
    }

    private void submitJob(Runnable runnable) {
        try {
            threadPool.submit(runnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
