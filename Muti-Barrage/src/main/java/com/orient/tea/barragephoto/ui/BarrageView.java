package com.orient.tea.barragephoto.ui;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import com.orient.tea.barragephoto.R;
import com.orient.tea.barragephoto.adapter.BarrageAdapter;
import com.orient.tea.barragephoto.listener.SimpleAnimationListener;
import com.orient.tea.barragephoto.model.DataSource;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * 弹幕视图
 * 使用属性动画实现监听事件
 * <p>
 * Created by wangjie on 2019/3/7.
 */

@SuppressWarnings({"unchecked", "FieldCanBeLocal", "unused", "MismatchedReadAndWriteOfArray"})
public class BarrageView extends ViewGroup implements IBarrageView {
    // TODO
    // 1. 多类型下的碰撞检测
    // 2. 测试
    // 3. 具体行数的设置
    // 4. 具体的高度设置和位置设置
    public static final String TAG = "BarrageView";

    // 1 碰撞检测模式 2 随机生成模式
    public final static int MODEL_COLLISION_DETECTION = 1;
    public final static int MODEL_RANDOM = 2;
    // 弹幕的相对位置
    public final static int GRAVITY_TOP = 1;
    public final static int GRAVITY_MIDDLE = 2;
    public final static int GRAVITY_BOTTOM = 4;
    public final static int GRAVITY_FULL = 7;
    // 设置最大的缓存View的数量 当达到200的时候回收View
    public final static int MAX_COUNT = 500;
    // 速度和波动速度的默认值
    public final static int DEFAULT_SPEED = 200;
    public final static int DEFAULT_WAVE_SPEED = 20;

    private BarrageHandler mHandler;
    // 记录放入缓存的View
    public int count = 0;
    // 发送间隔
    public long interval;
    // 模式
    private int model = MODEL_RANDOM;

    // 新增速度 px/100ms
    private int speed = 200;
    private int speedWaveValue = 20;
    // 每一行的速度的储存
    private int[] speedArray;

    // 是否设置当前动画
    private boolean cancel = false;

    // 当前的gravity
    private int gravity = GRAVITY_TOP;
    // 行数
    private int barrageLines;
    // 重复次数
    private int repeat;
    // 宽度和高度
    private int width, height;
    private List<View> barrageList;
    private BarrageAdapter mAdapter;
    // 单行的高度
    private int singleLineHeight = -1;
    // 是否阻止事件的下发
    private boolean isInterceptTouchEvent = false;
    // 上下弹幕之间的距离
    private int barrageDistance;
    // View的缓存
    private SparseArray<LinkedList<View>> mArray;
    private Random random = new Random();
    private CountDownLatch countDownLatch = new CountDownLatch(1);


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
        mHandler = new BarrageHandler(this);

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

