package com.minutes.swipelayout.temp;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Scroller;

import com.minutes.swipelayout.HeaderView;

/**
 * <p>Description  : SwipeLayout.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/11/6.</p>
 * <p>Time         : 上午11:46.</p>
 */
public class SwipeLayout extends ViewGroup{
    private static final float FRICTION = 3.0f;        //手指在屏幕上的移动距离与拉动的比例

    private View mContentView;
    private View mHeaderView;
    private View mFooterView;

    private int touchSlop;
    private int mHeaderHeight;

    private boolean isDragging;
    private boolean isRefreshing;

    private ScrollHelper mScrollerHelper;
    private Scroller mScroller;

    public SwipeLayout(Context context) {
        super(context);
        init();
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21) public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        mScroller = new Scroller(getContext());
        mScrollerHelper = new ScrollHelper(FRICTION);

        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        HeaderView head = new HeaderView(getContext());
        ViewGroup.LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        head.setLayoutParams(params);
        addView(head);
    }

    @Override protected boolean checkLayoutParams(LayoutParams p) {
        return super.checkLayoutParams(p) && p instanceof MarginLayoutParams;
    }

    @Override protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override protected void onFinishInflate() {
        final int count = getChildCount();
        for (int i = 0; i < count; i++){
            if (!(getChildAt(i) instanceof ILoadLayout)){
                mContentView = getChildAt(i);
            } else {
                mHeaderView = getChildAt(i);
            }
        }
        super.onFinishInflate();
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mContentView != null){
            measureContentView(mContentView, widthMeasureSpec, heightMeasureSpec);
        }

        if (mHeaderView != null){
            measureChildWithMargins(mHeaderView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            MarginLayoutParams lp = (MarginLayoutParams) mHeaderView.getLayoutParams();
            mHeaderHeight = mHeaderView.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
        }
    }

    @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        if (mContentView != null){
            MarginLayoutParams lp = (MarginLayoutParams) mContentView.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int top = paddingTop + lp.topMargin + mScrollerHelper.getOffsetY();
            final int right = left + mContentView.getMeasuredWidth();
            final int bottom = top + mContentView.getMeasuredHeight();
            mContentView.layout(left, top, right, bottom);
        }
        if (mHeaderView != null) {
            MarginLayoutParams lp = (MarginLayoutParams) mHeaderView.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int top = paddingTop + lp.topMargin + mScrollerHelper.getOffsetY() - mHeaderHeight;
            final int right = left + mHeaderView.getMeasuredWidth();
            final int bottom = top + mHeaderView.getMeasuredHeight();
            mHeaderView.layout(left, top, right, bottom);

        }
    }

    @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!canChildPullDown() && !canChildPullUp()){
            return super.onInterceptTouchEvent(ev);
        }
        final int action = MotionEventCompat.getActionMasked(ev);
        switch (action){
            case MotionEvent.ACTION_DOWN:{
                mScrollerHelper.setInitialY(ev.getY());
                mScrollerHelper.setCurrY(ev.getY());
            } break;
            case MotionEvent.ACTION_MOVE:{
                float delta = ev.getY() - mScrollerHelper.getInitialY();
                if (delta > 0) {//手指下拉
                    if (!canChildPullDown()) {
                        return false;
                    }
                }
                if (delta < 0) {//手指上拉
                    if (!canChildPullUp()) {
                        return false;
                    }
                }
                if (!isDragging && Math.abs(delta) >= touchSlop) {
                    isDragging = true;
                }
                return isDragging;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:{
                isDragging = false;
                return isRefreshing;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        if (action == MotionEvent.ACTION_DOWN) {
            isDragging = false;
        }
        if (action == MotionEvent.ACTION_MOVE) {
            if (isDragging) {
                mScrollerHelper.setCurrY(event.getY());
                // TODO: 15/11/7  Direction of judgment
                contentScrollY(mScrollerHelper.getOffsetY());
                headerScrollY(mScrollerHelper.getOffsetY());
            }
        }
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            if (isDragging) {
                isDragging = false;
                // TODO: 15/11/7  Direction of judgment
                contentScrollY(-mContentView.getTop());
                headerScrollY(-(mHeaderView.getTop() + mHeaderHeight));
            }
        }
        return super.onTouchEvent(event);
    }

    private void contentScrollY(int delta){
        if (mContentView != null){
            mContentView.offsetTopAndBottom(delta);
        }
    }

    private void headerScrollY(int delta){
        if (mHeaderView != null){
            mHeaderView.offsetTopAndBottom(delta);
        }
    }

    private void measureContentView(View child,
                                    int parentWidthMeasureSpec,
                                    int parentHeightMeasureSpec) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

        final int childWidthMeasureSpec = getChildMeasureSpec(
                parentWidthMeasureSpec,
                getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin, lp.width
        );

        final int childHeightMeasureSpec = getChildMeasureSpec(
                parentHeightMeasureSpec,
                getPaddingTop() + getPaddingBottom() + lp.topMargin, lp.height
        );

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    /**
     * @return 子视图是否可以下拉, 这里的下拉是指是否已经滑到顶端了
     */
    private boolean canChildPullDown() {
        final View child = mContentView;
        if (child == null){
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
        final View child = mContentView;
        if (child == null){
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
}
