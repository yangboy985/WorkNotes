package com.study.recyclerview.recyclerview;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public abstract class BaseRvAdapter<T> extends RecyclerView.Adapter<RvHolder<T>> {
    protected RvDataCtrl<T> mDataCtrl;

    public BaseRvAdapter() {
        mDataCtrl = new RvDataCtrl<T>(this, new ArrayList<T>());
    }

    public RvDataCtrl<T> getDataCtrl() {
        return mDataCtrl;
    }

    @Override
    public void onBindViewHolder(@NonNull RvHolder<T> holder, int position) {
        holder.bindData(mDataCtrl.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataCtrl.size();
    }
}