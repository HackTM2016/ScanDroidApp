package com.androidscanapp;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import java.util.List;

public class Util {


    public static String getAppName(Context context, String packageName){
        PackageManager pm = context.getPackageManager();
        ApplicationInfo ai = null;
        try {
            ai = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            return pm.getApplicationLabel(ai).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
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

    public static List<ApplicationInfo> getAllInstalledPackages(Context context){
        final PackageManager pm = context.getPackageManager();
        //get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        return packages;
    }


}
