package com.adzzblack.gmr;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by antonnw on 16/06/2016.
 */
public class SplashScreen extends AppCompatActivity {

    private static int timeout = 2000;
    private String isAppInstalled;
    private SharedPreferences sharedpreferences;
    private GlobalFunction globalfunction;

    private ProgressDialog mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
            }
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                globalfunction = new GlobalFunction(SplashScreen.this);
                globalfunction.setShared("server", "servernow", "gmr.inspiraworld.com");

                sharedpreferences = getSharedPreferences("shortcut", Context.MODE_PRIVATE);
                isAppInstalled  = sharedpreferences.getString("isAppInstalled", "");

                if (!isAppInstalled.equals("true")) {
                    addShortcutIcon();
                }

                String actionUrl = "Login/getVersion/";
                new getVersion().execute( actionUrl );
            }
        }, timeout);
    }

    public void addShortcutIcon() {
        //shorcutIntent object
        Intent shortcutIntent = new Intent(getApplicationContext(),
                SplashScreen.class);

        shortcutIntent.setAction(Intent.ACTION_MAIN);
        //shortcutIntent is added with addIntent
        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "GMR");
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(getApplicationContext(),
                        R.drawable.logo));

        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        // finally broadcast the new Intent
        getApplicationContext().sendBroadcast(addIntent);

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("isAppInstalled", "true");
        editor.commit();
    }

    private void checkDone()
    {
        String server = globalfunction.getShared("server", "servernow", "");

        if(server.equals(""))
        {
            Intent i = new Intent(SplashScreen.this, Login.class);
            startActivity(i);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
            finish();
//            Intent i = new Intent(SplashScreen.this, SelectServer.class);
//            startActivity(i);
//            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
//            finish();
        }
        else
        {
            Intent i = new Intent(SplashScreen.this, Login.class);
            startActivity(i);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
            finish();
        }
    }

    private void showSpinner(String t) {
        mSpinner = new ProgressDialog(this);
        mSpinner.setTitle("Downloading new version");
        mSpinner.setMessage("Please wait...");
        mSpinner.setCancelable(true);
        mSpinner.setCanceledOnTouchOutside(false);
        mSpinner.show();
    }

    private class getVersion extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            Index.jsonObject = new JSONObject();
            return globalfunction.executePost(urls[0], Index.jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("GETVERSION", result + "1");
            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(!obj.has("query")){
                            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                            String version = pInfo.versionName;
                            if(!version.equals(obj.getString("version")))
                            {
//                                showSpinner(version);
//
//                                UpdateApp atualizaApp = new UpdateApp();
//                                atualizaApp.setContext(getApplicationContext());
//                                atualizaApp.execute(obj.getString("url"));

                                showProgress();
                                Log.d("update", obj.getString("url"));
                                final UpdateApp atualizaApp = new UpdateApp();
                                atualizaApp.setContext(getApplicationContext());
                                atualizaApp.execute(obj.getString("url"));

                                mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        atualizaApp.cancel(true);
                                    }
                                });
                            }
                            else
                            {
                                checkDone();
                            }
                        }
                        else
                        {
                            checkDone();
                        }
                    }
                }
            }catch(Exception e)
            {
                e.printStackTrace();
                Toast.makeText(getBaseContext(), "Checking version failed", Toast.LENGTH_LONG).show();
                checkDone();
            }
        }
    }

    private ProgressDialog mProgressDialog;

    private void showProgress() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Downloading new version");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(false);
    }

    private class UpdateApp extends AsyncTask<String, Integer, String> {

        private PowerManager.WakeLock mWakeLock;
        private Context context;

        public void setContext(Context contextf) {
            context = contextf;
        }

        @Override
        protected String doInBackground(String... arg0) {
            try {
                URL url = new URL(arg0[0]);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.setDoOutput(true);
                c.connect();

                String PATH = "/mnt/sdcard/Download/";
                File file = new File(PATH);
                file.mkdirs();
                File outputFile = new File(file, "update.apk");
                if (outputFile.exists()) {
                    outputFile.delete();
                }
                FileOutputStream fos = new FileOutputStream(outputFile);

                int fileLength = c.getContentLength();

                InputStream is = c.getInputStream();


                byte[] buffer = new byte[2048];
                int len1 = 0;
                long total = 0;
                while ((len1 = is.read(buffer)) != -1) {
                    total += len1;

                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));

                    fos.write(buffer, 0, len1);
                }
                fos.close();
                is.close();

                if(Build.VERSION.SDK_INT>=24){
                    try{
                        Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                        m.invoke(null);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File("/mnt/sdcard/Download/update.apk")), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
                context.startActivity(intent);


            } catch (Exception e) {
                Log.e("UpdateAPP", "Update error! " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if (result != null)
                Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();
        }
    }
}
