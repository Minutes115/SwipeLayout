package com.minutes.swipelayout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * User: LiangLong
 * Date: 2015-11-05
 * Time: 09:46
 * Note: com.minutes.library.widget.swipe2refresh
 */
public class MyHeader implements ILoadLayout {
    public static final int    DURING   = 500;
    public static final String TEXT_RELEASE_TO_LOAD_MORE = "松开立即加载";
    public static final String TEXT_PULL_TO_LOAD_MORE = "上拉加载更多";
    public static final String TEXT_REFRESHING = "正在刷新";
    public static final String TEXT_PULL_TO_REFRESH = "下拉可以刷新";
    public static final String TEXT_RELEASE_TO_REFRESH = "松开立即刷新";

    private TextView text;
    private ImageView arrow;
    private ProgressBar progress;

    private Animation mRotateUpAnimation;
    private Animation mRotateDownAnimation;

    private View mLoadView;
    private SwipeToRefreshLayout.SmoothScrollToRunnable runnable;
    private Interpolator  interpolator = new DecelerateInterpolator();

    /**
     * 这里提供动画箭头图片 如果要替换箭头直接在此方法中获取Drawable或者在HeaderView中设置
     */
    private Bitmap getArrowBitmap(boolean isHeader) {
        int    width       = (int) (60 * .6);
        int    height      = (int) (60 * .6);
        Bitmap bmp         = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
        Canvas canvas      = new Canvas(bmp);
        Path   path        = new Path();
        float  arrowHeight = (float) (width * .3);
        float  arrowWidth  = (float) (width * .7);
        float  arrowPoint  = width / 2 / 2;
        float  barPad      = (float) (height * .52);
        Paint  paint       = new Paint();
        paint.setAntiAlias(true);
        if (isHeader) {
            path.moveTo(arrowPoint + arrowWidth / 2, height);
            path.lineTo(arrowPoint + arrowWidth, height - arrowHeight);
            path.lineTo((float) (arrowPoint + arrowWidth * .75), height - arrowHeight);
            path.lineTo((float) (arrowPoint + arrowWidth * .75), height - barPad);
            path.lineTo((float) (arrowPoint + arrowWidth * .25), height - barPad);
            path.lineTo((float) (arrowPoint + arrowWidth * .25), height - arrowHeight);
            path.lineTo(arrowPoint, height - arrowHeight);
            path.close();
            path.moveTo((float) (arrowPoint + arrowWidth * .75), (float) (barPad - barPad * .20));
            path.lineTo((float) (arrowPoint + arrowWidth * .25), (float) (barPad - barPad * .20));
            path.lineTo((float) (arrowPoint + arrowWidth * .25), (float) (barPad - barPad * .45));
            path.lineTo((float) (arrowPoint + arrowWidth * .75), (float) (barPad - barPad * .45));
            path.close();
            path.moveTo((float) (arrowPoint + arrowWidth * .75), (float) (barPad - barPad * .55));
            path.lineTo((float) (arrowPoint + arrowWidth * .25), (float) (barPad - barPad * .55));
            path.lineTo((float) (arrowPoint + arrowWidth * .25), (float) (barPad - barPad * .73));
            path.lineTo((float) (arrowPoint + arrowWidth * .75), (float) (barPad - barPad * .73));
            path.close();
        } else {
            path.moveTo(arrowPoint + arrowWidth / 2, 0);
            path.lineTo(arrowPoint + arrowWidth, arrowHeight);
            path.lineTo((float) (arrowPoint + arrowWidth * .75), arrowHeight);
            path.lineTo((float) (arrowPoint + arrowWidth * .75), height - barPad);
            path.lineTo((float) (arrowPoint + arrowWidth * .25), height - barPad);
            path.lineTo((float) (arrowPoint + arrowWidth * .25), arrowHeight);
            path.lineTo(arrowPoint, arrowHeight);
            path.close();
            path.moveTo((float) (arrowPoint + arrowWidth * .75), (float) (height - barPad + barPad * .10));
            path.lineTo((float) (arrowPoint + arrowWidth * .25), (float) (height - barPad + barPad * .10));
            path.lineTo((float) (arrowPoint + arrowWidth * .25), (float) (height - barPad + barPad * .35));
            path.lineTo((float) (arrowPoint + arrowWidth * .75), (float) (height - barPad + barPad * .35));
            path.close();
            path.moveTo((float) (arrowPoint + arrowWidth * .75), (float) (height - barPad + barPad * .45));
            path.lineTo((float) (arrowPoint + arrowWidth * .25), (float) (height - barPad + barPad * .45));
            path.lineTo((float) (arrowPoint + arrowWidth * .25), (float) (height - barPad + barPad * .63));
            path.lineTo((float) (arrowPoint + arrowWidth * .75), (float) (height - barPad + barPad * .63));
            path.close();
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#ffadada9"));
        canvas.drawPath(path, paint);
        return bmp;
    }

    @Override
    public View loadView(SwipeToRefreshLayout swipeToRefreshLayout) {
        return mLoadView;
    }

    @Override
    public int maxDistance() {
        return 220;
    }

    @Override
    public void onLayout(SwipeToRefreshLayout swipeToRefreshLayout,
                         boolean changed,
                         int left,
                         int top,
                         int right,
                         int bottom) {
        if (mLoadView != null){
            mLoadView.bringToFront();
            mLoadView.layout(
                mLoadView.getLeft(),
                mLoadView.getTop() - maxDistance(),
                mLoadView.getRight(),
                mLoadView.getBottom() - maxDistance()
            );
        }
    }

    @Override
    public void onAttach(SwipeToRefreshLayout swipeToRefreshLayout) {
        Context context = swipeToRefreshLayout.getContext();
        if (mLoadView == null) {
            //箭头翻转动画
            mRotateUpAnimation = new RotateAnimation(0,
                                                     -180,
                                                     RotateAnimation.RELATIVE_TO_SELF,
                                                     .5f,
                                                     RotateAnimation.RELATIVE_TO_SELF,
                                                     .5f);
            mRotateUpAnimation.setDuration(200);
            mRotateUpAnimation.setFillAfter(true);
            mRotateUpAnimation.setInterpolator(new LinearInterpolator());
            //箭头翻转动画
            mRotateDownAnimation = new RotateAnimation(-180,
                                                       0,
                                                       RotateAnimation.RELATIVE_TO_SELF,
                                                       .5f,
                                                       RotateAnimation.RELATIVE_TO_SELF,
                                                       .5f);
            mRotateDownAnimation.setDuration(200);
            mRotateDownAnimation.setFillAfter(true);
            mRotateDownAnimation.setInterpolator(new LinearInterpolator());

            mLoadView = View.inflate(context, R.layout.layout_refresh_loading, null);
            arrow = (ImageView) mLoadView.findViewById(R.id.refresh_arrow);
            arrow.setImageBitmap(getArrowBitmap(true));
            text = (TextView) mLoadView.findViewById(R.id.refresh_msg);
            text.setText(TEXT_PULL_TO_REFRESH);
            progress = (ProgressBar) mLoadView.findViewById(R.id.refresh_loading);
            progress.setVisibility(View.GONE);

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                                                       maxDistance());
            lp.gravity = Gravity.TOP;
            swipeToRefreshLayout.addView(mLoadView, -1, lp);
        }
    }


