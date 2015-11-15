package com.minutes.swipelayout;

import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * User: LiangLong
 * Date: 2015-11-13
 * Time: 16:17
 * Note: com.minutes.swipelayout
 */
public class SmoothTranslationYHelper implements Runnable{
    public static final long DURING = 300;

    public static final int FPS = 60;

    private Interpolator interpolator;
    private View target;
    private int fromY;
    private int toY;
    private int currentY;

    private long startTime = System.currentTimeMillis();
    private boolean isContinue = true;

    public SmoothTranslationYHelper(View target, int fromY, int toY, Interpolator interpolator) {
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
            long normalizedTime = (1000 * (System.currentTimeMillis() - startTime)) / DURING;
            normalizedTime = Math.max(Math.min(normalizedTime, 1000), 0);

            int deltaY = Math.round((fromY - toY) * interpolator.getInterpolation(normalizedTime / 1000f));
            currentY = fromY - deltaY;
            ViewCompat.setTranslationY(target, currentY);
        }
        if (isContinue && currentY != toY) {
            target.postDelayed(this, 1000 / FPS);
        } else {
            stop();
        }
    }

    public void stop() {
        isContinue = false;
        target.removeCallbacks(this);
    }

}
