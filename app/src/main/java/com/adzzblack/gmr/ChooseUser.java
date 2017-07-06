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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by owners on 04/07/2017.
 */

public class ChooseUser extends Fragment implements View.OnClickListener {

    private EditText et_search;
    private ImageButton ib_search;
    private ListView lv_choose;
    private ImageView iv_cancelselection, iv_send;
    private TextView tv_selecteditem;
    private LinearLayout choose_selecteduser;
    private ItemListUserAdapter itemadapter;
    private ItemListUserAdapter itemadapterall;

    private int counter = 0;
    private int counteruser = 0;
    private String userfrom_nomor = Index.globalfunction.getShared("user","id","");

    private ArrayList<String> selectedlist = new ArrayList<String>();
    private ArrayList<String> selectedlist_message = new ArrayList<String>();
    private ArrayList<String> selectedlist_url = new ArrayList<String>();
    private ArrayList<String> selectedlist_tipe = new ArrayList<String>();
    private ArrayList<String> selecteduser = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.choose_morethanone, container, false);
        if(Index.globalfunction.getShared("message","forwardmessage_nomor", "").length() > 0){
            getActivity().setTitle("Forward to...");
        }

        //-----START DECLARE---------------------------------------------------------------------------------------

        iv_cancelselection = (ImageView) v.findViewById(R.id.iv_cancelselection);
        iv_cancelselection.setOnClickListener(this);
        choose_selecteduser = (LinearLayout) v.findViewById(R.id.choose_selecteditem);
        tv_selecteditem = (TextView) v.findViewById(R.id.tv_selecteditem);
        iv_send = (ImageView) v.findViewById(R.id.iv_send);
        iv_send.setOnClickListener(this);
        et_search = (EditText) v.findViewById(R.id.et_search);
        itemadapter = new ItemListUserAdapter(getActivity(), R.layout.list_withcheckbox, new ArrayList<ItemUserAdapter>());
        itemadapterall = new ItemListUserAdapter(getActivity(), R.layout.list_withcheckbox, new ArrayList<ItemUserAdapter>());
        lv_choose = (ListView) v.findViewById(R.id.lv_choose);
        lv_choose.setAdapter(itemadapter);
        ib_search = (ImageButton) v.findViewById(R.id.ib_search);
        ib_search.setOnClickListener(this);

        //--------END DECLARE---------------------------------------------------------------------

        String actionUrl = "Master/alldatausermessage/";
        new getUserAll().execute( actionUrl );

        if(Index.globalfunction.getShared("message","forwardmessage_nomor", "").length() > 0){
            String message_nomor = Index.globalfunction.getShared("message","forwardmessage_nomor", "");
            String[] separated = message_nomor.split("\\,");
            for (int i=0 ; i < separated.length ; i++){
                selectedlist.add(separated[i]);
            }
        }

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Thread.currentThread().interrupt();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onClick(View v) {
        v.startAnimation(Index.buttoneffect);
        if(v.getId() == R.id.ib_search){
            itemadapter.clear();

            String actionUrl = "Master/alldatausermessage/";
            new getUser().execute( actionUrl );
            setSelectedItem();
        }else if(v.getId() == R.id.iv_cancelselection){
            getActivity().onBackPressed();
        }else if(v.getId() == R.id.iv_send){
            Toast.makeText(getContext(), "Please wait..", Toast.LENGTH_LONG).show();
            if(selecteduser.size()>0){
                counter = 0;
                new getSpecificMessage().execute( "Message/getSpecificPrivateMessage/" );
            }
        }
    }

    private void sendMessageSuccess(){
        Index.globalfunction.setShared("message","forwardmessage_from", "");
        Index.globalfunction.setShared("message","forwardmessage_nomor","done");
        String userto_nama = "";
        for (int i=0; i<itemadapter.getCount(); i++){
            ItemUserAdapter adapter = itemadapter.getItem(i);
            if(adapter.getNomor().equals(selecteduser.get(selecteduser.size()-1))){
                userto_nama = adapter.getNama();
            }
        }
        Index.globalfunction.setShared("message", "userto_nomor", selecteduser.get(selecteduser.size()-1));
        Index.globalfunction.setShared("message", "userto_nama", userto_nama);
        Index.globalfunction.setShared("message", "private_run_check", "100");
        getActivity().onBackPressed();
    }

    private class sendMessage extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                Index.jsonObject = new JSONObject();
                Index.jsonObject.put("userfrom_nomor", userfrom_nomor);
                Index.jsonObject.put("userto_nomor", selecteduser.get(counteruser));
                Index.jsonObject.put("new_message", selectedlist_message.get(counter));
                Index.jsonObject.put("url", selectedlist_url.get(counter));
                Index.jsonObject.put("tipe_send", selectedlist_tipe.get(counter));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return Index.globalfunction.executePost(urls[0],Index.jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray jsonarray = new JSONArray(result);
                for (int i = 0; i < jsonarray.length(); i++) {
                    JSONObject obj = jsonarray.getJSONObject(i);
                    String success = obj.getString("success");
                    if(success.equals("true")){
                        counter = counter + 1;

                        if(String.valueOf(counter).equals(String.valueOf(selectedlist.size()))){
                            counter = 0;
                            counteruser = counteruser + 1;
                            if(String.valueOf(counteruser).equals(String.valueOf(selecteduser.size()))){
                                sendMessageSuccess();
                            }else {
                                new sendMessage().execute( "Message/sendPrivateMessage/" );
                            }
                        }else {
                            new sendMessage().execute( "Message/sendPrivateMessage/" );
                        }
                    }else {

                        Toast.makeText(getContext(), "Failed send message", Toast.LENGTH_LONG).show();
                    }
                }
            }catch(Exception e)
            {
                e.printStackTrace();
                Toast.makeText(getContext(), "Failed send message", Toast.LENGTH_LONG).show();
            }
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    private class getSpecificMessage extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                Index.jsonObject = new JSONObject();
                Index.jsonObject.put("nomor", selectedlist.get(counter));
            } catch (JSONException e) { e.printStackTrace(); }

            return Index.globalfunction.executePost(urls[0],Index.jsonObject);
        }
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray jsonarray = new JSONArray(result);
                for (int i = 0; i < jsonarray.length(); i++) {
                    JSONObject obj = jsonarray.getJSONObject(i);
                    if(!obj.has("query")){
                        selectedlist_message.add(counter,(obj.getString("message")));
                        selectedlist_url.add(counter, (obj.getString("url")));
                        selectedlist_tipe.add(counter, (obj.getString("tipe")));
                        if(selectedlist_tipe.get(counter).equals("4")){
                            String[] separated = selectedlist_message.get(counter).split("\\-R3p7y-");
                            selectedlist_message.set(counter,separated[1]);
                            selectedlist_tipe.set(counter, "1");
                        }
                        counter = counter + 1;

                        if(String.valueOf(counter).equals(String.valueOf(selectedlist.size()))){
                            counter = 0;
                            counteruser = 0;
                            new sendMessage().execute( "Message/sendPrivateMessage/" );
                        }else{
                            new getSpecificMessage().execute( "Message/getSpecificPrivateMessage/" );
                        }
                    }else{
                        Toast.makeText(getContext(), "Failed get message", Toast.LENGTH_LONG).show();
                    }
                }
            }catch(Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Failed get message", Toast.LENGTH_LONG).show();
            }
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    private class getUserAll extends AsyncTask<String, Void, String> {
        String search = et_search.getText().toString();
        String user_nomor = Index.globalfunction.getShared("user", "id", "");

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
            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(!obj.has("query")){
                            String nomor = (obj.getString("nomor"));
                            String nama = (obj.getString("user_nama"));
                            String upper_nama = nama.substring(0, 1).toUpperCase() + nama.substring(1);
                            itemadapterall.add(new ItemUserAdapter(nomor, upper_nama));
                            itemadapterall.notifyDataSetChanged();
                            itemadapter.add(new ItemUserAdapter(nomor, upper_nama));
                            itemadapter.notifyDataSetChanged();
                        }
                    }
                }
            }catch(Exception e)
            {
                e.printStackTrace();
                Toast.makeText(getContext(), "User Load Failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class getUser extends AsyncTask<String, Void, String> {
        String search = et_search.getText().toString();
        String user_nomor = Index.globalfunction.getShared("user", "id", "");

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
            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(!obj.has("query")){
                            String nomor = (obj.getString("nomor"));
                            String nama = (obj.getString("user_nama"));
                            String upper_nama = nama.substring(0, 1).toUpperCase() + nama.substring(1);
                            itemadapter.add(new ItemUserAdapter(nomor, upper_nama));
                            itemadapter.notifyDataSetChanged();
                        }
                    }
                }
            }catch(Exception e)
            {
                e.printStackTrace();
                Toast.makeText(getContext(), "User Load Failed", Toast.LENGTH_LONG).show();
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

    private void clickItem(String nomor){
        if(selecteduser.contains(nomor)){
            int index = selecteduser.indexOf(nomor);
            selecteduser.remove(index);
        }else {
            selecteduser.add(nomor);
        }
        setSelectedItem();
    }

    private void setSelectedItem(){
        if(String.valueOf(selecteduser.size()).equals("0")){
            choose_selecteduser.setVisibility(View.GONE);
        }else if(selecteduser.size() > 0) {
            String tampnama = "";
            for(int i=(selecteduser.size()-1); i>=0; i--){
                for (int j = 0; j < itemadapterall.getCount(); j++) {
                    ItemUserAdapter adapter = itemadapterall.getItem(j);
                    if (adapter.getNomor().toString().equals(selecteduser.get(i))) {
                        if(tampnama.equals("")){
                            tampnama = adapter.getNama();
                        }else {
                            tampnama = tampnama + ", " + adapter.getNama();
                        }
                    }
                }
            }
            tv_selecteditem.setText(tampnama);
            choose_selecteduser.setVisibility(View.VISIBLE);
        }
        itemadapter.notifyDataSetChanged();
    }

    class ItemUserAdapter {
        private String nomor;
        private String nama;

        public ItemUserAdapter(String nomor, String nama) {
            this.setNomor(nomor);
            this.setNama(nama);
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
            TextView nama;
            ImageView checkicon;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            Holder holder = null;

            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ItemListUserAdapter.Holder();
            holder.adapterItem = items.get(position);

            holder.nama = (TextView)row.findViewById(R.id.tv_nama);
            holder.checkicon = (ImageView) row.findViewById(R.id.iv_selectitem);

            row.setTag(holder);
            setupItem(holder);

            final Holder finalHolder = holder;
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Index.listeffect);
                    clickItem(finalHolder.adapterItem.getNomor());
                }
            });
            holder.checkicon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Index.listeffect);
                    clickItem(finalHolder.adapterItem.getNomor());
                }
            });

            return row;
        }

        private void setupItem(ItemListUserAdapter.Holder holder) {
            holder.nama.setText(holder.adapterItem.getNama());
            if(selecteduser.size() > 0){
                for(int i=0; i<selecteduser.size(); i++){
                    if(holder.adapterItem.getNomor().equals(selecteduser.get(i))){
                        holder.checkicon.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }
}
