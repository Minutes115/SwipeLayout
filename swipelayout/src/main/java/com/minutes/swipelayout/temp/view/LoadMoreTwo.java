package com.minutes.swipelayout.temp.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.minutes.swipelayout.temp.ILoadLayout;
import com.minutes.swipelayout.temp.Layout;
import com.minutes.swipelayout.temp.Measure;
import com.minutes.swipelayout.temp.SwipeLayout;


/**
 * <p>Description  : LoadMoreTwo.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/12/5.</p>
 * <p>Time         : 下午2:13.</p>
 */
public class LoadMoreTwo extends LinearLayout implements ILoadLayout {
    public static final int NORMAL = 0;
    public static final int RELEASE = 1;
    public static final int REFRESHING = 2;
    public static final int RESET = 3;

    public static final String TEXT_PULL_TO_LOAD_MORE = "上拉加载更多";
    public static final String TEXT_REFRESHING = "正在刷新";
    public static final String TEXT_RELEASE_TO_REFRESH = "松开立即刷新";

    private TextView text;
    private ProgressBar progress;

    private int state = NORMAL;

    public LoadMoreTwo(Context context) {
        super(context);
        init();
    }

    public LoadMoreTwo(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @TargetApi(11)
    public LoadMoreTwo(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public LoadMoreTwo(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public int dip2px(float dpValue) {
        return dip2px(getContext(), dpValue);
    }

    private void init() {

        View root = LayoutInflater.from(getContext()).inflate(R.layout.item_more, this);
        text = (TextView) root.findViewById(R.id.hintText);
        text.setText(TEXT_PULL_TO_LOAD_MORE);
        progress = (ProgressBar) root.findViewById(R.id.refresh_loading);
        progress.setVisibility(GONE);

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dip2px(60));
        setLayoutParams(lp);
    }


    public void setState(int state) {
        if (this.state == state) {
            return;
        }
        switch (state) {
            case NORMAL:
                text.setText(TEXT_PULL_TO_LOAD_MORE);
                progress.setVisibility(GONE);
                break;
            case REFRESHING:
                text.setText(TEXT_REFRESHING);
                progress.setVisibility(VISIBLE);
                break;
            case RESET:
                progress.setVisibility(GONE);
                break;
            case RELEASE:
                text.setText(TEXT_RELEASE_TO_REFRESH);
                progress.setVisibility(GONE);
                break;
        }
        this.state = state;
    }

    @Override
    public int viewType() {
        return FOOTER;
    }

    @NonNull
    @Override
    public Measure onChildMeasure(SwipeLayout parent, int widthMeasureSpec, int heightMeasureSpec) {
        parent.superMeasureChildWithMargins(this, widthMeasureSpec, 0, heightMeasureSpec, 0);

        MarginLayoutParams lp = (MarginLayoutParams) getLayoutParams();
        int mHeaderHeight = getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
        return new Measure(0, mHeaderHeight);
    }

    @NonNull
    @Override
    public Layout onChildLayout(SwipeLayout parent, int offset, int pl, int pt, int pr, int pb) {
        int paddingLeft = parent.getPaddingLeft();
        int paddingTop = parent.getPaddingTop();

        MarginLayoutParams lp = (MarginLayoutParams) getLayoutParams();

        final int left = paddingLeft + lp.leftMargin;
        final int top = paddingTop + lp.topMargin + offset + parent.getContentMeasure().height;
        final int right = left + getMeasuredWidth();
        final int bottom = top + getMeasuredHeight();
        layout(left, top, right, bottom);
        return new Layout(left, top, right, bottom);
    }

    @Override
    public boolean onTouchMove(SwipeLayout parent, int delta, int offset) {
        parent.contentScrollY(delta);
        parent.childScrollY(this, delta);
        return false;
    }

    @Override
    public int refreshHeight() {
        return dip2px(60);
    }

    @Override
    public boolean canDoRefresh(SwipeLayout parent, int pullDistance) {
        return pullDistance > dip2px(60);
    }

    @Override
    public void startRefreshing(final SwipeLayout parent) {
        setState(REFRESHING);
    }

    @Override
    public void scrollOffset(SwipeLayout parent, int offset, int distance) {
        if (canDoRefresh(parent, distance)) {
            setState(RELEASE);
        } else {
            setState(NORMAL);
        }
    }

    @Override
    public void completeRefresh(SwipeLayout parent) {
        setState(RESET);
    }
}