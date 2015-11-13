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

    private static final String TAG = "SwipeToRefreshLayout";
    private static final float FRICTION = 3.0f;        //手指在屏幕上的移动距离与拉动的比例

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

    /**
     * 移动的偏移量
     */
    private float offset;

    /**
     * 内容视图,在没有header与footer时应该在本View中只有这一个视图
     */
    private View contentView;

    /**
     * 标示是否用户正在拉动
     */
    private boolean isDragging;

    /**
     * 是否正在刷新
     */
    private boolean isRefreshing;

//    private ILoadLayout header = new PullToRefreshHeader();
//    private ILoadLayout footer = new PullToLoadMoreFooter();

    private ILoadLayout header = new PhoenixHeader();
    private ILoadLayout footer = null;

//    private ILoadLayout header = new MaterialHeader();
//    private ILoadLayout footer = null;

    private SwipeToRefreshListener onRefreshListener;

    public void setOnRefreshListener(SwipeToRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    public void setHeader(ILoadLayout header) {
        if (this.header != null) {
            this.header.onDetach(this);
        }
        this.header = header;
        this.header.onAttach(this);
    }

    public void setFooter(ILoadLayout footer) {
        if (this.footer != null) {
            this.footer.onDetach(this);
        }
        this.footer = footer;
        this.footer.onAttach(this);
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public boolean isRefreshing() {
        return isRefreshing;
    }

    public void setRefreshing(final boolean refreshing) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (refreshing) {
                    startRefresh(true);
                } else {
                    stopRefresh();
                }
            }
        }, 100);
    }


    /**
     * @return 获取内容视图, 该View在xml中应该只包含这一个View
     */
    public View getContentView() {
        if (contentView == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (header != null && child.equals(header.getLoadView())) {
                    continue;
                }
                if (footer != null && child.equals(footer.getLoadView())) {
                    continue;
                }
                contentView = child;
            }
        }
        return contentView;
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
        if (header != null) {
            header.onAttach(this);
        }
        if (footer != null) {
            footer.onAttach(this);
        }
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
                Log.d(TAG, "TouchSlop : " + touchSlop + " getScrollY : " + getScrollY());
            }
        });
    }

