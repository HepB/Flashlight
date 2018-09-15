package com.github.hepb.flashlight.flashlight;

import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.annotation.Nullable;
import android.util.Log;

public class FlashlightService extends IntentService {
    private static final String TAG = "FlashlightService";
    public static final String EXTRA_IS_DONE = "isDone";

    private Camera cam;
    private volatile boolean isDone;

    public FlashlightService() {
        super("Flashlight service started.");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        isDone = intent != null && intent.getBooleanExtra(EXTRA_IS_DONE, true);
        synchronized (this) {
            if (isDone) {
                flashLightOff();
            } else {
                flashLightOn();
            }
        }
        while (!isDone);
    }

    public void flashLightOn() {
        Log.i(TAG, "Flashlight switch on");
        try {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                cam = Camera.open();
                Camera.Parameters p = cam.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                cam.setParameters(p);
                cam.startPreview();
            }
        } catch (Exception e) {
            Log.e(TAG, "Еrror: ", e);
            isDone = true;
        }
    }

    public void flashLightOff() {
        Log.i(TAG, "Flashlight switch off");
        try {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                cam.stopPreview();
                cam.release();
            }
        } catch (Exception e) {
            Log.e(TAG, "Еrror: ", e);
        } finally {
            isDone = true;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        flashLightOff();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
