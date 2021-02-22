package com.study.recyclerview.recyclerview;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RvItemClickHelper extends GestureDetector.SimpleOnGestureListener implements RecyclerView.OnItemTouchListener {
    private RecyclerView mRecyclerView;
    private GestureDetector mGestureDetector;
    private OnItemClickListener mClickListener;
    private OnItemLongClickListener mLongClickListener;

    public RvItemClickHelper(@NonNull RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        mGestureDetector = new GestureDetector(recyclerView.getContext(), this);
        mGestureDetector.setIsLongpressEnabled(false);
        recyclerView.addOnItemTouchListener(this);
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        if (mClickListener != null || mLongClickListener != null) {
            return mGestureDetector.onTouchEvent(e);
        }
        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (mClickListener != null) {
            View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
            if (view != null) {
                mClickListener.onItemClick(view, mRecyclerView.getChildAdapterPosition(view));
                return true;
            }
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (mLongClickListener != null) {
            View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
            if (view != null) {
                mLongClickListener.onItemLongClick(view, mRecyclerView.getChildAdapterPosition(view));
            }
        }
    }

    public RvItemClickHelper setClickListener(OnItemClickListener mClickListener) {
        this.mClickListener = mClickListener;
        return this;
    }

    public RvItemClickHelper setLongClickListener(OnItemLongClickListener mLongClickListener) {
        this.mLongClickListener = mLongClickListener;
        mGestureDetector.setIsLongpressEnabled(mLongClickListener != null);
        return this;
    }

    public interface OnItemClickListener {
        void onItemClick(View item, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View item, int position);
    }
}
