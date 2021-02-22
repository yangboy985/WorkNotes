package com.study.recyclerview.recyclerview;

import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 注释都有锤子正向作为参照
 */
public class RvDivider extends RecyclerView.ItemDecoration {
    private int paddingListStart = 0;
    private int xDivider;
    private int yDivider;

    private RecyclerView recyclerView;

    private boolean isReverseLayout;
    private boolean isVertical = true;
    private int span;
    private List<Point> outRectParamsForColumn = new ArrayList<>(); // 这里直接用Point类，懒得自己定义类了

    public RvDivider(@NonNull RecyclerView rv, int xDivider, int yDivider) {
        if (rv.getLayoutManager() == null) {
            throw new NullPointerException("layout manager is null");
        }
        this.xDivider = Math.max(xDivider, 0);
        this.yDivider = Math.max(yDivider, 0);
        this.recyclerView = rv;
        rv.addItemDecoration(this);

        initParams();
    }

    public void initParams() {
        isReverseLayout = isReverseLayout(recyclerView);
        isVertical = isVerticalRecyclerView(recyclerView);
        span = getSpan(recyclerView);
        getOutRectParams();
    }

    private void getOutRectParams() {
        // LinearLayoutManger只有一列，两边都是0,不用算
        if (recyclerView.getLayoutManager() instanceof GridLayoutManager
                || recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            if (isVertical) {
                // recyclerView的item均分是以margin+outRect+view合起来进行的，第一列比起其它列明显少个outRect.left
                int totalXDivider = xDivider * (span - 1);
                // 这里可能不准确
                int averageVal = totalXDivider / span;
                // 不管正反列表，都是从左至右
                int start = 0;
                int end = averageVal;
                for (int i = 0; i < span; i++) {
                    outRectParamsForColumn.add(new Point(start, end));
                    start = xDivider - end;
                    end = averageVal - start;
                }
            } else {
                int totalYDivider = yDivider * (span - 1);
                int averageVal = totalYDivider / span;
                // 不管正反列表，都是从上到下
                int start = 0;
                int end = averageVal;
                for (int i = 0; i < span; i++) {
                    outRectParamsForColumn.add(new Point(start, end));
                    start = yDivider - end;
                    end = averageVal - start;
                }
            }
        }
    }

    public RvDivider setPaddingListStart(int paddingListStart) {
        this.paddingListStart = paddingListStart;
        return this;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (isVertical) {
            outRect.left = xDivider;
            if (isReverseLayout) {
                outRect.bottom = yDivider;
            } else {
                outRect.top = yDivider;
            }
        } else {
            outRect.top = yDivider;
            if (isReverseLayout) {
                outRect.right = xDivider;
            } else {
                outRect.left = xDivider;
            }
        }
        setRealOutRect(view, position, outRect);
    }

    private void setRealOutRect(View view, int position, Rect outRect) {
        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            setGridOutRect(position, outRect, (GridLayoutManager.LayoutParams) view.getLayoutParams());
            return;
        }
        if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            setStaggeredGridOutRect(position, outRect, (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams());
            return;
        }
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            setLinearOutRect(position, outRect);
        }
    }

    private void setLinearOutRect(int position, Rect outRect) {
        if (position == 0) {
            paddingListStart(outRect);
        }
    }

    private void setGridOutRect(int position, Rect outRect, GridLayoutManager.LayoutParams params) {
        int spanIndex = params.getSpanIndex();
        int spanSize = params.getSpanSize();
        if (position == spanIndex) {
            paddingListStart(outRect);
        }
        int start = outRectParamsForColumn.get(spanIndex).x;
        int end = outRectParamsForColumn.get(spanIndex + spanSize - 1).y;
        if (isVertical) {
            outRect.left = start;
            outRect.right = end;
        } else {
            outRect.top = start;
            outRect.bottom = end;
        }
    }

    private void setStaggeredGridOutRect(int position, Rect outRect, StaggeredGridLayoutManager.LayoutParams params) {
        int spanIndex = params.getSpanIndex();
        boolean isFullSpan = params.isFullSpan();
        if (position == spanIndex) {
            paddingListStart(outRect);
        }
        int start = 0;
        int end = 0;
        if (!isFullSpan) {
            start = outRectParamsForColumn.get(spanIndex).x;
            end = outRectParamsForColumn.get(spanIndex).y;
        }
        if (isVertical) {
            outRect.left = start;
            outRect.right = end;
        } else {
            outRect.top = start;
            outRect.bottom = end;
        }
    }

    private void paddingListStart(Rect outRect) {
        if (isVertical) {
            if (isReverseLayout) {
                outRect.bottom = paddingListStart;
            } else {
                outRect.top = paddingListStart;
            }
        } else {
            if (isReverseLayout) {
                outRect.right = paddingListStart;
            } else {
                outRect.left = paddingListStart;
            }
        }
    }

    private boolean isReverseLayout(RecyclerView parent) {
        if (parent.getLayoutManager() instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) parent.getLayoutManager()).getReverseLayout();
        }
        if (parent.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) parent.getLayoutManager()).getReverseLayout();
        }
        return false;
    }

    private boolean isVerticalRecyclerView(RecyclerView view) {
        if (view.getLayoutManager() instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) view.getLayoutManager()).getOrientation() == RecyclerView.VERTICAL;
        }
        if (view.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) view.getLayoutManager()).getOrientation() == RecyclerView.VERTICAL;
        }
        return true;
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
