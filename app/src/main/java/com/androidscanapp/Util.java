package com.androidscanapp;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

/**
 * Created by Adrian on 5/21/2016.
 */
public class Util {


    public static String getAppName(Context context, String packageName) throws PackageManager.NameNotFoundException {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo ai = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        return pm.getApplicationLabel(ai).toString();
    }

    public static void startApplicationDetailsActivity(Activity activity,String packageName) {
        try {
            //Open the specific App Info page:
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + packageName));
            activity.startActivity(intent);

        } catch ( ActivityNotFoundException e ) {
            e.printStackTrace();
        }
    }
}
