package com.adzzblack.gmr;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChooseDeliveryOrder extends Fragment implements View.OnClickListener {

    private ItemListDeliveryOrderAdapter itemadapter;

    private ListView lv_choose;

    private String nomor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.choose_withoutsearch, container, false);
        getActivity().setTitle("Delivery Order");

        //-----START DECLARE---------------------------------------------------------------------------------------
        Index.globalfunction.setShared("global", "thdeliveryordernow", "");
        Index.globalfunction.setShared("global", "kodedeliveryordernow", "");
        Index.globalfunction.setShared("global", "detailbpm", "");

        itemadapter = new ItemListDeliveryOrderAdapter(getActivity(), R.layout.list_deliveryorderwithdetail, new ArrayList<ItemDeliveryOrderAdapter>());
        lv_choose = (ListView) v.findViewById(R.id.lv_choose);
        lv_choose.setAdapter(itemadapter);
        lv_choose.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                // TODO Auto-generated method stub
                v.startAnimation(Index.listeffect);

                Index.globalfunction.setShared("global", "thdeliveryordernow", itemadapter.items.get(position).getNomor());
                Index.globalfunction.setShared("global", "kodedeliveryordernow", itemadapter.items.get(position).getKode());
                Index.globalfunction.setShared("global", "detailbpm", itemadapter.items.get(position).getDetail());

                Fragment fragment = new DeliveryOrder();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        //-----END DECLARE---------------------------------------------------------------------------------------

        String actionUrl = "Master/alldatadeliveryorder/";
        new search().execute( actionUrl );

        return v;
    }

    public void onClick(View v) {
        v.startAnimation(Index.buttoneffect);
        if(v.getId() == R.id.ib_search){
//            itemadapter.clear();
//
//            String actionUrl = "Master/alldatadeliveryorder/";
//            new search().execute( actionUrl );
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
//        String search = et_search.getText().toString();

        @Override
        protected String doInBackground(String... urls) {
            try {
                Index.jsonObject = new JSONObject();
//                Index.jsonObject.put("search", search);
                Index.jsonObject.put("nomor_bangunan", Index.globalfunction.getShared("bangunan", "nomorNow", ""));
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
            if(result.equals(""))
            {
                hideLoading();
                return;
            }

            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = jsonarray.length() - 1; i >= 0; i--) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(!obj.has("query")){
                            String nomor = (obj.getString("nomor"));
                            String kode = (obj.getString("kode"));
                            String tanggal = (obj.getString("tanggal"));
                            String detail = (obj.getString("detail"));

                            itemadapter.add(new ItemDeliveryOrderAdapter(nomor, kode, tanggal, detail));
                            itemadapter.notifyDataSetChanged();
                        }
                    }
                }

                hideLoading();
            }catch(Exception e)
            {
                e.printStackTrace();
                Toast.makeText(getContext(), "Delivery Order Load Failed", Toast.LENGTH_LONG).show();
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

    class ItemDeliveryOrderAdapter {

        private String nomor;
        private String kode;
        private String tanggal;
        private String detail;

        public ItemDeliveryOrderAdapter(String nomor, String kode, String tanggal, String detail) {
            this.setNomor(nomor);
            this.setKode(kode);
            this.setTanggal(tanggal);
            this.setDetail(detail);
        }

        public String getNomor() {
            return nomor;
        }

        public void setNomor(String nomor) {
            this.nomor = nomor;
        }

        public String getKode() {return kode;}

        public void setKode(String kode) {
            this.kode = kode;
        }

        public String getTanggal() {return tanggal;}

        public void setTanggal(String tanggal) {
            this.tanggal = tanggal;
        }

        public String getDetail() {return detail;}

        public void setDetail(String detail) {
            this.detail = detail;
        }
    }

    class ItemListDeliveryOrderAdapter extends ArrayAdapter<ItemDeliveryOrderAdapter> {

        private List<ItemDeliveryOrderAdapter> items;
        private int layoutResourceId;
        private Context context;


        public ItemListDeliveryOrderAdapter(Context context, int layoutResourceId, List<ItemDeliveryOrderAdapter> items) {
            super(context, layoutResourceId, items);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.items = items;
        }

        public class Holder {
            ItemDeliveryOrderAdapter adapterItem;
            TextView nomor;
            TextView kode;
            TextView tanggal;
            ItemListAdapter1 griditemadapter;
            ListView lv_item;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ItemListDeliveryOrderAdapter.Holder holder = null;

            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ItemListDeliveryOrderAdapter.Holder();
            holder.adapterItem = items.get(position);

            holder.nomor = (TextView)row.findViewById(R.id.tv_nomor);
            holder.kode = (TextView)row.findViewById(R.id.tv_nama);
            holder.tanggal = (TextView)row.findViewById(R.id.tv_keterangan);

            holder.griditemadapter = new ItemListAdapter1(getActivity(), R.layout.list_order, new ArrayList<ItemAdapter1>());
            holder.lv_item = (ListView) row.findViewById(R.id.lv_item);
            holder.lv_item.setAdapter(holder.griditemadapter);

            row.setTag(holder);
            setupItem(holder);

            final Holder finalHolder = holder;
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Index.globalfunction.setShared("global", "thdeliveryordernow", finalHolder.adapterItem.getNomor());
                    Index.globalfunction.setShared("global", "kodedeliveryordernow", finalHolder.adapterItem.getKode());
                    Index.globalfunction.setShared("global", "detailbpm", finalHolder.adapterItem.getDetail());

                    Fragment fragment = new FormBPM();
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, fragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            });

            return row;
        }

        public void setListViewHeightBasedOnChildren(ListView listView) {
            ListAdapter listAdapter = listView.getAdapter();
            if (listAdapter == null) {
                // pre-condition
                return;
            }

            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = 200 * listAdapter.getCount();
            listView.setLayoutParams(params);
            listView.requestLayout();
        }

        private void setupItem(ItemListDeliveryOrderAdapter.Holder holder) {
            holder.nomor.setText(holder.adapterItem.getNomor());
            holder.kode.setText(holder.adapterItem.getKode());

            String date = (holder.adapterItem.getTanggal());

            try
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date newdate = sdf.parse(date);
                sdf = new SimpleDateFormat("dd-MM-yyyy");
                date = sdf.format(newdate);
                holder.tanggal.setText(date);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            try {
                JSONArray jsonarray = new JSONArray(holder.adapterItem.getDetail());
                if(jsonarray.length() > 0){
                    for (int i = jsonarray.length() - 1; i >= 0; i--) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        String namabarang = (obj.getString("nama"));
                        String jumlah = (obj.getString("jumlah"));
                        String satuan = (obj.getString("satuan"));
                        String keterangan = GlobalFunction.delimeter(jumlah) + " " + satuan;

                        holder.griditemadapter.add(new ItemAdapter1(namabarang, keterangan));
                        holder.griditemadapter.notifyDataSetChanged();
                    }
                    setListViewHeightBasedOnChildren(holder.lv_item);
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        private class ItemAdapter1 {
            private String namabarang;
            private String keterangan;


            public ItemAdapter1(String namabarang, String keterangan) {
                this.setNamabarang(namabarang);
                this.setKeterangan(keterangan);
            }

            public String getNamaBarang() {
                return namabarang;
            }

            public void setNamabarang(String param) {
                this.namabarang = param;
            }

            public String getKeterangan() {
                return keterangan;
            }

            public void setKeterangan(String param) {
                this.keterangan = param;
            }
        }

        private class ItemListAdapter1 extends ArrayAdapter<ItemAdapter1> {

            private List<ItemAdapter1> items;
            private int layoutResourceId;
            private Context context;


            public ItemListAdapter1(Context context, int layoutResourceId, List<ItemAdapter1> items) {
                super(context, layoutResourceId, items);
                this.layoutResourceId = layoutResourceId;
                this.context = context;
                this.items = items;
            }

            public class Holder {
                ItemAdapter1 adapterItem;
                TextView nama;
                TextView keterangan;
            }

            @Override
            public View getView(int position, View convertView, final ViewGroup parent) {
                View row = convertView;
                Holder holder = null;

                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);

                holder = new Holder();
                holder.adapterItem = items.get(position);

                holder.nama = (TextView)row.findViewById(R.id.tv_nama);
                holder.keterangan = (TextView)row.findViewById(R.id.tv_keterangan);

                row.setTag(holder);
                setupItem(holder);

                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

                return row;
            }

            private void setupItem(Holder holder) {
                holder.nama.setText(holder.adapterItem.getNamaBarang());
                holder.keterangan.setText(holder.adapterItem.getKeterangan());
            }
        }
    }
}