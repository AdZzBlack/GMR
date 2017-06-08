package com.adzzblack.gmr;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by owners on 15/04/2017.
 */

public class ChangePassword extends Fragment implements View.OnClickListener{
    private EditText et_oldpassword, et_newpassword, et_confirmnewpassword;
    private Button btn_changepassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.change_password, container, false);
        getActivity().setTitle("Change Password");

        et_oldpassword = (EditText) v.findViewById(R.id.et_oldpassword);
        et_newpassword = (EditText) v.findViewById(R.id.et_newpassword);
        et_confirmnewpassword = (EditText) v.findViewById(R.id.et_confirmnewpassword);
        btn_changepassword = (Button) v.findViewById(R.id.btn_changepassword);
        btn_changepassword.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View v) {
        v.startAnimation(Index.buttoneffect);
        if(v.getId() == R.id.btn_changepassword) {
            String newpassword = et_newpassword.getText().toString();
            String confirmnewpassword = et_confirmnewpassword.getText().toString();

            if(!newpassword.equals(confirmnewpassword)){
                Toast.makeText(getContext(), "The new password and the confirm new password do not match.", Toast.LENGTH_LONG).show();
            }else{
                String actionUrl = "Profile/changePassword/";
                new changePassword().execute( actionUrl );
            }
        }
    }

    private class changePassword extends AsyncTask<String, Void, String>{
        String oldpass = et_oldpassword.getText().toString();
        String newpass = et_newpassword.getText().toString();
        String user_id = GlobalFunction.getShared("user", "id", "");

        @Override
        protected String doInBackground(String... urls) {
            try {
                Index.jsonObject = new JSONObject();
                Index.jsonObject.put("user_id", user_id);
                Index.jsonObject.put("oldpass", oldpass);
                Index.jsonObject.put("newpass", newpass);
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
                        String success = obj.getString("success");
                        String pesan = obj.getString("pesan");
                        if(success.equals("true") && pesan.equals("1")){
                            Fragment fragment = new ChangePassword();
                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_container, fragment);
                            transaction.commit();
                            Toast.makeText(getContext(), "Change Password Success", Toast.LENGTH_LONG).show();
                        }
                        else if(success.equals("true") && pesan.equals("2"))
                        {
                            et_oldpassword.setVisibility(View.VISIBLE);
                            et_newpassword.setVisibility(View.VISIBLE);
                            et_confirmnewpassword.setVisibility(View.VISIBLE);
                            btn_changepassword.setVisibility(View.VISIBLE);
                            Toast.makeText(getContext(), "Incorrect Old Password", Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            et_oldpassword.setVisibility(View.VISIBLE);
                            et_newpassword.setVisibility(View.VISIBLE);
                            et_confirmnewpassword.setVisibility(View.VISIBLE);
                            btn_changepassword.setVisibility(View.VISIBLE);
                            Toast.makeText(getContext(), "Change Password Failed", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }catch(Exception e)
            {
                e.printStackTrace();
                et_oldpassword.setVisibility(View.VISIBLE);
                et_newpassword.setVisibility(View.VISIBLE);
                et_confirmnewpassword.setVisibility(View.VISIBLE);
                btn_changepassword.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "Change Password Failed", Toast.LENGTH_LONG).show();
            }
        }
    }
}
