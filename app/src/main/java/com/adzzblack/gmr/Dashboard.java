package com.adzzblack.gmr;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class Dashboard extends Fragment implements View.OnClickListener {

    private ImageButton ib_berita, ib_delivery, ib_bpm, ib_opname, ib_pasang;
    private RelativeLayout rl_berita, rl_delivery, rl_bpm, rl_opname, rl_pasang;
    private RelativeLayout rl_unavaiable1, rl_unavaiable2, rl_unavaiable3, rl_unavaiable4, rl_unavaiable5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dashboard, container, false);
        getActivity().setTitle("Dashboard");

        Index.globalfunction.setShared("global", "position", "dashboard");

        rl_berita = (RelativeLayout) v.findViewById(R.id.rl_beritaacara);
        ib_berita = (ImageButton) v.findViewById(R.id.ib_beritaacara);
        rl_delivery = (RelativeLayout) v.findViewById(R.id.rl_delivery);
        ib_delivery= (ImageButton) v.findViewById(R.id.ib_deliveryorder);
        rl_bpm = (RelativeLayout) v.findViewById(R.id.rl_bpm);
        ib_bpm= (ImageButton) v.findViewById(R.id.ib_bpm);
        rl_opname = (RelativeLayout) v.findViewById(R.id.rl_opname);
        ib_opname = (ImageButton) v.findViewById(R.id.ib_opname);
        rl_pasang = (RelativeLayout) v.findViewById(R.id.rl_pasang);
        ib_pasang = (ImageButton) v.findViewById(R.id.ib_pasang);

        rl_unavaiable1 = (RelativeLayout) v.findViewById(R.id.rl_unavaiable1);
        rl_unavaiable2 = (RelativeLayout) v.findViewById(R.id.rl_unavaiable2);
        rl_unavaiable3 = (RelativeLayout) v.findViewById(R.id.rl_unavaiable3);
        rl_unavaiable4 = (RelativeLayout) v.findViewById(R.id.rl_unavaiable4);
        rl_unavaiable5 = (RelativeLayout) v.findViewById(R.id.rl_unavaiable5);

        if(Index.globalfunction.getShared("user", "role_beritaacara", "0").equals("0"))
        {
            rl_berita.setVisibility(View.GONE);
            rl_unavaiable1.setVisibility(View.VISIBLE);
        }
        else
        {
            rl_berita.setVisibility(View.VISIBLE);
            rl_unavaiable1.setVisibility(View.GONE);
            rl_berita.setOnClickListener(this);
            ib_berita.setOnClickListener(this);
        }

        if(Index.globalfunction.getShared("user", "role_deliveryorder", "0").equals("0"))
        {
            rl_delivery.setVisibility(View.GONE);
            rl_unavaiable2.setVisibility(View.VISIBLE);
        }
        else
        {
            rl_delivery.setVisibility(View.VISIBLE);
            rl_unavaiable2.setVisibility(View.GONE);
            rl_delivery.setOnClickListener(this);
            ib_delivery.setOnClickListener(this);
        }

        if(Index.globalfunction.getShared("user", "role_bpm", "0").equals("0"))
        {
            rl_bpm.setVisibility(View.GONE);
            rl_unavaiable3.setVisibility(View.VISIBLE);
        }
        else
        {
            rl_bpm.setVisibility(View.VISIBLE);
            rl_unavaiable3.setVisibility(View.GONE);
            rl_bpm.setOnClickListener(this);
            ib_bpm.setOnClickListener(this);
        }

        Log.d("tes", Index.globalfunction.getShared("user", "role_opname", "0"));
        if(Index.globalfunction.getShared("user", "role_opname", "0").equals("0"))
        {
            rl_opname.setVisibility(View.GONE);
            rl_unavaiable4.setVisibility(View.VISIBLE);
        }
        else
        {
            rl_opname.setVisibility(View.VISIBLE);
            rl_unavaiable4.setVisibility(View.GONE);
            rl_opname.setOnClickListener(this);
            ib_opname.setOnClickListener(this);
        }

        Log.d("tes", Index.globalfunction.getShared("user", "role_pasang", "0"));
        if(Index.globalfunction.getShared("user", "role_pasang", "1").equals("0"))
        {
            rl_pasang.setVisibility(View.GONE);
            rl_unavaiable5.setVisibility(View.VISIBLE);
        }
        else
        {
            rl_pasang.setVisibility(View.VISIBLE);
            rl_unavaiable5.setVisibility(View.GONE);
            rl_pasang.setOnClickListener(this);
            ib_pasang.setOnClickListener(this);
        }

        return v;
    }
	
    @Override
    public void onResume(){
        super.onResume();

    }

    public void onClick(View v) {
        v.startAnimation(Index.buttoneffect);
        if(v.getId() == R.id.rl_beritaacara || v.getId() == R.id.ib_beritaacara){
            Index.globalfunction.setShared("bangunan", "header", "0");
            Index.globalfunction.setShared("bangunan", "before", "");
            Index.globalfunction.setShared("global", "destination", "beritaacara");

            Fragment fragment = new ChooseBangunan();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
        else if(v.getId() == R.id.rl_delivery || v.getId() == R.id.ib_deliveryorder){
            Index.globalfunction.setShared("bangunan", "header", "0");
            Index.globalfunction.setShared("bangunan", "before", "");
            Index.globalfunction.setShared("global", "destination", "delivery");

            Fragment fragment = new ChooseBangunan();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
        else if(v.getId() == R.id.rl_bpm || v.getId() == R.id.ib_bpm){
            Index.globalfunction.setShared("bangunan", "header", "0");
            Index.globalfunction.setShared("bangunan", "before", "");
            Index.globalfunction.setShared("global", "destination", "bpm");

            Fragment fragment = new ChooseBangunan();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
        else if(v.getId() == R.id.rl_opname || v.getId() == R.id.ib_opname){
            Index.globalfunction.setShared("bangunan", "header", "0");
            Index.globalfunction.setShared("bangunan", "before", "");
            Index.globalfunction.setShared("global", "destination", "opname");

            Fragment fragment = new ChooseBangunan();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
        else if(v.getId() == R.id.rl_pasang || v.getId() == R.id.ib_pasang){
            Index.globalfunction.setShared("bangunan", "header", "0");
            Index.globalfunction.setShared("bangunan", "before", "");
            Index.globalfunction.setShared("global", "destination", "pasang");

            Fragment fragment = new ChooseBangunan();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}