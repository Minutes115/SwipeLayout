package com.minutes.swipelayout.temp;

/**
 * <p>Description  : ScrollerHelper.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/11/7.</p>
 * <p>Time         : 下午12:13.</p>
 */
public class ScrollHelper {

    private float mFriction;
    private float mInitialY;
    private float mCurrY;
    private float mLastY;
    private int mOffsetY;

    public ScrollHelper(){

    }

    public ScrollHelper(float friction){
        this.mFriction = friction;
    }

    public float getInitialY() {
        return mInitialY;
    }

    public void setInitialY(float initialY) {
        this.mInitialY = initialY;
    }

    public float getCurrY() {
        return mCurrY;
    }

    public void setCurrY(float currY) {
        this.mLastY = this.mCurrY;
        this.mCurrY = currY;
        this.mOffsetY = -(int) ((this.mLastY - mCurrY) / mFriction);
    }

    public float getLastY() {
        return mLastY;
    }

    public int getOffsetY(){
        return mOffsetY;
    }

}
