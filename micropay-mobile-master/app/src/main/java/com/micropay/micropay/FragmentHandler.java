package com.micropay.micropay;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.micropay.api.CacheUtil;
import com.micropay.api.Constants;
import com.micropay.fragments.AccountMenu;
import com.micropay.fragments.AgentMessages;
import com.micropay.fragments.AgentNotifications;
import com.micropay.fragments.BanksMenu;
import com.micropay.fragments.BillsMenuAgent;
import com.micropay.fragments.AirtimeMenu;
import com.micropay.fragments.BillsMenuCustomer;
import com.micropay.fragments.DataMenu;
import com.micropay.fragments.InitiateWithdraw;
import com.micropay.fragments.IssuedReceiptSearch;
import com.micropay.fragments.MobileMoneyMenu;
import com.micropay.fragments.PINChange;
import com.micropay.fragments.SchoolFeesMenu;
import com.micropay.fragments.transactions.ApprovePayments;
import com.micropay.fragments.VoiceMenu;
import com.micropay.fragments.transactions.TXNCashDeposit;
import com.micropay.fragments.transactions.TXNCashWithdraw;
import com.micropay.fragments.CreateCustomer;
import com.micropay.fragments.EncashFloat;
import com.micropay.fragments.transactions.TXNOutletCashDeposit;
import com.micropay.fragments.transactions.TXNOutletCashWithdraw;
import com.micropay.fragments.transactions.TXNOutletTransfer;
import com.micropay.fragments.transactions.RequestPayment;
import com.micropay.fragments.transactions.TXNSuperAgentTransfer;
import com.micropay.fragments.transactions.TxnFundsTransfer;
import com.micropay.fragments.transactions.TXNVoucherRedeem;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FragmentHandler extends ParentActivity {


    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;
    private AppCompatTextView toolbar_title;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(1);

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_handler);

        cacheUtil = new CacheUtil(getApplicationContext());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar_title = toolbar.findViewById(R.id.toolbar_title);

        if (ContextCompat.checkSelfPermission(FragmentHandler.this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(FragmentHandler.this,
                    new String[]{
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.BLUETOOTH_CONNECT
                    },
                    1);
        }

        Fragment fragment = null;
        Intent intent = null;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            fragment = new AgentNotifications();
        } else {
            switch (cacheUtil.getString(Constants.KEY)) {
                case "Customer":
                    fragment = new CreateCustomer();
                    break;
                case "CashIn":
                    fragment = new TXNCashDeposit();
                    break;
                case "CashOut":
                    fragment = new TXNCashWithdraw();
                    break;
                case "VoucherRedeem":
                    fragment = new TXNVoucherRedeem();
                    break;
                case "InitiateWithdraw":
                    fragment = new InitiateWithdraw();
                    break;
                case "P2P":
                    fragment = new TxnFundsTransfer();
                    break;
                case "OutletTransfer":
                    fragment = new TXNOutletTransfer();
                    break;
                case "EnCashFloat":
                    fragment = new EncashFloat();
                    break;
                case "MyAccount":
                    fragment = new AccountMenu();
                    break;
                case "MobileMoney":
                    fragment = new MobileMoneyMenu();
                    break;
                case "OutletCashIn":
                    fragment = new TXNOutletCashDeposit();
                    break;
                case "OutletCashOut":
                    fragment = new TXNOutletCashWithdraw();
                    break;
                case "TransferToSuperAgent":
                    fragment = new TXNSuperAgentTransfer();
                    break;
                case "BillsMenuAgent":
                    fragment = new BillsMenuAgent();
                    break;
                case "BillsMenuCustomer":
                    fragment = new BillsMenuCustomer();
                    break;
                case "OtherBanks":
                    fragment = new BanksMenu();
                    break;
                case "AirtimeMenus":
                    fragment = new AirtimeMenu();
                    break;
                case "RequestPayment":
                    fragment = new RequestPayment();
                    break;
                case "ApprovePayment":
                    fragment = new ApprovePayments();
                    break;
                case "DataMenus":
                    fragment = new DataMenu();
                    break;
                case "SchoolFees":
                    fragment = new SchoolFeesMenu();
                    break;
                case "VoiceMenus":
                    fragment = new VoiceMenu();
                    break;
                case "AgentPINChange":
                    fragment = new PINChange();
                    break;
                case "ViewReceipts":
                    fragment = new IssuedReceiptSearch();
                    break;
                case "Settings":
                    intent = new Intent(FragmentHandler.this, AppSettings.class);
                    break;
                case "Notifications":
                    fragment = new AgentNotifications();
                    break;
                default:
                    fragment = new AgentMessages();
                    break;
            }
        }

        if (savedInstanceState == null) {
            if (fragment != null)
                getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, fragment)
                        .commit();
            else if (intent != null)
                startActivity(intent);
        }
    }

    public void setTitle(String title) {
        if (toolbar_title != null)
            toolbar_title.setText(title);
    }

    @Override
    protected void onPause() {
        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.close, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                stepBack();
                return true;
            case R.id.action_signout:
                FragmentHandler.this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void stepBack() {
        if (getFragmentManager().getBackStackEntryCount() <= 0) {
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }


    private static long back_pressed;

    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis())
            FragmentHandler.this.finish();
        else
            Toast.makeText(getBaseContext(), "Please use the app navigation", Toast.LENGTH_SHORT).show();
        back_pressed = System.currentTimeMillis();
    }



}
