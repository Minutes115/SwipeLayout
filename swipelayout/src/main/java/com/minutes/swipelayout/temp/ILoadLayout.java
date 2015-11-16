package com.minutes.swipelayout.temp;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.view.MotionEvent;

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

    /**
     * 视图类型 Header / Footer
     */
    @ILoadLayout.Type int viewType();

    /**
     * Measure Child
     *
     * @param parent                {@link SwipeLayout}
     * @param widthMeasureSpec      width measure spec
     * @param heightMeasureSpec     height measure spec
     * @return                      {@link Measure}
     */
    @NonNull Measure onChildMeasure(SwipeLayout parent, int widthMeasureSpec, int heightMeasureSpec);

    /**
     * Layout Child
     *
     * @param parent    {@link SwipeLayout}
     * @param offset    当前偏移总量
     * @param pl        left
     * @param pt        top
     * @param pr        right
     * @param pb        bottom
     * @return          {@link Layout}
     */
    @NonNull Layout onChildLayout(SwipeLayout parent, int offset, int pl, int pt, int pr, int pb);

    /**
     * 当 onTouchEvent 处理 {@link MotionEvent.ACTION_MOVE} 时候回调；
     * 当自动执行滑动动画时回调 eg.自动刷新。
     *
     * @param parent    {@link SwipeLayout}
     * @param delta     偏移差值
     * @param offset    纵向偏移量
     * @return          是否拦截事件，如果拦截事件将不保存当前产生的 offset
     */
    boolean onTouchMove(SwipeLayout parent, int delta, int offset);

    /**
     * 保持刷新时的高度
     */
    int refreshHeight();

    /**
     * 是否可以刷新
     *
     * @param parent        SwipeLayout
     * @param pullDistance  拉动偏移量
     * @return              Boolean
     */
    boolean canDoRefresh(SwipeLayout parent, int pullDistance);

    /**
     * 启动刷新
     */
    void startRefreshing(SwipeLayout parent);

    /**
     * 拉动时的偏移量
     *
     * @param parent    SwipeLayout
     * @param delta     当前距离上一次拉动的偏移量
     * @param offset    纵向偏移量
     */
    void scrollOffset(SwipeLayout parent, int delta, int offset);

    /**
     * 刷新结束
     */
    void completeRefresh(SwipeLayout parent);
}
