package com.adzzblack.gmr;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by antonnw on 23/06/2016.
 */
public class GCMInstanceIDRegistrationService extends IntentService {

    private String token;

    public GCMInstanceIDRegistrationService(){
        super("GCMInstanceIDRegistrationService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        try {
            InstanceID gcmTokenistanceID = InstanceID.getInstance(this);
            //we will get gcm_defaultSenderId by applying plugin: 'com.google.gms.google-services'
            token = gcmTokenistanceID.getToken("602412022493", GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            updateGCM();
        }
        catch (Exception e)
        {
            Log.i("TAG", "GCM Registration Token: " + e.getMessage().toString());
        }
    }

    private void updateGCM(){
        try {
            Index.jsonObject = new JSONObject();
            Index.jsonObject.put("user_nomor", Index.globalfunction.getShared("user", "id", "0"));
            Index.jsonObject.put("gcmid", token);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String actionUrl = "GCM/updateGCM/";
        Index.globalfunction.executePost(actionUrl, Index.jsonObject);
    }
}