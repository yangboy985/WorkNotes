package com.study.camera.camera;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

public class CameraHelper implements SurfaceManager.SurfaceEventListener {
    private final Context mContext;
    private CameraManager mCameraManager;
    private SurfaceManager mSurfaceManager;

    private CameraState mState = CameraState.INIT;
    private boolean isVisibleToUser = false;

    private String[] mCameraIdArr = null;

    public CameraHelper(@NonNull TextureView textureView) {
        mContext = textureView.getContext().getApplicationContext();
        mSurfaceManager = new SurfaceManager(textureView);
        mSurfaceManager.setSurfaceEventListener(this);
    }

    @MainThread
    public void onResume() {
        isVisibleToUser = true;
        if (mSurfaceManager.isSurfacePrepared()) {
            if (mState == CameraState.PAUSE) {
                setState(CameraState.RESUME);
            } else if (mState == CameraState.INIT) {
                setState(CameraState.PREPARED);
            }
        }
    }

    @MainThread
    public void onPause() {
        isVisibleToUser = false;
        setState(CameraState.PAUSE);
    }

    public void setState(CameraState state) {
        mState = state;
        switch (state) {
            case PREPARED:
            case RESUME:
                openCamera();
                break;
            case PAUSE:
                closeCamera();
                break;
        }
    }

    private void closeCamera() {
    }

    public void openCamera() {
        if (mCameraIdArr == null) {
            try {
                mCameraIdArr = mCameraManager.getCameraIdList();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        if (mCameraIdArr == null) {
            return;
        }
        for (String id : mCameraIdArr) {
            try {
                mCameraManager.getCameraCharacteristics(id);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSurfacePrepared(Surface surface) {
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        if (isVisibleToUser) {
            setState(CameraState.PREPARED);
        }
    }

    @Override
    public void onSurfaceUpdated(Surface surface) {
        if (isVisibleToUser) {
            if (mState == CameraState.PAUSE) {
                setState(CameraState.RESUME);
            } else if (mState == CameraState.INIT) {
                setState(CameraState.PREPARED);
            }
        }
    }

    @Override
    public void onSurfaceDestroyed(Surface surface) {
    }
}
