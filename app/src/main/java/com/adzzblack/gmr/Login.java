package com.adzzblack.gmr;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ADI on 2/13/2017.
 */

public class Login extends AppCompatActivity implements View.OnClickListener{

    private EditText et_username, et_password;
    private Button btn_login;
    private ProgressDialog loadingDialog;
    private String user_name, user_hash, user_role;
    private GlobalFunction globalfunction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        globalfunction = new GlobalFunction(this);

        globalfunction.setShared("server", "servernow", "gmr.inspiraworld.com");
//        globalfunction.setShared("server", "servernow", "117.102.229.10:99");

        et_username = (EditText) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_password);
        btn_login = (Button) findViewById(R.id.btn_login);

        btn_login.setOnClickListener(this);

        user_role = globalfunction.getShared("user", "role", "");
        user_name = globalfunction.getShared("user", "nama", "");
        user_hash = globalfunction.getShared("user", "hash", "");

        if (user_role.equals("")) {
            globalfunction.clearShared("user");
        }
        else
        {
            et_username.setVisibility(View.INVISIBLE);
            et_password.setVisibility(View.INVISIBLE);
            btn_login.setVisibility(View.INVISIBLE);

            Log.d("hash", user_hash+"a");
            String actionUrl = "Login/checkLogin/";
            new checkLogin().execute( actionUrl );
        }
    }

    @Override
    public void onClick(View v) {
        v.startAnimation(Index.buttoneffect);
        if(v.getId() == R.id.btn_login){
            String actionUrl = "Login/loginUser/";
            new loginUser().execute( actionUrl );
        }
    }

    private class checkLogin extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                Index.jsonObject = new JSONObject();
                Index.jsonObject.put("hash", user_hash);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return Index.globalfunction.executePost(urls[0], Index.jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("res1", result);
            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        String success = obj.getString("success");
                        if(success.equals("true")){
                            globalfunction.setShared("user", "role_beritaacara", obj.getString("role_beritaacara"));
                            globalfunction.setShared("user", "role_approveberitaacara", obj.getString("role_approveberitaacara"));
                            globalfunction.setShared("user", "role_deliveryorder", obj.getString("role_deliveryorder"));
                            globalfunction.setShared("user", "role_approvedeliveryorder", obj.getString("role_approvedeliveryorder"));
                            globalfunction.setShared("user", "role_bpm", obj.getString("role_bpm"));
                            globalfunction.setShared("user", "role_opname", obj.getString("role_opname"));
                            globalfunction.setShared("user", "role_notabeli", obj.getString("role_notabeli"));
                            globalfunction.setShared("user", "role_map", obj.getString("role_map"));
                            globalfunction.setShared("user", "role_pasang", obj.getString("role_pasang"));

                            Intent intent = new Intent(Login.this, Index.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                            finish();
                        }
                        else
                        {
                            et_username.setVisibility(View.VISIBLE);
                            et_password.setVisibility(View.VISIBLE);
                            btn_login.setVisibility(View.VISIBLE);
                            Toast.makeText(getBaseContext(), "User has been login in another device", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }catch(Exception e)
            {
                e.printStackTrace();
                et_username.setVisibility(View.VISIBLE);
                et_password.setVisibility(View.VISIBLE);
                btn_login.setVisibility(View.VISIBLE);
                Toast.makeText(getBaseContext(), "Login Failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class loginUser extends AsyncTask<String, Void, String> {
        String username = et_username.getText().toString();
        String password = et_password.getText().toString();

        @Override
        protected String doInBackground(String... urls) {
            try {
                Index.jsonObject = new JSONObject();
                Index.jsonObject.put("username", username);
                Index.jsonObject.put("password", password);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return Index.globalfunction.executePost(urls[0], Index.jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("tes", result);
            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(!obj.has("query")){
                            globalfunction.setShared("user", "username", username);
                            globalfunction.setShared("user", "id", obj.getString("user_id"));
                            globalfunction.setShared("user", "nomor", obj.getString("user_nomor"));
                            globalfunction.setShared("user", "nama", obj.getString("user_nama"));
                            globalfunction.setShared("user", "role", obj.getString("user_role"));
                            globalfunction.setShared("user", "hash", obj.getString("user_hash"));
                            globalfunction.setShared("user", "role_beritaacara", obj.getString("role_beritaacara"));
                            globalfunction.setShared("user", "role_approveberitaacara", obj.getString("role_approveberitaacara"));
                            globalfunction.setShared("user", "role_deliveryorder", obj.getString("role_deliveryorder"));
                            globalfunction.setShared("user", "role_approvedeliveryorder", obj.getString("role_approvedeliveryorder"));
                            globalfunction.setShared("user", "role_bpm", obj.getString("role_bpm"));
                            globalfunction.setShared("user", "role_opname", obj.getString("role_opname"));
                            globalfunction.setShared("user", "role_notabeli", obj.getString("role_notabeli"));
                            globalfunction.setShared("user", "role_map", obj.getString("role_map"));
                            globalfunction.setShared("user", "role_pasang", obj.getString("role_pasang"));

                            hideLoading();

                            Intent intent = new Intent(Login.this, Index.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                            finish();
                        }
                        else
                        {
                            Toast.makeText(getBaseContext(), "Login Failed", Toast.LENGTH_LONG).show();
                            hideLoading();
                        }
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
                Toast.makeText(getBaseContext(), "Login Failed", Toast.LENGTH_LONG).show();
                hideLoading();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoading();
        }
    }

    private void hideLoading()
    {
        loadingDialog.dismiss();
    }

    private void showLoading()
    {
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setTitle("Logging in");
        loadingDialog.setMessage("Please Wait");
        loadingDialog.setCancelable(true);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();
    }
}
