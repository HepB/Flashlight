package com.github.hepb.flashlight.flashlight;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import timber.log.Timber;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CAMERA_PERMISSIONS = 50;
    private static final String[] CAMERA_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!hasCameraPermission()) {
            Timber.i("On request permissions");
            ActivityCompat.requestPermissions(this, CAMERA_PERMISSIONS, REQUEST_CAMERA_PERMISSIONS);
        } else {
            switchFlashLight();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Timber.i(Integer.toString(requestCode));
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSIONS:
                if (hasCameraPermission()) {
                    switchFlashLight();
                } else {
                    Toast.makeText(this, getString(R.string.permission_denied_explanation), Toast.LENGTH_LONG).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        finish();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void switchFlashLight() {
        boolean isFlashlightServiceStarted = isMyServiceRunning(FlashlightService.class);
        Intent intent = new Intent(this, FlashlightService.class);
        intent.putExtra(FlashlightService.EXTRA_IS_DONE, isFlashlightServiceStarted);
        if (isFlashlightServiceStarted) {
            Timber.i("Stopping service");
            stopService(intent);
        } else {
            Timber.i("Starting service");
            startService(intent);
        }
    }

    private boolean hasCameraPermission() {
        int result = ContextCompat.checkSelfPermission(this, CAMERA_PERMISSIONS[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }
}
