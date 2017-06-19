package com.adzzblack.gmr;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChooseBangunan extends Fragment implements View.OnClickListener {

    private ItemListBangunanAdapter itemadapter;

    private EditText et_search;
    private ImageButton ib_search;
    private ListView lv_choose;

    private Boolean scroll = false;
    private int counter = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.choose, container, false);

        if(Index.globalfunction.getShared("bangunan", "header", "0").equals("0"))
        {
            getActivity().setTitle("Choose Project");
        }
        else
        {
            getActivity().setTitle("Choose Building");
        }

        Index.globalfunction.setShared("global", "position", "choosebangunan");

        //-----START DECLARE---------------------------------------------------------------------------------------
        et_search = (EditText) v.findViewById(R.id.et_search);
        itemadapter = new ItemListBangunanAdapter(getActivity(), R.layout.list_item, new ArrayList<ItemBangunanAdapter>());
        lv_choose = (ListView) v.findViewById(R.id.lv_choose);
        lv_choose.setAdapter(itemadapter);
        ib_search = (ImageButton) v.findViewById(R.id.ib_search);
        ib_search.setOnClickListener(this);
        lv_choose.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                // TODO Auto-generated method stub
                v.startAnimation(Index.listeffect);

                TextView tv_nomor = (TextView) v.findViewById(R.id.tv_nomor);
                TextView tv_nama = (TextView) v.findViewById(R.id.tv_nama);
            }
        });
        lv_choose.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                // Scroll Down
                int lastInScreen = firstVisibleItem + visibleItemCount;
                if((lastInScreen == totalItemCount) && totalItemCount != 0){
                    if(scroll == true)
                    {
                        scroll = false;
                        counter += 1;

                        String actionUrl = "Master/alldatabangunan/";
                        new search().execute( actionUrl );
                    }
                }
            }

        });
        //-----END DECLARE---------------------------------------------------------------------------------------

        scroll = false;
        String actionUrl = "Master/alldatabangunan/";
        new search().execute( actionUrl );

        return v;
    }

    public void onClick(View v) {
        v.startAnimation(Index.buttoneffect);
        if(v.getId() == R.id.ib_search){
            itemadapter.clear();
            counter = 0;

            String actionUrl = "Master/alldatabangunan/";
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
                Index.jsonObject.put("nomor_header", Index.globalfunction.getShared("bangunan", "header", "0"));
                Index.jsonObject.put("search", search);
                if(Index.globalfunction.getShared("global", "destination", "").equals("delivery") || Index.globalfunction.getShared("global", "destination", "").equals("opname"))
                {
                    Index.jsonObject.put("need_elevasi", "1");
                }
                else if(Index.globalfunction.getShared("global", "destination", "").equals("beritaacara"))
                {
                    Index.jsonObject.put("check_elevasi", "1");
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return Index.globalfunction.executePost(urls[0], Index.jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("result", result + ", " + Index.globalfunction.getShared("bangunan", "header", "0"));
            try {
                hideLoading();
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(!obj.has("query")){
                            String nomor = (obj.getString("nomor"));
                            String nama = (obj.getString("namalengkap"));
                            String status_anak = (obj.getString("status_anak"));
                            String kode = (obj.getString("kode"));
                            String tipe = (obj.getString("tipe"));

                            itemadapter.add(new ItemBangunanAdapter(nomor, nama, status_anak, kode, tipe));
                            itemadapter.notifyDataSetChanged();
                        }
                    }
                }
            }catch(Exception e)
            {
                e.printStackTrace();
                Toast.makeText(getContext(), "Building Load Failed", Toast.LENGTH_LONG).show();
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

    class ItemBangunanAdapter {

        private String nomor;
        private String nama;
        private String status_anak;
        private String kode;
        private String tipe;

        public ItemBangunanAdapter(String nomor, String nama, String status_anak, String kode, String tipe) {
            this.setNomor(nomor);
            this.setNama(nama);
            this.setStatusAnak(status_anak);
            this.setKode(kode);
            this.setTipe(tipe);
        }

        public String getNomor() {
            return nomor;
        }

        public void setNomor(String nomor) {
            this.nomor = nomor;
        }

        public String getNama() {return nama;}

        public void setNama(String nama) {
            this.nama = nama;
        }

        public String getStatusAnak() {return status_anak;}

        public void setStatusAnak(String status_anak) {
            this.status_anak = status_anak;
        }

        public String getKode() {return kode;}

        public void setKode(String kode) {
            this.kode = kode;
        }

        public String getTipe() {return tipe;}

        public void setTipe(String tipe) {
            this.tipe = tipe;
        }
    }

    class ItemListBangunanAdapter extends ArrayAdapter<ItemBangunanAdapter> {

        private List<ItemBangunanAdapter> items;
        private int layoutResourceId;
        private Context context;


        public ItemListBangunanAdapter(Context context, int layoutResourceId, List<ItemBangunanAdapter> items) {
            super(context, layoutResourceId, items);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.items = items;
        }

        public class Holder {
            ItemBangunanAdapter adapterItem;
            TextView nomor;
            TextView nama;
            ImageView map;
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
            holder.map = (ImageView)row.findViewById(R.id.iv_map);

            row.setTag(holder);
            setupItem(holder);

            final Holder finalHolder = holder;
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(finalHolder.adapterItem.getStatusAnak().equals("0") && !Index.globalfunction.getShared("global", "destination", "").equals("map"))
                    {
                        Index.globalfunction.setShared("bangunan", "before", Index.globalfunction.getShared("bangunan", "before", "0") + Index.globalfunction.getShared("bangunan", "header", "0") + ",");
                        Index.globalfunction.setShared("bangunan", "nomorNow", finalHolder.adapterItem.getNomor());
                        Index.globalfunction.setShared("bangunan", "kodeNow", finalHolder.adapterItem.getKode());
                        Index.globalfunction.setShared("bangunan", "namaNow", finalHolder.adapterItem.getNama());

                        if(Index.globalfunction.getShared("global", "destination", "").equals("beritaacara"))
                        {
                            Fragment fragment = new BeritaAcara();
                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_container, fragment);
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }
                        else if(Index.globalfunction.getShared("global", "destination", "").equals("delivery"))
                        {
                            Fragment fragment = new ChooseRAB();
                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_container, fragment);
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }
                        else if(Index.globalfunction.getShared("global", "destination", "").equals("bpm"))
                        {
                            Fragment fragment = new ChooseDeliveryOrder();
                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_container, fragment);
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }
                        else if(Index.globalfunction.getShared("global", "destination", "").equals("opname"))
                        {
                            Fragment fragment = new ChooseRABCheck();
                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_container, fragment);
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }
                        else if(Index.globalfunction.getShared("global", "destination", "").equals("pasang"))
                        {
                            Fragment fragment = new FormPasang();
                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_container, fragment);
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }
                        else if(Index.globalfunction.getShared("global", "destination", "").equals("galerypasang"))
                        {
                            Fragment fragment = new Galery();
                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_container, fragment);
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }
                    }
                    else if(finalHolder.adapterItem.getStatusAnak().equals("1"))
                    {
                        Index.globalfunction.setShared("bangunan", "before", Index.globalfunction.getShared("bangunan", "before", "0") + Index.globalfunction.getShared("bangunan", "header", "0") + ",");
                        Index.globalfunction.setShared("bangunan", "header", finalHolder.adapterItem.getNomor());
                        Fragment fragment = new ChooseBangunan();
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, fragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                }
            });

            holder.map.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Index.globalfunction.setShared("bangunan", "before", Index.globalfunction.getShared("bangunan", "before", "0") + Index.globalfunction.getShared("bangunan", "header", "0") + ",");
                    Index.globalfunction.setShared("bangunan", "nomorNow", finalHolder.adapterItem.getNomor());
                    Index.globalfunction.setShared("bangunan", "kodeNow", finalHolder.adapterItem.getKode());
                    Index.globalfunction.setShared("bangunan", "namaNow", finalHolder.adapterItem.getNama());

                    Fragment fragment = new Maps();
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, fragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            });

            return row;
        }

        private void setupItem(Holder holder) {
            holder.nomor.setText(holder.adapterItem.getNomor());
            holder.nama.setText(holder.adapterItem.getNama());

            if(Index.globalfunction.getShared("global", "destination", "").equals("map"))
            {
                if(holder.adapterItem.getTipe().equals("1"))
                {
                    holder.map.setVisibility(View.VISIBLE);
                }
                else if(holder.adapterItem.getStatusAnak().equals("0"))
                {
                    holder.map.setVisibility(View.VISIBLE);
                }
            }

        }
    }
}