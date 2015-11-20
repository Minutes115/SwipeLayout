package com.minutes.swipelayout;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

/**
 * User: LiangLong
 * Date: 2015-11-05
 * Time: 09:46
 * Note: com.minutes.library.widget.swipe2refresh
 */
public class PhoenixHeader implements ILoadLayout {
    private static final String TAG = PhoenixHeader.class.getSimpleName();

    private int refreshHeight, viewHeight;
    private View mLoadView;
    private ProgressBar refreshingBar;
    private ImageView pullImageView;
    private SmoothTranslationYHelper runnable;

    private Drawable pullDrawable, releaseDrawable;

    @Override
    public View getLoadView() {
        return mLoadView;
    }

    @Override
    public int refreshHeight() {
        return refreshHeight;
    }

    @Override
    public void onAttach(SwipeToRefreshLayout layout) {
        Context context = layout.getContext();
        if (refreshHeight == 0) {
            refreshHeight = SwipeToRefreshLayout.dip2Px(context, 110);
            viewHeight = refreshHeight + 60;
        }
        if (mLoadView == null) {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.MarginLayoutParams.MATCH_PARENT,
                                                                       viewHeight);
            lp.gravity = Gravity.TOP;

            mLoadView = View.inflate(context, R.layout.layout_phoenix, null);
            mLoadView.setLayoutParams(lp);
            refreshingBar = (ProgressBar) mLoadView.findViewById(R.id.pull_progressbar);
            pullImageView = (ImageView) mLoadView.findViewById(R.id.pull_img);

            layout.addView(mLoadView);
        }
    }

    @Override
    public void onLayout(SwipeToRefreshLayout layout, boolean changed, float offset, int left, int top, int right, int bottom) {
        if (mLoadView != null) {
            mLoadView.layout(mLoadView.getLeft(),
                             mLoadView.getTop(),
                             mLoadView.getRight(),
                             mLoadView.getTop() + refreshHeight());
            layout.getContentView().bringToFront();
        }
    }

    @Override
    public void onDetach(SwipeToRefreshLayout layout) {
        layout.removeView(getLoadView());
    }

    @Override
    public void onDragEvent(SwipeToRefreshLayout layout, float offset) {
        if (!pullImageView.isShown()) {
            pullImageView.setVisibility(View.VISIBLE);
            refreshingBar.setVisibility(View.GONE);
        }
        if (pullDrawable == null) {
            pullDrawable = layout.getContext().getResources().getDrawable(R.drawable.phoenix_pull);
        }
        ImageView view = pullImageView;
        if (view.getDrawable() != pullDrawable) {
            view.setImageDrawable(pullDrawable);
        }

        ViewCompat.setTranslationY(layout.getContentView(), offset);
    }

    @Override
    public void onOverDragging(SwipeToRefreshLayout layout, float offset) {
        if (Math.abs(offset) <= viewHeight) {
            if (releaseDrawable == null) {
                releaseDrawable = layout.getContext().getResources().getDrawable(R.drawable.phoenix_release);
            }
            ImageView view = pullImageView;
            if (view.getDrawable() != releaseDrawable) {
                view.setImageDrawable(releaseDrawable);
            }
            ViewCompat.setTranslationY(layout.getContentView(), offset);
        }
    }

    @Override
    public void onRefreshing(final SwipeToRefreshLayout strl) {
        if (!refreshingBar.isShown()) {
            refreshingBar.setVisibility(View.VISIBLE);
            pullImageView.setVisibility(View.GONE);
        }

        View v = strl.getContentView();
        smoothMove(v, ViewCompat.getY(v), refreshHeight());
    }

    @Override
    public void stopRefresh(SwipeToRefreshLayout strl) {
        refreshingBar.setVisibility(View.GONE);
        pullImageView.setVisibility(View.GONE);

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
