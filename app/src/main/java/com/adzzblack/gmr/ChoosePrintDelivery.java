package com.adzzblack.gmr;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
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
import java.util.ArrayList;
import java.util.List;

public class ChoosePrintDelivery extends Fragment implements View.OnClickListener {

    private ItemListAdapter itemadapter1;
    private ItemListAdapter itemadapter2;

    private EditText et_search;
    private ImageButton ib_search;
    private ListView lv_choose1;
    private ListView lv_choose2;

    private String nomor;

    private PdfPCell cell;
    private Image bgImage;
    private String path;
    private File dir;
    private File file;
    private String kode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.choose_print_do, container, false);
        getActivity().setTitle("Approved Order");

        //-----START DECLARE---------------------------------------------------------------------------------------
        Index.globalfunction.clearShared("rab");

        et_search = (EditText) v.findViewById(R.id.et_search);
        itemadapter1 = new ItemListAdapter(getActivity(), R.layout.list_item, new ArrayList<ItemAdapter>());
        itemadapter2 = new ItemListAdapter(getActivity(), R.layout.list_item, new ArrayList<ItemAdapter>());
        lv_choose1 = (ListView) v.findViewById(R.id.lv_choose1);
        lv_choose1.setAdapter(itemadapter1);
        lv_choose2 = (ListView) v.findViewById(R.id.lv_choose2);
        lv_choose2.setAdapter(itemadapter2);
        ib_search = (ImageButton) v.findViewById(R.id.ib_search);
        ib_search.setOnClickListener(this);
        lv_choose1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                // TODO Auto-generated method stub
                v.startAnimation(Index.listeffect);

                TextView tv_nomor = (TextView) v.findViewById(R.id.tv_nomor);
                TextView tv_nama = (TextView) v.findViewById(R.id.tv_nama);
                nomor = tv_nomor.getText().toString();
                kode = tv_nama.getText().toString().replace("/", "");

                Index.globalfunction.setShared("rab","nomorth",nomor);

                String actionUrl = "DeliveryOrder/alldatadetailneedprint/";
                new get().execute( actionUrl );
            }
        });
        lv_choose2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                // TODO Auto-generated method stub
                v.startAnimation(Index.listeffect);

                TextView tv_nomor = (TextView) v.findViewById(R.id.tv_nomor);
                TextView tv_nama = (TextView) v.findViewById(R.id.tv_nama);
                nomor = tv_nomor.getText().toString();
                kode = tv_nama.getText().toString().replace("/", "");

                Index.globalfunction.setShared("rab","nomorth",nomor);

                String actionUrl = "DeliveryOrder/alldatadetailneedprint/";
                new get().execute( actionUrl );
            }
        });
        //-----END DECLARE---------------------------------------------------------------------------------------

        String actionUrl = "DeliveryOrder/alldataneedprint/";
        new search().execute( actionUrl );

        //creating new file path
        path = Environment.getExternalStorageDirectory().getAbsolutePath() + Index.globalfunction.pdf_folder;
        dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return v;
    }

    public void onClick(View v) {
        v.startAnimation(Index.buttoneffect);
        if(v.getId() == R.id.ib_search){
            itemadapter1.clear();
            itemadapter2.clear();

            String actionUrl = "DeliveryOrder/alldataneedprint/";
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
                Index.jsonObject.put("user_nomor", Index.globalfunction.getShared("user", "nomor", ""));
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
            Log.d("tesss", result);
            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = jsonarray.length() - 1; i >= 0; i--) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(!obj.has("query")){
                            String nomor = (obj.getString("nomor"));
                            String nama = (obj.getString("nama"));
                            String print = (obj.getString("print"));

                            if(print.equals("0"))
                            {
                                itemadapter1.add(new ItemAdapter(nomor, nama));
                                itemadapter1.notifyDataSetChanged();
                            }
                            else
                            {
                                itemadapter2.add(new ItemAdapter(nomor, nama));
                                itemadapter2.notifyDataSetChanged();
                            }
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

    private class get extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                Index.jsonObject = new JSONObject();
                Index.jsonObject.put("nomor", nomor);
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
                        if(obj.has("success")){
                            String kode = obj.getString("kode");
                            String tanggal = obj.getString("tanggal");
                            String bangunan = obj.getString("bangunan");
                            String project = obj.getString("project");
                            String datalistbarang = obj.getString("detail");

                            hideLoading();

                            Index.globalfunction.setShared("global", "from", "deliveryorder");
                            Index.globalfunction.setShared("global", "print_kode", kode);
                            Index.globalfunction.setShared("global", "print_tanggal", tanggal);
                            Index.globalfunction.setShared("global", "print_bangunan", bangunan);
                            Index.globalfunction.setShared("global", "print_project", project);
                            Index.globalfunction.setShared("global", "print_nomorth", nomor);
                            Index.globalfunction.setShared("global", "print_data", datalistbarang);

                            Fragment fragment = new Sign();
                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_container, fragment);
                            transaction.commit();
                        }
                        else
                        {
                            Toast.makeText(getContext(), "Get Data Failed", Toast.LENGTH_LONG).show();
                        }
//                        if(!obj.has("query")){
//                            String actionUrl = "DeliveryOrder/print/";
//                            new print().execute( actionUrl );
//
//                            createPDF(result, kode);
//
//                            Fragment fragment = new ChoosePrintDelivery();
//                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
//                            transaction.replace(R.id.fragment_container, fragment);
//                            transaction.commit();
//
//                            hideLoading();
//                        }
                    }
                }
            }catch(Exception e)
            {
                e.printStackTrace();
                Toast.makeText(getContext(), "Get Data  Failed", Toast.LENGTH_LONG).show();
                hideLoading();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoading();
        }
    }

    private class print extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                Index.jsonObject = new JSONObject();
                Index.jsonObject.put("nomorth", nomor);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return Index.globalfunction.executePost(urls[0], Index.jsonObject);
        }
    }

    public void createPDF(String data, String kode) throws FileNotFoundException, DocumentException {

        //create document file
        Document doc = new Document();
        try {

            Log.e("PDFCreator", "PDF Path: " + path);

            String namee = "temp.pdf";
            if(!kode.equals(""))
            {
                namee = kode + ".pdf";
            }

            file = new File(dir, namee);
            FileOutputStream fOut = new FileOutputStream(file);
            PdfWriter writer = PdfWriter.getInstance(doc, fOut);

            //open the document
            doc.open();

            try {
                PdfPTable table = new PdfPTable(3);

                float[] columnWidth = new float[]{40, 10, 50};
                table.setWidths(columnWidth);

                Font fheader = new Font(Font.FontFamily.TIMES_ROMAN,10.0f, Font.BOLD, BaseColor.BLACK);
                Font fsubheader = new Font(Font.FontFamily.TIMES_ROMAN,6.0f, Font.BOLD, BaseColor.BLACK);
                Font ffooter = new Font(Font.FontFamily.TIMES_ROMAN,5.0f, Font.BOLD, BaseColor.BLACK);
                Font f = new Font(Font.FontFamily.TIMES_ROMAN,5.0f, Font.NORMAL, BaseColor.BLACK);
                Font fbold = new Font(Font.FontFamily.TIMES_ROMAN,6.0f, Font.BOLDITALIC, BaseColor.BLACK);
                Font f1 = new Font(Font.FontFamily.TIMES_ROMAN,5.0f, Font.NORMAL, BaseColor.RED);

                PdfPTable pTable = new PdfPTable(1);
                pTable.setWidthPercentage(100);
                cell = new PdfPCell(new Phrase("DELIVERY ORDER", fheader));
                cell.setColspan(3);
                cell.setBorder(Rectangle.NO_BORDER);
                table.addCell(cell);

                cell = new PdfPCell(new Phrase("Nama Item", fsubheader));
                cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                table.addCell(cell);

                cell = new PdfPCell(new Phrase("Jumlah Order", fsubheader));
                cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                table.addCell(cell);

                cell = new PdfPCell(new Phrase("Keterangan", fsubheader));
                cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                table.addCell(cell);

                JSONArray jsonarray = new JSONArray(data);
                if(jsonarray.length() > 0) {
                    for (int i = jsonarray.length() - 1; i >= 0; i--) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        String item = (obj.getString("item"));
                        String jumlahorder = (obj.getString("jumlahorder"));
                        String catatan = (obj.getString("catatan"));
                        String satuan = (obj.getString("satuan"));

                        cell = new PdfPCell(new Phrase(item, f));
                        table.addCell(cell);

                        cell = new PdfPCell(new Phrase(GlobalFunction.delimeter(jumlahorder) + " " + satuan, f));
                        table.addCell(cell);

                        cell = new PdfPCell(new Phrase(catatan, f));
                        table.addCell(cell);
                    }
                }

                doc.add(table);
                Toast.makeText(getContext(), "created PDF", Toast.LENGTH_LONG).show();
                sendpdf(file);
            } catch (DocumentException de) {
                Log.e("PDFCreator", "DocumentException:" + de);
            } finally {
                doc.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void displaypdf(File pdf) {

        File file = pdf;
//        file = new File(Environment.getExternalStorageDirectory() + path + "/" + pdf);
        Toast.makeText(getContext(), file.toString() , Toast.LENGTH_LONG).show();
        if(file.exists()) {
            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setDataAndType(Uri.fromFile(file), "application/pdf");
            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            Intent intent = Intent.createChooser(target, "Open File");
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                // Instruct the user to install a PDF reader here, or something
            }
        }
        else
            Toast.makeText(getContext(), "File path is incorrect." , Toast.LENGTH_LONG).show();
    }

    public void sendpdf(File pdf) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("application/pdf");
        share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        startActivity(Intent.createChooser(share, "Share PDF"));
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