package com.steganowork.morsecode;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.util.Log;

public class MosLight {
    private String TAG = "MosLight";
    private String cameraId;
    private CameraManager manager;
    private boolean isFlashAvailable;

    MosLight(Context context) {
        // 플래시 기능 확인
        isFlashAvailable = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        Log.i(TAG, "isFlashAvailable : " + isFlashAvailable);

        if (isFlashAvailable) {  // 플래시 기능이 있으면 카메라 매니저 확인
            manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            cameraId = getBackFacingCameraId(manager);
            Log.i(TAG, "Can Use Camera");
        }
    }

    private String getBackFacingCameraId(CameraManager cManager) {
        try {
            for (final String cameraId : cManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cManager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cOrientation == CameraCharacteristics.LENS_FACING_BACK) return cameraId;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void LightStart(long[] customTimings, int[] customOnOff, int vibTermTime) {
        try {
            if(isFlashAvailable) {  // 플래시 기능이 있을 때 접근시 실행
                Log.i(TAG, "Light On");
                cameraId = getBackFacingCameraId(manager);
                try {
                    int count = 0;
                    for (long i : customTimings) {
                        if (customOnOff[count] == 1) {
                            manager.setTorchMode(cameraId, true);
                            Thread.sleep(i);
                        } else if (customOnOff[count] == 0) {
                            manager.setTorchMode(cameraId, false);
                            Thread.sleep(i);
                        }
                        manager.setTorchMode(cameraId, false);
                        Thread.sleep(vibTermTime);
                        count++;
                    }
                    manager.setTorchMode(cameraId, false);  // 플래시 끄는걸로 마무리
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {  // 플래시 기능이 없을 때 접근시 실행
                Log.i(TAG, "Light 기능 없음");
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
