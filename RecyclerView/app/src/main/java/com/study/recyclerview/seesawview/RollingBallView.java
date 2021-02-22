package com.study.recyclerview.seesawview;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import static android.animation.ValueAnimator.INFINITE;
import static android.animation.ValueAnimator.RESTART;

public class RollingBallView extends View {
    private static final long ANIMATION_DURATION = 3600L; // 转3圈的时间
    private static final float HALF_DISTANCE_RATE = 6 / 4.5F;
    private static final float MIN_SPACE_RATE = 30 / 4.5F;  // 以原点半径为4.5计算
    private static final int DEFAULT_RADIUS = RollingBallView.dp2px(4.5f);

    private int radius = 0;
    private int halfDistance;
    private int leftColor = Color.parseColor("#14C5CD"); // 初始状态左边点颜色
    private int rightColor = Color.parseColor("#FF8254"); // 初始状态右边点颜色
    private Paint paint;

    private float leftRadius = 1;
    private float rightRadius = 1;
    private float planeRotateAngle = 180;  // 顺时针转动，以初始右边点做参照
    private float solidRotateAngle = 0;  // 逆时针转动，以初始右边点作为参照

    private ValueAnimator animator;

    public RollingBallView(Context context) {
        this(context, null);
    }

    public RollingBallView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RollingBallView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        createPaint();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RollingBallView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        createPaint();
    }

    private void createPaint() {
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    public void setColors(@ColorInt int leftColor, @ColorInt int rightColor) {
        this.leftColor = leftColor;
        this.rightColor = rightColor;
    }

    /**
     * 如果width和height都使用了wrap，这个值才可能生效（可能根据空间限制缩小）
     *
     * @param radius 小球正常状态时的半径
     */
    public void setRadius(@FloatRange(from = 0, fromInclusive = true) float radius) {
        if (radius == 0) {
            return;
        }
        this.radius = dp2px(radius);
    }

    private static int dp2px(float value) {
        Resources r = Resources.getSystem();
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, r.getDisplayMetrics()) + 0.5f);
    }

    public void onMoving(float percent) {
        if (percent < 0) {
            percent = 0;
        }
        if (percent > 1) {
            percent = 1;
        }
        if (animator != null) {
            return;
            // 下拉触发刷新以后再多次下拉会不正常
//            animator.cancel();
//            leftRadius = rightRadius = radius;
//            planeRotateAngle = 180;
//            solidRotateAngle = 0;
        }
        // 下拉过程动画：
        // 整个过程仅围绕Y轴进行180度顺时针转动
        // 这里这里是纠正currHalfDistance取反
        planeRotateAngle = 180;
        solidRotateAngle = percent * 180;
        rightRadius = getRealRadius(solidRotateAngle);
        leftRadius = getRealRadius(solidRotateAngle + 180);
        invalidate();
    }

    public void ignoreMoving() {
        planeRotateAngle = 180;
        solidRotateAngle = 180;
        rightRadius = getRealRadius(solidRotateAngle);
        leftRadius = getRealRadius(solidRotateAngle + 180);
        invalidate();
    }

    public void startAnimator() {
        // 下加载动画启动
        if (animator != null) {
            animator.start();
            return;
        }
        animator = ValueAnimator.ofFloat(0, 6f);
        animator.setDuration(ANIMATION_DURATION);
        animator.setRepeatCount(INFINITE);
        animator.setRepeatMode(RESTART);
        animator.setInterpolator(new TimeInterpolator() {
            @Override
            public float getInterpolation(float percent) {
                planeRotateAngle = (180 - percent * 360) % 360;
                solidRotateAngle = (180 + percent * 180 * 6) % 360;
                rightRadius = getRealRadius(solidRotateAngle);
                leftRadius = getRealRadius(solidRotateAngle + 180);
                invalidate();
                return percent;
            }
        });
        animator.start();
    }

    public void stopAnimator() {
        if (animator == null) {
            return;
        }
        animator.cancel();
        leftRadius = rightRadius = radius;
        planeRotateAngle = 180;
        solidRotateAngle = 0;
    }

    private float getRealRadius(float angle) {
        if (angle < 180) {
            return (float) (radius + radius * Math.sin(angle * Math.PI / 180));
        } else {
            return (float) (radius + radius / 2 * Math.sin(angle * Math.PI / 180));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode == heightMode && widthMode == MeasureSpec.AT_MOST) {
            int space = (int) (Math.min(width, height) + 0.5f);
            if (radius == 0) {
                radius = DEFAULT_RADIUS;
            }
            int needSpace = (int) (radius * MIN_SPACE_RATE + 0.5f);
            if (needSpace > space) {
                this.radius = (int) (space / MIN_SPACE_RATE);
            } else {
                space = needSpace;
            }
            int ms = MeasureSpec.makeMeasureSpec(space, MeasureSpec.EXACTLY);
            this.leftRadius = this.rightRadius = radius;
            this.halfDistance = (int) (radius * HALF_DISTANCE_RATE);
            super.onMeasure(ms, ms);
            return;
        }
        boolean isWidthLonger = width > height;

        if (isWidthLonger) {
            switch (widthMode) {
                case MeasureSpec.UNSPECIFIED:
                case MeasureSpec.AT_MOST:
                    width = height;
                    break;
            }
        } else {
            switch (heightMode) {
                case MeasureSpec.UNSPECIFIED:
                case MeasureSpec.AT_MOST:
                    height = width;
                    break;
            }
        }
        if (radius == 0) {
            this.radius = (int) (width / MIN_SPACE_RATE);
        } else {
            this.radius = Math.min((int) (width / MIN_SPACE_RATE), this.radius);
        }
        this.leftRadius = this.rightRadius = radius;
        this.halfDistance = (int) (radius * HALF_DISTANCE_RATE);
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        canvas.translate(width / 2f, height / 2f);

        // 动画阶段planeRotateAngle和solidRotateAngle都是从180开始的，因此需要取反
        double currHalfDistance = -halfDistance * Math.cos(solidRotateAngle * Math.PI / 180);

        if (leftRadius > rightRadius) {
            drawRightBall(canvas, currHalfDistance);
            drawLeftBall(canvas, currHalfDistance);
        } else {
            drawLeftBall(canvas, currHalfDistance);
            drawRightBall(canvas, currHalfDistance);
        }
    }

    private void drawRightBall(Canvas canvas, double currHalfDistance) {
        float x = (float) (Math.cos(planeRotateAngle * Math.PI / 180) * currHalfDistance);
        float y = (float) (-Math.sin(planeRotateAngle * Math.PI / 180) * currHalfDistance);
        paint.setColor(rightColor);
        canvas.drawCircle(x, y, rightRadius, paint);
    }

    private void drawLeftBall(Canvas canvas, double currHalfDistance) {
        float planeRotateAngle = this.planeRotateAngle + 180;
        float x = (float) (Math.cos(planeRotateAngle * Math.PI / 180) * currHalfDistance);
        float y = (float) (-Math.sin(planeRotateAngle * Math.PI / 180) * currHalfDistance);
        paint.setColor(leftColor);
        canvas.drawCircle(x, y, leftRadius, paint);
    }
}
