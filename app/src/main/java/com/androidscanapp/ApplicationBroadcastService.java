package com.androidscanapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;

public class ApplicationBroadcastService extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String packageName = intent.getDataString();
        Log.d("ApplicationBroadcast", "Action: " + action + " package: " + packageName);
        if (!action.contains("PACKAGE_REMOVED")){
            RequestParams params = new RequestParams();
            params.add("packageName", packageName);



            String linkURL = "api/links/packageName/";

            AndroidScanHttpClient.post("links", params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Log.d("ApplicationBroadcast", "StatusCode: "+statusCode+" Responded: "+responseBody.toString());
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.d("ApplicationBroadcast", "FAILURE! StatusCode: "+statusCode+" Responded: "+responseBody.toString());
                }
            });
        }

    }
}
