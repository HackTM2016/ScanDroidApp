package com.androidscanapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.androidscanapp.data.LinkData;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.Random;

import cz.msebera.android.httpclient.Header;

public class ApplicationBroadcastService extends BroadcastReceiver {
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        String packageName = intent.getDataString();

        String[] tokens = packageName.split(":");
        if (tokens != null && tokens.length == 2){
            packageName = tokens[1];
        }

        packageName = "com.rovio.popcorn";

        Log.d("ApplicationBroadcast", "Action: " + action + " package: " + packageName);

        int versionCode;
        String versionName;

        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            versionCode = packageInfo.versionCode;
            versionName = packageInfo.versionName;
            Log.d("ApplicationBroadcast", "VersionCode: " + versionCode + " VersionName: " + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("ApplicationBroadcast","Error getting package version code", e);
        }

        if (!action.contains("PACKAGE_REMOVED")){
            RequestParams params = new RequestParams();

            String linkURL = "api/links/packageName/"+packageName;

            AndroidScanHttpClient.get(linkURL, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String data = new String(responseBody);

                    Log.d("ApplicationBroadcast", "StatusCode: " + statusCode + " Responded: " + data);

                    Gson gson = new Gson();
                    LinkData[] links = gson.fromJson(data, LinkData[].class);
                    for(LinkData l:links){
                        Log.d("ApplicationBroadcast","Link "+l.toString());
                    }

                    sendNotification(context);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.d("ApplicationBroadcast", "FAILURE! StatusCode: " + statusCode);
                }
            });
        }
    }


    private void sendNotification(Context context){
        Log.d("ApplicationBroadcast", "Sending notification");
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.icon)
                        .setContentTitle("ScanDroid status: ")
                        .setContentText("Hello World!  sddsggdgsdsgdgsgdsdg");

        Intent intent = new Intent(context, ScanResultsActivity.class);

        //PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, 0);



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

        mNotificationManager.notify(0, notification);
    }
}
