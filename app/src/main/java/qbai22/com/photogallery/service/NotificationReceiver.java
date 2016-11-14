package qbai22.com.photogallery.service;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

/**
 * Created by qbai on 13.11.2016.
 */

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NOTIFICATION RECEIVER";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive: " + getResultCode());
        if (getResultCode() != Activity.RESULT_OK) {
            //foreground activity cancel broadcast
            return;
        }
        int requestCode = intent.getIntExtra(PollService.REQUEST_CODE, 0);
        Notification notification = intent.getParcelableExtra(PollService.NOTIFICATION);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(requestCode, notification);
    }
}
