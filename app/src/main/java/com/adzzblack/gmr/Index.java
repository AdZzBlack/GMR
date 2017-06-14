package com.adzzblack.gmr;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Index extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static AlphaAnimation buttoneffect = new AlphaAnimation(1F, 0.8F);
    public static AlphaAnimation listeffect = new AlphaAnimation(1F, 0.5F);

    public static GlobalFunction globalfunction;
    public static JSONObject jsonObject;

    public static ProgressDialog loadingDialog;

    public static FragmentManager fm;

    private TextView approveElevasi, approveOrder, approvedOrder, disapprovedOrder, privateMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_drawer);

        fm = getSupportFragmentManager();

        globalfunction = new GlobalFunction(this);

        globalfunction.setShared("server", "servernow", "gmr.inspiraworld.com");
//        globalfunction.setShared("server", "servernow", "117.102.229.10:99");

        // Start Registering GCM
        Intent intent = new Intent(this, GCMInstanceIDRegistrationService.class);
        startService(intent);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Menu navmenu = navigationView.getMenu();
        navmenu.findItem(R.id.nav_menu).setTitle("Welcome, " + Index.globalfunction.getShared("user", "nama", ""));

        approveElevasi = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.nav_approve_elevasi));
        approveOrder = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.nav_approve_order));
        approvedOrder = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.nav_approved_order));
        disapprovedOrder = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.nav_disapproved_order));
        privateMessage = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.nav_private_message));

        if(Index.globalfunction.getShared("user", "role_approveberitaacara", "0").equals("1"))
        {
            approveElevasi.setGravity(Gravity.CENTER_VERTICAL);
            approveElevasi.setTypeface(null, Typeface.BOLD);
            approveElevasi.setTextColor(getResources().getColor(R.color.colorPrimary));
            approveElevasi.setText("0");
        }
        else
        {
            navmenu.findItem(R.id.nav_approve_elevasi).setVisible(false);
        }

        if(Index.globalfunction.getShared("user", "role_approvedeliveryorder", "0").equals("1"))
        {
            approveOrder.setGravity(Gravity.CENTER_VERTICAL);
            approveOrder.setTypeface(null, Typeface.BOLD);
            approveOrder.setTextColor(getResources().getColor(R.color.colorPrimary));
            approveOrder.setText("0");
        }
        else
        {
            navmenu.findItem(R.id.nav_approve_order).setVisible(false);
        }

        if(Index.globalfunction.getShared("user", "role_deliveryorder", "0").equals("1"))
        {
            approvedOrder.setGravity(Gravity.CENTER_VERTICAL);
            approvedOrder.setTypeface(null, Typeface.BOLD);
            approvedOrder.setTextColor(getResources().getColor(R.color.colorPrimary));
            approvedOrder.setText("0");

            disapprovedOrder.setGravity(Gravity.CENTER_VERTICAL);
            disapprovedOrder.setTypeface(null, Typeface.BOLD);
            disapprovedOrder.setTextColor(getResources().getColor(R.color.colorPrimary));
            disapprovedOrder.setText("0");
        }
        else
        {
            navmenu.findItem(R.id.nav_delivery_order).setVisible(false);
        }

        if(Index.globalfunction.getShared("user", "role_notabeli", "0").equals("0"))
        {
            navmenu.findItem(R.id.nav_opname).setVisible(false);
        }

        if(Index.globalfunction.getShared("user", "role_map", "0").equals("0"))
        {
            navmenu.findItem(R.id.nav_map).setVisible(false);
        }

        privateMessage.setGravity(Gravity.CENTER_VERTICAL);
        privateMessage.setTypeface(null, Typeface.BOLD);
        privateMessage.setTextColor(getResources().getColor(R.color.colorPrimary));
        privateMessage.setText("0");

        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(5000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initializeCountDrawer();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        t.start();

        Fragment fragment = new Dashboard();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();

        // Open Specific Notification
        String menuFragment = this.getIntent().getStringExtra("fragment");
        String nomor = this.getIntent().getStringExtra("nomor");
        String nama = this.getIntent().getStringExtra("nama");
        if(menuFragment != null){
            if(menuFragment.equals("ChooseApprovalElevasi")){
                fragment = new ChooseApprovalElevasi();
                transaction = this.getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
            else if(menuFragment.equals("ChooseApprovalDelivery")){
                fragment = new ChooseApprovalDelivery();
                transaction = this.getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
            else if(menuFragment.equals("DeliveryOrderApproved")){
                fragment = new ChoosePrintDelivery();
                transaction = this.getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
            else if(menuFragment.equals("DeliveryOrderDisapproved")){
                fragment = new ChooseEditDelivery();
                transaction = this.getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        }

        navigationView.setNavigationItemSelectedListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
    }

    public void initializeCountDrawer(){
        //Gravity property aligns the text

        String actionUrl = "Master/getCount/";
        new getCounter().execute( actionUrl );
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_dashboard) {
            Fragment fragment = new Dashboard();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack("dashboard");
            transaction.commit();
        }
        else if (id == R.id.nav_map) {
            Index.globalfunction.setShared("bangunan", "header", "0");
            Index.globalfunction.setShared("bangunan", "before", "");
            Index.globalfunction.setShared("global", "destination", "map");

            Fragment fragment = new ChooseBangunan();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
        else if (id == R.id.nav_opname) {
            Fragment fragment = new CreateCreditNote();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
        else if (id == R.id.nav_approve_elevasi) {
            Fragment fragment = new ChooseApprovalElevasi();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
        else if (id == R.id.nav_approve_order) {
            Fragment fragment = new ChooseApprovalDelivery();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
        else if (id == R.id.nav_approved_order) {
            Fragment fragment = new ChoosePrintDelivery();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
        else if (id == R.id.nav_disapproved_order) {
            Fragment fragment = new ChooseEditDelivery();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
        else if (id == R.id.nav_private_message) {
            Fragment fragment = new PrivateMessageUser();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
        else if (id == R.id.nav_changepassword) {
            Fragment fragment = new ChangePassword();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
        else if (id == R.id.nav_logout) {
            String actionUrl = "Login/logoutUser/";
            new logout().execute( actionUrl );
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        Log.d("a", "onBackPressed: ");
        if(Index.globalfunction.getShared("global", "position", "").equals("choosebangunan"))
        {
            String before_ = Index.globalfunction.getShared("bangunan", "before", "");
            String[] before = before_.split(",");
            String now = "";
            before_ = "";
            for(int i=0 ; i < before.length ; i++){
                now = before[i];
                if(i<before.length-1)
                {
                    before_ = before_ + now + ",";
                }
            }

            Log.d("after", before_);
            Index.globalfunction.setShared("bangunan", "header", now);
            Index.globalfunction.setShared("bangunan", "before", before_);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
            }
            else {
                super.onBackPressed();
                finish();
            }
        }
    }

    private class getCounter extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls)
        {
            try {
                Index.jsonObject = new JSONObject();
                Index.jsonObject.put("user_nomor", Index.globalfunction.getShared("user", "nomor", ""));
                Index.jsonObject.put("user_id", Index.globalfunction.getShared("user","id",""));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return Index.globalfunction.executePost(urls[0], Index.jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("result", result);
            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = jsonarray.length() - 1; i >= 0; i--) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(!obj.has("query")){
                            String counterelevasi = obj.getString("elevasi_baru");
                            String counterorder = obj.getString("order_baru");
                            String counterorderapproved = obj.getString("order_approved_baru");
                            String counterorderdisapproved = obj.getString("order_disapproved_baru");
                            String counterprivatemessage = obj.getString("private_message");

                            approveElevasi.setGravity(Gravity.CENTER_VERTICAL);
                            approveElevasi.setTypeface(null, Typeface.BOLD);
                            approveElevasi.setTextColor(getResources().getColor(R.color.colorPrimary));
                            approveElevasi.setText(counterelevasi);

                            approveOrder.setGravity(Gravity.CENTER_VERTICAL);
                            approveOrder.setTypeface(null, Typeface.BOLD);
                            approveOrder.setTextColor(getResources().getColor(R.color.colorPrimary));
                            approveOrder.setText(counterorder);

                            approvedOrder.setGravity(Gravity.CENTER_VERTICAL);
                            approvedOrder.setTypeface(null, Typeface.BOLD);
                            approvedOrder.setTextColor(getResources().getColor(R.color.colorPrimary));
                            approvedOrder.setText(counterorderapproved);

                            disapprovedOrder.setGravity(Gravity.CENTER_VERTICAL);
                            disapprovedOrder.setTypeface(null, Typeface.BOLD);
                            disapprovedOrder.setTextColor(getResources().getColor(R.color.colorPrimary));
                            disapprovedOrder.setText(counterorderdisapproved);

                            privateMessage.setGravity(Gravity.CENTER_VERTICAL);
                            privateMessage.setTypeface(null, Typeface.BOLD);
                            privateMessage.setTextColor(getResources().getColor(R.color.colorPrimary));
                            privateMessage.setText(counterprivatemessage);
                        }
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private class logout extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                Index.jsonObject = new JSONObject();
                Index.jsonObject.put("nomor", globalfunction.getShared("user", "id", ""));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return Index.globalfunction.executePost(urls[0], Index.jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(obj.getString("success").equals("true")){
                            globalfunction.clearShared("user");

                            hideLoading();

                            Intent intent = new Intent(Index.this, Login.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                            finish();
                        }
                        else
                        {
                            Toast.makeText(getBaseContext(), "Logout Failed", Toast.LENGTH_LONG).show();
                            hideLoading();
                        }
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
                Toast.makeText(getBaseContext(), "Logout Failed", Toast.LENGTH_LONG).show();
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
        Index.loadingDialog.dismiss();
    }

    private void showLoading()
    {
        Index.loadingDialog = new ProgressDialog(this);
        Index.loadingDialog.setMessage("Loading");
        Index.loadingDialog.show();
    }
}
