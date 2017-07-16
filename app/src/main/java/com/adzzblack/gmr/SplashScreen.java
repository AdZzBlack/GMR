package com.adzzblack.gmr;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

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
        String server = globalfunction.getShared("server", "servernow", "");;

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
                                showSpinner(version);

                                UpdateApp atualizaApp = new UpdateApp();
                                atualizaApp.setContext(getApplicationContext());
                                atualizaApp.execute(obj.getString("url"));
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
}
