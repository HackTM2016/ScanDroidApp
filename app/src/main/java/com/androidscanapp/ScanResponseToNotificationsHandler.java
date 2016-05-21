package com.androidscanapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.androidscanapp.data.LinkData;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

public class ScanResponseToNotificationsHandler extends AsyncHttpResponseHandler {

    private static int notificationCounter = 0;

    final Context context;

    final String packageName;

    public ScanResponseToNotificationsHandler(Context context, String packageName){
        this.context = context;
        this.packageName = packageName;
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        String data = new String(responseBody);

        Log.d("ApplicationBroadcast", "StatusCode: " + statusCode + " Responded: " + data);

        Gson gson = new Gson();
        LinkData[] links = gson.fromJson(data, LinkData[].class);
        boolean safe = true;

        Intent intent = new Intent(context, ScanResultsActivity.class);
        intent.putExtra("scan_result", data);
        intent.putExtra("packageName", packageName);

        for (LinkData l : links) {
            if (l.id == -2 || l.id == -1) {
                sendAppNeedsScanWarning(intent, context, packageName);
                return;
            }
            if (l.id == -3) {
                sendScanFailedWarning(intent, context, packageName);
                return;
            }
            if (l.suspect) {
                safe = false;
            }
            Log.d("ApplicationBroadcast", "Link " + l.toString());
        }

        showScanResultNotification(safe, intent, context, packageName);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
        Log.d("ApplicationBroadcast", "FAILURE! StatusCode: " + statusCode);
        Intent intent = new Intent(context, MainActivity.class);
        sendNotification(context, R.drawable.icon, getBitmap(R.drawable.warning, context), "ScanDroid server has issues.", "Please try to scan the app later.", intent);
    }

    private static void showScanResultNotification(boolean safe, Intent intent, Context context, String finalAppName) {
        int icon = safe ? R.drawable.safe : R.drawable.alert;

        Bitmap bitmapIcon = getBitmap(icon, context);

        String title = finalAppName+" is ";
        String titleResult = safe ? "safe" : "not safe!";
        title = title + titleResult;

        String message = "Tap to see more details about "+finalAppName;

        sendNotification(context, R.drawable.icon, bitmapIcon, title, message, intent);
    }

    private static void sendAppNeedsScanWarning(Intent intent, Context context, String finalAppName) {
        int icon = R.drawable.warning;
        Bitmap bitmapIcon = getBitmap(icon, context);
        String title = finalAppName+" need scanning";
        String message = "Please wait for scanning.";
        sendNotification(context, R.drawable.warning, bitmapIcon, title, message, intent);
    }

    private static void sendScanFailedWarning(Intent intent, Context context, String finalAppName) {
        int icon = R.drawable.warning;
        Bitmap bitmapIcon = getBitmap(icon, context);
        String title = "Failed to scan "+finalAppName;
        String message = "Scan process failed.";
        sendNotification(context, R.drawable.warning, bitmapIcon, title, message, intent);
    }


    private static Bitmap getBitmap(int icon, Context context) {
        Drawable drawable = context.getResources().getDrawable(icon);
        return ((BitmapDrawable)drawable).getBitmap();
    }


    private static void sendNotification(Context context, int drawable,Bitmap icon, String title, String message,Intent intent){
        Log.d("ApplicationBroadcast", "Sending notification");
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(drawable)
                        .setLargeIcon(icon)
                        .setContentTitle(title)
                        .setContentText(message);



        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(ScanResultsActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        mNotificationManager.notify(notificationCounter++, notification);
    }
}
