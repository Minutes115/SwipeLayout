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
     * @return  加载的View
     */
    View loadView(SwipeToRefreshLayout swipeToRefreshLayout);

    /**
     * @return  最大拉动距离,超过这个距离触发onOverDragging函数
     */
    int maxDistance();

    void onAttach(SwipeToRefreshLayout swipeToRefreshLayout);

    void onLayout(SwipeToRefreshLayout root,
                  boolean changed,
                  int left,
                  int top,
                  int right,
                  int bottom);

    void onDragEvent(SwipeToRefreshLayout swipeToRefreshLayout, float offset);

    void onOverDragging(SwipeToRefreshLayout swipeToRefreshLayout, float offset);

    void onRefreshing(SwipeToRefreshLayout swipeToRefreshLayout);

    void stopRefresh(SwipeToRefreshLayout swipeToRefreshLayout);

}
