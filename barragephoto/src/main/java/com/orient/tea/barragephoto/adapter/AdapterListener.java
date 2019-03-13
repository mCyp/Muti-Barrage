package com.orient.tea.barragephoto.adapter;

/**
 * ViewHolder click Listener
 *
 * Created by wangjie on 2019/3/12.
 */

public interface AdapterListener<T> {
    // 点击事件
    void onItemClick(BarrageAdapter.BarrageViewHolder<T> holder, T item);
}
