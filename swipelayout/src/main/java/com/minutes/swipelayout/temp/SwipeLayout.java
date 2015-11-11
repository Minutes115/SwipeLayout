package com.minutes.swipelayout.temp;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.Scroller;

/**
 * <p>Description  : SwipeLayout.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/11/6.</p>
 * <p>Time         : 上午11:46.</p>
 */
public class SwipeLayout extends ViewGroup{
    private static final float FRICTION = 3.0f;        //手指在屏幕上的移动距离与拉动的比例

    private static final int PULL_DIRECTION_NON  = 0;
    private static final int PULL_DIRECTION_UP   = 1;
    private static final int PULL_DIRECTION_DOWN = 2;
    private int mDirection = PULL_DIRECTION_NON;

    private View mContentView;
    private View mHeaderView;
    private View mFooterView;

    private int touchSlop;
    private int mHeaderHeight;
    private int mFooterHeight;
    private int mContentHeight;

    private boolean isDragging;
    private boolean isRefreshing;

    private ScrollHelper mScrollerHelper;
    private ScrollerImpl mScroller;
    private int mScrollMode;

    private SwipeLayoutRefreshListener mListener;

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
        mScroller       = new ScrollerImpl(getContext());
        mScrollerHelper = new ScrollHelper(FRICTION);

