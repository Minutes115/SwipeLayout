package com.minutes.swipelayout;

/**
 * User: LiangLong
 * Date: 2015-11-03
 * Time: 21:33
 * Note: com.minutes.library.widget.swipe2refresh
 */
public interface SwipeToRefreshListener {

    void onLoadMore(SwipeToRefreshLayout swipeToRefreshLayout);

    void onPull2Refresh(SwipeToRefreshLayout swipeToRefreshLayout);

}
