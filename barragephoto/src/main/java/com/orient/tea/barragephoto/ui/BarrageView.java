package com.orient.tea.barragephoto.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * 弹幕视图
 *
 * Created by wangjie on 2019/3/7.
 */

public class BarrageView extends ViewGroup {
    // 三级速度
    public final static int SPEED_LEVEL_ONE = 1;
    public final static int SPEED_LEVEL_TWO = 2;
    public final static int SPEED_LEVEL_THREE = 3;

    // 弹幕的相对位置
    public final static int GRAVITY_TOP = 1;
    public final static int GRAVITY_MIDDLE = 2;
    public final static int GRAVITY_BOTTOM = 4;
    public final static int GRAVITY_FULL = 7;

    public BarrageView(Context context) {
        this(context,null);
    }

    public BarrageView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BarrageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }
}
