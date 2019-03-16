package com.borient.tea.arragephotoview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.orient.tea.barragephoto.ui.BarrageView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private BarrageView barrageView;
    private Button mAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdd = findViewById(R.id.btn_add);
        barrageView = findViewById(R.id.barrage);

        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = LayoutInflater.from(MainActivity.this)
                        .inflate(R.layout.item_danmu,null);
                barrageView.addBarrageItem(view);
            }
        });
    }
}
