package com.adzzblack.gmr;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

public class ChooseListElevasi extends Fragment implements View.OnClickListener {

    private ItemListElevasiAdapter itemadapter;

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

        getActivity().setTitle("List Elevasi");

        //-----START DECLARE---------------------------------------------------------------------------------------
        et_search = (EditText) v.findViewById(R.id.et_search);
        itemadapter = new ItemListElevasiAdapter(getActivity(), R.layout.list_elevasi, new ArrayList<ItemElevasiAdapter>());
        lv_choose = (ListView) v.findViewById(R.id.lv_choose);
        lv_choose.setAdapter(itemadapter);
        ib_search = (ImageButton) v.findViewById(R.id.ib_search);
        ib_search.setOnClickListener(this);
        //-----END DECLARE---------------------------------------------------------------------------------------

        String actionUrl = "BeritaAcara/getAllElevasi/";
        new search().execute( actionUrl );

        return v;
    }

    public void onClick(View v) {
        v.startAnimation(Index.buttoneffect);
         if(v.getId() == R.id.ib_search){
            itemadapter.clear();

             String actionUrl = "BeritaAcara/getAllElevasi/";
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
                Index.jsonObject.put("nomor_project", Index.globalfunction.getShared("bangunan", "nomorNow", ""));
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
                            String elevasi = (obj.getString("elevasi"));
                            String gambar = (obj.getString("gambar"));
                            String keterangan = (obj.getString("keterangan"));
                            String namalengkap = (obj.getString("namalengkap"));
                            String disetujui = (obj.getString("status_disetujui"));

                            itemadapter.add(new ItemElevasiAdapter(nomor, elevasi, gambar, keterangan, namalengkap, disetujui));
                            itemadapter.notifyDataSetChanged();
                        }
                    }
                }
                hideLoading();
            }catch(Exception e)
            {
                e.printStackTrace();
                Toast.makeText(getContext(), "Elevasi Load Failed", Toast.LENGTH_LONG).show();
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

    class ItemElevasiAdapter {

        private String nomor;
        private String elevasi;
        private String gambar;
        private String keterangan;
        private String namalengkap;
        private String disetujui;


        public ItemElevasiAdapter(String nomor, String elevasi, String gambar, String keterangan, String namalengkap, String disetujui) {
            this.setNomor(nomor);
            this.setElevasi(elevasi);
            this.setGambar(gambar);
            this.setKeterangan(keterangan);
            this.setNamalengkap(namalengkap);
            this.setDisetujui(disetujui);
        }

        public String getNomor() {
            return nomor;
        }

        public void setNomor(String param) {this.nomor = param; }

        public String getElevasi() {
            return elevasi;
        }

        public void setElevasi(String param) {this.elevasi = param; }

        public String getGambar() {
            return gambar;
        }

        public void setGambar(String param) {this.gambar = param; }

        public String getKeterangan() {
            return keterangan;
        }

        public void setKeterangan(String param) {this.keterangan = param; }

        public String getNamalengkap() {
            return namalengkap;
        }

        public void setNamalengkap(String param) {this.namalengkap = param; }

        public String getDisetujui() {
            return disetujui;
        }

        public void setDisetujui(String param) {this.disetujui = param; }
    }

    class ItemListElevasiAdapter extends ArrayAdapter<ItemElevasiAdapter> {

        private List<ItemElevasiAdapter> items;
        private int layoutResourceId;
        private Context context;


        public ItemListElevasiAdapter(Context context, int layoutResourceId, List<ItemElevasiAdapter> items) {
            super(context, layoutResourceId, items);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.items = items;
        }

        public class Holder {
            ItemElevasiAdapter adapterItem;
            TextView bangunan;
            TextView elevasi;
            TextView keterangan;
            TextView setuju;
            Button image;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            Holder holder = null;

            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new Holder();
            holder.adapterItem = items.get(position);

            holder.bangunan = (TextView)row.findViewById(R.id.tv_bangunan);
            holder.elevasi = (TextView)row.findViewById(R.id.tv_elevasi);
            holder.keterangan = (TextView)row.findViewById(R.id.tv_keterangan);
            holder.setuju = (TextView)row.findViewById(R.id.tv_setuju);
            holder.image = (Button) row.findViewById(R.id.btn_image);

            row.setTag(holder);
            setupItem(holder);

            final Holder finalHolder = holder;
            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                Index.globalfunction.setShared("global","photo", finalHolder.adapterItem.getGambar());

                Fragment fragment = new GaleryShared();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
                }
            });

            return row;
        }

        private void setupItem(Holder holder) {
            holder.bangunan.setText(holder.adapterItem.getNamalengkap());
            holder.elevasi.setText(holder.adapterItem.getElevasi());
            holder.keterangan.setText(holder.adapterItem.getKeterangan());
            if(holder.adapterItem.getDisetujui().equals("0"))
            {
                holder.setuju.setText("Belum Disetujui");
            }
            else if(holder.adapterItem.getDisetujui().equals("1"))
            {
                holder.setuju.setText("Disetujui");
                holder.setuju.setTextColor(getResources().getColor(R.color.colorBlue));
                holder.bangunan.setTextColor(getResources().getColor(R.color.colorBlue));
            }
            else if(holder.adapterItem.getDisetujui().equals("2"))
            {
                holder.setuju.setText("Tidak Disetujui");
                holder.setuju.setTextColor(getResources().getColor(R.color.colorRed));
                holder.bangunan.setTextColor(getResources().getColor(R.color.colorRed));
            }
        }
    }
}