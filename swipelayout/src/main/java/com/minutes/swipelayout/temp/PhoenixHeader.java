package com.minutes.swipelayout.temp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.minutes.swipelayout.R;

/**
 * <p>Description  : PhoenixHeader.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/11/12.</p>
 * <p>Time         : 上午8:36.</p>
 */
public class PhoenixHeader extends LinearLayout implements ILoadLayout{
    private SunRefreshView sunRefreshView;
    private int mTotalDragDistance;

    public PhoenixHeader(Context context) {
        super(context);
        init();
    }

    public PhoenixHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @TargetApi(11) public PhoenixHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21) public PhoenixHeader(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        mTotalDragDistance = convertDpToPixel(getContext(), 120);
        sunRefreshView = new SunRefreshView(getContext());

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mTotalDragDistance);
        ImageView imageView = new ImageView(getContext());
        imageView.setLayoutParams(params);
        imageView.setImageDrawable(sunRefreshView);
        addView(imageView);

        setWillNotDraw(false);
        ViewCompat.setChildrenDrawingOrderEnabled(this, true);
    }

    @Override
    public int viewType() {
        return HEADER;
    }

    @Override
    public int refreshHeight() {
        return 0;
    }

    @Override
    public boolean canDoRefresh(SwipeLayout parent, int pullDistance) {
        return false;
    }

    @Override
    public void startRefreshing(SwipeLayout parent) {

    }

    @Override
    public void pullOffset(SwipeLayout parent, int delta, int offset) {

    }

    @Override
    public void completeRefresh(SwipeLayout parent) {

    }

    public int convertDpToPixel(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public class SunRefreshView extends BaseRefreshView implements Animatable {


        private static final float SCALE_START_PERCENT = 0.5f;
        private static final int ANIMATION_DURATION = 1000;

        private final static float SKY_RATIO = 0.65f;
        private static final float SKY_INITIAL_SCALE = 1.05f;

        private final static float TOWN_RATIO = 0.22f;
        private static final float TOWN_INITIAL_SCALE = 1.20f;
        private static final float TOWN_FINAL_SCALE = 1.30f;

        private static final float SUN_FINAL_SCALE = 0.75f;
        private static final float SUN_INITIAL_ROTATE_GROWTH = 1.2f;
        private static final float SUN_FINAL_ROTATE_GROWTH = 1.5f;

        private final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();

        private Matrix mMatrix;
        private Animation mAnimation;

        private int mTop;
        private int mScreenWidth;

        private int mSkyHeight;
        private float mSkyTopOffset;
        private float mSkyMoveOffset;

        private int mTownHeight;
        private float mTownInitialTopOffset;
        private float mTownFinalTopOffset;
        private float mTownMoveOffset;

        private int mSunSize = 100;
        private float mSunLeftOffset;
        private float mSunTopOffset;

        private float mPercent = 0.0f;
        private float mRotate = 0.0f;

        private Bitmap mSky;
        private Bitmap mSun;
        private Bitmap mTown;

        private boolean isRefreshing = false;


        public SunRefreshView(Context context) {
            super(context);
            mMatrix = new Matrix();
            setupAnimations();
            DisplayMetrics metrics = new DisplayMetrics();
            Activity activity = (Activity) context;
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            final int width = metrics.widthPixels;

            post(new Runnable() {
                @Override
                public void run() {
                    initiateDimens(width);
                }
            });
        }

        public void initiateDimens(int viewWidth) {
            if (viewWidth <= 0 || viewWidth == mScreenWidth) return;

            mScreenWidth = viewWidth;
            mSkyHeight = (int) (SKY_RATIO * mScreenWidth);
            mSkyTopOffset = (mSkyHeight * 0.38f);
            mSkyMoveOffset = convertDpToPixel(getContext(), 15);

            mTownHeight = (int) (TOWN_RATIO * mScreenWidth);
            mTownInitialTopOffset = (mTotalDragDistance - mTownHeight * TOWN_INITIAL_SCALE);
            mTownFinalTopOffset = (mTotalDragDistance - mTownHeight * TOWN_FINAL_SCALE);
            mTownMoveOffset = convertDpToPixel(getContext(), 10);

            mSunLeftOffset = 0.3f * (float) mScreenWidth;
            mSunTopOffset = (mTotalDragDistance * 0.1f);

            mTop = -mTotalDragDistance;

            createBitmaps();
        }

        private void createBitmaps() {
            mSky = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.sky);
            mSky = Bitmap.createScaledBitmap(mSky, mScreenWidth, mSkyHeight, true);
            mTown = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.buildings);
            mTown = Bitmap.createScaledBitmap(mTown, mScreenWidth, (int) (mScreenWidth * TOWN_RATIO), true);
            mSun = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.sun);
            mSun = Bitmap.createScaledBitmap(mSun, mSunSize, mSunSize, true);
        }

        @Override
        public void setPercent(float percent, boolean invalidate) {
            setPercent(percent);
            if (invalidate) setRotate(percent);
        }

        @Override
        public void offsetTopAndBottom(int offset) {
            mTop += offset;
            invalidateSelf();
        }

        @Override
        public void draw(Canvas canvas) {
            if (mScreenWidth <= 0) return;

            final int saveCount = canvas.save();

            canvas.translate(0, mTop);
            canvas.clipRect(0, -mTop, mScreenWidth, mTotalDragDistance);

            drawSky(canvas);
            drawSun(canvas);
            drawTown(canvas);

            canvas.restoreToCount(saveCount);
        }

        private void drawSky(Canvas canvas) {
            Matrix matrix = mMatrix;
            matrix.reset();

            float dragPercent = Math.min(1f, Math.abs(mPercent));

            float skyScale;
            float scalePercentDelta = dragPercent - SCALE_START_PERCENT;
            if (scalePercentDelta > 0) {
                /** Change skyScale between {@link #SKY_INITIAL_SCALE} and 1.0f depending on {@link #mPercent} */
                float scalePercent = scalePercentDelta / (1.0f - SCALE_START_PERCENT);
                skyScale = SKY_INITIAL_SCALE - (SKY_INITIAL_SCALE - 1.0f) * scalePercent;
            } else {
                skyScale = SKY_INITIAL_SCALE;
            }

            float offsetX = -(mScreenWidth * skyScale - mScreenWidth) / 2.0f;
            float offsetY = (1.0f - dragPercent) * mTotalDragDistance - mSkyTopOffset // Offset canvas moving
                    - mSkyHeight * (skyScale - 1.0f) / 2 // Offset sky scaling
                    + mSkyMoveOffset * dragPercent; // Give it a little move top -> bottom

            matrix.postScale(skyScale, skyScale);
            matrix.postTranslate(offsetX, offsetY);
            canvas.drawBitmap(mSky, matrix, null);
        }

        private void drawTown(Canvas canvas) {
            Matrix matrix = mMatrix;
            matrix.reset();

            float dragPercent = Math.min(1f, Math.abs(mPercent));

            float townScale;
            float townTopOffset;
            float townMoveOffset;
            float scalePercentDelta = dragPercent - SCALE_START_PERCENT;
            if (scalePercentDelta > 0) {
                /**
                 * Change townScale between {@link #TOWN_INITIAL_SCALE} and {@link #TOWN_FINAL_SCALE} depending on {@link #mPercent}
                 * Change townTopOffset between {@link #mTownInitialTopOffset} and {@link #mTownFinalTopOffset} depending on {@link #mPercent}
                 */
                float scalePercent = scalePercentDelta / (1.0f - SCALE_START_PERCENT);
                townScale = TOWN_INITIAL_SCALE + (TOWN_FINAL_SCALE - TOWN_INITIAL_SCALE) * scalePercent;
                townTopOffset = mTownInitialTopOffset - (mTownFinalTopOffset - mTownInitialTopOffset) * scalePercent;
                townMoveOffset = mTownMoveOffset * (1.0f - scalePercent);
            } else {
                float scalePercent = dragPercent / SCALE_START_PERCENT;
                townScale = TOWN_INITIAL_SCALE;
                townTopOffset = mTownInitialTopOffset;
                townMoveOffset = mTownMoveOffset * scalePercent;
            }

            float offsetX = -(mScreenWidth * townScale - mScreenWidth) / 2.0f;
            float offsetY = (1.0f - dragPercent) * mTotalDragDistance // Offset canvas moving
                    + townTopOffset
                    - mTownHeight * (townScale - 1.0f) / 2 // Offset town scaling
                    + townMoveOffset; // Give it a little move

            matrix.postScale(townScale, townScale);
            matrix.postTranslate(offsetX, offsetY);

            canvas.drawBitmap(mTown, matrix, null);
        }

        private void drawSun(Canvas canvas) {
            Matrix matrix = mMatrix;
            matrix.reset();

            float dragPercent = mPercent;
            if (dragPercent > 1.0f) { // Slow down if pulling over set height
                dragPercent = (dragPercent + 9.0f) / 10;
            }

            float sunRadius = (float) mSunSize / 2.0f;
            float sunRotateGrowth = SUN_INITIAL_ROTATE_GROWTH;

            float offsetX = mSunLeftOffset;
            float offsetY = mSunTopOffset
                    + (mTotalDragDistance / 2) * (1.0f - dragPercent) // Move the sun up
                    - mTop; // Depending on Canvas position

            float scalePercentDelta = dragPercent - SCALE_START_PERCENT;
            if (scalePercentDelta > 0) {
                float scalePercent = scalePercentDelta / (1.0f - SCALE_START_PERCENT);
                float sunScale = 1.0f - (1.0f - SUN_FINAL_SCALE) * scalePercent;
                sunRotateGrowth += (SUN_FINAL_ROTATE_GROWTH - SUN_INITIAL_ROTATE_GROWTH) * scalePercent;

                matrix.preTranslate(offsetX + (sunRadius - sunRadius * sunScale), offsetY * (2.0f - sunScale));
                matrix.preScale(sunScale, sunScale);

                offsetX += sunRadius;
                offsetY = offsetY * (2.0f - sunScale) + sunRadius * sunScale;
            } else {
                matrix.postTranslate(offsetX, offsetY);

                offsetX += sunRadius;
                offsetY += sunRadius;
            }

            matrix.postRotate(
                    (isRefreshing ? -360 : 360) * mRotate * (isRefreshing ? 1 : sunRotateGrowth),
                    offsetX,
                    offsetY);

            canvas.drawBitmap(mSun, matrix, null);
        }

        public void setPercent(float percent) {
            mPercent = percent;
        }

        public void setRotate(float rotate) {
            mRotate = rotate;
            invalidateSelf();
        }

        public void resetOriginals() {
            setPercent(0);
            setRotate(0);
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);
        }

        @Override
        public void setBounds(int left, int top, int right, int bottom) {
            super.setBounds(left, top, right, mSkyHeight + top);
        }

        @Override
        public void setAlpha(int alpha) {

        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {

        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        @Override
        public boolean isRunning() {
            return false;
        }

        @Override
        public void start() {
            mAnimation.reset();
            isRefreshing = true;
            startAnimation(mAnimation);
        }

        @Override
        public void stop() {
            clearAnimation();
            isRefreshing = false;
            resetOriginals();
        }

        private void setupAnimations() {
            mAnimation = new Animation() {
                @Override
                public void applyTransformation(float interpolatedTime, Transformation t) {
                    setRotate(interpolatedTime);
                }
            };
            mAnimation.setRepeatCount(Animation.INFINITE);
            mAnimation.setRepeatMode(Animation.RESTART);
            mAnimation.setInterpolator(LINEAR_INTERPOLATOR);
            mAnimation.setDuration(ANIMATION_DURATION);
        }

    }

    public abstract class BaseRefreshView extends Drawable implements Drawable.Callback, Animatable {

        private boolean mEndOfRefreshing;
        private Context mContext;

        public BaseRefreshView(Context context) {
            this.mContext = context;
        }

        public Context getContext() {
            return mContext;
        }

        public abstract void setPercent(float percent, boolean invalidate);

        public abstract void offsetTopAndBottom(int offset);

        @Override
        public void invalidateDrawable(@NonNull Drawable who) {
            final Callback callback = getCallback();
            if (callback != null) {
                callback.invalidateDrawable(this);
            }
        }

        @Override
        public void scheduleDrawable(Drawable who, Runnable what, long when) {
            final Callback callback = getCallback();
            if (callback != null) {
                callback.scheduleDrawable(this, what, when);
            }
        }

        @Override
        public void unscheduleDrawable(Drawable who, Runnable what) {
            final Callback callback = getCallback();
            if (callback != null) {
                callback.unscheduleDrawable(this, what);
            }
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        @Override
        public void setAlpha(int alpha) {

        }

        @Override
        public void setColorFilter(ColorFilter cf) {

        }

        /**
         * Our animation depend on type of current work of refreshing.
         * We should to do different things when it's end of refreshing
         *
         * @param endOfRefreshing - we will check current state of refresh with this
         */
        public void setEndOfRefreshing(boolean endOfRefreshing) {
            mEndOfRefreshing = endOfRefreshing;
        }
    }

}
