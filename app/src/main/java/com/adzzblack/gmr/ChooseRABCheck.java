package com.adzzblack.gmr;

import android.app.ProgressDialog;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ChooseRABCheck extends Fragment implements View.OnClickListener {

    private MoreItemListAdapter itemadapter;

    private EditText et_search;
    private ImageButton ib_search;
    private ListView lv_choose;

    private String nomor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.choose, container, false);
        getActivity().setTitle("RAB");

        //-----START DECLARE---------------------------------------------------------------------------------------
        et_search = (EditText) v.findViewById(R.id.et_search);
        itemadapter = new MoreItemListAdapter(getActivity(), R.layout.list_moreitem, new ArrayList<MoreItemAdapter>());
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

                if(itemadapter.getItem(position).getData().equals(""))
                {
                    Index.globalfunction.setShared("global", "nomorrab", itemadapter.getItem(position).getNomor());
                    Index.globalfunction.setShared("global", "namarab", itemadapter.getItem(position).getNama());

                    Fragment fragment = new FormOpname();
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, fragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
                else
                {
                    Toast.makeText(getContext(), "Opname not valid", Toast.LENGTH_LONG).show();
                }
            }
        });
        //-----END DECLARE---------------------------------------------------------------------------------------

        String actionUrl = "Master/alldatarabWithCheck/";
        new search().execute( actionUrl );

        return v;
    }

    public void onClick(View v) {
        v.startAnimation(Index.buttoneffect);
        if(v.getId() == R.id.ib_search){
            itemadapter.clear();

            String actionUrl = "Master/alldatarabWithCheck/";
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
                Index.jsonObject.put("search", search);
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
            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = jsonarray.length() - 1; i >= 0; i--) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(!obj.has("query")){
                            String nomor = (obj.getString("nomor"));
                            String nama = (obj.getString("nama"));
                            String keterangan = (obj.getString("keterangan"));

                            if(keterangan.equals("1"))
                            {
                                itemadapter.add(new MoreItemAdapter(nomor, nama, ""));
                            }
                            else
                            {
                                itemadapter.add(new MoreItemAdapter(nomor, nama, keterangan));
                            }
                            itemadapter.notifyDataSetChanged();
                        }
                    }
                }

                hideLoading();
            }catch(Exception e)
            {
                e.printStackTrace();
                Toast.makeText(getContext(), "RAB Load Failed", Toast.LENGTH_LONG).show();
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
}