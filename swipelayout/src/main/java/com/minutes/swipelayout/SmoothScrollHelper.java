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

    public static final int FPS = 60;

    private Interpolator interpolator;
    private View target;
    private int fromY;
    private int toY;
    private int currentY;
    private long startTime = -1;
    private boolean isContinue = true;

    public SmoothScrollHelper(View target, int fromY, int toY, Interpolator interpolator) {
        this.target = target;
        this.currentY = toY - 1;
        this.fromY = fromY;
        this.toY = toY;
        if (interpolator == null) {
            interpolator = new DecelerateInterpolator();
        }
        this.interpolator = interpolator;
    }

    @Override
    public void run() {
        if (startTime == -1) {
            startTime = System.currentTimeMillis();
        } else {
            long normalizedTime = (1000 * (System.currentTimeMillis() - startTime)) / 200;
            normalizedTime = Math.max(Math.min(normalizedTime, 1000), 0);

            int deltaY = Math.round((fromY - toY) * interpolator.getInterpolation(normalizedTime / 1000f));
            currentY = fromY - deltaY;
            target.scrollTo(0, currentY);
        }
        if (isContinue && currentY != toY) {
            target.postDelayed(this, 1000 / FPS);
        }
    }

    public void stop() {
        isContinue = false;
        target.removeCallbacks(this);
    }
}