//    @Override
//    protected boolean checkLayoutParams(LayoutParams p) {
//        return super.checkLayoutParams(p) && p instanceof MarginLayoutParams;
//    }
//
//    @Override
//    protected LayoutParams generateDefaultLayoutParams() {
//        return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//    }
//
//    @Override
//    public LayoutParams generateLayoutParams(AttributeSet attrs) {
//        return new MarginLayoutParams(getContext(), attrs);
//    }
//
//    @Override
//    protected LayoutParams generateLayoutParams(LayoutParams p) {
//        return new MarginLayoutParams(p);
//    }
//
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        contentView = getContentView();
//        if (contentView != null) {
//            measureContentView(contentView, widthMeasureSpec, heightMeasureSpec);
//        }
////        if (header != null) {
////            measureChildWithMargins(header.getLoadView(), widthMeasureSpec, 0, heightMeasureSpec, 0);
////        }
////        if (footer != null) {
////            measureChildWithMargins(footer.getLoadView(), widthMeasureSpec, 0, heightMeasureSpec, 0);
////        }
//    }
//
//    private void measureContentView(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
//        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
//
//        final int childWidthMeasureSpec = getChildMeasureSpec(
//            parentWidthMeasureSpec,
//            getPaddingLeft() + getPaddingRight(),
//            lp.width
//        );
//
//        final int childHeightMeasureSpec = getChildMeasureSpec(
//            parentHeightMeasureSpec,
//            getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin,
//            lp.height
//        );
//        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
//    }
//
//    @Override
//    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        Log.e("SwipeToRefreshLayout","onLayout");
//        int paddingLeft = getPaddingLeft();
//        int paddingTop  = getPaddingTop();
//        contentView = getContentView();
//        if (contentView != null) {
//            LayoutParams lp = contentView.getLayoutParams();
//            final int left = paddingLeft;
//            final int top = (int) (paddingTop);
//            final int right = left + contentView.getMeasuredWidth();
//            final int bottom = top + contentView.getMeasuredHeight();
//            contentView.layout(left, top, right, bottom);
//        }
//        if (header != null) {
//            header.onLayout(this, changed, offset, l, t, r, b);
//        }
//        if (footer != null) {
//            footer.onLayout(this, changed, offset, l, t, r, b);
//        }
//    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.e("SwipeToRefreshLayout","onLayout");
        super.onLayout(changed, l, t, r, b);
        if (header != null) {
            header.onLayout(this, changed, offset, l, t, r, b);
        }
        if (footer != null) {
            footer.onLayout(this, changed, offset, l, t, r, b);
        }
    }

    /**
     * @return 判断设置是否允许拉动
     */
    private boolean isEnablePulling() {
        return mode != MODE_NONE;
    }

    /**
     * @return 当设置了下拉刷新时, 判断子View内容是否已经到头
     */
    private boolean canChildPullDown() {
        if (header == null) {
            return false;
        }
        if (mode != MODE_BOTH && mode != MODE_PULL_DOWN_TO_REFRESH) {
            return false;
        }
        View child = getContentView();
        if (child == null) {
            return false;
        }
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
                return child.getScrollY() > 0;
            } else {
                return !ViewCompat.canScrollVertically(child, -1);
            }
        }
        return false;
    }

    /**
     * @return 当设置了上拉加载时, 判断子View内容是否已经到底
     */
    private boolean canChildPullUp() {
        if (footer == null) {
            return false;
        }
        if (mode != MODE_BOTH && mode != MODE_PULL_UP_TO_REFRESH) {
            return false;
        }
        View child = getContentView();
        if (child == null) {
            return false;
        }
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
     * 拦截触摸事件
     *
     * @param ev 触摸事件
     * @return false 往子view传递调事件,用子view onTouchEvent,true 不往下传递 直接调用该容器的onTouchEvent
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnablePulling()) {
            return false;
        }
        int action = MotionEventCompat.getActionMasked(ev);
        if (action == MotionEvent.ACTION_DOWN) {
            initialY = ev.getY();
            isDragging = false;
        }
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            isDragging = false;
        }
        //在这里触发拉动事件,当没有在拉动的时候,如果是垂直拉动,并且距离大于指定的距离,设定为开始拉动,开始消费事件
        if (action == MotionEvent.ACTION_MOVE) {
            float delta = ev.getY() - initialY;
            //move事件只拦截符合滑动标准的事件,不满足的(比如轻微晃动的点击,不做处理)
            if (Math.abs(delta) >= touchSlop) {
                if (isRefreshing) {
                    return true;
                }
                if (!isDragging) {
                    isDragging = (delta > 0 && canChildPullDown())
                        || (delta < 0 && canChildPullUp());
                }
            }
        }
        return isDragging;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isRefreshing) {
            return true;
        }
        int action = MotionEventCompat.getActionMasked(event);
        if (action == MotionEvent.ACTION_MOVE) {
            if (isDragging) {
                offset = (event.getY() - initialY) / FRICTION;
                handleDragEvent(offset);
            }
        }
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            if (isDragging) {
                isDragging = false;
                offset = event.getY() - initialY;
                //没有设置回调的话就回弹
                if (canFireOnRefreshEvent(offset / FRICTION)) {
                    startRefresh(offset > 0);
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
    private void handleDragEvent(float distance) {
        float d = Math.abs(distance);
        if (distance > 0) {
            if (header != null) {
                if (d < header.refreshHeight()) {
                    header.onDragEvent(this, distance);
                } else {
                    header.onOverDragging(this, distance);
                }
            }
        } else {
            if (footer != null) {
                if (d < footer.refreshHeight()) {
                    footer.onDragEvent(this, distance);
                } else {
                    footer.onOverDragging(this, distance);
                }
            }
        }
    }


    /**
     * 根据移动的距离判断是否可以触发释放刷新事件
     *
     * @param dragDistance 带方向的距离
     * @return
     */
    private boolean canFireOnRefreshEvent(float dragDistance) {
        float rate = 1f;
        if (dragDistance > 0) {
            if (header != null){
                return Math.abs(dragDistance) >= header.refreshHeight() * rate;
            }
        } else {
            if (footer != null) {
                return Math.abs(dragDistance) >= footer.refreshHeight() * rate;
            }
        }
        return false;
    }


    /**
     * 切换到下拉刷新状态
     *
     * @param isPullingDown 是否是下拉刷新
     */
    private void startRefresh(boolean isPullingDown) {
        isRefreshing = true;
        if (isPullingDown) {
            if (header != null) {
                header.onRefreshing(this);
                if (onRefreshListener != null) {
                    onRefreshListener.onPull2Refresh(this);
                }
            }
        } else {
            if (footer != null) {
                footer.onRefreshing(this);
                if (onRefreshListener != null) {
                    onRefreshListener.onLoadMore(this);
                }
            }
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