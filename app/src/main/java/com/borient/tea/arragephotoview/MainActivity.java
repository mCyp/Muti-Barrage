package com.borient.tea.arragephotoview;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.borient.tea.arragephotoview.data.BarrageData;
import com.bumptech.glide.Glide;
import com.orient.tea.barragephoto.adapter.BarrageAdapter;
import com.orient.tea.barragephoto.ui.BarrageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private String SEED[] = {"桃树", "，都开满了花赶趟儿。红的像火，", "花里带着甜味儿，闭了眼，树上", "满是桃儿、", "嗡地闹着，大小的蝴蝶"};
    private Random random = new Random();
    private final int ICON_RESOURCES[] = {R.drawable.icon1, R.drawable.icon2, R.drawable.icon3, R.drawable.icon4, R.drawable.icon5};

    private BarrageView barrageView;
    private BarrageAdapter<BarrageData,ViewHolder> mAdapter;
    private Button mAdd;
    private List<BarrageData> barrageDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdd = findViewById(R.id.btn_add);
        barrageView = findViewById(R.id.barrage);
        barrageView.setInterval(20);
        barrageView.setModel(BarrageView.MODEL_COLLISION_DETECTION);
        barrageView.setAdapter(mAdapter = new BarrageAdapter<BarrageData, ViewHolder>(null,this) {
            @Override
            public ViewHolder onCreateViewHolder(View root, int type) {
                return new ViewHolder(root);
            }

            @Override
            public int getItemLayout(BarrageData barrageData) {
                return R.layout.item_barrage;
            }
        });

        //initData();

        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initData();
            }
        });
    }

    private void initData(){
        int strLength = SEED.length;
        for(int i = 0;i<50;i++){
            mAdapter.add(new BarrageData(SEED[random.nextInt(strLength-1)],1));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        barrageView.destroy();
    }

    class ViewHolder extends BarrageAdapter.BarrageViewHolder<BarrageData>{

        private ImageView mHeadView;
        private TextView mContent;

        public ViewHolder(View itemView) {
            super(itemView);

            mHeadView = itemView.findViewById(R.id.image);
            mContent = itemView.findViewById(R.id.content);
        }

        @Override
        protected void onBind(BarrageData data) {
            Glide.with(MainActivity.this).load(ICON_RESOURCES[random.nextInt(5)]).into(mHeadView);
            mContent.setText(data.getContent());
        }
    }
}