    public void setOptions(Options options){
        if(options != null){
            if(options.config.gravity != -1){
                this.gravity = options.config.gravity;
            }

            if(options.config.interval > 0){
                this.interval = options.config.interval;
            }

            if(options.config.speed != 0 && options.config.waveSpeed != 0){
                this.speed = options.config.speed;
                this.speedWaveValue = options.config.waveSpeed;
            }

            if(options.config.model != 0){
                this.model = options.config.model;
            }

            if(options.config.repeat != 0){
                this.repeat = options.config.repeat;
            }

            this.isInterceptTouchEvent = options.config.isInterceptTouchEvent;
        }
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

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isInterceptTouchEvent)
            return true;
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        this.width = width;
        this.height = height;
        //countDownLatch.countDown();
    }

    /**
     * 初始化一个空的弹幕列表和速度列表
     */
    private void initBarrageListAndSpeedArray() {
        barrageDistance = DeviceUtils.dp2px(getContext(), 12);
        /*try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        barrageLines = height / (singleLineHeight + barrageDistance);
        for (int i = 0; i < barrageLines; i++) {
            barrageList.add(i, null);
        }
        speedArray = new int[barrageLines];
        for (int i = 0; i < barrageLines; i++) {
            speedArray[i] = 0;
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
        // 生成动画
        final ValueAnimator valueAnimator = ValueAnimator.ofInt(width, -itemWidth);

        // 获取最佳的行数
        final int line = getBestLine(itemHeight);
        int curSpeed = getSpeed(line, itemWidth);
        long duration = (int)((float)(width+itemWidth)/(float)curSpeed+1) * 1000;
        //Log.i(TAG,"duration:"+duration);
        valueAnimator.setDuration(duration);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value =  animation.getAnimatedFraction();
                //animation.getAnimatedValue()
                //Log.e(TAG, "value:" + value);
                if(cancel){
                    valueAnimator.cancel();
                    BarrageView.this.removeView(view);
                }
                //view.layout(value, line * (singleLineHeight + barrageDistance) + barrageDistance / 2, value + itemWidth, line * (singleLineHeight + barrageDistance) + barrageDistance / 2 + itemHeight);
                view.layout((int) (width - (width + itemWidth) * value)
                        , line * (singleLineHeight + barrageDistance) + barrageDistance / 2
                        , (int) (width - (width + itemWidth) * value) + itemWidth
                        , line * (singleLineHeight + barrageDistance) + barrageDistance / 2 + itemHeight);
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
        speedArray[line] = curSpeed;
        // 因为使用缓存View，必须重置位置
        view.layout(width, line * (singleLineHeight + barrageDistance) + barrageDistance / 2, width + itemWidth, line * (singleLineHeight + barrageDistance) + barrageDistance / 2 + itemHeight);
        barrageList.set(line, view);
        valueAnimator.start();
    }

    /**
     * 获取速度
     *
     * @param line 最佳弹道
     * @param itemWidth 子View的宽度
     * @return 速度
     */
    private int getSpeed(int line, int itemWidth) {
        if (model == MODEL_RANDOM) {
            return speed - speedWaveValue + random.nextInt(2 * speedWaveValue);
        } else {
            int lastSpeed = speedArray[line];
            View view = barrageList.get(line);
            int curSpeed;
            if (view == null) {
                curSpeed = speed - speedWaveValue + random.nextInt(2 * speedWaveValue);
                //Log.e(TAG, "View:null" + ",line:" + line + ",speed:" + curSpeed);
                // 如果当前为空 随机生成一个滑动时间
                return curSpeed;
            }
            int slideLength = (int) (width - view.getX());
            if (view.getWidth() > slideLength) {
                // 数据密集的时候跟上面的时间间隔相同
                //Log.e(TAG, "View:------" + ",line:" + line + ",speed:" + lastSpeed);
                return lastSpeed;
            }
            // 得到上个View剩下的滑动时间
            int lastLeavedSlidingTime = (int) ((view.getX() + view.getWidth() ) / (float) lastSpeed)+1;
            //Log.e(TAG,"lastLeavedSlidingTime:"+lastLeavedSlidingTime+",lastLeavedSlidingTime:"+);
            int fastestSpeed = (width) / lastLeavedSlidingTime;
            fastestSpeed = Math.min(fastestSpeed, speed + speedWaveValue);
            if (fastestSpeed <= speed - speedWaveValue) {
                curSpeed = speed - speedWaveValue;
            } else
                curSpeed = speed - speedWaveValue + random.nextInt(fastestSpeed - (speed - speedWaveValue));
            //Log.e(TAG, "view:" + view.getX() + ",lastLeavedSlidingTime:" + lastLeavedSlidingTime + ",line:" + line + ",speed:" + curSpeed);
            return curSpeed;
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
        // 停止动画
        cancel = true;
        // 清除消息队列，防止内存泄漏
        mHandler.removeCallbacksAndMessages(null);
        mAdapter.destroy();
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

    @Override
    public View getCacheView(int type) {
        return removeViewFromCaches(type);
    }

    @Override
    public long getInterval() {
        return interval;
    }

    @Override
    public int getRepeat() {
        return repeat;
    }

    private static class BarrageHandler extends Handler {
        private WeakReference<BarrageView> barrageViewReference;

        BarrageHandler(BarrageView barrageView) {
            this.barrageViewReference = new WeakReference(barrageView);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (barrageViewReference.get().count < MAX_COUNT) {
                        // 思考一下200是否合适
                        barrageViewReference.get().count++;
                    } else {
                        // 发动gc
                        barrageViewReference.get().shrinkCacheSize();
                        // 计算一下
                        barrageViewReference.get().count = barrageViewReference.get().getCacheSize();
                    }
            }
        }
    }

    static class Config {
        int gravity = -1;
        long interval;
        int speed;
        int waveSpeed;
        int model;
        boolean isInterceptTouchEvent = true;
        int repeat = 1;
    }

    public static class Options{

        Config config;

        public Options() {
            config = new Config();
        }

        /**
         * 布局位置
         *
         * @param gravity 布局位置
         */
        public Options setGravity(int gravity){
            this.config.gravity = gravity;
            return this;
        }

        /**
         * 视图发送的间隔
         *
         * @param interval 间隔 单位毫秒
         */
        public Options setInterval(long interval) {
            this.config.interval = interval;
            return this;
        }

        /**
         * 设置间隔
         *
         * @param speed     弹幕滑动的基础速度
         * @param waveValue 滑动素的波动值
         */
        public Options setSpeed(int speed, int waveValue) {
            if (speed < waveValue
                    || speed <= 0
                    || waveValue < 0)
                throw new RuntimeException("duration or wavValue is not correct!");
            this.config.speed = speed;
            this.config.waveSpeed = waveValue;
            return this;
        }

        /**
         * 弹幕模式 默认随机速度模式
         *
         * @param model 模式类型
         */
        public Options setModel(int model) {
            this.config.model = model;
            return this;
        }

        /**
         * 循环次数 默认为1次 可以无限循环
         *
         * @param repeat 模式类型
         */
        public Options setRepeat(int repeat) {
            this.config.repeat = repeat;
            return this;
        }

        /**
         * 设置是否阻止事件的下发
         */
        public Options setClick(boolean isInterceptTouchEvent) {
            this.config.isInterceptTouchEvent = !isInterceptTouchEvent;
            return this;
        }

    }

}
