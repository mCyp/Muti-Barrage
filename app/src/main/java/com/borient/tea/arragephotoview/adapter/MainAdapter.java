package com.borient.tea.arragephotoview.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.borient.tea.arragephotoview.R;

/**
 * Author WangJie
 * Created on 2019/1/14.
 */
public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

    public static final String[] values = new String[]{
            "单视图弹幕",
            "多视图弹幕",
    };

    private OnSelectListener mListener;

    public MainAdapter(OnSelectListener mListener) {
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View root = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycle_item_main, viewGroup, false);
        ViewHolder holder = new ViewHolder(root);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final String s = values[i];
        viewHolder.content = viewHolder.itemView.findViewById(R.id.txt_content);
        viewHolder.content.setText(s);
        viewHolder.content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onSelectStr(s);
            }
        });
    }

    @Override
    public int getItemCount() {
        return values.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView content;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public interface OnSelectListener{
        void onSelectStr(String str);
    }
}
