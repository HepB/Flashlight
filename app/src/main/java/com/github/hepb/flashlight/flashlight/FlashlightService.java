package com.github.hepb.flashlight.flashlight;

import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.annotation.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

import timber.log.Timber;

public class FlashlightService extends IntentService {
    public static final String EXTRA_IS_DONE = "isDone";

    private Camera cam;

    private AtomicBoolean isDone = new AtomicBoolean();

    public FlashlightService() {
        super("Flashlight service started.");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        isDone.set(intent != null && intent.getBooleanExtra(EXTRA_IS_DONE, true));
        synchronized (this) {
            if (isDone.get()) {
                flashLightOff();
            } else {
                flashLightOn();
            }
        }
        while (!isDone.get());
    }

    public void flashLightOn() {
        Timber.i("Flashlight switch on");
        try {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                cam = Camera.open();
                Camera.Parameters p = cam.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                cam.setParameters(p);
                cam.startPreview();
            } else {
                isDone.set(true);
            }
        } catch (Exception e) {
            Timber.e(e, "Еrror: ");
            isDone.set(true);
        }
    }

    public void flashLightOff() {
        Timber.i("Flashlight switch off");
        try {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                cam.stopPreview();
                cam.release();
            }
        } catch (Exception e) {
            Timber.e(e, "Еrror: ");
        } finally {
            isDone.set(true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        flashLightOff();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
