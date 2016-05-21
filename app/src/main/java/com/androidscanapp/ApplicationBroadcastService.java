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

public class ApplicationBroadcastService extends BroadcastReceiver {

    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        final String packageName = extractPackageName(intent.getDataString());

        Log.d("ApplicationBroadcast", "Action: " + action + " package: " + packageName);

        if (!action.contains("PACKAGE_REMOVED") && !action.contains("PACKAGE_REPLACED")) {
            AppScanRequester.requestScan(context, packageName);
        }
    }

    private String extractPackageName(String packageName) {
        String[] tokens = packageName.split(":");
        if (tokens != null && tokens.length == 2) {
            packageName = tokens[1];
        }
        return packageName;
    }
}
