package com.orient.tea.barragephoto.adapter;

import android.util.SparseArray;
import android.view.View;

import com.orient.tea.barragephoto.model.DataSource;

import java.util.LinkedList;

/**
 * 基础的适配器
 *
 * Created by wangjie on 2019/3/7.
 */

public class BarrageAdapter<T extends DataSource> {
    private SparseArray<LinkedList<View>> array;
    private int[] typeArray;

    public BarrageAdapter(int[] typeArray) {
        array = new SparseArray<>();
        this.typeArray = typeArray;
        if(typeArray == null || typeArray.length == 0){
            throw new RuntimeException("typeArray is null or typeArray's length is 0");
        }
        for(int i=0;i<typeArray.length;i++){
            LinkedList<View> list = new LinkedList<>();
            array.put(i,list);
        }
    }



}
