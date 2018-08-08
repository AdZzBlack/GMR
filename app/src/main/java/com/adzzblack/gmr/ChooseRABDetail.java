package com.adzzblack.gmr;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChooseRABDetail extends Fragment implements View.OnClickListener {

    private ItemListRABDetailAdapter itemadapter;

    private EditText et_search;
    private ImageButton ib_search;
    private ListView lv_choose;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.choose, container, false);

        getActivity().setTitle("Choose Item");

        Index.globalfunction.setShared("global", "position", "chooserabdetail");

        //-----START DECLARE---------------------------------------------------------------------------------------
        et_search = (EditText) v.findViewById(R.id.et_search);
        itemadapter = new ItemListRABDetailAdapter(getActivity(), R.layout.list_moreitem, new ArrayList<ItemRABDetailAdapter>());
        lv_choose = (ListView) v.findViewById(R.id.lv_choose);
        lv_choose.setAdapter(itemadapter);
        ib_search = (ImageButton) v.findViewById(R.id.ib_search);
        ib_search.setOnClickListener(this);
        //-----END DECLARE---------------------------------------------------------------------------------------

        String actionUrl = "DeliveryOrder/alldatarabdetail/";
        new search().execute( actionUrl );

        return v;
    }

    public void onClick(View v) {
        v.startAnimation(Index.buttoneffect);
         if(v.getId() == R.id.ib_search){
            itemadapter.clear();

            String actionUrl = "DeliveryOrder/alldatarabdetail/";
            new search().execute( actionUrl );
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }



    private class search extends AsyncTask<String, Void, String> {
        String search = et_search.getText().toString();

        @Override
        protected String doInBackground(String... urls) {
            try {
                Index.jsonObject = new JSONObject();
                Index.jsonObject.put("nomor_rab", Index.globalfunction.getShared("rab", "rab", "0"));
                Index.jsonObject.put("nomor_bangunan", Index.globalfunction.getShared("bangunan", "nomorNow", ""));
                Index.jsonObject.put("search", search);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return Index.globalfunction.executePost(urls[0], Index.jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("result", result);
            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(!obj.has("query")){
                            String nomor = (obj.getString("nomor"));
                            String nomorbarang = (obj.getString("nomorbarang"));
                            String namabarang = (obj.getString("namabarang"));
                            String satuan = (obj.getString("satuan"));
                            String jumlah = (obj.getString("jumlah"));
                            String harga = (obj.getString("harga"));
                            String delivery = (obj.getString("do"));
                            String waste = (obj.getString("waste"));
                            String keterangan = (obj.getString("keterangan"));

                            itemadapter.add(new ItemRABDetailAdapter(nomor, nomorbarang, namabarang, satuan, jumlah, delivery, harga, waste, keterangan));
                            itemadapter.notifyDataSetChanged();
                        }
                    }
                }
                hideLoading();
            }catch(Exception e)
            {
                e.printStackTrace();
                Toast.makeText(getContext(), "Item Load Failed", Toast.LENGTH_LONG).show();
                hideLoading();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoading();
        }
    }

    private void hideLoading()
    {
        Index.loadingDialog.dismiss();
    }

    private void showLoading()
    {
        Index.loadingDialog = new ProgressDialog(getActivity());
        Index.loadingDialog.setMessage("Loading");
        Index.loadingDialog.setCancelable(true);
        Index.loadingDialog.setCanceledOnTouchOutside(false);
        Index.loadingDialog.show();
    }

    class ItemRABDetailAdapter {

        private String nomor;
        private String nomorBarang;
        private String namabarang;
        private String satuan;
        private String jumlah;
        private String harga;
        private String delivery;
        private String waste;
        private String keterangan;


        public ItemRABDetailAdapter(String nomor, String nomorbarang, String namabarang, String satuan, String jumlah, String delivery, String harga, String waste, String keterangan) {
            this.setNomor(nomor);
            this.setNomorBarang(nomorbarang);
            this.setNamabarang(namabarang);
            this.setSatuan(satuan);
            this.setJumlah(jumlah);
            this.setDelivery(delivery);
            this.setHarga(harga);
            this.setWaste(waste);
            this.setKeterangan(keterangan);
        }

        public String getNomor() {
            return nomor;
        }

        public void setNomor(String param) {this.nomor = param; }

        public String getNomorBarang() {
            return nomorBarang;
        }

        public void setNomorBarang(String param) {
            this.nomorBarang = param;
        }

        public String getNamaBarang() {
            return namabarang;
        }

        public void setNamabarang(String param) {
            this.namabarang = param;
        }

        public String getSatuan() {
            return satuan;
        }

        public void setSatuan(String param) {
            this.satuan = param;
        }

        public String getJumlah() {
            return jumlah;
        }

        public void setJumlah(String param) {
            this.jumlah = param;
        }

        public String getHarga() {
            return harga;
        }

        public void setHarga(String param) {
            this.harga = param;
        }

        public String getDelivery() {
            return delivery;
        }

        public void setDelivery(String param) {
            this.delivery = param;
        }

        public String getWaste() {
            return waste;
        }

        public void setWaste(String param) {
            this.waste = param;
        }

        public String getKeterangan() {
            return keterangan;
        }

        public void setKeterangan(String param) {
            this.keterangan = param;
        }
    }

    class ItemListRABDetailAdapter extends ArrayAdapter<ItemRABDetailAdapter> {

        private List<ItemRABDetailAdapter> items;
        private int layoutResourceId;
        private Context context;


        public ItemListRABDetailAdapter(Context context, int layoutResourceId, List<ItemRABDetailAdapter> items) {
            super(context, layoutResourceId, items);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.items = items;
        }

        public class Holder {
            ItemRABDetailAdapter adapterItem;
            TextView nomor;
            TextView nama;
            TextView keterangan;
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
            holder.keterangan = (TextView)row.findViewById(R.id.tv_keterangan);

            row.setTag(holder);
            setupItem(holder);

            final Holder finalHolder = holder;
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Index.globalfunction.setShared("rab", "nomor", finalHolder.adapterItem.getNomor());
                    Index.globalfunction.setShared("rab", "nomorbarang", finalHolder.adapterItem.getNomorBarang());
                    Index.globalfunction.setShared("rab", "namabarang", finalHolder.adapterItem.getNamaBarang());
                    Index.globalfunction.setShared("rab", "satuan", finalHolder.adapterItem.getSatuan());
                    Index.globalfunction.setShared("rab", "jumlah", finalHolder.adapterItem.getJumlah());
                    Index.globalfunction.setShared("rab", "harga", finalHolder.adapterItem.getHarga());
                    Index.globalfunction.setShared("rab", "delivery", finalHolder.adapterItem.getDelivery());
                    Index.globalfunction.setShared("rab", "waste", finalHolder.adapterItem.getWaste());
                    Index.globalfunction.setShared("rab", "keterangan", finalHolder.adapterItem.getKeterangan());

                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
                    if (drawer.isDrawerOpen(GravityCompat.START)) {
                        drawer.closeDrawer(GravityCompat.START);
                    }
                    else {
                        if(fm.getBackStackEntryCount() > 0) {
                            fm.popBackStack();
                        }
                    }
                }
            });

            return row;
        }

        private void setupItem(Holder holder) {
            holder.nomor.setText(holder.adapterItem.getNomor());
            holder.nama.setText(holder.adapterItem.getNamaBarang());
            holder.keterangan.setText("Need: " + GlobalFunction.delimeter(holder.adapterItem.getJumlah()) + " " + holder.adapterItem.getSatuan() + "  ---  Ordered: " + GlobalFunction.delimeter(holder.adapterItem.getDelivery()) + " " + holder.adapterItem.getSatuan());
        }
    }
}