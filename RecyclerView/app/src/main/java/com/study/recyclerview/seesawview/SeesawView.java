package com.study.recyclerview.seesawview;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

public class SeesawView extends ViewGroup implements NestedScrollingParent3 {
    private float initScrollYRate = 1f; // 0~1
    private NestedScrollingParentHelper helper = new NestedScrollingParentHelper(this);
    private RecyclerView child1;
    private RecyclerView child2;

    private boolean isChild1Top = true;
    private boolean isChild2Top = true;
    private int maxScrollDistance = 0;
    private int scrolledY = -1;

    public SeesawView(Context context) {
        super(context);
    }

    public SeesawView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SeesawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SeesawView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setInitScrollYRate(@FloatRange(from = 0, to = 1f) float initScrollYRate) {
        this.initScrollYRate = initScrollYRate;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        findChild();
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode == MeasureSpec.UNSPECIFIED) {
            widthMode = MeasureSpec.AT_MOST;
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, widthMode);
        }
        if (heightMode == MeasureSpec.UNSPECIFIED) {
            heightMode = MeasureSpec.AT_MOST;
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, heightMode);
        }
        measureChild(child1, widthMeasureSpec, heightMeasureSpec);
        measureChild(child2, widthMeasureSpec, heightMeasureSpec);
        int expectWidth = width;
        if (widthMode == MeasureSpec.AT_MOST) {
            expectWidth = Math.min(width, Math.max(child1.getMeasuredWidth(), child2.getMeasuredWidth()));
        }
        int expectHeight = height;
        if (heightMode == MeasureSpec.AT_MOST) {
            expectHeight = Math.min(height, Math.max(child1.getMeasuredHeight(), child2.getMeasuredHeight()));
        }
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(expectWidth, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(expectHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void findChild() {
        if (getChildCount() != 2) {
            throw new RuntimeException("SeesawView must add two child");
        }
        child1 = (RecyclerView) getChildAt(0);
        child2 = (RecyclerView) getChildAt(1);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            boolean isScrolledBottom = scrolledY == maxScrollDistance;
            maxScrollDistance = child1.getMeasuredHeight() + child2.getMeasuredHeight() - b + t;
            if (scrolledY == -1) {
                scrolledY = (int) (initScrollYRate * maxScrollDistance);
            }
            if (isScrolledBottom || scrolledY > maxScrollDistance) {
                scrolledY = maxScrollDistance;
            }
        }

        int left = getPaddingLeft();
        int top = getPaddingTop() - scrolledY;
        int right = left + child1.getMeasuredWidth();
        int bottom = top + child1.getMeasuredHeight();
        child1.layout(left, top, right, bottom);

        left = getPaddingLeft();
        top = bottom + 1;
        right = left + child2.getMeasuredWidth();
        bottom = top + child2.getMeasuredHeight();
        child2.layout(left, top, right, bottom);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
        return (ViewCompat.SCROLL_AXIS_VERTICAL & axes) == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
        helper.onNestedScrollAccepted(child, target, axes, type);
    }

    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {
        helper.onStopNestedScroll(target, type);
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        if (scrolledY == 0 && (dy < 0 || !isChild1Top)) {
            // child1完全展示，下滑时父View不处理；child1完全展示，但child1没有滑动到顶端（child1是倒着滑动的，顶端实际上是底部）
            return;
        }
        if (scrolledY == maxScrollDistance && (dy > 0 || !isChild2Top)) {
            // child2完全展示，上滑时父View不处理；child2完全展示，但child2没有滑动到顶端
            return;
        }
        scrollChildByDy(dy, consumed);
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, @NonNull int[] consumed) {
        if (dyUnconsumed == 0) {
            if (target == child2 && dyConsumed > 0) {
                isChild2Top = false;
            }
            if (target == child1 && dyConsumed < 0) {
                isChild1Top = false;
            }
            return;
        }
        if (dyUnconsumed > 0 && target == child1) {
            isChild1Top = true;
            handleOverScroll(target, dyUnconsumed, consumed);
        } else if (dyUnconsumed < 0 && target == child2) {
            isChild2Top = true;
            handleOverScroll(target, dyUnconsumed, consumed);
        }
    }

    private void handleOverScroll(@NonNull View target, int dyUnconsumed, @NonNull int[] consumed) {
        scrollChildByDy(dyUnconsumed, consumed);
        if (consumed[1] == dyUnconsumed) {
            // 滑动距离完全被消费了
            return;
        }
        // 遗留下的滑动距离
        dyUnconsumed -= consumed[1];
        int canScrollDistance;
        int dy = dyUnconsumed;
        if (child1 == target) {
            // child2处理剩下的滑动距离
            canScrollDistance = child2.computeVerticalScrollRange() - child2.computeVerticalScrollExtent() - 1 - child2.computeVerticalScrollOffset();
            if (canScrollDistance > 0) {
                if (canScrollDistance < dyUnconsumed) {
                    dy = canScrollDistance;
                }
                child2.scrollBy(0, dy);
                isChild2Top = false;
                consumed[1] += dy;
            }
        } else {
            // child1处理剩下的滑动距离
            canScrollDistance = child1.computeVerticalScrollOffset();
            if (canScrollDistance > 0) {
                if (-canScrollDistance > dyUnconsumed) {
                    dy = -canScrollDistance;
                }
                child1.scrollBy(0, dy);
                isChild1Top = false;
                consumed[1] += dy;
            }
        }
    }

    private void scrollChildByDy(int dyUnconsumed, int[] consumed) {
        int offset = dyUnconsumed;
        scrolledY += dyUnconsumed;
        // 大前提，scrolledY取值范围0-maxScrollDistance
        if (scrolledY < 0) {
            // 这次滑动会到child1完全展示，并有一部分滑动距离需要交给其它子View处理
            // onNestedPreScroll是交给滑动事件发起方自己处理
            // onNestedScroll是交给另一个child处理
            consumed[1] += offset = dyUnconsumed - scrolledY;
            scrolledY = 0;
        } else if (scrolledY > maxScrollDistance) {
            // 这个和scrolledY < 0差不多
            consumed[1] += offset = dyUnconsumed - (scrolledY - maxScrollDistance);
            scrolledY = maxScrollDistance;
        } else {
            // 滑动距离完全由SeesawView消费
            consumed[1] = dyUnconsumed;
        }
        child1.offsetTopAndBottom(-offset);
        child2.offsetTopAndBottom(-offset);
    }
}
