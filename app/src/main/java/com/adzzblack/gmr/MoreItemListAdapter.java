package com.adzzblack.gmr;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by antonnw on 22/06/2016.
 */
public class MoreItemListAdapter extends ArrayAdapter<MoreItemAdapter> {

    private List<MoreItemAdapter> items;
    private int layoutResourceId;
    private Context context;

    public MoreItemListAdapter(Context context, int layoutResourceId, List<MoreItemAdapter> items) {
        super(context, layoutResourceId, items);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.items = items;
    }

    public static class Holder {
        MoreItemAdapter adapterArea;
        TextView nomor;
        TextView nama;
        TextView data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        Holder holder = null;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        row = inflater.inflate(layoutResourceId, parent, false);

        holder = new Holder();
        holder.adapterArea = items.get(position);

        holder.nomor = (TextView)row.findViewById(R.id.tv_nomor);
        holder.nama = (TextView)row.findViewById(R.id.tv_nama);
        holder.data = (TextView)row.findViewById(R.id.tv_keterangan);

        row.setTag(holder);
        setupItem(holder);

        return row;
    }

    private void setupItem(Holder holder) {
        holder.nomor.setText(holder.adapterArea.getNomor());
        holder.nama.setText(holder.adapterArea.getNama());
        if(holder.adapterArea.getData().equals(""))
        {
            holder.data.setVisibility(View.GONE);
        }
        else
        {
            holder.data.setText(holder.adapterArea.getData());
        }
    }
}
