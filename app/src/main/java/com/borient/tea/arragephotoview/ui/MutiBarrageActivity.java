package com.borient.tea.arragephotoview.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.borient.tea.arragephotoview.R;
import com.borient.tea.arragephotoview.data.BarrageData;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.orient.tea.barragephoto.adapter.AdapterListener;
import com.orient.tea.barragephoto.adapter.BarrageAdapter;
import com.orient.tea.barragephoto.ui.BarrageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MutiBarrageActivity extends AppCompatActivity {

    private String text[] = {"666666666666666", "大盖伦无敌！", "这波操作不亏，50血极限反杀，我们还有机会！"
            , "雷瑟守备钻石守门员求带～", "反向操作！！！！！", "谢谢金克丝送的一发火箭"};

    private String name[] = {"逍遥子送了一架飞机", "王境泽送了一辆UFO", "无敌的VN送了一辆宝马", "快乐的皮皮虾送了一发火箭"};

    private final int ICON_RESOURCES[] = {R.drawable.plane, R.drawable.ufo, R.drawable.car
            , R.drawable.rocket};

    private Random random = new Random();

    private BarrageView barrageView;
    private BarrageAdapter<BarrageData> mAdapter;
    //private Button mAdd;
    private List<BarrageData> barrageDataList = new ArrayList<>();

    public static void show(Context context) {
        Intent intent = new Intent(context, MutiBarrageActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muti_barrage);

        //mAdd = findViewById(R.id.btn_add);
        barrageView = findViewById(R.id.barrage);
        barrageView.setInterval(50);
        barrageView.setSpeed(200, 20);
        barrageView.setModel(BarrageView.MODEL_COLLISION_DETECTION);
        barrageView.setInterceptTouchEvent(true);
        barrageView.setGravity(BarrageView.GRAVITY_TOP);
        barrageView.setAdapter(mAdapter = new BarrageAdapter<BarrageData>(null, this) {
            @Override
            public BarrageViewHolder<BarrageData> onCreateViewHolder(View root, int type) {
                switch (type) {
                    case R.layout.barrage_item_text:
                    case R.layout.barrage_item_text_vip:
                        return new TextViewHolder(root);
                    default:
                        return new BigHolder(root);
                }

            }

            @Override
            public int getItemLayout(BarrageData barrageData) {
                switch (barrageData.getType()) {
                    case 0:
                        return R.layout.barrage_item_text;
                    case 1:
                        return R.layout.barrage_item_text_vip;
                    default:
                        return R.layout.barrage_item_big;
                }
            }
        });
        mAdapter.setAdapterListener(new AdapterListener<BarrageData>() {
            @Override
            public void onItemClick(BarrageAdapter.BarrageViewHolder<BarrageData> holder, BarrageData item) {
                Toast.makeText(MutiBarrageActivity.this, item.getContent() + "点击了一次", Toast.LENGTH_SHORT).show();
            }
        });

        /*mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initData();
            }
        });*/
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*TimerTask task = new TimerTask() {
            @Override
            public void run() {
                initData();
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 1000);*/

    }


    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);

        initData();
    }


    private void initData() {
        int strLength = text.length;
        for (int i = 0; i < 50; i++) {
            if (i == 0 || i % 9 != 0)
                if (i % 4 != 0)
                    mAdapter.add(new BarrageData(text[i % 6], 0, i));
                else
                    mAdapter.add(new BarrageData(text[i % 6], 1, i));
            else
                mAdapter.add(new BarrageData(name[i % name.length], 2, i));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        barrageView.destroy();
    }

    class TextViewHolder extends BarrageAdapter.BarrageViewHolder<BarrageData> {

        private TextView mContent;

        public TextViewHolder(View itemView) {
            super(itemView);

            mContent = itemView.findViewById(R.id.content);
        }

        @Override
        protected void onBind(BarrageData data) {
            mContent.setText(data.getContent());
        }
    }

    class BigHolder extends BarrageAdapter.BarrageViewHolder<BarrageData> {

        private ImageView mHeadView;
        private TextView mContent;

        public BigHolder(View itemView) {
            super(itemView);

            mHeadView = itemView.findViewById(R.id.image);
            mContent = itemView.findViewById(R.id.content);
        }

        @Override
        protected void onBind(BarrageData data) {
            mHeadView.setImageResource(ICON_RESOURCES[data.getPos() % ICON_RESOURCES.length]);
            mContent.setText(data.getContent());
        }
    }
}
