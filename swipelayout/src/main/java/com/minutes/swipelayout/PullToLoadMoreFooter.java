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
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * User: LiangLong
 * Date: 2015-11-05
 * Time: 11:34
 * Note: com.minutes.library.widget.swipe2refresh
 */
public class PullToLoadMoreFooter implements ILoadLayout {
    public static final String TEXT_RELEASE_TO_LOAD_MORE    = "松开立即加载";
    public static final String TEXT_PULL_TO_LOAD_MORE       = "上拉加载更多";
    public static final String TEXT_REFRESHING              = "正在刷新";

    private static final String TAG = PullToLoadMoreFooter.class.getSimpleName();

    private TextView text;
    private ImageView arrow;
    private ProgressBar progress;

    private Animation mRotateUpAnimation;
    private Animation mRotateDownAnimation;

    private int height;
    private View mLoadView;
    private SmoothScrollHelper scrollHelper;
    private SmoothTranslationYHelper translationYHelper;

    /**
     * 这里提供动画箭头图片 如果要替换箭头直接在此方法中获取Drawable或者在HeaderView中设置
     */
    private Bitmap getArrowBitmap() {
        int    width       = (int) (60 * .6);
        int    height      = (int) (60 * .6);
        Bitmap bmp         = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas      = new Canvas(bmp);
        Path   path        = new Path();
        float  arrowHeight = (float) (width * .3);
        float  arrowWidth  = (float) (width * .7);
        float  arrowPoint  = width / 2 / 2;
        float  barPad      = (float) (height * .52);
        Paint  paint       = new Paint();
        paint.setAntiAlias(true);
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
        paint.setStyle(Paint.Style.STROKE);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#ffadada9"));
        canvas.drawPath(path, paint);
        return bmp;
    }

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
        if (height == 0){
            height = SwipeToRefreshLayout.dip2Px(context, 60);
        }
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
            Bitmap bmArrow = getArrowBitmap();
            arrow = (ImageView) mLoadView.findViewById(R.id.refresh_arrow);
            arrow.setImageBitmap(bmArrow);
            text = (TextView) mLoadView.findViewById(R.id.refresh_msg);
            text.setText(TEXT_PULL_TO_LOAD_MORE);
            progress = (ProgressBar) mLoadView.findViewById(R.id.refresh_loading);
            progress.setVisibility(View.GONE);

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.MarginLayoutParams.MATCH_PARENT,
                                                                       refreshHeight());
            lp.gravity = Gravity.BOTTOM;
            mLoadView.setLayoutParams(lp);
            layout.addView(mLoadView);
        }
    }

    @Override
    public void onDetach(SwipeToRefreshLayout layout) {
        layout.removeView(getLoadView());
    }

    @Override
    public void onLayout(SwipeToRefreshLayout layout, boolean changed, float offset, int left, int top, int right, int bottom) {
        if (mLoadView != null) {
            mLoadView.layout(mLoadView.getLeft(),
                             mLoadView.getTop() + refreshHeight(),
                             mLoadView.getRight(),
                             mLoadView.getBottom() + refreshHeight());
            mLoadView.bringToFront();
        }
    }

    @Override
    public void onDragEvent(SwipeToRefreshLayout swipeToRefreshLayout, float offset) {
        text.setText(TEXT_PULL_TO_LOAD_MORE);
        progress.setVisibility(View.GONE);
        arrow.setVisibility(View.VISIBLE);
        if (arrow.getAnimation() != mRotateDownAnimation) {
            arrow.clearAnimation();
            arrow.startAnimation(mRotateDownAnimation);
        }
        swipeToRefreshLayout.scrollTo(0, -(int) offset);
    }

    @Override
    public void onOverDragging(SwipeToRefreshLayout swipeToRefreshLayout, float offset) {
        text.setText(TEXT_RELEASE_TO_LOAD_MORE);
        progress.setVisibility(View.GONE);
        arrow.setVisibility(View.VISIBLE);
        if (arrow.getAnimation() != mRotateUpAnimation) {
            arrow.clearAnimation();
            arrow.startAnimation(mRotateUpAnimation);
        }
        swipeToRefreshLayout.scrollTo(0, -(int) offset);
    }

    @Override
    public void onRefreshing(SwipeToRefreshLayout strl) {
        text.setText(TEXT_REFRESHING);
        arrow.clearAnimation();
        arrow.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        smoothScroll(strl, strl.getScrollY(), refreshHeight());
    }

    @Override
    public void stopRefresh(SwipeToRefreshLayout strl) {
        arrow.clearAnimation();
        arrow.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);

        smoothScroll(strl, strl.getScrollY(), 0);
    }

    private void smoothScroll(View view, float fromScrollY, float toScrollY) {
        if (scrollHelper != null) {
            scrollHelper.stop();
        }
        scrollHelper = new SmoothScrollHelper(view, (int) fromScrollY, (int) toScrollY);
        view.post(scrollHelper);
    }
}
