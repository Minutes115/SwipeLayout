package com.minutes.swipelayout.temp;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>Description  : ILoadLayout.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/11/6.</p>
 * <p>Time         : 下午12:47.</p>
 */
public interface ILoadLayout {
    int HEADER = 1;
    int FOOTER = 2;

    @IntDef({
            HEADER,
            FOOTER
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface Type{}

    @ILoadLayout.Type int viewType();

    int refreshHeight();

    boolean canDoRefresh(SwipeLayout parent, int pullDistance);

    void startRefreshing(SwipeLayout parent);

    void pullOffset(SwipeLayout parent, int offset, int distance);

    void completeRefresh(SwipeLayout parent);
}
