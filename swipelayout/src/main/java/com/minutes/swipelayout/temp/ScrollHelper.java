package com.minutes.swipelayout.temp;

import android.graphics.Rect;

/**
 * <p>Description  : ScrollerHelper.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/11/7.</p>
 * <p>Time         : 下午12:13.</p>
 */
public class ScrollHelper{

    private float mFriction;
    private float mInitialY;
    private float mCurrY;
    private float mLastY;
    private int mCurrOffsetY;

    private Rect mContentRect = new Rect();
    private Rect mHeaderRect = new Rect();
    private Rect mFooterRect = new Rect();

    public ScrollHelper(){

    }

    public ScrollHelper(float friction){
        this.mFriction = friction;
    }

    public void setContentOnLayoutRect(int left, int top, int right, int bottom){
        mContentRect.set(left, top, right, bottom);
    }

    public Rect getContentOnLayoutRect(){
        return mContentRect;
    }

    public void setHeaderOnLayoutRect(int left, int top, int right, int bottom){
        mHeaderRect.set(left, top, right, bottom);
    }

    public Rect getHeaderOnLayoutRect(){
        return mHeaderRect;
    }

    public Rect getFooterOnLayoutRect() {
        return mFooterRect;
    }

    public void setFooterOnLayoutRect(int left, int top, int right, int bottom) {
        this.mFooterRect.set(left, top, right, bottom);
    }

    public float getInitialY() {
        return mInitialY;
    }

    public void setInitialY(float initialY) {
        this.mInitialY   = initialY;
        this.mCurrY      = initialY;
        this.mLastY      = initialY;
    }

    public float getCurrY() {
        return mCurrY;
    }

    public void setCurrY(float currY) {
        this.mLastY = this.mCurrY;
        this.mCurrY = currY;
        this.mCurrOffsetY = (int) ((mCurrY - mLastY) / mFriction);
    }

    public float getLastY() {
        return mLastY;
    }

    /**
     * 如果当前是下拉，那么需要的偏移量为负数
     * 如果当前是上拉，那么需要的偏移量为正数
     *
     * @return 视图需要的偏移量
     */
    public int getCurrOffsetY(){
        return mCurrOffsetY;
    }

    /**
     *
     *
     * @return
     */
    public int getOffsetY() {
        return (int) (Math.abs(mCurrY - mInitialY) / mFriction);
    }
}
