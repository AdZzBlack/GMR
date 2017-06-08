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
 * Created by ADI on 06/04/2017.
 */
public class ItemListAdapter extends ArrayAdapter<ItemAdapter> {

    private List<ItemAdapter> items;
    private int layoutResourceId;
    private Context context;

    public ItemListAdapter(Context context, int layoutResourceId, List<ItemAdapter> items) {
        super(context, layoutResourceId, items);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.items = items;
    }

    public List<ItemAdapter> getItems() {
        return items;
    }

    public static class Holder {
        ItemAdapter adapterItem;
        TextView nomor;
        TextView nama;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        Holder holder = null;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        row = inflater.inflate(layoutResourceId, parent, false);

        holder = new Holder();
        holder.adapterItem = items.get(position);

        holder.nomor = (TextView)row.findViewById(R.id.tv_nomor);
        holder.nama = (TextView)row.findViewById(R.id.tv_nama);

        row.setTag(holder);
        setupItem(holder);

        return row;
    }

    private void setupItem(Holder holder) {
        holder.nomor.setText(holder.adapterItem.getNomor());
        holder.nama.setText(holder.adapterItem.getNama());
    }
}
