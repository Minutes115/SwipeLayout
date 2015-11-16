package com.minutes.swipelayout;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.minutes.swipelayout.temp.view.CircleImageView;
import com.minutes.swipelayout.temp.view.MaterialProgressDrawable;

/**
 * User: LiangLong
 * Date: 2015-11-13
 * Time: 09:59
 * Note: com.minutes.swipelayout
 */
public class MaterialHeader implements ILoadLayout {
    private static final String TAG = MaterialHeader.class.getSimpleName();

    private static final int CIRCLE_DIAMETER = 40;
    private static final int CIRCLE_BG_LIGHT = 0xFFFAFAFA;

    private MaterialProgressDrawable drawable;
    private float mScale = 1f;
    private Animation mScaleAnimation = new Animation() {

        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            mScale = 1f - interpolatedTime;
            drawable.setAlpha((int) (255 * mScale));
            getLoadView().postInvalidate();
        }

    };

    private int height;
    private View mLoadView;
    private SmoothTranslationYHelper runnable;

    @Override
    public View getLoadView() {
        return mLoadView;
    }

    @Override
    public int refreshHeight() {
        return height;
    }

    @Override
    public void onAttach(SwipeToRefreshLayout layout) {
        Context context = layout.getContext();
        if (height == 0) {
            height = SwipeToRefreshLayout.dip2Px(context, 60);
        }
        if (mLoadView == null) {
            //箭头翻转动画
            mLoadView = new LinearLayout(layout.getContext());
            ((LinearLayout) mLoadView).setGravity(Gravity.CENTER);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.MarginLayoutParams.MATCH_PARENT,
                                                                       refreshHeight());
            lp.gravity = Gravity.TOP;
            mLoadView.setLayoutParams(lp);

            drawable = new MaterialProgressDrawable(context, mLoadView);
            drawable.setCallback(mLoadView);

            CircleImageView circleImageView = new CircleImageView(context,
                                                                  CIRCLE_BG_LIGHT,
                                                                  CIRCLE_DIAMETER / 2);
            circleImageView.setImageDrawable(drawable);
            circleImageView.setVisibility(View.VISIBLE);

            ((LinearLayout) mLoadView).addView(circleImageView);
            layout.addView(mLoadView);
        }
    }

    @Override
    public void onLayout(SwipeToRefreshLayout layout,boolean changed, float offset,int left,int top, int right, int bottom) {
        if (mLoadView != null) {
            mLoadView.layout(mLoadView.getLeft(),
                             mLoadView.getTop() - refreshHeight(),
                             mLoadView.getRight(),
                             mLoadView.getBottom() - refreshHeight());
            mLoadView.bringToFront();
        }
    }

    @Override
    public void onDetach(SwipeToRefreshLayout layout) {
        layout.removeView(getLoadView());
    }

    @Override
    public void onDragEvent(SwipeToRefreshLayout swipeToRefreshLayout, float offset) {
        animation(offset);

        //移动的效果在这里
        View v = getLoadView();
        ViewCompat.setTranslationY(v, offset);
    }

    @Override
    public void onOverDragging(SwipeToRefreshLayout swipeToRefreshLayout, float offset) {
        animation(offset);

        //移动的效果在这里
        View v = getLoadView();
        ViewCompat.setTranslationY(v, offset);
    }

    private void animation(float offset) {
        float percent = Math.min(1f, offset / 220);

        drawable.setAlpha((int) (255 * percent));
        drawable.showArrow(true);

        float strokeStart = ((percent) * .8f);
        drawable.setStartEndTrim(0f, Math.min(0.8f, strokeStart));
        drawable.setArrowScale(Math.min(1f, percent));

        // magic
        float rotation = (-0.25f + .4f * percent + percent * 2) * .5f;
        drawable.setProgressRotation(rotation);
    }

    @Override
    public void onRefreshing(final SwipeToRefreshLayout strl) {
        drawable.start();

        View v = getLoadView();
        smoothScroll(v, ViewCompat.getY(v), refreshHeight());
    }

    @Override
    public void stopRefresh(SwipeToRefreshLayout strl) {
        drawable.stop();

        View v = getLoadView();
        smoothScroll(v, (int) ViewCompat.getY(v), 0);
    }

    private void smoothScroll(View view, float fromScrollY, float toScrollY) {
        if (runnable != null) {
            runnable.stop();
        }
        runnable = new SmoothTranslationYHelper(view, (int) fromScrollY, (int) toScrollY);
        view.post(runnable);
    }

}
