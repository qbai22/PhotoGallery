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

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import qbai22.com.photogallery.R;
import qbai22.com.photogallery.model.Flickr;
import qbai22.com.photogallery.network.ApiFactory;
import qbai22.com.photogallery.network.FlickrService;
import qbai22.com.photogallery.screen.PhotoGalleryActivity;
import qbai22.com.photogallery.utils.QueryPreferences;

//This service checks availability of new pictures
//by comparing Id's, and make an notification if id's are different

public class PollService extends IntentService {
    public static final String ACTION_SHOW_NOTIFICATION =
            "qbai22.com.photogallery.SHOW_NOTIFICATION";
    public static final String PERMISSION_PRIVATE =
            "qbai22.com.photogallery.PRIVATE";

    public static final String REQUEST_CODE = "REQUEST CODE";
    public static final int REQUEST_CODE_VALUE = 0;
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

        FlickrService flickrService = ApiFactory.getFlickrService();

        Observable<Flickr> flickrObservable;
        if (query == null || query.equals("")) {
            flickrObservable = flickrService.getPageFlickr(1); // most recent images
        } else {
            flickrObservable = flickrService.searchFlickr(1, query); // most recent + query
        }
        flickrObservable
                .map(flickr -> flickr.photos.photo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    if (list != null) {
                        String resultId = list.get(0).getId();
                        if (!lastResultId.equals(resultId)) {
                            showBackgroundNotification(REQUEST_CODE_VALUE);
                        }
                    }
                }, t -> Log.e(TAG, "onHandleIntent: ", t));
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
