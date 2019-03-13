package com.orient.tea.barragephoto.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.orient.tea.barragephoto.adapter.BarrageAdapter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 弹幕视图
 *
 * Created by wangjie on 2019/3/7.
 */

public class BarrageView extends ViewGroup {
    // 三级速度
    public final static int SPEED_LEVEL_ONE = 1;
    public final static int SPEED_LEVEL_TWO = 4;
    public final static int SPEED_LEVEL_THREE = 8;

    // 弹幕的相对位置
    public final static int GRAVITY_TOP = 1;
    public final static int GRAVITY_MIDDLE = 2;
    public final static int GRAVITY_BOTTOM = 4;
    public final static int GRAVITY_FULL = 7;

    // 当前的gravity
    private int gravity = GRAVITY_TOP;
    // 行数
    private int barrageCount;
    // 宽度和高度
    private int width,height;
    private List<View> barrageList;
    // 速度设置
    // TODO 先暂时设置固定的速度 根据需要再修改
    private int speed = SPEED_LEVEL_ONE;
    private BarrageAdapter mAdapter;
    // 单行的高度
    // TODO 利用UI工具使用当前高度的1/8
    private int singleLineHeight = 100;


    public BarrageView(Context context) {
        this(context,null);
    }

    public BarrageView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BarrageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.barrageList = new ArrayList<>();
    }

    /**
     * 设置适配器
     *
     * @param adapter 适配器
     */
    public void setAdapter(BarrageAdapter adapter){
        this.mAdapter = adapter;
    }






    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }
}
