/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.plugin.lightbluebean;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * This class is a implementation of Settings activity. <br>
 * This activity is able to be launched by Kadecot devices tab.
 */
public class SettingsActivity extends Activity {

    private final static int SDKVER_MARSHMALLOW = 23;
    private final static int REQUEST_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        getFragmentManager().beginTransaction()
                .add(R.id.container, SettingsFragment.newInstance())
                .commit();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(Build.VERSION.SDK_INT >= SDKVER_MARSHMALLOW){
            requestBlePermission();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @TargetApi(SDKVER_MARSHMALLOW)
    private void requestBlePermission() {
        if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            // no permission
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION
            },REQUEST_PERMISSIONS);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 権限リクエストの結果を取得する.
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "Succeed", Toast.LENGTH_SHORT).show();
            } else {
                // failed
                //Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
            }
        }else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        finish();
    }

    public static final class SettingsFragment extends PreferenceFragment {

        public static SettingsFragment newInstance() {
            return new SettingsFragment();
        }

        public SettingsFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.sample_preferences);
        }
    }
}
