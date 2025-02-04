package com.micropay.api;

import android.app.DownloadManager;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.micropay.micropay.BuildConfig;
import com.micropay.micropay.R;

import java.io.File;
import java.util.Objects;

public class AppService extends Service {

    private BroadcastReceiver onComplete;
    private String filePath;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            Log.v("status", "starting service now");
            System.out.println("zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz");
            String url = Constants.getAppVersionUrl() + "/appDownload";
            System.out.println(url);
            String dir = Environment.getExternalStorageDirectory() + "/APK";


            File absoluteFolder = new File(dir);
            if (!absoluteFolder.exists())
                absoluteFolder.mkdirs();

            String fileName = "micropay_mobile.apk";
            File orbitLte = new File(dir, fileName);

            final Uri fileUri = Uri.fromFile(new File(dir, fileName));
            //Delete update file if exists
            if (orbitLte.exists())
                orbitLte.delete();

            Log.v("pre-file", orbitLte.getAbsolutePath());
            Log.v("pre-file exists", ":" + orbitLte.exists());
            filePath = orbitLte.getAbsolutePath();

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setDescription("Downloading newer version of Micropay mobile");
            request.setTitle(this.getString(R.string.app_name));

            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                    DownloadManager.Request.NETWORK_MOBILE);
            request.setAllowedOverRoaming(false);
            request.setVisibleInDownloadsUi(true);
            //set destination
            request.setDestinationUri(fileUri);
//            request.setDestinationInExternalPublicDir("APK", fileName);
            Log.v("uri", ":" + Environment.getExternalStorageDirectory() + "/APK/" + fileName);
            // get download service and enqueue file
            final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            final long downloadId = manager.enqueue(request);





            //set BroadcastReceiver to install app when .apk is downloaded
            onComplete = new BroadcastReceiver() {
                public void onReceive(Context ctxt, Intent intent) {

                    installAPK();








//                    Log.v("url", ":" + fileUri.getPath());
//                    Intent intentActivity;
//
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        Uri apkUri = FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()), BuildConfig.APPLICATION_ID + ".provider", orbitLte);
//                        intentActivity = new Intent(Intent.ACTION_INSTALL_PACKAGE);
//                        intentActivity.setData(apkUri);
//                        intentActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
//                                | Intent.FLAG_ACTIVITY_NEW_TASK
//                                | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                    } else {
//                        Uri apkUri = Uri.fromFile(orbitLte);
//                        intentActivity = new Intent(Intent.ACTION_VIEW);
//                        intentActivity.setDataAndType(apkUri, "application/vnd.android.package-archive");
//                        intentActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
//                                | Intent.FLAG_ACTIVITY_NEW_TASK
//                                | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                    }
//                    startActivity(intentActivity);
//                    unregisterReceiver(this);


//                    Uri data = FileProvider.getUriForFile(getBaseContext(), BuildConfig.APPLICATION_ID + ".provider", orbitLte);
//                    intent.setDataAndType(data, "application/vnd.android.package-archive");
//                    intentActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
//                            | Intent.FLAG_ACTIVITY_NEW_TASK
//                            | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                    startActivity(intentActivity);
//                    unregisterReceiver(this);

//                    intentActivity.setDataAndType(fileUri, "application/vnd.android.package-archive");
//                    intentActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
//                            | Intent.FLAG_ACTIVITY_NEW_TASK
//                    |Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                   // intentActivity.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                    startActivity(intentActivity);
//                    unregisterReceiver(this);
                }
            };
            //register receiver for when .apk download is compete
            registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void installAPK(){
        String PATH = filePath;
        File file = new File(PATH);
        if(file.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uriFromFile(getApplicationContext(), new File(PATH)), "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                getApplicationContext().startActivity(intent);
                Toast.makeText(getApplicationContext(),"Installing",Toast.LENGTH_LONG).show();
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                Log.e("TAG", "Error in opening the file!");
            }
        }
    }
    Uri uriFromFile(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
        } else {
            return Uri.fromFile(file);
        }
    }


    private File getAbsoluteFile(String relativePath) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return new File(getExternalFilesDir(null), relativePath);
        } else {
            return new File(getFilesDir(), relativePath);
        }
    }

    @Override
    public void onDestroy() {
        if (onComplete != null) {
            unregisterReceiver(onComplete);
        }
        super.onDestroy();
    }

}
