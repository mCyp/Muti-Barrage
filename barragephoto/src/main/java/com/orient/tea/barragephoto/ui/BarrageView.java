package com.orient.tea.barragephoto.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.nfc.Tag;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

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

public class BarrageView extends ViewGroup implements IBarrageView {

    public static final String TAG = "BarrageView";

    // 设置默认的
    public final static int DURATION = 3000;
    // 设置延迟
    public final static int DELAY = 1000;

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

    public BarrageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.barrageList = new ArrayList<>();
        this.mArray = new SparseArray<>();

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
            return mArray.get(type).pop();
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
    }

    /**
     * 设置单行的高度
     */
    public void setSingleLineHeight(int singleLineHeight) {
        this.singleLineHeight = singleLineHeight;
        barrageLines = height / singleLineHeight;
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
            barrageLines = width / singleLineHeight;
        }

        // 获取最佳的行数
        final int line = getBestLine(itemHeight);

        // 生成动画
        ValueAnimator valueAnimator = ValueAnimator.ofInt(width, -itemWidth);
        int speed = random.nextInt(DELAY);
        speed += 3000;
        valueAnimator.setDuration(speed);
        valueAnimator.setStartDelay(0);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                Log.e(TAG, "value:" + value);
                view.layout(value, line * singleLineHeight, value + itemWidth, line * singleLineHeight + itemHeight);
            }
        });
        valueAnimator.addListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                // TODO 清空View 如何清空缓存
                // 想要实现这个功能 必须保证防碰撞检测
                BarrageView.this.removeView(view);
                BarrageAdapter.BarrageViewHolder holder = (BarrageAdapter.BarrageViewHolder) view.getTag(R.id.barrage_view_holder);
                DataSource d = (DataSource) holder.mData;
                int type = d.getType();
                addViewToCaches(type,view);

            }
        });
        addView(view);
        valueAnimator.start();
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
     * TODO 设置防碰撞检测
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

    @Override
    public View getCacheView(int type) {
        return removeViewFromCaches(type);
    }

}
