package com.androidscanapp;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.androidscanapp.data.LinkData;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        setupListViewWithAllInstalledApps();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void setupListViewWithAllInstalledApps(){
        ListView listView = (ListView) findViewById(R.id.main_apps_listView);

        List<ApplicationInfo> installedApps = Util.getAllInstalledPackages(this);

        final List<Map<String, Object>> adapterData = new ArrayList<>(installedApps.size());

        PackageManager packageManager = this.getPackageManager();

        for(ApplicationInfo applicationInfo:installedApps){
            Map<String, Object> objectMap = new HashMap<>();
            String packageName = applicationInfo.packageName;
            String appName = Util.getAppName(this, packageName);
            objectMap.put("appName", appName);
            objectMap.put("packageName", packageName);

            Drawable icon = getAppIcon(packageManager, packageName);

            objectMap.put("icon", icon);
            adapterData.add(objectMap);
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(this, adapterData, R.layout.app_data_row, new String[]{"icon","appName"}, new int[]{R.id.app_data_row_icon, R.id.app_data_row_name});

        simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view.getId() == R.id.app_data_row_icon) {
                    ImageView imageView = (ImageView) view;
                    Drawable drawable = (Drawable) data;
                    imageView.setImageDrawable(drawable);
                    return true;
                }
                return false;
            }
        });

        listView.setAdapter(simpleAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> item = adapterData.get(position);
                final String packageName = (String) item.get("packageName");
                AppScanRequester.requestScan(MainActivity.this, packageName, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String data = new String(responseBody);

                        Log.d("ApplicationBroadcast", "StatusCode: " + statusCode + " Responded: " + data);

                        Intent intent = new Intent(MainActivity.this, ScanResultsActivity.class);
                        intent.putExtra("scan_result", data);
                        intent.putExtra("packageName", packageName);

                        MainActivity.this.startActivity(intent);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        //// TODO: 5/22/2016 show error notification
                    }
                });
            }
        });
    }

    @Nullable
    private Drawable getAppIcon(PackageManager packageManager, String packageName) {
        Drawable icon = null;
        try {
            icon = packageManager.getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return icon;
    }


}
