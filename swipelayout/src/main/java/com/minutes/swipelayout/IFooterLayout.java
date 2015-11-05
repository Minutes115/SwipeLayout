package com.minutes.swipelayout;

/**
 * <p>Description  : IFooterLayout.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/11/4.</p>
 * <p>Time         : 下午1:32.</p>
 */
public interface IFooterLayout {

    /**
     * @param offset 手指拉动时的监听
     */
    void onDrag(float offset);


    /**
     * @param state 设置View的状态
     */
    void setState(int state);
}
