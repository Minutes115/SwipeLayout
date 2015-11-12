package com.minutes.swipelayout.temp;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>Description  : ScrollMode.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/11/8.</p>
 * <p>Time         : 上午10:24.</p>
 */
public interface ScrollMode {
    int SCROLL_FONT = 0;
    int SCROLL_BACK = 1;
    int SCROLL_FOLLOW = 2;

    @IntDef({
            SCROLL_FONT,
            SCROLL_BACK,
            SCROLL_FOLLOW
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface Mode{}
}
