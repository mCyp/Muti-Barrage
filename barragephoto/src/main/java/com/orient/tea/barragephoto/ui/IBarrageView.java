package com.orient.tea.barragephoto.ui;

import android.view.View;

import java.util.List;

/**
 * 弹幕视图的接口
 *
 * Created by wangjie on 2019/3/15.
 */

public interface IBarrageView {
    // 添加视图
    void addBarrageItem(View view);
    // 获取是否存在缓存
    View getCacheView(int type);

}
