package com.androidscanapp;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;


public class AppScanRequester {

        public static void requestScan(final Context context,final String packageName, final AsyncHttpResponseHandler requestHandler) {
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

            RequestParams params = new RequestParams();

            String linkURL = "api/links/packageName/"+packageName;

            AndroidScanHttpClient.get(linkURL, params, requestHandler);

        }
}
