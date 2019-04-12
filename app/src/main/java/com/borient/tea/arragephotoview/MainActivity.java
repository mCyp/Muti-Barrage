package com.borient.tea.arragephotoview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.borient.tea.arragephotoview.adapter.MainAdapter;
import com.borient.tea.arragephotoview.data.BarrageData;
import com.borient.tea.arragephotoview.ui.MutiBarrageActivity;
import com.borient.tea.arragephotoview.ui.SingleBarrageActivity;
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

public class MainActivity extends AppCompatActivity implements MainAdapter.OnSelectListener{

    private static final String TAG = "MainActivity";

    private RecyclerView mRecyclerView;
    private MainAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWidget();
    }

    private void initWidget() {
        mRecyclerView = findViewById(R.id.recycle);
        mAdapter = new MainAdapter(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onSelectStr(String str) {
        switch (str){
            case "单视图弹幕":
                SingleBarrageActivity.show(this);
                break;
            case "多视图弹幕":
                MutiBarrageActivity.show(this);
                break;
            default:
                break;
        }
    }
}