    @Override
    public void onDragEvent(SwipeToRefreshLayout swipeToRefreshLayout, float offset) {
        text.setText(TEXT_PULL_TO_REFRESH);
        progress.setVisibility(View.GONE);
        arrow.setVisibility(View.VISIBLE);
        if (arrow.getAnimation() != mRotateDownAnimation) {
            arrow.clearAnimation();
            arrow.startAnimation(mRotateDownAnimation);
        }
        swipeToRefreshLayout.scrollTo(0, (int) offset);
    }

    @Override
    public void onOverDragging(SwipeToRefreshLayout swipeToRefreshLayout, float offset) {
        text.setText(TEXT_RELEASE_TO_REFRESH);
        progress.setVisibility(View.GONE);
        arrow.setVisibility(View.VISIBLE);
        if (arrow.getAnimation() != mRotateUpAnimation) {
            arrow.clearAnimation();
            arrow.startAnimation(mRotateUpAnimation);
        }
        swipeToRefreshLayout.scrollTo(0, (int) offset);
    }

    @Override
    public void onRefreshing(final SwipeToRefreshLayout strl) {
        text.setText(TEXT_REFRESHING);
        arrow.clearAnimation();
        arrow.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        if (runnable != null){
            runnable.stop();
        }
        runnable = new SwipeToRefreshLayout.SmoothScrollToRunnable(strl,
                                                                   strl.getScrollY(),
                                                                   -maxDistance(),
                                                                   interpolator);
        runnable.run();
    }

    @Override
    public void stopRefresh(SwipeToRefreshLayout strl) {
        arrow.clearAnimation();
        arrow.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);

        if (runnable != null) {
            runnable.stop();
        }
        runnable = new SwipeToRefreshLayout.SmoothScrollToRunnable(strl,
                                                                   strl.getScrollY(),
                                                                   0,
                                                                   interpolator);
        runnable.run();
    }

}
