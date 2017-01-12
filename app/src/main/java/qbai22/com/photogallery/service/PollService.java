package qbai22.com.photogallery.service;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.List;

import qbai22.com.photogallery.utils.QueryPreferences;
import qbai22.com.photogallery.R;
import qbai22.com.photogallery.model.Flickr;
import qbai22.com.photogallery.model.GalleryItem;
import qbai22.com.photogallery.network.FlickrService;
import qbai22.com.photogallery.screen.PhotoGalleryActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PollService extends IntentService {
    public static final String ACTION_SHOW_NOTIFICATION =
            "qbai22.com.photogallery.SHOW_NOTIFICATION";
    public static final String PERMISSION_PRIVATE =
            "qbai22.com.photogallery.PRIVATE";
    //AlarmManager.INTERVAL_FIFTEEN_MINUTES;
    public static final String REQUEST_CODE = "REQUEST CODE";
    public static final String NOTIFICATION = "NOTIFICATION";
    private static final String TAG = "PollSERVICE";
    private static final long POLL_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;


    public PollService() {
        super(TAG);
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, PollService.class);
    }

    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent i = PollService.newIntent(context);
        //by calling PendingIntent.getService(…),
        // you say to the OS, “Please remember that I want to send this intent with startService(Intent).
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, i, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (isOn) {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(), POLL_INTERVAL, pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
        QueryPreferences.setAlarmOn(context, isOn);
    }

    public static boolean isServiceAlarmOn(Context context) {
        Intent i = PollService.newIntent(context);
        PendingIntent pendingIntent = PendingIntent
                .getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pendingIntent != null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (!isNetworkAvailableAndConnected()) {
            return;
        }

        String query = QueryPreferences.getStoredQuery(this);
        String lastResultId = QueryPreferences.getLastResultId(this);

        FlickrService flickrService = FlickrService.retrofit
                .create(FlickrService.class);
        Call<Flickr> call;
        if (query == null || query.equals("")) {
            call = flickrService.getPageFlickr(1); // most recent images
        } else {
            call = flickrService.searchFlickr(1, query); // most recent + query
        }
        call.enqueue(new Callback<Flickr>() {
            @Override
            public void onResponse(Call<Flickr> call, Response<Flickr> response) {
                List<GalleryItem> responseList = response.body().photos.photo;
                if (responseList.size() == 0) {
                    return;
                }
                String resultId = responseList.get(0).getId();
                if (lastResultId.equals(resultId)) {
                    Log.e(TAG, "got an old result  " + lastResultId);
                } else {
                    Log.e(TAG, "got a new result  " + resultId);
                    showBackgroundNotification(0);

                }
                QueryPreferences.setLastResultId(PollService.this, resultId);

            }

            @Override
            public void onFailure(Call<Flickr> call, Throwable t) {
                Log.e(TAG, t.toString());
            }
        });
    }

    private void showBackgroundNotification(int requestCode) {

        Resources resources = getResources();
        Intent i = PhotoGalleryActivity.newIntent(this);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setTicker(resources.getString(R.string.new_pictures_title))
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(resources.getString(R.string.new_pictures_title))
                .setContentText(resources.getString(R.string.new_pictures_text))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        Intent broadcastIntent = new Intent(ACTION_SHOW_NOTIFICATION);
        broadcastIntent.putExtra(REQUEST_CODE, requestCode);
        broadcastIntent.putExtra(NOTIFICATION, notification);
        sendOrderedBroadcast(broadcastIntent, PERMISSION_PRIVATE, null, null,
                Activity.RESULT_OK, null, null);
    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable &&
                cm.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }
}
