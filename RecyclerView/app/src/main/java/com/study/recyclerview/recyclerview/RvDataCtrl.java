package com.study.recyclerview.recyclerview;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RvDataCtrl<T> {
    private static final String TAG = "RvDataCtrl";

    private RecyclerView.Adapter<?> mAdapter;
    private List<T> mData = new ArrayList<>();
    private Handler mHandler;

    RvDataCtrl(RecyclerView.Adapter<?> adapter, List<T> list) {
        mAdapter = adapter;
        if (list != null) {
            mData.addAll(list);
        }
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void setData(final List<T> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mData.clear();
                mData.addAll(list);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    public void add(final T t) {
        if (t == null) {
            Log.e(TAG, "add null element");
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mData.add(t);
                mAdapter.notifyItemInserted(mData.size() - 1);
            }
        });
    }

    public void addAll(final List<T> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                int startIndex = mData.size();
                mData.addAll(list);
                mAdapter.notifyItemRangeInserted(startIndex, list.size());
            }
        });
    }

    public void insert(final T mark, final T t, final boolean isBeforeMark) {
        if (t == null || mark == null) {
            Log.e(TAG, "insert null element or mark is null");
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                int index = mData.indexOf(mark);
                if (index < 0) {
                    Log.e(TAG, "mark is't exist");
                    return;
                }
                index += isBeforeMark ? 0 : 1;
                mData.add(index, t);
                mAdapter.notifyItemInserted(index);
            }
        });
    }

    public void update(final T old, final T t) {
        if (t == null || old == null) {
            Log.e(TAG, "update: t is null or old is null");
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                int index = mData.indexOf(old);
                if (index < 0) {
                    Log.e(TAG, "update is't exist");
                    return;
                }
                mData.set(index, t);
                mAdapter.notifyItemChanged(index, old);
            }
        });
    }

    public void delete(final T t) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                int index = mData.indexOf(t);
                if (index < 0) {
                    Log.w(TAG, "t was deleted");
                    return;
                }
                mData.remove(index);
                mAdapter.notifyItemRemoved(index);
            }
        });
    }

    public void clear() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mData.clear();
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @MainThread
    public T get(int position) {
        return mData.get(position);
    }

    @MainThread
    public int size() {
        return mData.size();
    }
}
