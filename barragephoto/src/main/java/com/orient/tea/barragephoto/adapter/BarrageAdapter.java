package com.orient.tea.barragephoto.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orient.tea.barragephoto.R;
import com.orient.tea.barragephoto.model.DataSource;
import com.orient.tea.barragephoto.ui.IBarrageView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 基础的适配器
 * <p>
 * Created by wangjie on 2019/3/7.
 */

@SuppressWarnings("unchecked")
public abstract class BarrageAdapter<T extends DataSource, VH extends BarrageAdapter.BarrageViewHolder<T>>
        implements View.OnClickListener {

    private static final int MSG_CREATE_VIEW = 1;
    private static final String TAG = "BarrageAdapter";

    // View的点击监听
    private AdapterListener<T> mAdapterListener;
    // 类型List
    private Set<Integer> mTypeList;
    // 持有的barrageView
    private IBarrageView barrageView;
    // 当前的数据
    private LinkedList<T> mDataList;
    private Context mContext;
    // 默认的间隔
    private long interval;

    // 单线程的消息对立
    private ExecutorService mService = Executors.newSingleThreadExecutor();
    // 主线程的Handler
    private BarrageAdapterHandler<T> mHandler = new BarrageAdapterHandler<>(Looper.getMainLooper(),this);
    /*private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what){
                case MSG_CREATE_VIEW:{
                    T data = mDataList.remove();
                    if(data == null)
                        break;
                    if(barrageView == null)
                        throw new RuntimeException("please set barrageView,barrageView can't be null");
                    // get from cache
                    View cacheView = barrageView.getCacheView(data.getType());
                    createItemView(data,cacheView);
                }
            }

        }
    };*/


    public BarrageAdapter(AdapterListener<T> adapterListener,Context context) {
        this.mAdapterListener = adapterListener;
        this.mTypeList = new HashSet<>();
        this.mContext = context;
        this.mDataList = new LinkedList<>();
    }

    public void setAdapterListener(AdapterListener<T> adapterListener){
        this.mAdapterListener = adapterListener;
    }


    public void setBarrageView(IBarrageView barrageView){
        this.barrageView = barrageView;
        this.interval = barrageView.getInterval();
    }

    // TODO 数据的增加处理

    /**
     * 创建子视图的过程
     *
     * @param cacheView 缓存视图
     * @return 视图
     */
    public void createItemView(T data, View cacheView) {
        // 1.获取子布局
        // 2. 创建ViewHolder
        // 3. 绑定ViewHolder
        // 4. 返回视图
        int layoutType = getItemLayout(data);
        VH holder = null;
        if (cacheView != null) {
            holder = (VH) cacheView.getTag(R.id.barrage_view_holder);
        }
        if (null == holder) {
            holder = createViewHolder(mContext, layoutType);
            mTypeList.add(data.getType());
        }
        bindViewHolder(holder,data);
        if(barrageView != null)
            barrageView.addBarrageItem(holder.getItemView());
    }

    /**
     * 创建ViewHolder
     *
     * @param type   布局类型
     * @return ViewHolder
     */
    private VH createViewHolder(Context context, int type) {
        View root = LayoutInflater.from(context).inflate(type,null);
        VH holder = onCreateViewHolder(root, type);

        // 设置点击时间
        root.setTag(R.id.barrage_view_holder, holder);
        root.setOnClickListener(this);
        return holder;
    }

    /**
     * 真正创建ViewHolder的方法
     *
     * @param type   类型
     * @return ViewHolder
     */
    protected abstract VH onCreateViewHolder(View root, int type);

    /**
     * 得到布局的xml文件
     *
     * @return xml文件
     */
    public abstract @LayoutRes
    int getItemLayout(T t);


    /**
     * 绑定数据
     *
     * @param holder   BarrageViewHolder
     * @param data T
     */
    protected void bindViewHolder(VH holder, T data) {
        if (null == data)
            return;
        holder.bind(data);
    }

    public Set<Integer> getTypeList() {
        return mTypeList;
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
     * 添加一组数据
     *
     * @param data T
     */
    public void add(T data){
        if(data == null)
            return;
        mDataList.add(data);
        mService.submit(new DelayRunnable(1));
    }

    /**
     * 添加一组数据
     *
     * @param dataList 一组数据
     */
    public void addList(List<T> dataList){
        if(dataList == null || dataList.size() ==0)
            return;
        int len = dataList.size();
        mDataList.addAll(dataList);
        mService.submit(new DelayRunnable(len));
    }

    public void destroy(){
        mService.shutdownNow();
        mHandler.removeCallbacksAndMessages(null);
        barrageView = null;
    }

    public abstract static class BarrageViewHolder<T> {
        public T mData;
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

    /**
     * 延迟的Runnable
     */
    public class DelayRunnable implements Runnable{

        private int len;

        public DelayRunnable(int len) {
            this.len = len;
        }

        @Override
        public void run() {
            for(int i = 0;i<len;i++){
                mHandler.sendEmptyMessage(MSG_CREATE_VIEW);
                try {
                    Thread.sleep(interval*20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class BarrageAdapterHandler<T extends DataSource> extends Handler{
        private WeakReference<BarrageAdapter> adapterReference;

        public BarrageAdapterHandler(Looper looper,BarrageAdapter adapter) {
            super(looper);
            adapterReference = new WeakReference<BarrageAdapter>(adapter);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what){
                case MSG_CREATE_VIEW:{
                    T data = (T)adapterReference.get().mDataList.remove();
                    if(data == null)
                        break;
                    if(adapterReference.get().barrageView == null)
                        throw new RuntimeException("please set barrageView,barrageView can't be null");
                    // get from cache
                    View cacheView = adapterReference.get().barrageView.getCacheView(data.getType());
                    adapterReference.get().createItemView(data,cacheView);
                }
            }

        }

    }
}
