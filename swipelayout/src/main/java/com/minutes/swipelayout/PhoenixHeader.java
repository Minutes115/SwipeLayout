package com.minutes.swipelayout;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * User: LiangLong
 * Date: 2015-11-05
 * Time: 09:46
 * Note: com.minutes.library.widget.swipe2refresh
 */
public class PhoenixHeader implements ILoadLayout {
    private static final String TAG = PhoenixHeader.class.getSimpleName();

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
            height = SwipeToRefreshLayout.dip2Px(context, 100);
            Log.d(TAG,"refreshHeight = " + height);
        }
        if (mLoadView == null) {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.MarginLayoutParams.MATCH_PARENT,
                                                                       refreshHeight());
            lp.gravity = Gravity.TOP;

            ImageView imageView = new ImageView(context);
            imageView.setImageResource(R.drawable.sun);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setLayoutParams(lp);
            imageView.setBackgroundColor(context.getResources().getColor(android.R.color.holo_red_dark));

            mLoadView = imageView;
            layout.addView(mLoadView);
        }
    }

    @Override
    public void onLayout(SwipeToRefreshLayout layout, boolean changed, float offset, int left, int top, int right, int bottom) {
        if (mLoadView != null) {
            mLoadView.layout(left, 0, right, refreshHeight());
            layout.getContentView().bringToFront();
        }
    }

    @Override
    public void onDetach(SwipeToRefreshLayout layout) {
        layout.removeView(getLoadView());
    }

    @Override
    public void onDragEvent(SwipeToRefreshLayout layout, float offset) {

        ViewCompat.setTranslationY(layout.getContentView(), offset);
    }

    @Override
    public void onOverDragging(SwipeToRefreshLayout layout, float offset) {
        if (Math.abs(offset) <= getLoadView().getHeight()) {

            ViewCompat.setTranslationY(layout.getContentView(), offset);
        }
    }

    @Override
    public void onRefreshing(final SwipeToRefreshLayout strl) {

        View v = strl.getContentView();
        smoothMove(v, ViewCompat.getY(v), refreshHeight());
    }

    @Override
    public void stopRefresh(SwipeToRefreshLayout strl) {

        View v = strl.getContentView();
        smoothMove(v, ViewCompat.getY(v), 0);
    }

    /**
     * 自动平滑移动
     *
     * @param v
     * @param fromY
     * @param toY
     */
    private void smoothMove(View v, float fromY, float toY) {
        if (runnable != null) {
            runnable.stop();
        }
        runnable = new SmoothTranslationYHelper(v, (int) fromY, (int) toY);
        v.post(runnable);
    }

}
