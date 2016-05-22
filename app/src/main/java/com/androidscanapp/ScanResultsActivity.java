package com.androidscanapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.androidscanapp.data.LinkData;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScanResultsActivity extends Activity {

    private static final String TAG = "ScanResultsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_results);

        String data = getIntent().getStringExtra("scan_result");

        Log.d(TAG, "Received scan data: " + data);

        final String packageName = getIntent().getStringExtra("packageName");

        ListView lv = (ListView) findViewById(R.id.results_listview);

        View resultsContainer = findViewById(R.id.urlsContainer);

        TextView noDataWarning = (TextView) findViewById(R.id.results_no_data_warning);

        TextView appNameTextView = (TextView) findViewById(R.id.results_appName);

        if (data != null && data.length() > 0){
            Gson gson = new Gson();
            LinkData[] links = gson.fromJson(data, LinkData[].class);
            List<Map<String,?>> adapterData = new ArrayList<>(links.length);

            int appStatus = 1;

            for(LinkData l:links){
                Map<String, Object> dataMap = new HashMap<>();

                if (l.id == -2 || l.id == -1) {
                    appStatus = -2;
                }
                if (l.id == -3) {
                    appStatus = -3;
                }

                if (l.suspect){
                    dataMap.put("icon", R.drawable.alert);
                    if (appStatus >= 0){
                        appStatus = 2;
                    }
                }
                else{
                    dataMap.put("icon", R.drawable.safe);
                }
                dataMap.put("url", l.url);
                adapterData.add(dataMap);
            }

            SharedPreferences sharedpreferences = this.getApplicationContext().getApplicationContext().getSharedPreferences(Util.STATUS_PREFS_FILE, Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = sharedpreferences.edit();

            editor.putInt(packageName, appStatus);
            editor.commit();

            SimpleAdapter simpleAdapter = new SimpleAdapter(this, adapterData, R.layout.scan_result_row, new String[]{"icon","url","status_icon"}, new int[]{R.id.scan_result__row_icon,R.id.scan_result__row_name,R.id.app_data_row_status});

            resultsContainer.setVisibility(View.VISIBLE);
            noDataWarning.setVisibility(View.GONE);
            lv.setAdapter(simpleAdapter);

            try {

                PackageInfo packageInfo = this.getPackageManager().getPackageInfo(packageName, 0);
                int versionCode = packageInfo.versionCode;
                String versionName = packageInfo.versionName;

                String appName = Util.getAppName(this, packageName);

                appNameTextView.setText(appName);

                appNameTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Util.startApplicationDetailsActivity(ScanResultsActivity.this, packageName);
                    }
                });

                Log.d("ApplicationBroadcast", "VersionCode: " + versionCode + " VersionName: " + versionName);
            } catch (PackageManager.NameNotFoundException e) {
                Log.d("ApplicationBroadcast","Error getting package version code", e);
            }
        }
        else{
            resultsContainer.setVisibility(View.GONE);
            noDataWarning.setVisibility(View.VISIBLE);
        }
    }


}
