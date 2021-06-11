package com.study.utils.log;

import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Logger {
    static final int MAX_LINE_CHARACTERS = 2048;

    public void d(String classTag, String msg) {
        if (!LogFactory.sIsDebug) {
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
                Log.d(LogFactory.APP_TAG, getLogMsgWithClassTag(classTag, sb.toString()));
                return;
            }
            for (String s : splitMsg) {
                Log.d(LogFactory.APP_TAG, getLogMsgWithClassTag(classTag, s));
            }
        }
    }

    public void d(Object... msg) {
        if (!LogFactory.sIsDebug) {
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

    public void i(String classTag, String msg) {
        List<String> splitMsg = splitPrint(msg);
        for (String s : splitMsg) {
            Log.i(LogFactory.APP_TAG, getLogMsgWithClassTag(classTag, s));
        }
    }

    public void w(String classTag, String msg) {
        List<String> splitMsg = splitPrint(msg);
        for (String s : splitMsg) {
            Log.w(LogFactory.APP_TAG, getLogMsgWithClassTag(classTag, s));
        }
    }

    public void e(String classTag, String msg) {
        List<String> splitMsg = splitPrint(msg);
        for (String s : splitMsg) {
            Log.e(LogFactory.APP_TAG, getLogMsgWithClassTag(classTag, s));
        }
    }

    public void e(String classTag, String msg, Throwable e) {
        List<String> splitMsg = splitPrint(String.format(Locale.getDefault(), "%s: %s", msg, e.getMessage()));
        for (String s : splitMsg) {
            Log.e(LogFactory.APP_TAG, getLogMsgWithClassTag(classTag, s));
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

    private String getLogMsgWithClassTag(String classTag, String msg) {
        if (TextUtils.isEmpty(classTag)) {
            return msg;
        }
        return String.format(Locale.getDefault(), "%s: %s", classTag, msg);
    }
}
