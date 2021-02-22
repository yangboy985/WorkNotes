package com.study.recyclerview.recyclerview;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class RvItemDivider extends RecyclerView.ItemDecoration implements RecyclerView.OnChildAttachStateChangeListener, Runnable {
    private Rect padding = new Rect(0, 0, 0, 0);
    private int xDivider;
    private int yDivider;

    private RecyclerView recyclerView;
    private int lastPos;
    private boolean isNeedInvalidateDecoration = false;
    private boolean isLastItemFooter = false;

    public RvItemDivider(@NonNull RecyclerView rv, @NonNull RecyclerView.Adapter<?> adapter, int xDivider, int yDivider) {
        this.xDivider = Math.max(xDivider, 0);
        this.yDivider = Math.max(yDivider, 0);
        this.recyclerView = rv;
        initListener(adapter);
    }

    private void initListener(RecyclerView.Adapter<?> adapter) {
        recyclerView.addOnChildAttachStateChangeListener(this);
        recyclerView.addItemDecoration(this);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (positionStart > lastPos && (isReverseLayout(recyclerView) ? padding.top != 0 : padding.bottom != 0)) {
                    // 最后一行item存在额外的padding，需要更新
                    isNeedInvalidateDecoration = true;
                }
            }
        });
    }

    public RvItemDivider setPadding(int l, int t, int r, int b) {
        padding.left = l;
        padding.top = t;
        padding.right = r;
        padding.bottom = b;
        return this;
    }

    public RvItemDivider setLastItemFooter(boolean lastItemFooter) {
        isLastItemFooter = lastItemFooter;
        return this;
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int span = getSpan(parent);
        int orientation = getOrientation(parent);
        boolean isReverse = isReverseLayout(parent);
        if (isReverse) {
            outRect.right = xDivider;
            outRect.bottom = yDivider;
        } else {
            outRect.left = xDivider;
            outRect.top = yDivider;
        }
        drawPadding(position, span, parent.getAdapter().getItemCount(), outRect, orientation, isReverse);
    }

    private boolean isReverseLayout(RecyclerView parent) {
        if (parent.getLayoutManager() instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) parent.getLayoutManager()).getReverseLayout();
        }
        return false;
    }

    private void drawPadding(int position, int span, int size, Rect outRect, int orientation, boolean isReverseLayout) {
        if (position < span) {
            if (orientation == RecyclerView.VERTICAL) {
                if (isReverseLayout) {
                    outRect.bottom = padding.bottom;
                } else {
                    outRect.top = padding.top;
                }
            } else {
                if (isReverseLayout) {
                    outRect.right = padding.right;
                } else {
                    outRect.left = padding.left;
                }
            }
        }
        int location = position % span; // 行（列）中的位置
        int endLocation = span - 1; // 行（列）中最后一个元素的位置
        if (location == 0) {
            if (orientation == RecyclerView.VERTICAL) {
                outRect.left = padding.left;
            } else {
                outRect.top = padding.top;
            }
        }
        if (location == endLocation) {
            if (orientation == RecyclerView.VERTICAL) {
                outRect.right = padding.right;
            } else {
                outRect.bottom = padding.bottom;
            }
        }
        // 和最后一列（行）的第一个元素比较
        boolean isLastLocation = (size + span - 1) / span * span - span <= position;
        if (isLastLocation) {
            lastPos = position;
            if (orientation == RecyclerView.VERTICAL) {
                if (isReverseLayout) {
                    outRect.top = padding.top;
                } else {
                    outRect.bottom = padding.bottom;
                }
            } else {
                if (isReverseLayout) {
                    outRect.left = padding.left;
                } else {
                    outRect.right = padding.right;
                }
            }
            if (isLastItemFooter) {
                if (orientation == RecyclerView.VERTICAL) {
                    if (isReverseLayout) {
                        outRect.bottom = 0;
                    } else {
                        outRect.top = 0;
                    }
                } else {
                    if (isReverseLayout) {
                        outRect.right = 0;
                    } else {
                        outRect.bottom = 0;
                    }
                }
            }
        }
    }

    @Override
    public void onChildViewAttachedToWindow(@NonNull View view) {
        if (isNeedInvalidateDecoration) {
            isNeedInvalidateDecoration = false;
            recyclerView.post(this);
        }
    }

    @Override
    public void run() {
        recyclerView.invalidateItemDecorations();
    }

    @Override
    public void onChildViewDetachedFromWindow(@NonNull View view) {
    }

    private int getOrientation(RecyclerView view) {
        if (view.getLayoutManager() instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) view.getLayoutManager()).getOrientation();
        }
        if (view.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) view.getLayoutManager()).getOrientation();
        }
        return RecyclerView.VERTICAL;
    }

    private int getSpan(RecyclerView view) {
        if (view.getLayoutManager() instanceof GridLayoutManager) {
            return ((GridLayoutManager) view.getLayoutManager()).getSpanCount();
        }
        if (view.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) view.getLayoutManager()).getSpanCount();
        }
        return 1;
    }
}
