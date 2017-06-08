package com.adzzblack.gmr;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CreateCreditNote extends Fragment implements View.OnClickListener {

    private ItemListOpnameAdapter itemadapter;

    private ListView lv_choose;

    private String nomor;

    private Button btn_create;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.choose_opname, container, false);
        getActivity().setTitle("Opname Result");

        //-----START DECLARE---------------------------------------------------------------------------------------
        itemadapter = new ItemListOpnameAdapter(getActivity(), R.layout.list_opname, new ArrayList<ItemOpnameAdapter>());
        lv_choose = (ListView) v.findViewById(R.id.lv_choose);
        lv_choose.setAdapter(itemadapter);
        btn_create = (Button) v.findViewById(R.id.btn_create);
        btn_create.setOnClickListener(this);
        //-----END DECLARE---------------------------------------------------------------------------------------

        String actionUrl = "Opname/getOpname/";
        new search().execute( actionUrl );

        return v;
    }

    public void onClick(View v) {
        v.startAnimation(Index.buttoneffect);
        if(v.getId() == R.id.btn_create){
            String actionUrl = "Opname/createCreditNote/";
            new createCreditNote().execute( actionUrl );
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
        @Override
        protected String doInBackground(String... urls) {
            Index.jsonObject = new JSONObject();
            return Index.globalfunction.executePost(urls[0], Index.jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("tes", result);
            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = jsonarray.length() - 1; i >= 0; i--) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(!obj.has("query")){
                            String nomor = (obj.getString("nomoropname"));
                            String namalengkap = (obj.getString("namalengkap"));
                            String mandor = (obj.getString("mandor"));
                            String progress = (obj.getString("progress"));
                            String pekerjaan = (obj.getString("pekerjaan"));
                            String tanggal = (obj.getString("tanggal"));

                            itemadapter.add(new ItemOpnameAdapter(nomor, namalengkap, mandor, progress, pekerjaan, tanggal));
                            itemadapter.notifyDataSetChanged();
                        }
                    }
                }

                hideLoading();
            }catch(Exception e)
            {
                e.printStackTrace();
                Toast.makeText(getContext(), "Approval Order Load Failed", Toast.LENGTH_LONG).show();
                hideLoading();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoading();
        }
    }

    private class createCreditNote extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                Index.jsonObject = new JSONObject();
                Index.jsonObject.put("nomor_user", Index.globalfunction.getShared("user", "nomor", ""));
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
                        if(obj.has("success")){
                            Toast.makeText(getContext(), "Create Credit Note Success", Toast.LENGTH_LONG).show();
                            Fragment fragment = new Dashboard();
                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_container, fragment);
                            transaction.commit();
                        }
                        else
                        {
                            Toast.makeText(getContext(), "Create Credit Note Failed", Toast.LENGTH_LONG).show();
                        }
                    }
                }

                hideLoading();
            }catch(Exception e)
            {
                e.printStackTrace();
                Toast.makeText(getContext(), "Create Credit Note Failed", Toast.LENGTH_LONG).show();
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

    class ItemOpnameAdapter {

        private String nomor;
        private String namalengkap;
        private String mandor;
        private String progress;
        private String pekerjaan;
        private String tanggal;

        public ItemOpnameAdapter(String nomor, String namalengkap, String mandor, String progress, String pekerjaan, String tanggal) {
            this.setNomor(nomor);
            this.setNamalengkap(namalengkap);
            this.setMandor(mandor);
            this.setProgress(progress);
            this.setPekerjaan(pekerjaan);
            this.setTanggal(tanggal);
        }

        public String getNomor() {
            return nomor;
        }

        public void setNomor(String param) {
            this.nomor = param;
        }

        public String getNamalengkap() {
            return namalengkap;
        }

        public void setNamalengkap(String param) {
            this.namalengkap = param;
        }

        public String getMandor() {
            return mandor;
        }

        public void setMandor(String param) {
            this.mandor = param;
        }

        public String getProgress() {
            return progress;
        }

        public void setProgress(String param) {
            this.progress = param;
        }

        public String getPekerjaan() {
            return pekerjaan;
        }

        public void setPekerjaan(String param) {
            this.pekerjaan = param;
        }

        public String getTanggal() {
            return tanggal;
        }

        public void setTanggal(String param) {
            this.tanggal = param;
        }
    }

    class ItemListOpnameAdapter extends ArrayAdapter<ItemOpnameAdapter> {

        private List<ItemOpnameAdapter> items;
        private int layoutResourceId;
        private Context context;


        public ItemListOpnameAdapter(Context context, int layoutResourceId, List<ItemOpnameAdapter> items) {
            super(context, layoutResourceId, items);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.items = items;
        }

        public class Holder {
            ItemOpnameAdapter adapterItem;
            TextView bangunan;
            TextView pekerjaan;
            TextView mandor;
            TextView tanggal;
            TextView progress;
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
            holder.pekerjaan = (TextView)row.findViewById(R.id.tv_pekerjaan);
            holder.mandor = (TextView)row.findViewById(R.id.tv_mandor);
            holder.tanggal = (TextView)row.findViewById(R.id.tv_tanggal);
            holder.progress = (TextView)row.findViewById(R.id.tv_progress);

            final Holder finalHolder = holder;
            holder.progress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Edit Progress");
//                    builder.setMessage("Order quantity exceed, are you sure want to order? this order need Owner approval, give note to Owner");

                    // Set up the input
                    final EditText input = new EditText(getContext());
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    input.setText(finalHolder.adapterItem.getProgress());
                    builder.setView(input);

                    // Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String actionUrl = "Opname/editProgress/";
                            new edit(finalHolder, input.getText().toString()).execute( actionUrl );
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                }
            });

            row.setTag(holder);
            setupItem(holder);

            return row;
        }

        public void setupItem(Holder holder) {
            holder.bangunan.setText(holder.adapterItem.getNamalengkap());
            holder.pekerjaan.setText(holder.adapterItem.getPekerjaan());
            holder.mandor.setText(holder.adapterItem.getMandor());
            holder.tanggal.setText(holder.adapterItem.getTanggal());
            holder.progress.setText(holder.adapterItem.getProgress());
        }

        private class edit extends AsyncTask<String, Void, String> {
            private ItemListOpnameAdapter.Holder holder;
            private String input;

            public edit(ItemListOpnameAdapter.Holder h, String input)
            {
                this.holder = h;
                this.input = input;
            }


            @Override
            protected String doInBackground(String... urls) {
                try {
                    Index.jsonObject = new JSONObject();
                    Index.jsonObject.put("nomor", holder.adapterItem.getNomor());
                    Index.jsonObject.put("input", input);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                return Index.globalfunction.executePost(urls[0], Index.jsonObject);
            }
            // onPostExecute displays the results of the AsyncTask.
            @Override
            protected void onPostExecute(String result) {
                Log.d("RESSSS", result);
                try {
                    JSONArray jsonarray = new JSONArray(result);
                    if(jsonarray.length() > 0){
                        for (int i = jsonarray.length() - 1; i >= 0; i--) {
                            JSONObject obj = jsonarray.getJSONObject(i);
                            if(!obj.has("query")){
                                holder.adapterItem.setProgress(input);
                                setupItem(holder);
                                hideLoading();
                            }
                            else
                            {
                                hideLoading();
                                Toast.makeText(getContext(), "Edit Progress Failed", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }catch(Exception e)
                {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Edit Progress Failed", Toast.LENGTH_LONG).show();
                    hideLoading();
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showLoading();
            }
        }
    }
}