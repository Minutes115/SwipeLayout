package com.minutes.swipelayout;

import android.view.View;

/**
 * User: LiangLong
 * Date: 2015-11-05
 * Time: 09:10
 * Note: com.minutes.library.widget.swipe2refresh
 */
public interface ILoadLayout {

    /**
     * @return 加载的View
     */
    View getLoadView();

    /**
     * @return 最大拉动距离, 超过这个距离触发onOverDragging函数(可以视作是LoadView的高度)
     */
    int refreshHeight();

    /**
     * 当LoadView添加到{@link SwipeToRefreshLayout}时触发,此时应该完成LoadView的初始化操作
     *
     * @param layout
     */
    void onAttach(SwipeToRefreshLayout layout);

    /**
     * 当LoadView从{@link SwipeToRefreshLayout}移除时触发(比如切换header效果的时候)
     *
     * @param layout
     */
    void onDetach(SwipeToRefreshLayout layout);

    /**
     * 摆放 getLoadView() 的位置
     *
     * @param layout
     * @param changed
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    void onLayout(SwipeToRefreshLayout layout,
                  boolean changed,
                  float offset,
                  int left,
                  int top,
                  int right,
                  int bottom);

    /**
     * 正常情况下的滑动
     *
     * @param layout
     * @param offset
     */
    void onDragEvent(SwipeToRefreshLayout layout, float offset);

    /**
     * 拉动超过自身高度时触发
     *
     * @param layout
     * @param offset
     */
    void onOverDragging(SwipeToRefreshLayout layout, float offset);

    /**
     * 刷新或者加载更多时触发
     *
     * @param layout
     */
    void onRefreshing(SwipeToRefreshLayout layout);

    /**
     * 停止刷新(也就是需要复位时触发)
     *
     * @param layout
     */
    void stopRefresh(SwipeToRefreshLayout layout);

}
