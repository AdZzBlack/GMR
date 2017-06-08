package com.adzzblack.gmr;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
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

public class ApproveDeliveryOrder extends Fragment implements View.OnClickListener {

    private ItemListOrderAdapter itemadapter;

    private ListView lv_choose;

    private String nomor;

    private Button btn_approve, btn_approve_selected, btn_disapprove;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.choose_approve_order, container, false);
        getActivity().setTitle("Approval Order");

        //-----START DECLARE---------------------------------------------------------------------------------------
        Index.globalfunction.setShared("rab", "listapprove", "");
        itemadapter = new ItemListOrderAdapter(getActivity(), R.layout.list_approve_order, new ArrayList<ItemOrderAdapter>());
        lv_choose = (ListView) v.findViewById(R.id.lv_choose);
        lv_choose.setAdapter(itemadapter);
        btn_approve = (Button) v.findViewById(R.id.btn_approve);
        btn_disapprove = (Button) v.findViewById(R.id.btn_disapprove);
        btn_approve_selected = (Button) v.findViewById(R.id.btn_approve_selected);

        btn_approve.setOnClickListener(this);
        btn_disapprove.setOnClickListener(this);
        btn_approve_selected.setOnClickListener(this);
        //-----END DECLARE---------------------------------------------------------------------------------------

        String actionUrl = "DeliveryOrder/alldatadetailneedapproval/";
        new search().execute( actionUrl );

        return v;
    }

    public void onClick(View v) {
        v.startAnimation(Index.buttoneffect);
//        if(v.getId() == R.id.btn_approve){
//            Log.d("test", Index.globalfunction.getShared("rab", "listapprove", ""));
//        }
        if(v.getId() == R.id.btn_approve){
            String actionUrl = "DeliveryOrder/approveall/";
            new approveall().execute( actionUrl );
        }
        else if(v.getId() == R.id.btn_disapprove){
            String actionUrl = "DeliveryOrder/disapproveall/";
            new disapproveall().execute( actionUrl );
        }
        else if(v.getId() == R.id.btn_approve_selected){
            String actionUrl = "DeliveryOrder/approveselected/";
            new approveselected().execute( actionUrl );
            Log.d("test", Index.globalfunction.getShared("rab", "listapprove", ""));
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
            try {
                Index.jsonObject = new JSONObject();
                Index.jsonObject.put("nomor", Index.globalfunction.getShared("rab", "nomorth", ""));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

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
                            String nomor = (obj.getString("nomor"));
                            String nomordelivery = (obj.getString("nomorthdeliveryorder"));
                            String waste = (obj.getString("waste"));
                            String jumlah = (obj.getString("jumlah"));
                            String delivered = (obj.getString("do"));
                            String jumlahorder = (obj.getString("jumlahorder"));
                            String catatan = (obj.getString("catatan"));
                            String namalengkap = (obj.getString("namalengkap"));
                            String item = (obj.getString("item"));
                            String satuan = (obj.getString("satuan"));

                            itemadapter.add(new ItemOrderAdapter(nomor, nomordelivery, waste, jumlah, delivered, jumlahorder, catatan, namalengkap, item, satuan));
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

    private class approveall extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                Index.jsonObject = new JSONObject();
                Index.jsonObject.put("nomorth", Index.globalfunction.getShared("rab", "nomorth", ""));
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
                            if(obj.getString("success").equals("true"))
                            {
                                Toast.makeText(getContext(), "Approve Success", Toast.LENGTH_LONG).show();
                                Fragment fragment = new ChooseApprovalDelivery();
                                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                transaction.replace(R.id.fragment_container, fragment);
                                transaction.commit();
                            }
                            else
                            {
                                Toast.makeText(getContext(), "Approve Failed", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }

                hideLoading();
            }catch(Exception e)
            {
                e.printStackTrace();
                Toast.makeText(getContext(), "Approve Failed", Toast.LENGTH_LONG).show();
                hideLoading();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoading();
        }
    }

    private class disapproveall extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                Index.jsonObject = new JSONObject();
                Index.jsonObject.put("nomorth", Index.globalfunction.getShared("rab", "nomorth", ""));
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
                            if(obj.getString("success").equals("true"))
                            {
                                Toast.makeText(getContext(), "Disapprove Success", Toast.LENGTH_LONG).show();
                                Fragment fragment = new ChooseApprovalDelivery();
                                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                transaction.replace(R.id.fragment_container, fragment);
                                transaction.commit();
                            }
                            else
                            {
                                Toast.makeText(getContext(), "Disapprove Failed", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }

                hideLoading();
            }catch(Exception e)
            {
                e.printStackTrace();
                Toast.makeText(getContext(), "Disapprove Failed", Toast.LENGTH_LONG).show();
                hideLoading();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoading();
        }
    }

    private class approveselected extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                Index.jsonObject = new JSONObject();
                Index.jsonObject.put("nomorth", Index.globalfunction.getShared("rab", "nomorth", ""));
                Index.jsonObject.put("list", Index.globalfunction.getShared("rab", "listapprove", ""));
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
                            if(obj.getString("success").equals("true"))
                            {
                                Toast.makeText(getContext(), "Approve Selected Success", Toast.LENGTH_LONG).show();
                                Fragment fragment = new ChooseApprovalDelivery();
                                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                transaction.replace(R.id.fragment_container, fragment);
                                transaction.commit();
                            }
                            else
                            {
                                Toast.makeText(getContext(), "Approve Selected Failed", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }

                hideLoading();
            }catch(Exception e)
            {
                e.printStackTrace();
                Toast.makeText(getContext(), "Approve Selected Failed", Toast.LENGTH_LONG).show();
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

    class ItemOrderAdapter {

        private String nomor;
        private String nomordeliveryorder;
        private String waste;
        private String jumlah;
        private String delivered;
        private String jumlahorder;
        private String catatan;
        private String namalengkap;
        private String item;
        private String satuan;
        private Boolean clicked;

        public ItemOrderAdapter(String nomor, String nomordelivery, String waste, String jumlah, String jumlahterorder, String jumlahorder, String catatan, String namalengkap, String item, String satuan) {
            this.setNomor(nomor);
            this.setNomorDelivery(nomordelivery);
            this.setWaste(waste);
            this.setJumlah(jumlah);
            this.setTerorder(jumlahterorder);
            this.setOrder(jumlahorder);
            this.setCatatan(catatan);
            this.setNamalengkap(namalengkap);
            this.setItem(item);
            this.setSatuan(satuan);
            this.setClicked(false);
        }

        public String getNomor() {
            return nomor;
        }

        public void setNomor(String param) {
            this.nomor = param;
        }

        public String getNomorDelivery() {
            return nomordeliveryorder;
        }

        public void setNomorDelivery(String param) {
            this.nomordeliveryorder = param;
        }

        public String getWaste() {
            return waste;
        }

        public void setWaste(String param) {
            this.waste = param;
        }

        public String getJumlah() {
            return jumlah;
        }

        public void setJumlah(String param) {
            this.jumlah = param;
        }

        public String getTerorder() {
            return delivered;
        }

        public void setTerorder(String param) {
            this.delivered = param;
        }

        public String getOrder() {
            return jumlahorder;
        }

        public void setOrder(String param) {
            this.jumlahorder = param;
        }

        public String getCatatan() {
            return catatan;
        }

        public void setCatatan(String param) {
            this.catatan = param;
        }

        public String getNamalengkap() {
            return namalengkap;
        }

        public void setNamalengkap(String param) {
            this.namalengkap = param;
        }

        public String getItem() {
            return item;
        }

        public void setItem(String param) {
            this.item = param;
        }

        public String getSatuan() {
            return satuan;
        }

        public void setSatuan(String param) {
            this.satuan = param;
        }

        public Boolean getClicked() {
            return clicked;
        }

        public void setClicked(Boolean param) {
            this.clicked = param;
        }
    }

    class ItemListOrderAdapter extends ArrayAdapter<ItemOrderAdapter> {

        private List<ItemOrderAdapter> items;
        private int layoutResourceId;
        private Context context;


        public ItemListOrderAdapter(Context context, int layoutResourceId, List<ItemOrderAdapter> items) {
            super(context, layoutResourceId, items);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.items = items;
        }

        public class Holder {
            ItemOrderAdapter adapterItem;
            TextView bangunan;
            TextView item;
            TextView waste;
            TextView need;
            TextView ordered;
            TextView order;
            TextView satuan1, satuan2, satuan3;
            TextView catatan;
            int clickCtr = 0;
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
            holder.item = (TextView)row.findViewById(R.id.tv_item);
            holder.waste = (TextView)row.findViewById(R.id.tv_waste);
            holder.need = (TextView)row.findViewById(R.id.tv_need);
            holder.ordered = (TextView)row.findViewById(R.id.tv_ordered);
            holder.order = (TextView)row.findViewById(R.id.tv_order);
            holder.satuan1 = (TextView)row.findViewById(R.id.tv_satuan1);
            holder.satuan2 = (TextView)row.findViewById(R.id.tv_satuan2);
            holder.satuan3 = (TextView)row.findViewById(R.id.tv_satuan3);
            holder.catatan = (TextView)row.findViewById(R.id.tv_keterangan);

            row.setTag(holder);
            setupItem(holder);

            final Holder finalHolder = holder;
            final View finalRow = row;
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(finalHolder.adapterItem.getClicked())
                    {
                        finalHolder.adapterItem.setClicked(false);
//                        finalRow.setBackgroundColor(Color.LTGRAY);
//                        finalHolder.clickCtr = 1;

                        deleteData(finalHolder.adapterItem.getNomor());
                    }
                    else
                    {
                        finalHolder.adapterItem.setClicked(true);
//                        finalRow.setBackgroundColor(Color.WHITE);
//                        finalHolder.clickCtr = 0;

                        String oldData = Index.globalfunction.getShared("rab", "listapprove", "");
                        Index.globalfunction.setShared("rab", "listapprove", oldData + finalHolder.adapterItem.getNomor() + "|");
                    }

                    if(finalHolder.adapterItem.getClicked())
                    {
                        finalRow.setBackgroundColor(Color.LTGRAY);
                        Log.d("click", "1");
                    }
                    else
                    {
                        finalRow.setBackgroundColor(Color.WHITE);
                        Log.d("click", "2");
                    }
                }
            });

            if(holder.adapterItem.getClicked())
            {
                row.setBackgroundColor(Color.LTGRAY);
                Log.d("click", "1");
            }
            else
            {
                row.setBackgroundColor(Color.WHITE);
                Log.d("click", "2");
            }

            return row;
        }

        public void deleteData(final String pos){
            String realData = "";
            String datalistbarang = Index.globalfunction.getShared("rab", "listapprove", "");
            if(!datalistbarang.equals("")){
                String[] pieces = datalistbarang.trim().split("\\|");
                for(int i=0 ; i < pieces.length ; i++){
                    if(!pieces[i].equals(pos)){
                        realData += pieces[i] + "|";
                    }
                }
                Index.globalfunction.setShared("rab", "listapprove", realData);
            }
        }

        private void setupItem(Holder holder) {
            holder.bangunan.setText(holder.adapterItem.getNamalengkap());
            holder.item.setText(holder.adapterItem.getItem());
            holder.waste.setText(holder.adapterItem.getWaste());
            holder.need.setText(holder.adapterItem.getJumlah());
            holder.ordered.setText(holder.adapterItem.getTerorder());
            holder.order.setText(holder.adapterItem.getOrder());
            holder.satuan1.setText(holder.adapterItem.getSatuan());
            holder.satuan2.setText(holder.adapterItem.getSatuan());
            holder.satuan3.setText(holder.adapterItem.getSatuan());
            holder.catatan.setText(holder.adapterItem.getCatatan());
        }
    }
}