package com.micropay.micropay;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.micropay.api.CacheUtil;
import com.micropay.api.Constants;
import com.micropay.api.NetworkUtil;
import com.micropay.popups.DialogUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgentLocator extends ParentActivity implements PermissionListener,
        GoogleMap.OnMarkerClickListener {

    private final String TAG = AgentLocator.class.getSimpleName();
    MapView mMapView;
    private GoogleMap googleMap;
    private List<MarkerOptions> options = new ArrayList<>();
    private ArrayList<LatLng> latlngs = new ArrayList<>();
    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        NetworkUtil.handleSSLHandshake();
        setContentView(R.layout.activity_locator);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ((AppCompatTextView) toolbar.findViewById(R.id.toolbar_title)).setText("Locate Agent");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            mMapView = findViewById(R.id.mapView);
            cacheUtil = new CacheUtil(getApplicationContext());
            mMapView.onCreate(savedInstanceState);
            mMapView.onResume(); // needed to get the map to display immediately
            MapsInitializer.initialize(AgentLocator.this);
            checkPermission();

            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap mMap) {
                    if (mMap != null) {
                        googleMap = mMap;
                        // For showing a move to my location button
                        setupLocalTracing();
                        LatLng sample = null;
                        for (MarkerOptions option : options) {
                            googleMap.addMarker(option);
                            sample = option.getPosition();
                        }
                        if (sample != null) {
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(sample).zoom(15).build();
                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        }
                    }
                }
            });
        } catch (Exception ex) {
            showAlertDialog("Maps is unavailable at the moment");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_signout:
                showLogoutPrompt();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void showLogoutPrompt() {
        alertDialog = new AlertDialog.Builder(this, R.style.dialogTheme)
                .setMessage("Are you sure you want to logout ?.")
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

    private void showDialogOptions() {
        final CharSequence options[] = new CharSequence[]{"Copy Details", "Share Details"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Choose an action");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
//                        NetworkUtil.copyToClip(AgentLocator.this, title.getText().toString(),
//                                body.getText().toString());
                        break;
                    case 1:
//                        NetworkUtil.shareTextUrl(AgentLocator.this, "Share " + title.getText().toString(),
//                                body.getText().toString());
                        break;
                }
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private JSONArray getBranchList() {
        JSONArray jsonArray;
        try {
            String branches = cacheUtil.getString("branches");
            if (branches != null) {
                jsonArray = new JSONArray(branches);
            } else {
                jsonArray = setupDefaultBranch();
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonArray = setupDefaultBranch();
        }
        return jsonArray;
    }

    private void setupLocalTracing() {
        if (ContextCompat.checkSelfPermission(AgentLocator.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (googleMap != null) {
                googleMap.setMyLocationEnabled(true);
                googleMap.setIndoorEnabled(true);
                googleMap.setBuildingsEnabled(true);
            }
        }
    }

    private JSONArray setupDefaultBranch() {
        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray.put(0, new JSONObject().put("BRANCH", "HEAD OFFICE").put("LNG", 37.2800)
                    .put("LAT", 0.5028).put("REC_ST", "Active"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return jsonArray;
    }

    private void checkPermission() {
        Dexter.withActivity(AgentLocator.this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(this)
                .onSameThread()
                .check();
    }

    public void showAlertDialog(String body) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AgentLocator.this);
        builder.setMessage(body)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                                AgentLocator.this.finish();
                            }
                        }).setCancelable(false);
        alertDialog = builder.create();
        if (!isFinishing()) {
            alertDialog.show();
        }
    }

    protected void showPermissionDeniedDialog() {
        alertDialog = new AlertDialog.Builder(AgentLocator.this, R.style.dialogTheme).create();
        alertDialog.setCancelable(false);
        alertDialog.setMessage("You need to grant permission to eLoan to access your current location in order to " +
                "determine the nearest branch to your current location");

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        AgentLocator.this.finish();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "GRANT",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        checkPermission();
                    }
                });
        alertDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null)
            mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMapView != null)
            mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMapView != null)
            mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null)
            mMapView.onLowMemory();
    }

    @Override
    public void onPermissionGranted(PermissionGrantedResponse response) {
        setupLocalTracing();
    }

    @Override
    public void onPermissionDenied(PermissionDeniedResponse response) {
        showPermissionDeniedDialog();
    }

    @Override
    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
        token.continuePermissionRequest();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker != null) {
//            title.setText(marker.getTitle());
//            body.setText(marker.getSnippet());
        }
        return false;
    }

    private void retrieveBranchListing() {
        if (NetworkUtil.isNetworkAvailable(getApplicationContext())) {
            try {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                        Constants.getBaseUrl() + "/branchLocations", new JSONObject(),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null && response.optJSONArray("branch_array") != null) {
                                    JSONArray branches = response.optJSONArray("branch_array");
                                    if (branches.length() > 0) {
                                        plotBranches(branches);
                                    }
                                    cacheUtil.putString("branch_array", branches.toString());
                                } else if (cacheUtil.getString("branch_array").length() > 0) {
                                    JSONArray branches = null;
                                    try {
                                        branches = new JSONArray(cacheUtil.getString("branch_array"));
                                        plotBranches(branches);
                                        Toast.makeText(AgentLocator.this, "Showing offline locations", Toast.LENGTH_LONG).show();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    showAlertDialog("Looks like the bank hasn't configured any branches yet.");
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (alertDialog != null && alertDialog.isShowing())
                            alertDialog.dismiss();
                        showAlertDialog(NetworkUtil.getErrorDesc(error));
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Authorization", "Basic "
                                + Base64.encodeToString("admin:admin".getBytes(),
                                Base64.NO_WRAP));
                        return headers;
                    }
                };
                jsonObjectRequest.setTag(TAG);
                NetworkUtil.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
                alertDialog = DialogUtils.showProgressDialog(this, "Updating schematics", false);
            } catch (Exception e) {
                showAlertDialog("Oops. Something happened with your connection!");
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        NetworkUtil.getInstance(getApplicationContext()).cancel(TAG);
    }

    private void plotBranches(JSONArray branches) {
        String titleTxt = "Micropay (U) Ltd", bodyTxt = "Located at Rwenzori Towers";
        for (int i = 0; i < branches.length(); i++) {
            JSONObject jsonObject = branches.optJSONObject(i);
            MarkerOptions option = new MarkerOptions();
            LatLng latLng = new LatLng(jsonObject.optDouble("LNG"),
                    jsonObject.optDouble("LAT"));
            option.position(latLng);
            option.title(jsonObject.optString("BRANCH"));
            option.snippet(jsonObject.optString("BRANCH_DESC"));
            option.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_round));
            option.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            options.add(option);
            titleTxt = jsonObject.optString("BRANCH");
            bodyTxt = jsonObject.optString("BRANCH_DESC");
        }
    }

}
