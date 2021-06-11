package com.study.utils.log;

import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NLogger {
    private static final int MAX_LINE_CHARACTERS = 2048;

    private final String TAG;
    private final String CLASS_TAG;
    private boolean sIsDebug;

    NLogger(String appTag, String classTag, boolean isDebug) {
        this.TAG = appTag;
        this.CLASS_TAG = classTag;
        this.sIsDebug = isDebug;
    }

    public NLogger setDebug() {
        // 可能需要局部debug
        this.sIsDebug = true;
        return this;
    }

    public void d(String msg) {
        if (!sIsDebug) {
            return;
        }
        List<String> splitMsg = splitPrint(msg);
        if (splitMsg != null) {
            if (LogFactory.sIsDebugEnableWrap && !splitMsg.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append(splitMsg.get(0));
                for (int i = 1; i < splitMsg.size(); i++) {
                    sb.append("\n").append(splitMsg.get(i));
                }
                Log.d(TAG, getLogMsgWithClassTag(sb.toString()));
                return;
            }
            for (String s : splitMsg) {
                Log.d(TAG, getLogMsgWithClassTag(s));
            }
        }
    }

    public void d(Object... msg) {
        if (!sIsDebug) {
            return;
        }
        d(parseStringLog(msg));
    }

    /**
     * 键值对打印
     *
     * @param methodName 方法名
     * @param params     要打印的键值对
     */
    public void dMKV(String methodName, Object... params) {
        if (params == null || params.length % 2 != 0) {
            return;
        }
        d(combineKeyVal(methodName, params));
    }

    /**
     * 键值对打印
     *
     * @param params 要打印的键值对
     */
    public void dKV(Object... params) {
        if (params == null || params.length % 2 != 0) {
            return;
        }
        d(combineKeyVal(null, params));
    }

    public void i(String msg) {
        List<String> splitMsg = splitPrint(msg);
        for (String s : splitMsg) {
            Log.i(TAG, getLogMsgWithClassTag(s));
        }
    }

    public void w(String msg) {
        List<String> splitMsg = splitPrint(msg);
        for (String s : splitMsg) {
            Log.w(TAG, getLogMsgWithClassTag(s));
        }
    }

    public void e(String msg) {
        List<String> splitMsg = splitPrint(msg);
        for (String s : splitMsg) {
            Log.e(TAG, getLogMsgWithClassTag(s));
        }
    }

    public void e(String msg, Throwable e) {
        List<String> splitMsg = splitPrint(String.format(Locale.getDefault(), "%s: %s", msg, e.getMessage()));
        for (String s : splitMsg) {
            Log.e(TAG, getLogMsgWithClassTag(s));
        }
    }

    private String parseStringLog(Object... msg) {
        if (msg == null || msg.length == 0) {
            return null;
        }
        StringBuilder strMsg = new StringBuilder();
        for (Object s : msg) {
            if (s == null) {
                strMsg.append("null");
            } else {
                strMsg.append(s.toString());
            }
        }
        return strMsg.toString();
    }

    private String combineKeyVal(String methodName, Object... params) {
        StringBuilder strLog = new StringBuilder();
        if (!TextUtils.isEmpty(methodName)) {
            strLog.append(methodName).append(": ");
        }
        int len = params.length;
        for (int i = 0; i < len; i += 2) {
            if (!(params[i] instanceof String)) {
                return null;
            }
            if (i != 0) {
                strLog.append(", ");
            }
            strLog.append("[").append(params[i].toString()).append("]: ");
            if (params[i + 1] == null) {
                strLog.append("null");
            } else {
                strLog.append(params[i + 1].toString());
            }
        }
        return strLog.toString();
    }

    private List<String> splitPrint(String msg) {
        if (msg == null) {
            return null;
        }
        List<String> splitMsg = new ArrayList<>();
        if (msg.length() <= MAX_LINE_CHARACTERS) {
            splitMsg.add(msg);
        } else {
            int len = msg.length();
            int lines = (len + MAX_LINE_CHARACTERS - 1) / MAX_LINE_CHARACTERS;
            int start = 0;
            for (int i = 0; i < lines; i++) {
                start = MAX_LINE_CHARACTERS * i;
                splitMsg.add(msg.substring(start, start + MAX_LINE_CHARACTERS));
            }
        }
        return splitMsg;
    }

    private String getLogMsgWithClassTag(String msg) {
        if (TextUtils.isEmpty(CLASS_TAG)) {
            return msg;
        }
        return String.format(Locale.getDefault(), "%s: %s", CLASS_TAG, msg);
    }
}
