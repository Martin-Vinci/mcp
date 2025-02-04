package com.micropay.micropay;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.micropay.api.CacheUtil;
import com.micropay.api.Constants;
import com.micropay.fragments.AgentMessages;
import com.micropay.fragments.AgentNotifications;
import com.micropay.fragments.PINChange;
import com.micropay.fragments.ContactInstitution;
import com.micropay.fragments.LoansMenu;
import com.prowesspride.api.Setup;

public class SettingsActivity extends ParentActivity implements NavigationView.OnNavigationItemSelectedListener {

    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;
    private AppCompatTextView toolbar_title;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_settings);

        cacheUtil = new CacheUtil(getApplicationContext());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar_title = toolbar.findViewById(R.id.toolbar_title);
        Fragment fragment;

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            fragment = new AgentNotifications();
        } else {
            switch (cacheUtil.getString(Constants.KEY)) {

                case "Credit":
                    fragment = new LoansMenu();
                    break;
                case "Contact":
                    fragment = new ContactInstitution();
                    break;
                case "AgentPINChange":
                    fragment = new PINChange();
                    break;
                case "Notifications":
                    fragment = new AgentNotifications();
                    break;
                default:
                    fragment = new AgentMessages();
                    break;
            }
        }

        if (!cacheUtil.getBoolean("builtin_printer", false))
            try {
                MicropayMobile.setup = new Setup();
                MicropayMobile.setup.blActivateLibrary(this, R.raw.licencefull_pride_gen);
            } catch (Exception e) {
                e.printStackTrace();
            }


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .replace(R.id.container, fragment)
                    .commit();
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
                SettingsActivity.this.finish();
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
        if (back_pressed + 2000 > System.currentTimeMillis()) {
            SettingsActivity.this.finish();
        }
        else
            Toast.makeText(getBaseContext(), "Please use the app navigation", Toast.LENGTH_SHORT).show();
        back_pressed = System.currentTimeMillis();
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
                showAlertDialog("This feature is only available in the next version!", "Dialog");
                break;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
