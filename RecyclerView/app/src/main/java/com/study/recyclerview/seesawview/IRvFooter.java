package com.study.recyclerview.seesawview;

import android.view.View;

import androidx.annotation.NonNull;

public interface IRvFooter {
    @NonNull
    View getView();

    int getMinLoadingTime();

    void onStartLoading();

    void onFinish(boolean isSuccess, boolean noMoreData);
}
