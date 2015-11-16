package com.minutes.swipelayout.temp;

import android.graphics.Rect;
import android.view.MotionEvent;

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
    private float mCurrDeltaY;
    private int mCurrOffsetY;
    private int mLastOffsetY;
    private MotionEvent mInitialEvent;

    private Rect mContentRect = new Rect();
    private Rect mHeaderRect = new Rect();
    private Rect mFooterRect = new Rect();

    public ScrollHelper(){
        this(1f);
    }

    public ScrollHelper(float friction){
        this.mFriction = friction;
        this.mCurrOffsetY = 0;
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

    /**
     * 设置初始化 y 坐标
     */
    public void setInitialEvent(MotionEvent event) {
        this.mInitialEvent = event;
        this.mInitialY   = event.getY();
        this.mCurrY      = event.getY();
        this.mLastY      = event.getY();
    }

    /**
     * 获取初始化 Event
     */
    public MotionEvent getInitialEvent(){
        return mInitialEvent;
    }

    /**
     * y 坐标方向移动，计算 deltaY
     */
    public void moveY(float currY){
        this.mLastY = this.mCurrY;
        this.mCurrY = currY;
        this.mCurrDeltaY = (int) ((mCurrY - mLastY) / mFriction);
    }

    /**
     * 设置当前视图偏移量
     */
    public void setCurrOffsetY(int offsetY){
        this.mLastOffsetY = this.mCurrOffsetY;
        this.mCurrOffsetY = offsetY;
    }

    /**
     * 获取当前视图偏移量
     */
    public int getCurrOffsetY(){
        return this.mCurrOffsetY;
    }

    /**
     * 获取上一次的视图偏移量
     */
    public int getLastOffsetY(){
        return this.mLastOffsetY;
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
    public float getCurrDeltaY(){
        return mCurrDeltaY;
    }

    /**
     * 返回绝对值的当前视图偏移量
     */
    public int getAbsCurrOffsetY() {
        return Math.abs(mCurrOffsetY);
    }
}
