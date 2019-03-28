package com.orient.tea.barragephoto.ui;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.nfc.Tag;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import com.orient.tea.barragephoto.R;
import com.orient.tea.barragephoto.adapter.BarrageAdapter;
import com.orient.tea.barragephoto.listener.SimpleAnimationListener;
import com.orient.tea.barragephoto.model.DataSource;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 弹幕视图
 * 使用属性动画实现监听事件
 * TODO 使用替换缓存的方式可能存在问题 到时候检测一下
 * <p>
 * Created by wangjie on 2019/3/7.
 */

@SuppressWarnings({"unchecked", "FieldCanBeLocal", "unused", "MismatchedReadAndWriteOfArray"})
public class BarrageView extends ViewGroup implements IBarrageView {
    public static final String TAG = "BarrageView";

    // 防碰撞模式
    public final static int MODEL_COLLISION_DETECTION = 1;
    // 随机生成
    public final static int MODEL_RANDOM = 2;
    // 设置默认的
    public final static int DURATION = 4000;
    // 设置滑动波动值
    public final static int WAVE_VALUE = 2000;
    // 设置最大的缓存View的数量 当达到200的时候回收View
    public final static int MAX_COUNT = 200;
    // 记录放入缓存的View
    public volatile int count = 0;
    // 发送间隔
    public long interval;
    // 模式
    private int model = MODEL_RANDOM;
    // 基础的一条弹幕滑动时间
    private int duration = DURATION;
    // 基础的上下波动的时间
    private int waveValue = WAVE_VALUE;

    private Handler mHandler;

    // 弹幕的相对位置
    public final static int GRAVITY_TOP = 1;
    public final static int GRAVITY_MIDDLE = 2;
    public final static int GRAVITY_BOTTOM = 4;
    public final static int GRAVITY_FULL = 7;

    // 当前的gravity
    private int gravity = GRAVITY_TOP;
    // 行数
    private int barrageLines;
    // 宽度和高度
    private int width, height;
    private List<View> barrageList;
    // 每一行的动画时间的数组
    private int[] durationArray;
    // 速度设置
    // TODO 先暂时设置固定的速度 根据需要再修改
    private BarrageAdapter mAdapter;
    // 单行的高度
    // TODO 利用UI工具使用当前高度的1/8
    private int singleLineHeight = -1;

    // View的缓存
    private SparseArray<LinkedList<View>> mArray;
    // 随机值
    private Random random = new Random();


    public BarrageView(Context context) {
        this(context, null);
    }

