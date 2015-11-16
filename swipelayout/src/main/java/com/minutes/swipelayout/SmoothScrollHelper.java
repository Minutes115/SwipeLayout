package com.minutes.swipelayout;

import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * User: LiangLong
 * Date: 2015-11-09
 * Time: 14:34
 * Note: com.minutes.swipelayout
 */
public class SmoothScrollHelper implements Runnable {
    public static final long DURING = 200;
    public static final int FPS = 60;

    private View target;
    private int fromScrollY;
    private int toScrollY;
    private int currentScrollY;

    private Interpolator interpolator = new DecelerateInterpolator();
    private long startTime = -1;
    private boolean isContinue = true;

    private OnScrollListener onScrollListener;
    private OnFinishListener onFinishListener;

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }

    public void setOnFinishListener(OnFinishListener onFinishListener) {
        this.onFinishListener = onFinishListener;
    }

    public SmoothScrollHelper(View target, int fromScrollY, int toScrollY) {
        this.target = target;
        this.currentScrollY = toScrollY - 1;
        this.fromScrollY = fromScrollY;
        this.toScrollY = toScrollY;
    }

    @Override
    public void run() {
        if (startTime == -1){
            startTime = System.currentTimeMillis();
        } else {
            long normalizedTime = (1000 * (System.currentTimeMillis() - startTime)) / DURING;
            normalizedTime = Math.max(Math.min(normalizedTime, 1000), 0);

            int deltaY = Math.round((fromScrollY - toScrollY) * interpolator.getInterpolation(normalizedTime / 1000f));
            currentScrollY = fromScrollY - deltaY;
            target.scrollTo(0, currentScrollY);
            if (onScrollListener != null){
                onScrollListener.onScroll(currentScrollY);
            }
        }
        if (isContinue && currentScrollY != toScrollY) {
            target.postDelayed(this, 1000 / FPS);
        } else {
            stop();
        }
    }

    public void stop() {
        isContinue = false;
        target.removeCallbacks(this);
        if (onFinishListener != null){
            onFinishListener.onFinished();
        }
    }

    interface OnScrollListener {
        void onScroll(int scrollY);
    }

    interface OnFinishListener {
        void onFinished();
    }

}
