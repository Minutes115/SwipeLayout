package com.minutes.swipelayout;

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
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * <p>Description  : LoadingView.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/11/3.</p>
 * <p>Time         : 下午10:37.</p>
 */
public class HeaderView extends LinearLayout implements com.minutes.swipelayout.temp.ILoadLayout {

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

    private int state = LoadingState.NORMAL;
    private boolean isHeader;
    private int headerHeight;

    public HeaderView(Context context) {
        super(context);
        init();
    }

    public HeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @TargetApi(11) public HeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21) public HeaderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private void init(){
        this.isHeader = true;
        this.headerHeight = dip2px(60);

        View root = LayoutInflater.from(getContext()).inflate(R.layout.layout_refresh_loading, this);
        arrow = (ImageView) root.findViewById(R.id.refresh_arrow);
        arrow.setImageBitmap(getArrowBitmap(isHeader));
        text = (TextView) root.findViewById(R.id.refresh_msg);
        text.setText(isHeader ? TEXT_PULL_TO_REFRESH : TEXT_PULL_TO_LOAD_MORE);
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

        setState(LoadingState.NORMAL);
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
            case LoadingState.NORMAL:
                if (isHeader) {
                    text.setText(TEXT_PULL_TO_REFRESH);
                    progress.setVisibility(GONE);
                    arrow.setVisibility(VISIBLE);
                    if (arrow.getAnimation() == mRotateUpAnimation) {
                        arrow.clearAnimation();
                        arrow.startAnimation(mRotateDownAnimation);
                    }
                }
                else {
                    text.setText(TEXT_PULL_TO_LOAD_MORE);
                    progress.setVisibility(GONE);
                    arrow.setVisibility(VISIBLE);
                    if (arrow.getAnimation() == mRotateUpAnimation) {
                        arrow.clearAnimation();
                        arrow.startAnimation(mRotateDownAnimation);
                    }
                }
                break;
            case LoadingState.REFRESHING:
                text.setText(TEXT_REFRESHING);
                arrow.clearAnimation();
                arrow.setVisibility(GONE);
                progress.setVisibility(VISIBLE);
                break;
            case LoadingState.RESET:
                arrow.clearAnimation();
                arrow.setVisibility(GONE);
                progress.setVisibility(GONE);
                break;
            case LoadingState.RELEASE:
                if (isHeader) {
                    text.setText(TEXT_RELEASE_TO_REFRESH);
                    progress.setVisibility(GONE);
                    arrow.setVisibility(VISIBLE);
                    arrow.clearAnimation();
                    arrow.startAnimation(mRotateUpAnimation);
                }
                else {
                    text.setText(TEXT_RELEASE_TO_LOAD_MORE);
                    progress.setVisibility(GONE);
                    arrow.setVisibility(VISIBLE);
                    arrow.clearAnimation();
                    arrow.startAnimation(mRotateUpAnimation);
                }
                break;
        }
        this.state = state;
    }

    public void onDrag(float offset) {

    }
}

