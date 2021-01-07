package com.hrst.silent_update;

import androidx.appcompat.app.AppCompatActivity;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.IOException;

import static com.hrst.silent_update.Utilities.installPackage;
import static com.hrst.silent_update.Utilities.validPermissions;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private final String updatePackagePath = "/sdcard/Download/app-update.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request permissions
        if (validPermissions(this)) {
            Log.i(TAG, "Permissions granted");
        } else {
            Log.w(TAG, "Permissions not granted");
        }
    }

    /**
     * Update App Button callback
     * @param v Button view
     */
    public void updateApp(View v) {
        Log.i(TAG, "Updating app...");
        try {
            installPackage(this, this.getPackageName(), updatePackagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Disable Admin Button callback
     * @param v Button view
     */
    public void disableAdmin(View v) {
        // https://stackoverflow.com/questions/13107986/android-programmatically-remove-my-app-from-device-administrator#13108103
        // https://stackoverflow.com/questions/49128293/how-to-remove-set-device-owner-in-android-dpm
        ComponentName cn = new ComponentName(this.getPackageName(), DeviceAdmin.class.getName());
        DevicePolicyManager dpm = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (dpm.isDeviceOwnerApp(this.getPackageName())) {
            Log.i(TAG, "Disabling device admin");
            dpm.removeActiveAdmin(cn);
            dpm.clearDeviceOwnerApp(this.getPackageName());
        } else {
            Log.w(TAG, "Not a device admin");
        }
    }
}

