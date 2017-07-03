package com.adzzblack.gmr;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by owners on 15/04/2017.
 */

public class ViewDeliveryOrder extends Fragment implements View.OnClickListener{
    private TextView tv_kode, tv_tanggal, tv_proyek, tv_ruangan;
    private Button btn_back, btn_send;
    private String data;

    private ItemListDeliveryOrderAdapter itemadapter;
    private ListView lv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.view_deliveryorder, container, false);
        getActivity().setTitle("Delivery Order");

        tv_kode = (TextView) v.findViewById(R.id.tv_kode);
        tv_tanggal = (TextView) v.findViewById(R.id.tv_tanggal);
        tv_proyek = (TextView) v.findViewById(R.id.tv_proyek);
        tv_ruangan = (TextView) v.findViewById(R.id.tv_ruangan);

        btn_back = (Button) v.findViewById(R.id.btn_back);
        btn_send = (Button) v.findViewById(R.id.btn_send);
        btn_back.setOnClickListener(this);
        btn_send.setOnClickListener(this);

        tv_kode.setText("DELIVERY ORDER " + Index.globalfunction.getShared("global", "print_kode", ""));
        tv_tanggal.setText(Index.globalfunction.getShared("global", "print_tanggal", ""));
        tv_proyek.setText(Index.globalfunction.getShared("global", "print_project", ""));
        tv_ruangan.setText(Index.globalfunction.getShared("global", "print_bangunan", ""));
        data = Index.globalfunction.getShared("global", "print_data", "");

        lv = (ListView) v.findViewById(R.id.lv_item);
        itemadapter = new ItemListDeliveryOrderAdapter(getActivity(), R.layout.list_deliveryorder, new ArrayList<ItemDeliveryOrderAdapter>());

        lv.setAdapter(itemadapter);

        String[] pieces = data.trim().split("\\|");
        for(int i=pieces.length-1 ; i >=0 ; i--){
            String string = pieces[i];
            String[] parts = string.trim().split("\\~");

            itemadapter.add(new ItemDeliveryOrderAdapter(parts[1], parts[2], parts[7]));
        }

        return v;
    }

    @Override
    public void onClick(View v) {
        v.startAnimation(Index.buttoneffect);
        if(v.getId() == R.id.btn_back) {
            Index.fm.popBackStack();
        }
        else if(v.getId() == R.id.btn_send) {
            Fragment fragment = new Sign();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    class ItemDeliveryOrderAdapter {

        private String nama;
        private String jumlah;
        private String keterangan;

        public ItemDeliveryOrderAdapter(String nama, String jumlah, String keterangan) {
            this.setNama(nama);
            this.setJumlah(jumlah);
            this.setKeterangan(keterangan);
        }

        public String getNama() {
            return nama;
        }

        public void setNama(String param) {
            this.nama = param;
        }

        public String getJumlah() {
            return jumlah;
        }

        public void setJumlah(String param) {
            this.jumlah = param;
        }

        public String getKeterangan() {
            return keterangan;
        }

        public void setKeterangan(String param) {
            this.keterangan = param;
        }
    }

    class ItemListDeliveryOrderAdapter extends ArrayAdapter<ItemDeliveryOrderAdapter> {

        private List<ItemDeliveryOrderAdapter> items;
        private int layoutResourceId;
        private Context context;

        private List<ItemDeliveryOrderAdapter> getList()
        {
            return items;
        }

        public ItemListDeliveryOrderAdapter(Context context, int layoutResourceId, List<ItemDeliveryOrderAdapter> items) {
            super(context, layoutResourceId, items);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.items = items;
        }

        public class Holder {
            ItemDeliveryOrderAdapter adapterItem;
            TextView nama;
            TextView jumlah;
            TextView keterangan;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ItemListDeliveryOrderAdapter.Holder holder = null;

            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ItemListDeliveryOrderAdapter.Holder();
            holder.adapterItem = items.get(position);

            holder.nama = (TextView)row.findViewById(R.id.tv_nama);
            holder.jumlah = (TextView)row.findViewById(R.id.tv_jumlah);
            holder.keterangan = (TextView)row.findViewById(R.id.tv_data);

            row.setTag(holder);
            setupItem(holder);

            return row;
        }

        private void setupItem(ItemListDeliveryOrderAdapter.Holder holder) {
            holder.nama.setText(holder.adapterItem.getNama());
            holder.jumlah.setText(holder.adapterItem.getJumlah());
            if(holder.adapterItem.getKeterangan().equals("0"))
            {
                holder.keterangan.setVisibility(View.GONE);
            }
            else
            {
                holder.keterangan.setText(holder.adapterItem.getKeterangan());
            }
        }
    }
}
