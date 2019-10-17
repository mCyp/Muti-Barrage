package com.orient.tea.barragephoto.ui;

import android.content.Context;

/**
 * Device Utils
 *
 * Created by wangjie on 2019/3/16.
 */
 class DeviceUtils {
     static int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @SuppressWarnings("unused")
    static int sp2px(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
