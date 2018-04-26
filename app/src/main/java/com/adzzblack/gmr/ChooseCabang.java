package com.adzzblack.gmr;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ADI on 7/11/2017.
 */

public class ChooseCabang extends Dialog implements View.OnClickListener {

    public Activity c;
    public Dialog d;
    OnMyDialogResult mDialogResult;
    private EditText et_search;

    private ListView lvSearch;
    private ItemListAdapter itemadapter;
    private ArrayList<ItemAdapter> list;

    public ChooseCabang(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_without_header);

        et_search = (EditText) findViewById(R.id.et_search);

        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                search();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        list = new ArrayList<ItemAdapter>();
        itemadapter = new ItemListAdapter(c, R.layout.list_item, new ArrayList<ItemAdapter>());
        itemadapter.clear();
        lvSearch = (ListView) findViewById(R.id.lv_choose);
        lvSearch.setAdapter(itemadapter);

        refreshList();

        String actionUrl = "Master/alldatacabang/";
        new getData().execute( actionUrl );
    }

    private void search()
    {
        itemadapter.clear();
        for(int ctr=0;ctr<list.size();ctr++)
        {
            if(et_search.getText().equals(""))
            {
                itemadapter.add(list.get(ctr));
                itemadapter.notifyDataSetChanged();
            }
            else
            {
                if(contains(list.get(ctr).getNama(),et_search.getText().toString() ))
                {
                    itemadapter.add(list.get(ctr));
                    itemadapter.notifyDataSetChanged();
                }
            }
        }
    }

    public static Boolean contains(String _textData, String _textSearch)
    {
        Boolean showData = true;
        String[] piecesSearch = _textSearch.toLowerCase().trim().split("\\ ");
        String[] piecesData = _textData.toLowerCase().trim().split("\\ ");
        ArrayList<Boolean> checked = new ArrayList<Boolean>();
        for(int i=0; i<piecesSearch.length; i++)
        {
            int ctrFound = 0;
            for(int j=0; j<piecesData.length; j++)
            {
                if(piecesData[j].contains(piecesSearch[i])) ctrFound++;
            }
            if(ctrFound==0) checked.add(false);
            else checked.add(true);
        }
        for(int j=0; j<checked.size(); j++)
        {
            if(!checked.get(j)) showData = false;
        }
        return showData;
    }

    private void refreshList()
    {
        itemadapter.clear();
        list.clear();

        String data = Index.globalfunction.getShared("global", "cabang", "");
        String[] pieces = data.trim().split("\\|");
        if(pieces.length==1 && pieces[0].equals(""))
        {
        }
        else
        {
            for(int i=0 ; i < pieces.length ; i++){
                if(!pieces[i].equals(""))
                {
                    String[] parts = pieces[i].trim().split("\\~");

                    String nomor = parts[0];
                    String nama = parts[1];

                    if(nomor.equals("null")) nomor = "";
                    if(nama.equals("null")) nama = "";

                    ItemAdapter dataItem = new ItemAdapter();
                    dataItem.setNomor(nomor);
                    dataItem.setNama(nama);
                    list.add(dataItem);

                    itemadapter.add(dataItem);
                    itemadapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.btn_ok:
//                GlobalFunction.setShared(globalfunction.anggotasharedpreferences, "alamat", et_alamat.getText().toString());
//                GlobalFunction.setShared(globalfunction.anggotasharedpreferences, "rt", et_rt.getText().toString());
//                GlobalFunction.setShared(globalfunction.anggotasharedpreferences, "rw", et_rw.getText().toString());
//                GlobalFunction.setShared(globalfunction.anggotasharedpreferences, "kodepos", et_kodepos.getText().toString());
//                GlobalFunction.setShared(globalfunction.anggotasharedpreferences, "kelurahan", et_kelurahan.getText().toString());
//                GlobalFunction.setShared(globalfunction.anggotasharedpreferences, "kecamatan", et_kecamatan.getText().toString());
//                GlobalFunction.setShared(globalfunction.anggotasharedpreferences, "kabkota", kabkota);
//                GlobalFunction.setShared(globalfunction.anggotasharedpreferences, "kota", et_kota.getText().toString());
//                mDialogResult.finish(et_alamat.getText().toString(), et_rt.getText().toString(), et_rw.getText().toString(), et_kodepos.getText().toString(), et_kelurahan.getText().toString(), et_kecamatan.getText().toString(), kabkota, et_kota.getText().toString());
//                break;
//            case R.id.btn_cancel:
//                dismiss();
//                break;
//            default:
//                break;
//        }
        dismiss();
    }

    public void setDialogResult(OnMyDialogResult dialogResult){
        mDialogResult = dialogResult;
    }

    public interface OnMyDialogResult{
        void finish(String alamat, String rt, String rw, String kodepos, String kelurahan, String kecamatan, String kabkota, String kota);
    }

    private class getData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            Index.jsonObject = new JSONObject();
            return Index.globalfunction.executePost(urls[0], Index.jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("resultQuery", result);
            try
            {
                String tempData= "";
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = jsonarray.length() - 1; i >= 0; i--) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(!obj.has("query")){
                            String nomor = (obj.getString("nomor"));
                            String nama = (obj.getString("nama"));

                            if(nomor.equals("")) nomor = "null";
                            if(nama.equals("")) nama = "null";

                            tempData = tempData + nomor + "~" + nama + "|";
                        }
                    }

                    if(!tempData.equals(Index.globalfunction.getShared("global", "cabang", "")))
                    {
                        Index.globalfunction.setShared("global", "cabang", tempData);
                        refreshList();
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    public class ItemAdapter {

        private String nomor;
        private String nama;

        public ItemAdapter() {}

        public String getNomor() {return nomor;}
        public void setNomor(String _param) {this.nomor = _param;}

        public String getNama() {return nama;}
        public void setNama(String _param) {this.nama = _param;}
    }

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

        public class Holder {
            ItemAdapter adapterItem;
            TextView tvNama;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            Holder holder = null;

            if(row==null)
            {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);
            }

            holder = new Holder();
            holder.adapterItem = items.get(position);

            holder.tvNama = (TextView)row.findViewById(R.id.tv_nama);

            row.setTag(holder);
            setupItem(holder, row);

            final Holder finalHolder = holder;
            final View finalRow = row;
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Index.globalfunction.setShared("user", "cabang", finalHolder.adapterItem.getNomor());
                    dismiss();
                }
            });

            return row;
        }

        private void setupItem(final Holder holder, final View row) {
            holder.tvNama.setText(holder.adapterItem.getNama().toUpperCase());
        }
    }
}