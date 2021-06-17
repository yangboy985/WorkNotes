package com.study.camera.camera;

public class ResizeUtil {
    private static final String TAG = "ResizeUtil";

    public static int[] getProportionalScale(int containerWidth, int containerHeight, int targetWidth, int targetHeight) {
        // 以target的宽高比为标准进行等比缩放。这里不管横竖屏的差异，在调用之前处理好
        if (containerWidth <= 0 || targetWidth <= 0) {
            return new int[]{containerWidth, containerHeight};
        }
        float targetRate = targetHeight * 1f / targetWidth;
        float containerRate = containerHeight * 1f / containerWidth;
        int pxWidth = containerWidth;
        int pxHeight = containerHeight;
        if (containerRate > targetRate) {
            pxHeight = (int) (pxWidth * targetRate);
        } else {
            pxWidth = (int) (pxHeight / targetRate);
        }
        return new int[]{pxWidth, pxHeight};
    }
}
