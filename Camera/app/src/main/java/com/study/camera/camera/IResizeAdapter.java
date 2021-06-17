package com.study.camera.camera;

import android.view.TextureView;

public interface IResizeAdapter {
    int[] getRealSize(TextureView textureView, int contentWidth, int contentHeight);
}
