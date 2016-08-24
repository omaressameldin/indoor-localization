package Classes;


import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.oessa_000.countsteps.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by oessa_000 on 8/2/2016.
 */
public class EstimotesListAdapter extends RecyclerView.Adapter {
    /* Adapter Variables */
    ArrayList<HashMap<String,String>> data;
    public EstimotesListAdapter(ArrayList<HashMap<String, String>> data){
        this.data = data;
    }


    /* List Item View Class */
    public static class EstimoteViewHolder extends RecyclerView.ViewHolder {
        /*Estimote Item Variables */
        EditText mac;
        EditText xpos;
        EditText ypos;
        EditText baserssi;
        TextView estimoteTitle;


        EstimoteViewHolder(View itemView) {
            super(itemView);
            /* get view items from layout */
            mac = (EditText)itemView.findViewById(R.id.mac);
            xpos = (EditText)itemView.findViewById(R.id.xpos);
            ypos = (EditText)itemView.findViewById(R.id.ypos);
            baserssi = (EditText)itemView.findViewById(R.id.base_rssi);
            estimoteTitle = (TextView) itemView.findViewById(R.id.estimoteTitle);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.estimote_list_item, parent, false);
        EstimoteViewHolder pvh = new EstimoteViewHolder(v);
        return pvh;
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        /* Set Estimote Title based on position */
        ((EstimoteViewHolder) holder).estimoteTitle.setText("Estimote " + (position + 1));
        /* Update changed Mac */
        ((EstimoteViewHolder) holder).mac.setText(data.get(position).get("mac"));
        ((EstimoteViewHolder) holder).mac.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                data.get(position).put("mac", s.toString());
            }
            public void afterTextChanged(Editable s) {
            }
        });
        /* Update changed x-position */
        ((EstimoteViewHolder) holder).xpos.setText(data.get(position).get("xpos"));
        ((EstimoteViewHolder) holder).xpos.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                data.get(position).put("xpos", s.toString());
            }
            public void afterTextChanged(Editable s) {
            }
        });
        /* Update changed y-position */
        ((EstimoteViewHolder) holder).ypos.setText(data.get(position).get("ypos"));
        ((EstimoteViewHolder) holder).ypos.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                data.get(position).put("ypos", s.toString());
            }
            public void afterTextChanged(Editable s) {
            }
        });
        /* Update changed base RSSI */
        ((EstimoteViewHolder) holder).baserssi.setText(data.get(position).get("base_rssi"));
        ((EstimoteViewHolder) holder).baserssi.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                data.get(position).put("base_rssi", s.toString());
            }
            public void afterTextChanged(Editable s) {
            }
        });
    }


    @Override
    public int getItemCount() {
        return data.size();
    }


}
