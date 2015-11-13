package com.minutes.swipelayout.temp;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
    private static final int MAX_OFFSET_ANIMATION_DURATION = 700;

    private static final int PULL_DIRECTION_NON  = 0;
    private static final int PULL_DIRECTION_UP   = 1;
    private static final int PULL_DIRECTION_DOWN = 2;
    private int mDirection = PULL_DIRECTION_NON;

    private View mContentView;
    private ILoadLayout mHeaderView;
    private ILoadLayout mFooterView;

    private int touchSlop;
    private Measure mHeaderMeasure;
    private Measure mFooterMeasure;
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
                        mHeaderView = iLoadLayout;
                    } break;
                    case ILoadLayout.FOOTER:{
                        mFooterView = iLoadLayout;
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
            MarginLayoutParams lp = (MarginLayoutParams) mContentView.getLayoutParams();
            mContentHeight = mContentView.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
        }

        if (mHeaderView != null){
            mHeaderMeasure = mHeaderView.onChildMeasure(this, widthMeasureSpec, heightMeasureSpec);
        }

        if (mFooterView != null){
            mFooterMeasure = mFooterView.onChildMeasure(this, widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * Content view Measure
     */
    public Measure getContentMeasure(){
        return new Measure(0, mContentHeight);
    }

    /**
     * Header view Measure
     */
    public Measure getHeaderMeasure(){
        return mHeaderMeasure == null ? defaultMeasure() : mHeaderMeasure;
    }

    /**
     * Footer view Measure
     */
    public Measure getFooterMeasure(){
        return mFooterMeasure == null ? defaultMeasure() : mFooterMeasure;
    }

    //default measure, height = width = 0
    private Measure defaultMeasure(){
        return new Measure(0, 0);
    }

    /**
     * {@link #measureChildWithMargins(View, int, int, int, int)}
     */
    public void superMeasureChildWithMargins(View child,
                                             int parentWidthMeasureSpec, int widthUsed,
                                             int parentHeightMeasureSpec, int heightUsed){
        measureChildWithMargins(child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
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
        int offset = mScrollerHelper.getCurrOffsetY();
        if (mContentView != null){
            MarginLayoutParams lp = (MarginLayoutParams) mContentView.getLayoutParams();
            //is font mode
            boolean isFont = mScrollMode == ScrollMode.SCROLL_FONT;

            final int left = paddingLeft + lp.leftMargin;
            final int top = paddingTop + lp.topMargin + (isFont ? 0 : offset);
            final int right = left + mContentView.getMeasuredWidth();
            final int bottom = top + mContentView.getMeasuredHeight();
            if (!isRefreshing){
                mScrollerHelper.setContentOnLayoutRect(left, top, right, bottom);
            }
            mContentView.layout(left, top, right, bottom);
        }

        if (mHeaderView != null) {
            Layout layout = mHeaderView.onChildLayout(this, offset, l, t, r, b);
            if (!isRefreshing) {
                mScrollerHelper.setHeaderOnLayoutRect(layout.left, layout.top, layout.right, layout.bottom);
            }
        }

        if (mFooterView != null){
            Layout layout = mFooterView.onChildLayout(this, offset, l, t, r, b);

            if (!isRefreshing) {
                mScrollerHelper.setFooterOnLayoutRect(layout.left, layout.top, layout.right, layout.bottom);
            }
        }
    }

    @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isRefreshing){
            return true;
        }
        if (!isEnabled() || (!canChildPullDown() && !canChildPullUp())){
            return super.onInterceptTouchEvent(ev);
        }
        final int action = MotionEventCompat.getActionMasked(ev);
        switch (action){
            case MotionEvent.ACTION_DOWN:{
                isDragging = false;
                mScrollerHelper.setInitialY(ev.getY());
            } break;
            case MotionEvent.ACTION_MOVE:{
                float delta = ev.getY() - mScrollerHelper.getInitialY();
                boolean isMove = Math.abs(delta) >= touchSlop;
                if (isMove){
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
                    if (!isDragging){
                        mScroller.interceptScroll();
                        isDragging = true;
                    }
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
        if (!isDragging){
            return super.onTouchEvent(event);
        }

        final int action = MotionEventCompat.getActionMasked(event);
        switch (action){
            case MotionEvent.ACTION_DOWN:{
                isDragging = false;
            } break;
            case MotionEvent.ACTION_MOVE:{
                mScrollerHelper.moveY(event.getY());
                float delta = event.getY() - mScrollerHelper.getInitialY();
                if (delta < 0 && mDirection == PULL_DIRECTION_DOWN){
                    return false;
                }
                if (delta > 0 && mDirection == PULL_DIRECTION_UP){
                    return false;
                }

                move((int) mScrollerHelper.getCurrDeltaY());

            } break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:{
                isDragging = false;
                release();
            } break;
        }
        return true;
    }

    /**
     * 处理 MotionEvent.ACTION_MOVE
     */
    private void move(int delta){
        int currOffset = mScrollerHelper.getCurrOffsetY() + delta;
        mScrollerHelper.setCurrOffsetY(currOffset);

        if (mHeaderView != null && mDirection == PULL_DIRECTION_DOWN){
            mHeaderView.onMove(this, delta);
        }
        if (mFooterView != null && mDirection == PULL_DIRECTION_UP){
            mFooterView.onMove(this, delta);
        }

    }

    private ILoadLayout convert(View view){
        return (ILoadLayout) view;
    }

    private View convert(ILoadLayout iLoadLayout){
        return (View) iLoadLayout;
    }

    /**
     * 获取 ILoadLayout ，可能为空
     */
    @Nullable private ILoadLayout ILoadLayout(){
        ILoadLayout loadLayout = null;
        if (mDirection == PULL_DIRECTION_DOWN){
            loadLayout = mHeaderView;
        }
        else if (mDirection == PULL_DIRECTION_UP){
            loadLayout = mFooterView;
        }
        return loadLayout;
    }


    private boolean canOffsetTopAndBottom(){
        if (mDirection == PULL_DIRECTION_DOWN){
            return canChildPullDown();
        }
        return mDirection == PULL_DIRECTION_UP && canChildPullUp();
    }

    /**
     * 获取当前视图偏移量
     */
    private int currContentViewOffset(){
        return mScrollerHelper.getAbsCurrOffsetY();
    }

    /**
     * 偏移 child
     * 在没开始刷新的时候会触发 {@link ILoadLayout#pullOffset(SwipeLayout, int, int)}
     *
     * @param child     target view
     * @param offset    offset
     */
    public void childScrollY(View child, int offset){
        if (child != null && canOffsetTopAndBottom()){
            child.offsetTopAndBottom(offset);
            if (!isRefreshing){
                ILoadLayout iLoadLayout = ILoadLayout();
                if (iLoadLayout != null){
                    iLoadLayout.pullOffset(this, offset, mScrollerHelper.getAbsCurrOffsetY());
                }
            }
            invalidate();
        }
    }

    /**
     * 偏移 {@link #mContentView}
     * 在没开始刷新的时候会触发 {@link ILoadLayout#pullOffset(SwipeLayout, int, int)}
     *
     * @param offset    offset
     */
    public void contentScrollY(int offset){
        if (mContentView != null) {
            mContentView.offsetTopAndBottom(offset);
            if (!isRefreshing){
                ILoadLayout iLoadLayout = ILoadLayout();
                if (iLoadLayout != null){
                    iLoadLayout.pullOffset(this, offset, mScrollerHelper.getAbsCurrOffsetY());
                }
            }
            invalidate();
        }
    }

    /**
     * 释放操作处理
     */
    private void release(){
        int offset = currContentViewOffset();
        final ILoadLayout loadLayout = ILoadLayout();
        if (canPullToRefresh(loadLayout)){
            final int refreshHeight = loadLayout.refreshHeight();
            offset -= refreshHeight;
            if (mDirection == PULL_DIRECTION_DOWN){
                offset = -offset;
            }

            if (mDirection == PULL_DIRECTION_DOWN){
                doListenerPullToRefresh();
            } else {
                doListenerLoadMore();
            }
            isRefreshing = true;
            loadLayout.startRefreshing(SwipeLayout.this);
            mScroller.startScroll(offset, MAX_OFFSET_ANIMATION_DURATION);

        } else {
            refreshComplete();
        }

    }

    /**
     * 执行回调
     */
    private void doListenerPullToRefresh(){
        if (mListener != null){
            mListener.pullToRefresh();
        }
    }

    /**
     * 执行回调
     */
    private void doListenerLoadMore(){
        if (mListener != null){
            mListener.loadMore();
        }
    }

    /**
     * 自动刷新
     */
    private void autoRefreshHeader(){
        mDirection = PULL_DIRECTION_DOWN;
        final ILoadLayout loadLayout = mHeaderView;
        final int refreshHeight = loadLayout.refreshHeight();
        isRefreshing = true;
        doListenerPullToRefresh();
        loadLayout.startRefreshing(SwipeLayout.this);
        mScroller.startScroll(refreshHeight, MAX_OFFSET_ANIMATION_DURATION);
    }

    /**
     * 是否可以触发刷新
     */
    private boolean canPullToRefresh(ILoadLayout loadLayout){
        return loadLayout != null && loadLayout.canDoRefresh(this, mScrollerHelper.getAbsCurrOffsetY());
    }

    /**
     * 完成刷新
     */
    private void refreshComplete(){
        int offset = currContentViewOffset();
        final ILoadLayout loadLayout = ILoadLayout();
        if (loadLayout == null){
            return;
        }

        if (mDirection == PULL_DIRECTION_DOWN){
            offset = -offset;
        }

        mScroller.startScroll(offset, MAX_OFFSET_ANIMATION_DURATION, new ScrollListener() {
            @Override
            public void finish() {
                isRefreshing = false;
                loadLayout.completeRefresh(SwipeLayout.this);
            }
        });
    }

    /**
     * @return 子视图是否可以下拉, 这里的下拉是指是否已经滑到顶端了
     */
    private boolean canChildPullDown() {
        final View child  = mContentView;
        final View header = convert(mHeaderView);
        if (child == null || header == null){
            return false;
        }
        if (child instanceof AbsListView) {
            AbsListView abs = (AbsListView) child;
            int count = abs.getAdapter().getCount();
            if (count == 0) {
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
        final View footer = convert(mFooterView);
        if (child == null || footer == null){
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

    /**
     * 判断是否为 ILoadLayout
     */
    private boolean isILoadLayout(View view){
        return view != null && view instanceof ILoadLayout;
    }

    /**
     * 设置滑动模式{@link ScrollMode}
     */
    public void setScrollMode(@ScrollMode.Mode int scrollMode){
        this.mScrollMode = scrollMode;
        if (mScrollMode == ScrollMode.SCROLL_FONT){
            if (mHeaderView != null){
                convert(mHeaderView).bringToFront();
            }
            if (mFooterView != null){
                convert(mFooterView).bringToFront();
            }
        }
        else {
            if (mContentView != null){
                mContentView.bringToFront();
            }
        }
    }

    /**
     * 设置 Header
     */
    public void setHeaderView(@NonNull View header) {
        if (!isILoadLayout(header)){
            return;
        }
        if (mHeaderView != null) {
            removeView(convert(mHeaderView));
        }
        ViewGroup.LayoutParams lp = header.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            header.setLayoutParams(lp);
        }
        mHeaderView = (ILoadLayout) header;
        addView(header);
    }

    /**
     * 设置 Footer
     */
    public void setFooterView(@NonNull View footer) {
        if (!isILoadLayout(footer)){
            return;
        }
        if (mFooterView != null){
            removeView(convert(mFooterView));
        }
        ViewGroup.LayoutParams lp = footer.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            footer.setLayoutParams(lp);
        }
        mFooterView = (ILoadLayout) footer;
        addView(footer);
    }

    /**
     * 设置是否刷新
     */
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
                move(delta);
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

        /**
         * 终端当前自动滑动（如果有的话）
         */
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

        /**
         * 自动滑动
         *
         * @param distance  滑动距离
         * @param duration  持续时间
         * @param listener  结束监听
         */
        public void startScroll(int distance, int duration, ScrollListener listener){
            mScrollListener = listener;
            mLastY = 0;
            isAutoRunning = true;
            startScroll(0, 0, 0, distance, duration);
            post(this);
        }

        /**
         * 自动滑动
         *
         * @param distance 滑动距离
         * @param duration 持续时间
         */
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
