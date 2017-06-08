package com.adzzblack.gmr;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FormBPM extends Fragment implements View.OnClickListener {

    private ItemListDeliveryOrderAdapter itemadapter;

    private ListView lv_choose;

    private String nomor;
    private String kode;
    private String detail;

    private TextView tv_kode;
    private Button btn_back;
    private Button btn_next;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.form_bpm, container, false);
        getActivity().setTitle("Form BPM");

        //-----START DECLARE---------------------------------------------------------------------------------------
        nomor = Index.globalfunction.getShared("global", "thdeliveryordernow", "");
        kode = Index.globalfunction.getShared("global", "kodedeliveryordernow", "");
        detail = Index.globalfunction.getShared("global", "detailbpm", "");

        btn_back = (Button) v.findViewById(R.id.btn_back);
        btn_next = (Button) v.findViewById(R.id.btn_next);
        btn_back.setOnClickListener(this);
        btn_next.setOnClickListener(this);

        tv_kode = (TextView) v.findViewById(R.id.tv_kode);
        itemadapter = new ItemListDeliveryOrderAdapter(getActivity(), R.layout.list_form_bpm, new ArrayList<ItemDeliveryOrderAdapter>());
        lv_choose = (ListView) v.findViewById(R.id.lv_choose);
        lv_choose.setAdapter(itemadapter);
        tv_kode.setText(kode);
        //-----END DECLARE---------------------------------------------------------------------------------------


        Log.d("detail", detail);
        try {
            JSONArray jsonarray = new JSONArray(detail);
            if(jsonarray.length() > 0){
                for (int i = jsonarray.length() - 1; i >= 0; i--) {
                    JSONObject obj = jsonarray.getJSONObject(i);
                    String nomor = (obj.getString("nomor"));
                    String namabarang = (obj.getString("nama"));
                    String jumlah = (obj.getString("jumlah"));
                    String satuan = (obj.getString("satuan"));
                    String harga = (obj.getString("harga"));
                    String nomorbarang = (obj.getString("nomorbarang"));
                    String nama = namabarang + " (" + jumlah + " " + satuan + ")";

                    itemadapter.add(new ItemDeliveryOrderAdapter(nomor, nama, jumlah, harga, nomorbarang));
                    itemadapter.notifyDataSetChanged();
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return v;
    }

    public void onClick(View v) {
        v.startAnimation(Index.buttoneffect);
        if(v.getId() == R.id.btn_back){
            Index.fm.popBackStack();
        }
        else if(v.getId() == R.id.btn_next){
            String data = "";
            for(int i=0;i<itemadapter.items.size();i++)
            {
                data = data + itemadapter.items.get(i).getNomor() + "~" + itemadapter.items.get(i).getJumlahBPM() + "~" + itemadapter.items.get(i).getHarga() + "~" + itemadapter.items.get(i).getNomorBarang() + "~" + itemadapter.items.get(i).getKeterangan() + "|";
            }
            Index.globalfunction.setShared("global", "detailbpmbaru", data);

            Index.globalfunction.setShared("global", "from", "formBPM");
            Fragment fragment = new Sign();
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

    class ItemDeliveryOrderAdapter {

        private String nama;
        private String jumlah;
        private String jumlahbpm;
        private String nomor;
        private String harga;
        private String nomorbarang;
        private String keterangan;

        public ItemDeliveryOrderAdapter(String nomor, String nama, String jumlah, String harga, String nomorbarang) {
            this.setNomor(nomor);
            this.setNama(nama);
            this.setJumlah(jumlah);
            this.setJumlahBPM(jumlah);
            this.setHarga(harga);
            this.setNomorBarang(nomorbarang);
            this.setKeterangan("");
        }

        public String getNomor() {return nomor;}

        public void setNomor(String nomor) {
            this.nomor = nomor;
        }

        public String getNomorBarang() {return nomorbarang;}

        public void setNomorBarang(String nomorbarang) {
            this.nomorbarang = nomorbarang;
        }

        public String getNama() {return nama;}

        public void setNama(String nama) {
            this.nama = nama;
        }

        public String getJumlah() {return jumlah;}

        public void setJumlah(String jumlah) {
            this.jumlah = jumlah;
        }

        public String getJumlahBPM() {return jumlahbpm;}

        public void setJumlahBPM(String jumlahbpm) {
            this.jumlahbpm = jumlahbpm;
        }

        public String getHarga() {return harga;}

        public void setHarga(String harga) {
            this.harga = harga;
        }

        public String getKeterangan() {return keterangan;}

        public void setKeterangan(String keterangan) {
            this.keterangan = keterangan;
        }
    }

    class ItemListDeliveryOrderAdapter extends ArrayAdapter<ItemDeliveryOrderAdapter> {

        private List<ItemDeliveryOrderAdapter> items;
        private int layoutResourceId;
        private Context context;


        public ItemListDeliveryOrderAdapter(Context context, int layoutResourceId, List<ItemDeliveryOrderAdapter> items) {
            super(context, layoutResourceId, items);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.items = items;
        }

        public class Holder {
            ItemDeliveryOrderAdapter adapterItem;
            TextView nama;
            Button bpm, keterangan;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            Holder holder = null;

            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new Holder();
            holder.adapterItem = items.get(position);

            holder.nama = (TextView) row.findViewById(R.id.tv_nama);
            holder.bpm = (Button) row.findViewById(R.id.btn_bpm);
            holder.keterangan = (Button) row.findViewById(R.id.btn_keterangan);

            row.setTag(holder);
            setupItem(holder);

            final Holder finalHolder = holder;
            holder.bpm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Number of received items");

                    // Set up the input
                    final EditText input = new EditText(getContext());
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setText(finalHolder.adapterItem.getJumlahBPM());
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    input.setSelection(input.getText().length());
                    builder.setView(input);

                    // Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(Integer.parseInt(input.getText().toString())<=Integer.parseInt(finalHolder.adapterItem.getJumlah()))
                            {
                                finalHolder.adapterItem.setJumlahBPM(input.getText().toString());
                            }
                            else
                            {
                                Toast.makeText(getContext(), "Not valid", Toast.LENGTH_LONG).show();
                            }
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

            holder.keterangan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Keterangan");

                    // Set up the input
                    final EditText input = new EditText(getContext());
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setText(finalHolder.adapterItem.getKeterangan());
                    input.setSelection(input.getText().length());
                    builder.setView(input);

                    // Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finalHolder.adapterItem.setKeterangan(input.getText().toString());
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

            return row;
        }

        private void setupItem(Holder holder) {
            holder.nama.setText(holder.adapterItem.getNama());
            holder.bpm.setText(holder.adapterItem.getJumlahBPM());
            holder.keterangan.setText(holder.adapterItem.getKeterangan());
        }
    }
}