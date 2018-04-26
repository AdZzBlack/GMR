package com.adzzblack.gmr;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by antonnw on 23/06/2016.
 */
public class NotificationListenerService extends GcmListenerService {

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        String title = data.getString("title");
        String fragment = data.getString("fragment");
        String nomor = data.getString("nomor");
        String nama = data.getString("nama");

        ShowSimpleNotifications(message,title, fragment, nomor, nama);
    }

    public void ShowSimpleNotifications(String message, String title, String fragment, String nomor, String nama) {
        NotificationCompat.Builder nb = new NotificationCompat.Builder(this);
        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long[] pattern = {500,500,500};

        nb.setSmallIcon(R.drawable.logo);
        nb.setContentTitle(title);
        nb.setContentText(message);
        nb.setSound(uri);
        nb.setPriority(Notification.PRIORITY_HIGH);
        nb.setLights(Color.YELLOW, 500, 500);
        nb.setVibrate(pattern);

        try {
            Index.initializeCountDrawer();
        }
        catch (Exception e)
        {

        }

        if(fragment.equals("PrivateMessage") && nomor.equals(Index.globalfunction.getShared("message","userto_nomor","")))
        {
            Index.globalfunction.setShared("message","private_run_check","1");
        }else {
            Intent resultIntent = new Intent(this, Index.class);
            resultIntent.putExtra("fragment", fragment);
            resultIntent.putExtra("nomor", nomor);
            resultIntent.putExtra("nama", nama);
            TaskStackBuilder TSB = TaskStackBuilder.create(this);
            TSB.addParentStack(Index.class);
            // Adds the Intent that starts the Activity to the top of the stack
            TSB.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    TSB.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            nb.setContentIntent(resultPendingIntent);
            nb.setAutoCancel(true);
            NotificationManager mNotificationManager =
                    (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            mNotificationManager.notify(11221, nb.build());
        }
    }
}
