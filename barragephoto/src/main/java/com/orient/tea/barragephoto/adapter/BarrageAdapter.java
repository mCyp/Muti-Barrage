package com.orient.tea.barragephoto.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orient.tea.barragephoto.R;
import com.orient.tea.barragephoto.model.DataSource;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * 基础的适配器
 * <p>
 * Created by wangjie on 2019/3/7.
 */

@SuppressWarnings("unchecked")
public abstract class BarrageAdapter<T extends DataSource, VH extends BarrageAdapter.BarrageViewHolder<T>>
        implements View.OnClickListener {
    // View的缓存
    private SparseArray<LinkedList<View>> mArray;
    // 数据
    private List<T> mDataList;
    // View的点击监听
    private AdapterListener<T> mAdapterListener;
    // 类型List
    private List<Integer> mTypeList;

    public BarrageAdapter(List<T> dataList,AdapterListener<T> adapterListener) {
        this.mArray = new SparseArray<>();
        this.mDataList = dataList;
        this.mAdapterListener = adapterListener;
        this.mTypeList = new LinkedList<>();
    }

    public BarrageAdapter() {
        this(new LinkedList<T>(),null);
    }

    /**
     * 设置适配器的监听器
     *
     * @param adapterListener 适配器的监听器
     */
    public void setAdapterListener(AdapterListener<T> adapterListener){
        this.mAdapterListener = adapterListener;
    }

    // TODO 数据的增加处理

    /**
     * 创建子视图的过程
     *
     * @param position   位置
     * @param cacheView 缓存视图
     * @return 视图
     */
    public View createSubView(int position, View cacheView, ViewGroup parent) {
        // 1.获取子布局
        // 2. 创建ViewHolder
        // 3. 绑定ViewHolder
        // 4. 返回视图
        if (mDataList == null || mDataList.size() == 0) {
            throw new RuntimeException("mDataList can't be null, you must sure that mDataList has been initialized");
        }
        T data = mDataList.get(position);
        int layoutType = getItemLayout(data, position);
        VH holder = null;
        if (cacheView != null) {
            holder = (VH) cacheView.getTag(R.id.barrage_view_holder);
        }
        if (null == holder) {
            holder = createViewHolder(parent, layoutType);
            mTypeList.add(data.getType());
        }
        bindViewHolder(holder, position);
        return holder.getItemView();
    }

    /**
     * 创建ViewHolder
     *
     * @param parent 视图
     * @param type   布局类型
     * @return ViewHolder
     */
    public VH createViewHolder(ViewGroup parent, int type) {
        View root = LayoutInflater.from(parent.getContext()).inflate(type, parent, false);
        VH holder = onCreateViewHolder(parent, type);

        // 设置点击时间
        root.setTag(R.id.barrage_view_holder, root);
        root.setOnClickListener(this);
        return holder;
    }

    /**
     * 真正创建ViewHolder的方法
     *
     * @param parent 父布局
     * @param type   类型
     * @return ViewHolder
     */
    public abstract VH onCreateViewHolder(View parent, int type);

    /**
     * 得到布局的xml文件
     *
     * @return xml文件
     */
    public abstract @LayoutRes
    int getItemLayout(T t, int position);


    /**
     * 绑定数据
     *
     * @param holder   BarrageViewHolder
     * @param position 位置
     */
    protected void bindViewHolder(VH holder, int position) {
        T data = mDataList.get(position);
        if (null == data)
            return;
        holder.bind(data);
    }

    /**
     * 添加进缓存
     *
     * @param root 缓存的View
     */
    public synchronized void addViewToCaches(int type,View root){
        if (mArray.get(type) == null) {
            LinkedList<View> linkedList = new LinkedList<>();
            linkedList.add(root);
            mArray.put(type, linkedList);
        } else {
            mArray.get(type).add(root);
        }
    }


    /**
     * 删除视图
     *
     * @return 类型
     */
    public synchronized View removeViewFromCaches(int type){
        if(mArray.indexOfKey(type) >= 0){
            return mArray.get(type).pop();
        }else{
            return null;
        }
    }

    /**
     * 缩小缓存长度，减少内存的使用
     */
    public synchronized void shrinkCacheSize(){
        for (Integer type : mTypeList) {
            if(mArray.indexOfKey(type) >= 0){
                LinkedList<View> list = mArray.get(type);
                int len = list.size();
                while(list.size() > (len / 2.0 + 0.5)){
                    list.pop();
                }
                mArray.put(type,list);
            }
        }
    }

    /**
     * 获取内存View的数量
     *
     * @return 内存的大小
     */
    public int getCacheSize(){
        int sum = 0;
        for (Integer type : mTypeList) {
            if(mArray.indexOfKey(type)>=0){
                sum += mArray.get(type).size();
            }
        }
        return sum;
    }

    @Override
    public void onClick(View v) {
        VH holder = (VH) v.getTag(R.id.barrage_view_holder);
        if (holder != null) {
            if (mAdapterListener != null) {
                mAdapterListener.onItemClick(holder, holder.mData);
            }
        }
    }

    /**
     * 得到单行的高度
     *
     * @return 返回高度
     */
    public abstract int getSingleLineHeight();

    public abstract static class BarrageViewHolder<T> {
        protected T mData;
        private View itemView;

        public BarrageViewHolder(View itemView) {
            this.itemView = itemView;
        }

        public View getItemView() {
            return itemView;
        }

        void bind(T data) {
            mData = data;
            onBind(data);
        }

        protected abstract void onBind(T data);
    }
}
