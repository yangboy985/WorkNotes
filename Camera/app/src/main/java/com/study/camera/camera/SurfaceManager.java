package com.study.camera.camera;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;

import androidx.annotation.MainThread;

public class SurfaceManager implements TextureView.SurfaceTextureListener {
    private static final String TAG = "SurfaceManager";

    private TextureView mTextureView;
    private Surface mSurface;
    private SurfaceEventListener mListener;
    private IResizeAdapter mAdapter = new ParentResizeAdapter();

    private final int[] mContentSize = new int[2];
    private boolean isSurfacePrepared = false;
    private boolean isInit = false;

    @MainThread
    public SurfaceManager(TextureView textureView) {
        mTextureView = textureView;
        init();
    }

    private void init() {
        mTextureView.setSurfaceTextureListener(this);
        mTextureView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> updateSurfaceSize());
        if (mTextureView.isAvailable()) {
            mSurface = new Surface(mTextureView.getSurfaceTexture());
            isSurfacePrepared = true;
            if (mListener != null) {
                mListener.onSurfacePrepared(mSurface);
            }
        }
    }

    public void setSurfaceEventListener(SurfaceEventListener listener) {
        this.mListener = listener;
        if (isSurfacePrepared) {
            if (mListener != null && mSurface != null) {
                mListener.onSurfacePrepared(mSurface);
            }
        }
    }

    public boolean isSurfacePrepared() {
        return isSurfacePrepared;
    }

    public void setResizeAdapter(IResizeAdapter adapter) {
        this.mAdapter = adapter;
    }

    public void uploadVideoSize(final int width, final int height) {
        if (width == 0 || height == 0) {
            return;
        }
        if (width == mContentSize[0] || height == mContentSize[1]) {
            return;
        }
        mContentSize[0] = width;
        mContentSize[1] = height;
        mTextureView.post(this::updateSurfaceSize);
    }

    private void updateSurfaceSize() {
        if (mAdapter == null) {
            return;
        }
        int[] res = mAdapter.getRealSize(mTextureView, mContentSize[0], mContentSize[1]);
        if (res == null) {
            return;
        }
        ViewGroup.LayoutParams lp = mTextureView.getLayoutParams();
        if (lp != null) {
            lp.width = res[0];
            lp.height = res[1];
            mTextureView.setLayoutParams(lp);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (surface == null) {
            return;
        }
        if (mSurface != null) {
            mSurface.release();
        }
        mSurface = new Surface(surface);
        updateSurfaceSize();
        isSurfacePrepared = true;
        if (isInit) {
            if (mListener != null) {
                mListener.onSurfaceUpdated(mSurface);
            }
        } else {
            isInit = true;
            if (mListener != null) {
                mListener.onSurfacePrepared(mSurface);
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        updateSurfaceSize();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        isSurfacePrepared = false;
        if (mListener != null) {
            mListener.onSurfaceDestroyed(mSurface);
        }
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    public void releaseSurfaceManager() {
        isSurfacePrepared = false;
        isInit = false;
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        mTextureView = null;
    }

    public interface SurfaceEventListener {
        void onSurfacePrepared(Surface surface);

        void onSurfaceUpdated(Surface surface);

        void onSurfaceDestroyed(Surface surface);
    }

    public static class ParentResizeAdapter implements IResizeAdapter {
        @Override
        public int[] getRealSize(TextureView textureView, int contentWidth, int contentHeight) {
            // 这种不适用parent里面还有其它子view可以挤压TextureView空间的情况
            if (contentWidth == 0 || contentHeight == 0) {
                return null;
            }
            ViewGroup parent = (ViewGroup) textureView.getParent();
            if (parent == null) {
                return null;
            }
            int width = parent.getWidth();
            int height = parent.getHeight();
            if (width == 0 || height == 0) {
                return null;
            }
            return ResizeUtil.getProportionalScale(width, height, contentWidth, contentHeight);
        }
    }
}
