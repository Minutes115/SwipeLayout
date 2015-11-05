package com.minutes.swipelayout;

import android.content.Context;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.FrameLayout;

/**
 * 2015/04/03 - 11:37
 * Created by lianglong.
 */
public class SwipeToRefreshLayout extends FrameLayout {

    private static final String TAG = "RefreshView";
    private static final float FRICTION = 3.0f;        //手指在屏幕上的移动距离与拉动的比例
    private static final int DURING = 500;        //手指在屏幕上的移动距离与拉动的比例


    public static final int MODE_NONE = 0;
    public static final int MODE_BOTH = 1;
    public static final int MODE_PULL_UP_TO_REFRESH = 2;
    public static final int MODE_PULL_DOWN_TO_REFRESH = 3;

    private int mode = MODE_NONE;

    private int touchSlop;

    /**
     * 上一个触发onTouchEvent的点;
     */
    private float initialY;

    private View childView;

    private ILoadLayout header = new MyHeader();
    private ILoadLayout footer = new MyFooter();


    /**
     * 标示是否用户正在拉动
     */
    private boolean isDragging;
    private boolean isRefreshing;
    private boolean touchableWhileRefreshing;

    private SwipeToRefreshListener onRefreshListener;

    public void setOnRefreshListener(SwipeToRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    public void setHeader(ILoadLayout header) {
        if (this.header != null){
            removeView(header.loadView(this));
        }
        this.header = header;
        this.header.onAttach(this);
    }

    public void setFooter(ILoadLayout footer) {
        if (this.footer != null){
            removeView(this.footer.loadView(this));
        }
        this.footer = footer;
        this.footer.onAttach(this);
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setTouchableWhileRefreshing(boolean touchableWhileRefreshing) {
        this.touchableWhileRefreshing = touchableWhileRefreshing;
    }

    public boolean isRefreshing() {
        return isRefreshing;
    }

    public void setRefreshing(boolean refreshing) {
        if (header != null){
            if (refreshing) {
                header.onRefreshing(this);
            } else {
                header.stopRefresh(this);
            }
        }
    }

    public SwipeToRefreshLayout(Context context) {
        super(context);
        init(context);
    }

    public SwipeToRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SwipeToRefreshLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        if (header != null) {
            header.onAttach(this);
        }
        if (footer != null) {
            footer.onAttach(this);
        }
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
                Log.d(TAG, "TouchSlop : " + touchSlop + "  State Ready ? ");
            }
        });
    }

    private View getChildView() {
        if (childView == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (!child.equals(header.loadView(this)) && !child.equals(footer.loadView(this))) {
                    childView = child;
                    break;
                }
            }
        }
        return childView;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
            if (header != null) {
                header.onLayout(this, changed, left, top, right, bottom);
            }
            if (footer != null) {
                footer.onLayout(this, changed, left, top, right, bottom);
            }
    }


    /**
     * @return 设置是否允许拉动
     */
    private boolean isEnablePulling() {
        return mode != MODE_NONE;
    }

    /**
     * @return 子视图是否可以下拉, 这里的下拉是指是否已经滑到顶端了
     */
    private boolean canChildPullDown() {
        View child = getChildView();
        if (child instanceof AbsListView) {
            AbsListView abs = (AbsListView) child;
            int count = abs.getAdapter().getCount();
            View topView = abs.getChildAt(0);
            if (count == 0 || topView == null) {
                return true;
            }
            if (abs.getFirstVisiblePosition() == 0 && topView.getTop() >= abs.getPaddingTop()) {
                return true;
            }
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                return child.getScaleY() > 0;
            } else {
                return !ViewCompat.canScrollVertically(child, -1);
            }
        }
        return false;
    }

    /**
     * @return 子视图是否可以上拉, 这里的上拉是指是否已经滑到底端了
     */
    private boolean canChildPullUp() {
        View child = getChildView();
        if (child instanceof AbsListView) {
            AbsListView av = (AbsListView) child;
            int count = av.getAdapter().getCount();
            View bottomView = av.getChildAt(av.getChildCount() - 1);
            if (av.getLastVisiblePosition() == count - 1 && bottomView.getBottom() < av.getBottom()) {
                return true;
            }
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                return child.getScrollY() < 0;
            } else {
                return !ViewCompat.canScrollVertically(child, 1);
            }
        }
        return false;
    }


    /**
     * @param ev 触摸事件
     * @return false 证明事件继续往子view传递调用子view onTouchEvent,true 表示不往下传递 直接调用该容器的onTouchEvent
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnablePulling() || (!canChildPullDown() && !canChildPullUp())) {
            return false;
        }
        final int action = MotionEventCompat.getActionMasked(ev);
        if (isRefreshing && action == MotionEvent.ACTION_MOVE) {
            return true;
        }
        if (action == MotionEvent.ACTION_DOWN) {
            initialY = ev.getY();
            return isRefreshing && touchableWhileRefreshing;
        }
        //在这里触发拉动事件,当没有在拉动的时候,如果是垂直拉动,并且距离大于指定的距离,设定为开始拉动,开始消费事件
        if (action == MotionEvent.ACTION_MOVE) {
            float delta = ev.getY() - initialY;
            if (delta > 0) {//手指下拉
                if (header == null || !canChildPullDown()
                    || (mode != MODE_BOTH && mode != MODE_PULL_DOWN_TO_REFRESH)) {
                    return false;
                }
            }
            if (delta < 0) {//手指上拉
                if (footer == null || !canChildPullUp()
                    || (mode != MODE_BOTH && mode != MODE_PULL_UP_TO_REFRESH)) {
                    return false;
                }
            }
            if (!isDragging && Math.abs(delta) >= touchSlop) {
                isDragging = true;
            }
            return isDragging;
        }
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            isDragging = false;
            return isRefreshing;
        }
        return super.onInterceptTouchEvent(ev);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        if (action == MotionEvent.ACTION_DOWN) {
            isDragging = false;
        }
        if (action == MotionEvent.ACTION_MOVE) {
            if (isDragging) {
                int distance = (int) ((initialY - event.getY()) / FRICTION);
                handleDragEvent(distance);
            }
        }
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            if (isDragging) {
                isDragging = false;
                float delta = event.getY() - initialY;
                if (delta > 0) {
                    isRefreshing = Math.abs(delta) >= header.maxDistance() * 1.3;
                } else {
                    isRefreshing = Math.abs(delta) >= footer.maxDistance() * 1.3;
                }
                if (isRefreshing) {
                    startRefresh(delta);
                } else {
                    stopRefresh();
                }
            }
        }
        return true;
    }

    /**
     * @param distance 手动滑动事件
     */
    private void handleDragEvent(int distance) {
        int d = Math.abs(distance);
        if (distance < 0 && header != null) {
            if (d < header.maxDistance()) {
                header.onDragEvent(this, distance);
            } else {
                header.onOverDragging(this, distance);
            }
        }
        if (distance > 0 && footer != null) {
            if (d < footer.maxDistance()) {
                footer.onDragEvent(this, distance);
            } else {
                footer.onOverDragging(this, distance);
            }
        }
    }


    /**
     * 切换到下拉刷新状态
     *
     * @param direction 方向指示,如果为负数,是上拉状态,如果是正数,是下拉状态
     */
    private void startRefresh(float direction) {
        boolean isPullDown = direction > 0;
        if (isPullDown && header != null) {
            header.onRefreshing(this);
        }
        if (!isPullDown && footer != null) {
            footer.onRefreshing(this);
        }
    }

    /**
     * 滑动完成
     */
    public void stopRefresh() {
        isRefreshing = false;
        if (canChildPullDown() && header != null) {
            header.stopRefresh(this);
        }
        if (canChildPullUp() && footer != null) {
            footer.stopRefresh(this);
        }
    }

}