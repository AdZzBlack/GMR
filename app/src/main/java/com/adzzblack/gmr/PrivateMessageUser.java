package com.adzzblack.gmr;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
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

/**
 * Created by owners on 18/04/2017.
 */

public class PrivateMessageUser extends Fragment implements View.OnClickListener{

    private EditText et_search;
    private ImageButton ib_search;
    private ListView lv_choose;
    private ItemListUserAdapter itemadapter;

    private Thread t;
    private Boolean running = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.choose, container, false);
        getActivity().setTitle("Choose User");

        Index.globalfunction.setShared("message","userto_nomor","");

        //-----START DECLARE---------------------------------------------------------------------------------------
        et_search = (EditText) v.findViewById(R.id.et_search);
        itemadapter = new ItemListUserAdapter(getActivity(), R.layout.list_item, new ArrayList<ItemUserAdapter>());
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
        //--------END DECLARE---------------------------------------------------------------------

        String actionUrl = "Master/alldatausermessage/";
        new getUser().execute( actionUrl );

        t = new Thread() {
            @Override
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted() && running) {
                        try {
                            String actionUrl = "Master/alldatausermessage/";
                            new updateNotif().execute( actionUrl );
                            Thread.sleep(2000);
                        }catch (InterruptedException e){
                            running = false;
                            Thread.currentThread().interrupt();
                        }
                    }
                }catch (Exception e){
                    running = false;
                    Thread.currentThread().interrupt();
                }
            }
        };
        t.start();

        running = true;
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        running = false;
        Thread.currentThread().interrupt();
    }

    public void onClick(View v) {
        v.startAnimation(Index.buttoneffect);
        if(v.getId() == R.id.ib_search){
            itemadapter.clear();

            String actionUrl = "Master/alldatausermessage/";
            new getUser().execute( actionUrl );
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private class getUser extends AsyncTask<String, Void, String> {
        String search = et_search.getText().toString();
        String user_nomor = Index.globalfunction.getShared("user", "nomor", "");

        @Override
        protected String doInBackground(String... urls) {
            try {
                Index.jsonObject = new JSONObject();
                Index.jsonObject.put("user_nomor", user_nomor);
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
            Log.d("data", result);
            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(!obj.has("query")){
                            String nomor = (obj.getString("nomor"));
                            String nama = (obj.getString("user_nama"));
                            String notif = (obj.getString("notif"));
                            String upper_nama = nama.substring(0, 1).toUpperCase() + nama.substring(1);
                            itemadapter.add(new ItemUserAdapter(nomor, upper_nama, notif));
                            itemadapter.notifyDataSetChanged();
                        }
                    }
                }
                hideLoading();
            }catch(Exception e)
            {
                e.printStackTrace();
                Toast.makeText(getContext(), "User Load Failed", Toast.LENGTH_LONG).show();
                hideLoading();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoading();
        }
    }

    private class updateNotif extends AsyncTask<String, Void, String> {
        String search = et_search.getText().toString();
        String user_nomor = Index.globalfunction.getShared("user", "nomor", "");

        @Override
        protected String doInBackground(String... urls) {
            try {
                Index.jsonObject = new JSONObject();
                Index.jsonObject.put("user_nomor", user_nomor);
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
            Log.d("data", result);
            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(!obj.has("query")){
                            String nomor = (obj.getString("nomor"));
                            String nama = (obj.getString("user_nama"));
                            String notif = (obj.getString("notif"));
                            String upper_nama = nama.substring(0, 1).toUpperCase() + nama.substring(1);
                            for (int j = 0; j < itemadapter.getCount(); j++) {
                                ItemUserAdapter adapter = itemadapter.getItem(j);
                                if(adapter.getNomor().toString().equals(nomor)){
                                    adapter.setNotif(notif);
                                    itemadapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                }
            }catch(Exception e)
            {
                e.printStackTrace();
            }
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

    class ItemUserAdapter {
        private String nomor;
        private String nama;
        private String notif;

        public ItemUserAdapter(String nomor, String nama, String notif) {
            this.setNomor(nomor);
            this.setNama(nama);
            this.setNotif(notif);
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

        public String getNotif() {return notif;}

        public void setNotif(String notif) {
            this.notif = notif;
        }
    }

    class ItemListUserAdapter extends ArrayAdapter<ItemUserAdapter> {

        private List<ItemUserAdapter> items;
        private int layoutResourceId;
        private Context context;


        public ItemListUserAdapter(Context context, int layoutResourceId, List<ItemUserAdapter> items) {
            super(context, layoutResourceId, items);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.items = items;
        }

        public class Holder {
            ItemUserAdapter adapterItem;
            TextView nomor;
            TextView nama;
            TextView notif;
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
            holder.notif = (TextView)row.findViewById(R.id.tv_notif);

            row.setTag(holder);
            setupItem(holder);

            final Holder finalHolder = holder;
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    /*String nomor = finalHolder.adapterItem.getNomor();
                    String nama = finalHolder.adapterItem.getNama();
                    Toast.makeText(getContext(), "Success" + " " + nama + " " + nomor, Toast.LENGTH_LONG).show();*/
                    Index.globalfunction.setShared("message", "userto_nomor", finalHolder.adapterItem.getNomor());
                    Index.globalfunction.setShared("message", "userto_nama", finalHolder.adapterItem.getNama());
                    Index.globalfunction.setShared("message", "private_run_check", "100");

                    Fragment fragment = new PrivateMessage();
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, fragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            });

            return row;
        }

        private void setupItem(Holder holder) {
            if(!holder.adapterItem.getNotif().equals("0")){
                holder.notif.setVisibility(View.VISIBLE);
                holder.notif.setText(holder.adapterItem.getNotif());
            }
            holder.nomor.setText(holder.adapterItem.getNomor());
            holder.nama.setText(holder.adapterItem.getNama());
        }
    }
}