    public BarrageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("HandlerLeak")
    public BarrageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.barrageList = new ArrayList<>();
        this.mArray = new SparseArray<>();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        if (count < MAX_COUNT) {
                            // 思考一下200是否合适
                            count++;
                        } else {
                            // 发动gc
                            shrinkCacheSize();
                            // 计算一下
                            count = getCacheSize();
                        }
                }
            }
        };
    }

    /**
     * 添加进缓存
     *
     * @param root 缓存的View
     */
    public synchronized void addViewToCaches(int type, View root) {
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
    public synchronized View removeViewFromCaches(int type) {
        if (mArray.indexOfKey(type) >= 0) {
            return mArray.get(type).poll();
        } else {
            return null;
        }
    }

    /**
     * 缩小缓存长度，减少内存的使用
     */
    public synchronized void shrinkCacheSize() {
        Set<Integer> mTypeList = mAdapter.getTypeList();
        for (Integer type : mTypeList) {
            if (mArray.indexOfKey(type) >= 0) {
                LinkedList<View> list = mArray.get(type);
                int len = list.size();
                while (list.size() > (len / 2.0 + 0.5)) {
                    list.pop();
                }
                mArray.put(type, list);
            }
        }
    }

    /**
     * 获取内存View的数量
     *
     * @return 内存的大小
     */
    public int getCacheSize() {
        int sum = 0;
        Set<Integer> mTypeList = mAdapter.getTypeList();
        for (Integer type : mTypeList) {
            if (mArray.indexOfKey(type) >= 0) {
                sum += mArray.get(type).size();
            }
        }
        return sum;
    }

    /**
     * 设置适配器
     *
     * @param adapter 适配器
     */
    public void setAdapter(BarrageAdapter adapter) {
        this.mAdapter = adapter;
        // 相互绑定
        mAdapter.setBarrageView(this);
    }

    /**
     * 视图发送的间隔
     *
     * @param interval 间隔 单位毫秒
     */
    public void setInterval(long interval) {
        this.interval = interval;
    }

    /**
     * 设置间隔
     *
     * @param duration  弹幕滑行时间
     * @param waveValue 波动时间
     */
    public void setDuration(int duration, int waveValue) {
        if (duration < waveValue
                || duration <= 0
                || waveValue < 0)
            throw new RuntimeException("duration or wavValue is not correct!");
        this.duration = duration;
        this.waveValue = waveValue;
    }

    /**
     * 弹幕模式 默认随机速度模式
     *
     * @param model 模式类型
     */
    public void setModel(int model) {
        this.model = model;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        this.width = width;
        this.height = height;

        // TODO 暂时设置全屏
        // 根据需要可以设置高度
        //measureChildren(widthMeasureSpec,heightMeasureSpec);
       /* if(singleLineHeight == -1){

            initBarrageListAndSpeedArray();
        }*/

    }

    /**
     * 初始化一个空的弹幕列表和速度列表
     */
    private void initBarrageListAndSpeedArray() {
        barrageLines = height / singleLineHeight;
        for (int i = 0; i < barrageLines; i++) {
            barrageList.add(i, null);
        }
        durationArray = new int[barrageLines];
        for (int i = 0; i < barrageLines; i++) {
            durationArray[i] = 0;
        }
    }

    /**
     * 设置单行的高度
     */
    public void setSingleLineHeight(int singleLineHeight) {
        this.singleLineHeight = singleLineHeight;
    }

    @Override
    public void addBarrageItem(final View view) {
        // 获取高度和宽度
        int w = View.MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        view.measure(w, h);
        final int itemWidth = view.getMeasuredWidth();
        final int itemHeight = view.getMeasuredHeight();

        if (singleLineHeight == -1) {
            // 如果没有设置高度 启用添加的第一个Item作为行数
            // 建议使用最小的Item的高度
            singleLineHeight = itemHeight;
            initBarrageListAndSpeedArray();
        }

        // TODO 设置防碰撞检测
        // 获取最佳的行数
        final int line = getBestLine(itemHeight);

        // 计算速度

        // 生成动画
        ValueAnimator valueAnimator = ValueAnimator.ofInt(width, -itemWidth);
        int duration = getDuration(line, itemWidth);
        valueAnimator.setDuration(duration);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                //Log.e(TAG, "value:" + value);
                view.layout(value, line * singleLineHeight, value + itemWidth, line * singleLineHeight + itemHeight);
            }
        });
        valueAnimator.addListener(new SimpleAnimationListener() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                BarrageView.this.removeView(view);
                BarrageAdapter.BarrageViewHolder holder = (BarrageAdapter.BarrageViewHolder) view.getTag(R.id.barrage_view_holder);
                DataSource d = (DataSource) holder.mData;
                int type = d.getType();
                addViewToCaches(type, view);
                // 通知内存添加缓存
                mHandler.sendEmptyMessage(0);
            }
        });
        addView(view);
        durationArray[line] = duration;
        // 因为使用缓存View，必须重置位置
        view.layout(width, line * singleLineHeight, width + itemWidth, line * singleLineHeight + itemHeight);
        barrageList.set(line, view);
        valueAnimator.start();
    }

    /**
     * 获取弹幕从屏幕开始到结束所花时间
     *
     * @return 弹幕时间
     */
    private int getDuration(int line, int itemWidth) {
        if (model == MODEL_RANDOM) {
            return duration - waveValue + random.nextInt(2 * waveValue);
        } else {
            int lastDuration = durationArray[line];
            View view = barrageList.get(line);
            int currDuration;
            if (view == null) {
                currDuration = duration - waveValue + random.nextInt(2 * waveValue);
                Log.e(TAG, "View:null" + ",line:" + line + ",duration:" + currDuration);
                // 如果当前为空 随机生成一个滑动时间
                return currDuration;
            }
            int slideLength = (int) (width - view.getX());
            if (itemWidth > slideLength) {
                // 数据密集的时候跟上面的时间间隔相同
                Log.e(TAG, "View:------" + ",line:" + line + ",duration:" + lastDuration);
                return lastDuration;
            }
            // 得到上个View剩下的滑动时间
            int lastLeavedSlidingTime = (int) ((view.getX() + itemWidth) / (width * 1.0f) * lastDuration);
            //Log.e(TAG,"lastLeavedSlidingTime:"+lastLeavedSlidingTime+",lastLeavedSlidingTime:"+);
            lastLeavedSlidingTime = Math.max(lastLeavedSlidingTime, duration - waveValue);
            currDuration = lastLeavedSlidingTime + random.nextInt(duration + waveValue - lastLeavedSlidingTime);
            Log.e(TAG, "view:" + view.getX() + ",lastLeavedSlidingTime:" + lastLeavedSlidingTime + ",line:" + line + ",duration:" + currDuration);
            return currDuration;
        }
    }

    /**
     * 获取最佳的行数
     *
     * @param currentItemHeight 当前的高度
     * @return 最佳行数
     */
    private int getBestLine(int currentItemHeight) {
        if (currentItemHeight <= singleLineHeight) {
            return realGetBestLine(1);
        } else {
            int v = currentItemHeight / singleLineHeight;
            if (v * singleLineHeight < currentItemHeight)
                v++;
            return realGetBestLine(v);
        }
    }

    /**
     * 真实获取最佳的行数
     *
     * @param v 当前View的高度/单行的标准高度
     * @return 最佳行数
     */
    private int realGetBestLine(int v) {
        //转换成2进制
        int gewei = gravity % 2;   //个位是
        int temp = gravity / 2;
        int shiwei = temp % 2;
        temp = temp / 2;
        int baiwei = temp % 2;

        //将所有的行分为三份,前两份行数相同,将第一份的行数四舍五入
        int firstPart = (int) (barrageLines / 3.0f + 0.5f);

        //构造允许输入行的列表
        List<Integer> legalLines = new ArrayList<>();
        if (gewei == 1) {
            for (int i = 0; i < firstPart; i++)
                if (i % v == 0)
                    legalLines.add(i);
        }
        if (shiwei == 1) {
            for (int i = firstPart; i < 2 * firstPart; i++)
                if (i % v == 0)
                    legalLines.add(i);
        }
        if (baiwei == 1) {
            for (int i = 2 * firstPart; i < barrageLines; i++)
                if (i % v == 0 && i <= barrageLines - v)
                    legalLines.add(i);
        }


        int bestLine = 0;
        //如果有空行直接结束
        for (int i = 0; i < barrageLines; i++) {
            if (barrageList.get(i) == null && i % v == 0) {
                bestLine = i;
                if (legalLines.contains(bestLine))
                    return bestLine;
            }
        }
        float minSpace = Integer.MAX_VALUE;
        //没有空行，就找最大空间的
        for (int i = barrageLines - 1; i >= 0; i--) {
            if (i % v == 0 && i <= barrageLines - v)
                if (legalLines.contains(i)) {
                    if (barrageList.get(i).getX() + barrageList.get(i).getWidth() <= minSpace) {
                        minSpace = barrageList.get(i).getX() + barrageList.get(i).getWidth();
                        bestLine = i;
                    }
                }
        }
        return bestLine;
    }

    public void destroy() {
        // 清除消息队列，防止内存泄漏
        mHandler.removeCallbacksAndMessages(null);
        mAdapter.destroy();
    }

    @Override
    public View getCacheView(int type) {
        return removeViewFromCaches(type);
    }

    @Override
    public long getInterval() {
        return interval;
    }

}
