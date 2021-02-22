package com.study.recyclerview.recyclerview;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class RvHolder<T> extends RecyclerView.ViewHolder {
    protected BaseRvAdapter<T> mAdapter;

    public RvHolder(@NonNull View itemView) {
        super(itemView);
    }

    public RvHolder(@NonNull View itemView, BaseRvAdapter<T> adapter) {
        super(itemView);
        mAdapter = adapter;
    }

    public abstract void bindData(T data);
}
