package com.minutes.swipelayout.temp.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import com.minutes.swipelayout.temp.Layout;
import com.minutes.swipelayout.temp.SwipeLayout;

/**
 * <p>Description  : PhoenixFooter.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/11/15.</p>
 * <p>Time         : 上午8:43.</p>
 */
public class PhoenixFooter extends PhoenixHeader{
    public PhoenixFooter(Context context) {
        super(context);
    }

    public PhoenixFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhoenixFooter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PhoenixFooter(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public int viewType() {
        return FOOTER;
    }

    @NonNull
    @Override
    public Layout onChildLayout(SwipeLayout parent, int offset, int pl, int pt, int pr, int pb) {
        final int paddingLeft = parent.getPaddingLeft();
        final int paddingTop  = parent.getPaddingTop();

        MarginLayoutParams lp = (MarginLayoutParams) getLayoutParams();
        final int left   = paddingLeft + lp.leftMargin;
        final int top    = paddingTop + lp.topMargin +
                (parent.getContentMeasure().height - getMeasuredHeight());
        final int right  = left + getMeasuredWidth();
        final int bottom = top + getMeasuredHeight();

        parent.bringContentViewToFont();
        layout(left, top, right, bottom);
        return new Layout(left, top, right, bottom);
    }

    @Override
    public void pullOffset(SwipeLayout parent, int delta, int offset) {
        float percent = Math.min(1f, offset / mTotalDragDistance);
        sunRefreshView.setPercent(percent, true);
        sunRefreshView.offsetTopAndBottom(-delta);
    }
}
