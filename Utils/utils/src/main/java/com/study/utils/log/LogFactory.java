package com.study.utils.log;

public class LogFactory {
    static final String APP_TAG = "APP_TAG";

    public static boolean sIsDebug = true;
    public static boolean sIsDebugEnableWrap = true;

    private LogFactory() {
    }

    public static NLogger getLogger(Class<?> clz) {
        if (clz == null) {
            return new NLogger(APP_TAG, "", sIsDebug);
        }
        return new NLogger(APP_TAG, clz.getSimpleName(), sIsDebug);
    }

    public static NLogger getLogger(String tag) {
        return new NLogger(APP_TAG, tag, sIsDebug);
    }
}
