package com.steganowork.morsecode;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.util.Log;

public class MosLight {
    private String TAG = "MosLight";
    private String cameraId;
    private CameraManager manager;
    private int cameraVersion;
    private Context context;
    private Camera camera;

    MosLight(Context context, int cameraVersion) {
        this.context = context;
        this.cameraVersion = cameraVersion;
        Log.i(TAG, "cameraVersion : " + cameraVersion);

        if (cameraVersion == 1) {  // 하위버전에서 카메라1 api 사용
            try {
                camera = Camera.open();
                Log.i(TAG, "Can Use Camera");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (cameraVersion == 2) {  // 상위 버전에서 카메라2 api 사용
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

    public void LightStart(long[] customTimings, int[] customOnOff, int TermTime) {
        try {
            //Log.i(TAG, "Light On");
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {  // 하위버전에서 카메라1 api 사용
                Camera.Parameters parameters = camera.getParameters();
                try {
                    int count = 0;
                    for (long i : customTimings) {
                        if (customOnOff[count] == 1) {
                            parameters.setFlashMode(parameters.FLASH_MODE_TORCH);
                            camera.setParameters(parameters);
                            camera.startPreview();
                            Thread.sleep(i);
                        } else if (customOnOff[count] == 0) {
                            parameters.setFlashMode(parameters.FLASH_MODE_OFF);
                            camera.setParameters(parameters);
                            camera.startPreview();
                            Thread.sleep(i);
                        }
                        parameters.setFlashMode(parameters.FLASH_MODE_OFF);
                        camera.setParameters(parameters);
                        camera.startPreview();
                        Thread.sleep(TermTime);
                        count++;
                    }
                    camera.stopPreview();  // 카메라 사용 끔
                    camera.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
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
                        Thread.sleep(TermTime);
                        count++;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
