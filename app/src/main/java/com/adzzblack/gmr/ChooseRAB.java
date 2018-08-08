package com.adzzblack.gmr;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
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

import java.nio.Buffer;
import java.util.ArrayList;

public class ChooseRAB extends Fragment implements View.OnClickListener {

    private ItemListAdapter itemadapter;

    private EditText et_search;
    private ImageButton ib_search;
    private ListView lv_choose;

    private String nomor;
    private Boolean isTyping = false;
    private Search search;
    private final Handler handler = new Handler();

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
        itemadapter = new ItemListAdapter(getActivity(), R.layout.list_item, new ArrayList<ItemAdapter>());
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
                nomor = tv_nomor.getText().toString();
                Index.globalfunction.clearShared("rab");
                Index.globalfunction.setShared("rab", "rab", nomor);


                Fragment fragment = new DeliveryOrder();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        final Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                itemadapter.clear();
                String actionUrl = "Master/alldatarab/";
                search = new Search();
                search.execute( actionUrl );
            }
        };
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                handler.removeCallbacks(myRunnable);
                handler.removeCallbacksAndMessages(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                isTyping = false;
                if(!et_search.getText().toString().trim().equals("") && !isTyping) {
                    isTyping = true;
                    handler.postDelayed(myRunnable, 2000);
                }
            }
        });
        //-----END DECLARE---------------------------------------------------------------------------------------

        String actionUrl = "Master/alldatarab/";
        search = new Search();
        search.execute( actionUrl );

        return v;
    }

    public void onClick(View v) {
        v.startAnimation(Index.buttoneffect);
        if(v.getId() == R.id.ib_search){
            isTyping = true;
            handler.removeCallbacksAndMessages(null);
            itemadapter.clear();

            String actionUrl = "Master/alldatarab/";
            search = new Search();
            search.execute( actionUrl );
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(search != null) search.cancel(true);
    }

    private class Search extends AsyncTask<String, Void, String> {
        String strSearch = et_search.getText().toString();

        @Override
        protected String doInBackground(String... urls) {
            try {
                Index.jsonObject = new JSONObject();
                Index.jsonObject.put("search", strSearch);
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
            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = jsonarray.length() - 1; i >= 0; i--) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(!obj.has("query")){
                            String nomor = (obj.getString("nomor"));
                            String nama = (obj.getString("nama"));
                            String namamandor = (obj.getString("namamandor"));
                            Log.wtf("query RAB ", obj.getString("querymessage"));

                            itemadapter.add(new ItemAdapter(nomor, nama + "\n \n"
                                    + Html.fromHtml("<font color=\"green\"> Mandor " + namamandor + "</font>")));
                            itemadapter.notifyDataSetChanged();
                        }
                    }
                }
            }catch(Exception e)
            {
                e.printStackTrace();
                Toast.makeText(getContext(), "RAB Load Failed", Toast.LENGTH_LONG).show();
            }finally {
                LibInspira.hideLoading();
            }
            isTyping = false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LibInspira.showLoading(getContext(), "Getting data RAB", "Loading");
        }
    }
}