        //default mode
        mScrollMode     = ScrollMode.SCROLL_FOLLOW;
        touchSlop       = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        //debug code
        TestHeader head = new TestHeader(getContext());
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
            final View child = getChildAt(i);
            if (child instanceof ILoadLayout){
                final ILoadLayout iLoadLayout = (ILoadLayout) child;
                switch (iLoadLayout.viewType()){
                    case ILoadLayout.HEADER:{
                        mHeaderView = child;
                    } break;
                    case ILoadLayout.FOOTER:{
                        mFooterView = child;
                    } break;
                }
            } else {
                mContentView = child;
            }
        }

        setScrollMode(mScrollMode);
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

        if (mFooterView != null){
            measureChildWithMargins(mFooterView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            MarginLayoutParams lp = (MarginLayoutParams) mFooterView.getLayoutParams();
            mFooterHeight = mFooterView.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
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

    @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        if (mContentView != null){
            MarginLayoutParams lp = (MarginLayoutParams) mContentView.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int top = paddingTop + lp.topMargin + mScrollerHelper.getCurrOffsetY();
            final int right = left + mContentView.getMeasuredWidth();
            final int bottom = top + mContentView.getMeasuredHeight();
            mScrollerHelper.setContentOnLayoutRect(left, top, right, bottom);
            mContentView.layout(left, top, right, bottom);
        }

        if (mHeaderView != null) {
            MarginLayoutParams lp = (MarginLayoutParams) mHeaderView.getLayoutParams();
            //is follow mode
            boolean isScrollFollow = (mScrollMode == ScrollMode.SCROLL_FOLLOW);

            final int left = paddingLeft + lp.leftMargin;
            final int top = paddingTop + lp.topMargin + mScrollerHelper.getCurrOffsetY() -
                    (isScrollFollow ? mHeaderHeight : 0);
            final int right = left + mHeaderView.getMeasuredWidth();
            final int bottom = top + mHeaderView.getMeasuredHeight();
            mScrollerHelper.setHeaderOnLayoutRect(left, top, right, bottom);
            mHeaderView.layout(left, top, right, bottom);
        }

        if (mFooterView != null){
            MarginLayoutParams lp = (MarginLayoutParams) mFooterView.getLayoutParams();
            //is follow mode
            boolean isScrollFollow = (mScrollMode == ScrollMode.SCROLL_FOLLOW);

            final int left = paddingLeft + lp.leftMargin;
            final int top = paddingTop + lp.topMargin + mScrollerHelper.getCurrOffsetY() -
                    (isScrollFollow ? mContentHeight : 0);
            final int right = left + mFooterView.getMeasuredWidth();
            final int bottom = top + mFooterView.getMeasuredHeight();
            mScrollerHelper.setFooterOnLayoutRect(left, top, right, bottom);
            mFooterView.layout(left, top, right, bottom);
        }
    }

    @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isRefreshing){
            return true;
        }
        if (!canChildPullDown() && !canChildPullUp()){
            return super.onInterceptTouchEvent(ev);
        }
        final int action = MotionEventCompat.getActionMasked(ev);
        switch (action){
            case MotionEvent.ACTION_DOWN:{
                isDragging = false;
                mScrollerHelper.setInitialY(ev.getY());
                mDirection = PULL_DIRECTION_NON;
                mScroller.interceptScroll();
            } break;
            case MotionEvent.ACTION_MOVE:{
                float delta = ev.getY() - mScrollerHelper.getInitialY();
                if (delta > 0) {//手指下拉
                    if (!canChildPullDown() || mHeaderView == null) {
                        log("MotionEvent.ACTION_MOVE --- can not pull down!");
                        return false;
                    } else {
                        mDirection = PULL_DIRECTION_DOWN;
                    }
                }
                if (delta < 0) {//手指上拉
                    if (!canChildPullUp() || mFooterView == null) {
                        log("MotionEvent.ACTION_MOVE --- can not pull up!");
                        return false;
                    } else {
                        mDirection = PULL_DIRECTION_UP;
                    }
                }
                if (!isDragging && Math.abs(delta) >= touchSlop) {
                    isDragging = true;
                }
            } break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:{
                isDragging = false;
            } break;
        }
        return isDragging;
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        switch (action){
            case MotionEvent.ACTION_DOWN:{
                isDragging = false;
            } break;
            case MotionEvent.ACTION_MOVE:{
                if (isDragging) {
                    mScrollerHelper.setCurrY(event.getY());
                    switch (mScrollMode){
                        case ScrollMode.SCROLL_FOLLOW:
                            contentScrollY(mScrollerHelper.getCurrOffsetY());
                            headerScrollY(mScrollerHelper.getCurrOffsetY());
                            break;
                        case ScrollMode.SCROLL_BACK:
                            contentScrollY(mScrollerHelper.getCurrOffsetY());
                            break;
                        case ScrollMode.SCROLL_FONT:
                            headerScrollY(mScrollerHelper.getCurrOffsetY());
                            break;
                    }
                }
            } break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:{
                if (isDragging) {
                    isDragging = false;
                    release();
                }
            } break;
        }
        return super.onTouchEvent(event);
    }

    private ILoadLayout ILoadLayout(View view){
        return (ILoadLayout) view;
    }

    private ILoadLayout ILoadLayout(){
        ILoadLayout loadLayout = null;
        if (mDirection == PULL_DIRECTION_DOWN){
            loadLayout = ILoadLayout(mHeaderView);
        }
        else if (mDirection == PULL_DIRECTION_UP){
            loadLayout = ILoadLayout(mFooterView);
        }
        return loadLayout;
    }

    private void contentScrollY(int offset){
        if (mContentView != null){
            mContentView.offsetTopAndBottom(offset);
        }
    }

    private void headerScrollY(int offset){
        if (mHeaderView != null){
            mHeaderView.offsetTopAndBottom(offset);
            ILoadLayout(mHeaderView).pullOffset(this, offset, mScrollerHelper.getOffsetY());
        }
    }

    private void footerScrollY(int offset){
        if (mFooterView != null){
            mFooterView.offsetTopAndBottom(offset);
            ILoadLayout(mFooterView).pullOffset(this, offset, mScrollerHelper.getOffsetY());
        }
    }

    private int currContentViewOffset(){
        return Math.abs(mContentView.getTop()) - mScrollerHelper.getContentOnLayoutRect().top;
    }

    private void release(){
        int offset = currContentViewOffset();
        final ILoadLayout loadLayout = ILoadLayout();
        if (canPullToRefresh(loadLayout)){
            offset -= loadLayout.refreshHeight();
            if (mDirection == PULL_DIRECTION_DOWN){
                offset = -offset;
            }

            mScroller.startScroll(offset, 1000, new ScrollListener() {
                @Override
                public void finish() {
                    if (mDirection == PULL_DIRECTION_DOWN){
                        doListenerPullToRefresh();
                    } else {
                        doListenerLoadMore();
                    }
                    isRefreshing = true;
                    loadLayout.startRefreshing(SwipeLayout.this);
                }
            });

        } else {
            refreshComplete();
        }

    }

    private void doListenerPullToRefresh(){
        if (mListener != null){
            mListener.pullToRefresh();
        }
    }

    private void doListenerLoadMore(){
        if (mListener != null){
            mListener.loadMore();
        }
    }

    private void autoRefreshHeader(){
        mDirection = PULL_DIRECTION_DOWN;
        final ILoadLayout loadLayout = ILoadLayout(mHeaderView);
        int refreshHeight = loadLayout.refreshHeight();
        mScroller.startScroll(refreshHeight, 1000, new ScrollListener() {
            @Override
            public void finish() {
                isRefreshing = true;
                doListenerPullToRefresh();
                loadLayout.startRefreshing(SwipeLayout.this);
            }
        });
    }

    private boolean canPullToRefresh(ILoadLayout loadLayout){
        return loadLayout != null && loadLayout.canDoRefresh(this, mScrollerHelper.getOffsetY());
    }

    private void refreshComplete(){
        int offset = currContentViewOffset();
        final ILoadLayout loadLayout = ILoadLayout();
        if (loadLayout == null){
            return;
        }

        if (mDirection == PULL_DIRECTION_DOWN){
            offset = -offset;
        }

        mScroller.startScroll(offset, 1000, new ScrollListener() {
            @Override
            public void finish() {
                loadLayout.completeRefresh(SwipeLayout.this);
                mDirection = PULL_DIRECTION_NON;
                isRefreshing = false;
            }
        });
    }

    /**
     * @return 子视图是否可以下拉, 这里的下拉是指是否已经滑到顶端了
     */
    private boolean canChildPullDown() {
        final View child  = mContentView;
        final View header = mHeaderView;
        if (child == null){
            return false;
        }
        if (child instanceof AbsListView) {
            AbsListView abs = (AbsListView) child;
            int count = abs.getAdapter().getCount();
            if (count == 0 || header == null) {
                return true;
            }
            if (abs.getFirstVisiblePosition() == 0 && header.getTop() >= abs.getPaddingTop()) {
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
        final View child  = mContentView;
        final View footer = mFooterView;
        if (child == null){
            return false;
        }
        if (child instanceof AbsListView) {
            AbsListView av = (AbsListView) child;
            int count = av.getAdapter().getCount();
            if (av.getLastVisiblePosition() == count - 1 && footer.getBottom() < av.getBottom()) {
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

    private boolean isILoadLayout(View view){
        return view != null && view instanceof ILoadLayout;
    }

    public void setScrollMode(@ScrollMode.Mode int scrollMode){
        this.mScrollMode = scrollMode;
        if (mScrollMode == ScrollMode.SCROLL_FONT){
            if (mHeaderView != null){
                mHeaderView.bringToFront();
            }
            if (mFooterView != null){
                mFooterView.bringToFront();
            }
        }
        else {
            if (mContentView != null){
                mContentView.bringToFront();
            }
        }
    }

    public void setHeaderView(@NonNull View header) {
        if (!isILoadLayout(header)){
            return;
        }
        if (mHeaderView != null) {
            removeView(mHeaderView);
        }
        ViewGroup.LayoutParams lp = header.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            header.setLayoutParams(lp);
        }
        mHeaderView = header;
        addView(header);
    }

    public void setFooterView(@NonNull View footer) {
        if (!isILoadLayout(footer)){
            return;
        }
        if (mFooterView != null){
            removeView(mFooterView);
        }
        ViewGroup.LayoutParams lp = footer.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            footer.setLayoutParams(lp);
        }
        mHeaderView = footer;
        addView(footer);
    }

    public void setRefresh(boolean refresh){
        if (refresh){
            autoRefreshHeader();
        } else {
            refreshComplete();
        }
    }

    public void setSwipeLayoutRefreshListener(SwipeLayoutRefreshListener listener){
        this.mListener = listener;
    }

    /*                  Inner Scroller                  */
    private class ScrollerImpl extends Scroller implements Runnable{

        private int mLastY;
        private boolean isAutoRunning;
        private ScrollListener mScrollListener;

        public ScrollerImpl(Context context) {
            super(context);
        }

        public ScrollerImpl(Context context, Interpolator interpolator) {
            super(context, interpolator);
        }

        @TargetApi(11) public ScrollerImpl(Context context, Interpolator interpolator, boolean flywheel) {
            super(context, interpolator, flywheel);
        }

        @Override public void run() {
            if (computeScrollOffset() && !isFinished()){
                int delta = getCurrY() - mLastY;
                mLastY = getCurrY();
                switch (mScrollMode){
                    case ScrollMode.SCROLL_FOLLOW:
                        contentScrollY(delta);
                        headerScrollY(delta);
                        break;
                    case ScrollMode.SCROLL_BACK:
                        contentScrollY(delta);
                        break;
                    case ScrollMode.SCROLL_FONT:
                        headerScrollY(delta);
                        break;
                }
                post(this);
            } else {
                if (mScrollListener != null){
                    mScrollListener.finish();
                }
                isAutoRunning = false;
                mLastY = 0;
                removeCallbacks(this);
            }
        }

        public boolean isAutoRunning(){
            return isAutoRunning;
        }

        public void interceptScroll(){
            if (isAutoRunning && !isFinished()){
                forceFinished(true);
                if (mScrollListener != null){
                    mScrollListener.finish();
                }
                isAutoRunning = false;
                mLastY = 0;
                removeCallbacks(this);
            }
        }

        public void startScroll(int distance, int duration, ScrollListener listener){
            mScrollListener = listener;
            mLastY = 0;
            isAutoRunning = true;
            startScroll(0, 0, 0, distance, duration);
            post(this);
        }

        public void startScroll(int distance, int duration){
            startScroll(distance, duration, null);
        }

    }

    public interface ScrollListener{
        void finish();
    }

    public static void log(String msg){
        Log.e(SwipeLayout.class.getSimpleName(), msg);
    }
}
