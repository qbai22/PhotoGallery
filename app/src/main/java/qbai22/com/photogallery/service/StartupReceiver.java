package qbai22.com.photogallery.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import qbai22.com.photogallery.utils.QueryPreferences;


public class StartupReceiver extends BroadcastReceiver{
    
    public static final String TAG = "StartupReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "received broadcast intent"+ intent.getAction());

        boolean isOn = QueryPreferences.isAlarmOn(context);
        PollService.setServiceAlarm(context, isOn);
    }
}
