package com.minutes.swipelayout.temp;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.minutes.swipelayout.R;

/**
 * <p>Description  : MaterialHeader.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/11/12.</p>
 * <p>Time         : 上午10:29.</p>
 */
public class MaterialHeader extends LinearLayout implements com.minutes.swipelayout.temp.ILoadLayout {
    public static final int NORMAL = 0;
    public static final int RELEASE = 1;
    public static final int REFRESHING = 2;
    public static final int RESET = 3;

    public static final String TEXT_RELEASE_TO_LOAD_MORE = "松开立即加载";
    public static final String TEXT_PULL_TO_LOAD_MORE    = "上拉加载更多";
    public static final String TEXT_REFRESHING           = "正在刷新";
    public static final String TEXT_PULL_TO_REFRESH      = "下拉可以刷新";
    public static final String TEXT_RELEASE_TO_REFRESH   = "松开立即刷新";

    private TextView text;
    private ImageView arrow;
    private ProgressBar progress;

    private Animation mRotateUpAnimation;
    private Animation mRotateDownAnimation;

    private int state = NORMAL;
    private boolean isHeader;
    private int headerHeight;

    public void setHeader(boolean isHeader){
        this.isHeader = isHeader;
    }

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
        this.isHeader = true;
        this.headerHeight = dip2px(60);

        View root = LayoutInflater.from(getContext()).inflate(R.layout.layout_refresh_loading, this);
        arrow = (ImageView) root.findViewById(R.id.refresh_arrow);
        arrow.setImageBitmap(getArrowBitmap(isHeader));
        text = (TextView) root.findViewById(R.id.refresh_msg);
        text.setVisibility(GONE);
        progress = (ProgressBar) root.findViewById(R.id.refresh_loading);
        progress.setVisibility(GONE);
        //箭头翻转动画
        mRotateUpAnimation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, .5f, RotateAnimation.RELATIVE_TO_SELF, .5f);
        mRotateUpAnimation.setDuration(200);
        mRotateUpAnimation.setFillAfter(true);
        mRotateUpAnimation.setInterpolator(new LinearInterpolator());
        //箭头翻转动画
        mRotateDownAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, .5f, RotateAnimation.RELATIVE_TO_SELF, .5f);
        mRotateDownAnimation.setDuration(200);
        mRotateDownAnimation.setFillAfter(true);
        mRotateDownAnimation.setInterpolator(new LinearInterpolator());

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dip2px(50));
        setLayoutParams(lp);
    }

    /**
     * 这里提供动画箭头图片 如果要替换箭头直接在此方法中获取Drawable或者在HeaderView中设置
     */
    private Bitmap getArrowBitmap(boolean isHeader) {
        int    width       = (int) (headerHeight * .6);
        int    height      = (int) (headerHeight * .6);
        Bitmap bmp         = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
        Canvas canvas      = new Canvas(bmp);
        Path path        = new Path();
        float  arrowHeight = (float) (width * .3);
        float  arrowWidth  = (float) (width * .7);
        float  arrowPoint  = width / 2 / 2;
        float  barPad      = (float) (height * .52);
        Paint paint       = new Paint();
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
        }
        else {
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

    public void setState(int state) {
        if (this.state == state) {
            return;
        }
        switch (state) {
            case NORMAL:
                if (isHeader) {
                    progress.setVisibility(GONE);
                    arrow.setVisibility(VISIBLE);
                    if (arrow.getAnimation() == mRotateUpAnimation) {
                        arrow.clearAnimation();
                        arrow.startAnimation(mRotateDownAnimation);
                    }
                }
                else {
                    progress.setVisibility(GONE);
                    arrow.setVisibility(VISIBLE);
                    if (arrow.getAnimation() == mRotateUpAnimation) {
                        arrow.clearAnimation();
                        arrow.startAnimation(mRotateDownAnimation);
                    }
                }
                break;
            case REFRESHING:
                arrow.clearAnimation();
                arrow.setVisibility(GONE);
                progress.setVisibility(VISIBLE);
                break;
            case RESET:
                arrow.clearAnimation();
                arrow.setVisibility(GONE);
                progress.setVisibility(GONE);
                break;
            case RELEASE:
                if (isHeader) {
                    progress.setVisibility(GONE);
                    arrow.setVisibility(VISIBLE);
                    arrow.clearAnimation();
                    arrow.startAnimation(mRotateUpAnimation);
                }
                else {
                    progress.setVisibility(GONE);
                    arrow.setVisibility(VISIBLE);
                    arrow.clearAnimation();
                    arrow.startAnimation(mRotateUpAnimation);
                }
                break;
        }
        this.state = state;
    }

    @Override
    public int viewType() {
        return HEADER;
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
        setState(REFRESHING);
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
            setState(RELEASE);
        } else {
            setState(NORMAL);
        }
    }

    @Override
    public void completeRefresh(SwipeLayout parent) {
        setState(RESET);
    }

    public static class MaterialFooter extends PullToRefreshHeader{

        public MaterialFooter(Context context) {
            super(context);
            setHeader(false);
        }

        @Override
        public int viewType() {
            return FOOTER;
        }
    }
}
