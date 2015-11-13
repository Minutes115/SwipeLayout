package com.minutes.swipelayout.temp.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import com.minutes.swipelayout.temp.Layout;
import com.minutes.swipelayout.temp.Measure;
import com.minutes.swipelayout.temp.SwipeLayout;

/**
 * <p>Description  : MaterialHeader.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/11/12.</p>
 * <p>Time         : 上午10:29.</p>
 */
public class MaterialHeader extends LinearLayout implements com.minutes.swipelayout.temp.ILoadLayout {
    private static final int CIRCLE_DIAMETER = 40;
    private static final int CIRCLE_BG_LIGHT = 0xFFFAFAFA;

    private MaterialProgressDrawable mDrawable;
    private float mScale = 1f;

    private Animation mScaleAnimation = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            mScale = 1f - interpolatedTime;
            mDrawable.setAlpha((int) (255 * mScale));
            invalidate();
        }
    };

    public MaterialHeader(Context context) {
        super(context);
        init();
    }

    public MaterialHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @TargetApi(11) public MaterialHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21) public MaterialHeader(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public int dip2px(float dpValue){
        return dip2px(getContext(), dpValue);
    }

    private void init(){
        mDrawable = new MaterialProgressDrawable(getContext(), this);
        mDrawable.setCallback(this);

        CircleImageView mCircleView = new CircleImageView(getContext(), CIRCLE_BG_LIGHT, CIRCLE_DIAMETER/2);
        mCircleView.setImageDrawable(mDrawable);
        mCircleView.setVisibility(View.VISIBLE);

        addView(mCircleView);
        setGravity(Gravity.CENTER_HORIZONTAL);

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dip2px(50));
        setLayoutParams(lp);
    }

    @Override
    public int viewType() {
        return HEADER;
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
        final int paddingLeft = parent.getPaddingLeft();
        final int paddingTop  = parent.getPaddingTop();

        MarginLayoutParams lp = (MarginLayoutParams) getLayoutParams();

        final int left   = paddingLeft + lp.leftMargin;
        final int top    = paddingTop + lp.topMargin + offset - parent.getHeaderMeasure().height;
        final int right  = left + getMeasuredWidth();
        final int bottom = top + getMeasuredHeight();

        layout(left, top, right, bottom);
        return new Layout(left, top, right, bottom);
    }

    @Override
    public void onMove(SwipeLayout parent, int delta) {
        parent.childScrollY(this, delta);
        SwipeLayout.log("onMove == " + delta);
    }

    @Override
    public int refreshHeight() {
        return dip2px(50);
    }

    @Override
    public boolean canDoRefresh(SwipeLayout parent, int pullDistance) {
        return pullDistance > dip2px(50);
    }

    @Override
    public void startRefreshing(final SwipeLayout parent) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                parent.setRefresh(false);
            }
        }, 2000);
    }

    @Override
    public void pullOffset(SwipeLayout parent, int offset, int distance) {
        if (canDoRefresh(parent, distance)){
            mDrawable.start();
        } else {
            float percent = Math.min(1f, distance / dip2px(50));

            mDrawable.setAlpha((int) (255 * percent));
            mDrawable.showArrow(true);

            float strokeStart = ((percent) * .8f);
            mDrawable.setStartEndTrim(0f, Math.min(0.8f, strokeStart));
            mDrawable.setArrowScale(Math.min(1f, percent));

            // magic
            float rotation = (-0.25f + .4f * percent + percent * 2) * .5f;
            mDrawable.setProgressRotation(rotation);
        }

    }

    @Override
    public void completeRefresh(SwipeLayout parent) {
        mDrawable.stop();
    }

    public static class MaterialFooter extends PullToRefreshFooter{

        public MaterialFooter(Context context) {
            super(context);
        }

        @Override
        public int viewType() {
            return FOOTER;
        }
    }
}