package com.androidscanapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.androidscanapp.data.LinkData;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import cz.msebera.android.httpclient.Header;

public class ApplicationBroadcastService extends BroadcastReceiver {

    int notificationCounter = 0;

    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        final String packageName = extractPackageName(intent.getDataString());

        Log.d("ApplicationBroadcast", "Action: " + action + " package: " + packageName);

        int versionCode;
        String versionName;
        String appName = "";

        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            versionCode = packageInfo.versionCode;
            versionName = packageInfo.versionName;

            appName = Util.getAppName(context, packageName);

            Log.d("ApplicationBroadcast", "VersionCode: " + versionCode + " VersionName: " + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("ApplicationBroadcast","Error getting package version code", e);
        }

        final String finalAppName = appName;

        if (!action.contains("PACKAGE_REMOVED") && !action.contains("PACKAGE_REPLACED")){
            RequestParams params = new RequestParams();

            String linkURL = "api/links/packageName/"+packageName;

            AndroidScanHttpClient.get(linkURL, params, new AsyncHttpResponseHandler() {
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

                    for(LinkData l:links){
                        if (l.id == -2 || l.id == -1){
                            sendAppNeedsScanWarning(intent, context, finalAppName);
                            return;
                        }
                        if (l.id == -3){
                            sendScanFailedWarning(intent, context, finalAppName);
                            return;
                        }
                        if (l.suspect){
                            safe = false;
                        }
                        Log.d("ApplicationBroadcast","Link "+l.toString());
                    }

                    showScanResultNotification(safe, intent, context, finalAppName);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.d("ApplicationBroadcast", "FAILURE! StatusCode: " + statusCode);
                    Intent intent = new Intent(context, MainActivity.class);
                    sendNotification(context,R.drawable.icon, getBitmap(R.drawable.warning, context), "ScanDroid server has issues.", "Please try to scan the app later.", intent);
                }
            });
        }
    }


    private void showScanResultNotification(boolean safe, Intent intent, Context context, String finalAppName) {
        int icon = safe ? R.drawable.safe : R.drawable.alert;

        Bitmap bitmapIcon = getBitmap(icon, context);

        String title = finalAppName+" is ";
        String titleResult = safe ? "safe" : "not safe!";
        title = title + titleResult;

        String message = "Tap to see more details about "+finalAppName;

        sendNotification(context, R.drawable.icon, bitmapIcon, title, message, intent);
    }

    private void sendAppNeedsScanWarning(Intent intent, Context context, String finalAppName) {
        int icon = R.drawable.warning;
        Bitmap bitmapIcon = getBitmap(icon, context);
        String title = finalAppName+" need scanning";
        String message = "Please wait for scanning.";
        sendNotification(context, R.drawable.warning, bitmapIcon, title, message, intent);
    }

    private void sendScanFailedWarning(Intent intent, Context context, String finalAppName) {
        int icon = R.drawable.warning;
        Bitmap bitmapIcon = getBitmap(icon, context);
        String title = "Failed to scan "+finalAppName;
        String message = "Scan process failed.";
        sendNotification(context, R.drawable.warning, bitmapIcon, title, message, intent);
    }

    private String extractPackageName(String packageName) {
        String[] tokens = packageName.split(":");
        if (tokens != null && tokens.length == 2){
            packageName = tokens[1];
        }
        return packageName;
    }

    private Bitmap getBitmap(int icon, Context context) {
        Drawable drawable = context.getResources().getDrawable(icon);
        return ((BitmapDrawable)drawable).getBitmap();
    }


    private void sendNotification(Context context, int drawable,Bitmap icon, String title, String message,Intent intent){
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
