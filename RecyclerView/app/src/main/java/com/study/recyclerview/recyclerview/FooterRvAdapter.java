package com.study.recyclerview.recyclerview;

import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.study.recyclerview.seesawview.IRvFooter;

import java.util.List;

public abstract class FooterRvAdapter<T> extends BaseRvAdapter<T> {
    protected static final int VIEW_TYPE_FOOTER = -2;

    private RecyclerView recyclerView;
    private boolean isHorizontally;
    private FooterHolder<T> footerHolder;
    private OnLoadMoreListener loadMoreListener;
    private int flag;

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            if (footerHolder == null) {
                return;
            }
            Rect rect = new Rect();
            footerHolder.itemView.getLocalVisibleRect(rect);
            footerHolder.onFooterShowing();
        }
    };

    protected FooterRvAdapter(@NonNull RecyclerView rv, boolean isHorizontally) {
        this.recyclerView = rv;
        this.isHorizontally = isHorizontally;
        this.recyclerView.addOnScrollListener(scrollListener);
    }

    public void finishLoadMore(final boolean isSuccess, @NonNull final List<T> list) {
        long delay = footerHolder.footer.getMinLoadingTime() - SystemClock.uptimeMillis() + footerHolder.loadingStartTime;
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                footerHolder.footer.onFinish(isSuccess, list.isEmpty());
                if (isSuccess && !list.isEmpty()) {
                    mDataCtrl.addAll(list);
                }
                footerHolder.isNoMoreData = list.isEmpty();
                footerHolder.isLoading = false;
            }
        }, delay); // 负值没问题，handler本身有判断，变0
    }

    public FooterRvAdapter<T> setLoadMoreListener(@NonNull OnLoadMoreListener listener, int flag) {
        this.loadMoreListener = listener;
        this.flag = flag;
        if (this.footerHolder != null) {
            this.footerHolder.loadMoreListener = listener;
            this.footerHolder.flag = flag;
        }
        return this;
    }

    @NonNull
    @Override
    public RvHolder<T> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_FOOTER) {
            if (footerHolder == null) {
                footerHolder = new FooterHolder<>(createFooter(parent));
                footerHolder.loadMoreListener = loadMoreListener;
                footerHolder.flag = flag;
            }
            return footerHolder;
        }
        return onCreateItemViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RvHolder<T> holder, int position) {
        if (position < getFooterPosition()) {
            holder.bindData(mDataCtrl.get(position));
        } else {
            holder.bindData(null);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getFooterPosition()) {
            return VIEW_TYPE_FOOTER;
        }
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        if (super.getItemCount() == 0 || !isFullOnePage()) {
            return super.getItemCount();
        }
        return super.getItemCount() + 1;
    }

    protected boolean isFullOnePage() {
        if (isHorizontally) {
            return recyclerView.canScrollHorizontally(1) || recyclerView.canScrollHorizontally(-1);
        }
        return recyclerView.canScrollVertically(1) || recyclerView.canScrollVertically(-1);
    }

    protected int getFooterPosition() {
        return super.getItemCount();
    }

    protected abstract IRvFooter createFooter(@NonNull ViewGroup parent);

    @NonNull
    protected abstract RvHolder<T> onCreateItemViewHolder(@NonNull ViewGroup parent, int viewType);

    public static class FooterHolder<T> extends RvHolder<T> {
        private IRvFooter footer;
        private OnLoadMoreListener loadMoreListener;
        private int flag;

        private boolean isLoading = false;
        private boolean isNoMoreData = false;
        private long loadingStartTime = -1;

        public FooterHolder(@NonNull IRvFooter footer) {
            super(footer.getView());
            this.footer = footer;
        }

        @Override
        public void bindData(T data) {
        }

        void onFooterShowing() {
            if (!isNoMoreData && !isLoading) {
                loadingStartTime = SystemClock.uptimeMillis();
                isLoading = true;
                footer.onStartLoading();
                if (loadMoreListener != null) {
                    loadMoreListener.onLoadMore(flag);
                }
            }
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int flag);
    }
}
