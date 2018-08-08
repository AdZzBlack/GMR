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
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableRow;
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


public class DeliveryOrder extends Fragment implements View.OnClickListener {

    private TableRow tr_keterangan;
    private TextView tv_item, tv_waste, tv_need, tv_delivery, tv_satuan1, tv_satuan2, tv_satuan3, tv_keterangan;
    private EditText et_order;
    private Button btn_order, btn_orderall;

    private String nomor, nama, waste, need, delivery, satuan, harga, keterangan;

    private ItemListAdapter griditemadapter;
    private ListView lv_item;

    private String m_Text = "";
    private Boolean disetujui = true;

    private PdfPCell cell;
    private Image bgImage;
    private String path;
    private File dir;
    private File file;

    private String nomorth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.deliveryorder, container, false);
        getActivity().setTitle("Delivery Order");

        griditemadapter = new ItemListAdapter(getActivity(), R.layout.list_order, new ArrayList<ItemAdapter>());
        lv_item = (ListView) v.findViewById(R.id.lv_item);
        lv_item.setAdapter(griditemadapter);


        tv_item = (TextView) v.findViewById(R.id.tv_item);
        tv_waste = (TextView) v.findViewById(R.id.tv_waste);
        tv_need = (TextView) v.findViewById(R.id.tv_need);
        tv_delivery = (TextView) v.findViewById(R.id.tv_ordered);
        tv_satuan1 = (TextView) v.findViewById(R.id.tv_satuan1);
        tv_satuan2 = (TextView) v.findViewById(R.id.tv_satuan2);
        tv_satuan3 = (TextView) v.findViewById(R.id.tv_satuan3);
        tv_keterangan = (TextView) v.findViewById(R.id.tv_keterangan);

        tr_keterangan = (TableRow) v.findViewById(R.id.tr_keterangan);

        et_order = (EditText) v.findViewById(R.id.et_order);
        btn_order = (Button) v.findViewById(R.id.btn_order);
        btn_orderall = (Button) v.findViewById(R.id.btn_orderall);

        et_order.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

                return false;
            }
        });

        et_order.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

                return false;
            }
        });

//        lv_item.setLongClickable(true);
        lv_item.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                // TODO Auto-generated method stub
                v.startAnimation(Index.listeffect);
                alertbox("Item Detail", "Delete Item Data ?", getActivity(), position, 1);
            }
        });
        lv_item.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                alertbox("Item Detail", "Delete Item Data ?", getActivity(), pos, 1);
                return true;
            }
        });

        nomor = Index.globalfunction.getShared("rab", "nomorbarang", "");
        nama = Index.globalfunction.getShared("rab", "namabarang", "");
        waste = Index.globalfunction.getShared("rab", "waste", "0");
        need = Index.globalfunction.getShared("rab", "jumlah", "");
        delivery = Index.globalfunction.getShared("rab", "delivery", "");
        satuan = Index.globalfunction.getShared("rab", "satuan", "");
        harga = Index.globalfunction.getShared("rab", "harga", "");
        keterangan = Index.globalfunction.getShared("rab", "keterangan", "");

        Float needFloat = Float.parseFloat(Index.globalfunction.getShared("rab", "jumlah", "0"));
        Float wasteFloat = Float.parseFloat(Index.globalfunction.getShared("rab", "waste", "0")) * needFloat / 100;

        tv_item.setText(nama);
        if(need.equals("")) tv_need.setText(need);
        else tv_need.setText(GlobalFunction.delimeter(need));
        if(delivery.equals("")) tv_delivery.setText(delivery);
        else tv_delivery.setText(GlobalFunction.delimeter(delivery));
        tv_satuan1.setText(satuan);
        tv_satuan2.setText(satuan);
        tv_satuan3.setText(satuan);
        tv_keterangan.setText(keterangan);
        if(keterangan.equals("")) tr_keterangan.setVisibility(View.GONE);
        tv_waste.setText(waste + "% (" + GlobalFunction.delimeter(String.valueOf(wasteFloat)) + " " + satuan + ")");

        btn_order.setOnClickListener(this);
        btn_orderall.setOnClickListener(this);
        tv_item.setOnClickListener(this);

        refreshGrid();

        //creating new file path
        path = Environment.getExternalStorageDirectory().getAbsolutePath() + Index.globalfunction.pdf_folder;
        dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return v;
    }
	
    @Override
    public void onResume(){
        super.onResume();

    }

    public void onClick(View v) {
        v.startAnimation(Index.buttoneffect);
        if(v.getId() == R.id.tv_item){

            Fragment fragment = new ChooseRABDetail();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
        else if(v.getId() == R.id.btn_order){
            if(!et_order.getText().toString().equals("") && !Index.globalfunction.getShared("rab", "namabarang", "").equals(""))
            {
                Float needFloat = Float.parseFloat(Index.globalfunction.getShared("rab", "jumlah", "0"));
                Float orderFloat = Float.parseFloat(Index.globalfunction.getShared("rab", "delivery", "0")) + Float.parseFloat(et_order.getText().toString());
                Float wasteFloat = Float.parseFloat(Index.globalfunction.getShared("rab", "waste", "0")) * needFloat / 100;

                if(orderFloat<=needFloat+wasteFloat)
                {
                    addGrid(1);
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Need Approval");
                    builder.setMessage("Order quantity exceed, are you sure want to order? this order need Owner approval, give note to Owner");

                    // Set up the input
                    final EditText input = new EditText(getContext());
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);

                    // Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            m_Text = input.getText().toString();
                            disetujui = false;
                            addGrid(0);
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
            }
        }
        else if(v.getId() == R.id.btn_orderall){
            if(!Index.globalfunction.getShared("rab", "listorder", "").equals(""))
            {
                String actionUrl = "DeliveryOrder/createDeliveryOrder/";
                new createDO().execute( actionUrl );
            }
            else
            {
                Toast.makeText(getContext(), "Delivery order can't be empty", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void addGrid(int disetujui){
        String rabdetail = Index.globalfunction.getShared("rab", "nomor", "");
        String nomorbarang = Index.globalfunction.getShared("rab", "nomorbarang", "");
        String namabarang = Index.globalfunction.getShared("rab", "namabarang", "");
        String jumlah = et_order.getText().toString();
        String keterangan = GlobalFunction.delimeter(et_order.getText().toString()) + " " + Index.globalfunction.getShared("rab", "satuan", "");
        String catatan = "";

        if(m_Text.equals("")) catatan = "0";
        else catatan = m_Text;

        String oldData = Index.globalfunction.getShared("rab", "listorder", "");

        Boolean same = false;

        if(!oldData.equals("")){
            String[] pieces = oldData.trim().split("\\|");
            for(int i=0 ; i < pieces.length ; i++){
                String string = pieces[i];
                String[] parts = string.trim().split("\\~");

                if(nomorbarang.equals(parts[0]))
                {
                    same = true;
                }
            }
        }

        if(same)
        {
            Toast.makeText(getContext(), "Item already ordered", Toast.LENGTH_LONG).show();
        }
        else
        {
            Index.globalfunction.setShared("rab", "listorder", oldData + nomorbarang + "~" + namabarang + "~" + keterangan + "~" + jumlah + "~" + harga + "~" + disetujui + "~" + rabdetail + "~" + catatan + "|");

            griditemadapter.add(new ItemAdapter(namabarang, keterangan));
            griditemadapter.notifyDataSetChanged();

            Index.globalfunction.setShared("rab", "nomor", "");
            Index.globalfunction.setShared("rab", "nomorbarang", "");
            Index.globalfunction.setShared("rab", "namabarang", "");
            Index.globalfunction.setShared("rab", "satuan", "");
            Index.globalfunction.setShared("rab", "jumlah", "");
            Index.globalfunction.setShared("rab", "harga", "");
            Index.globalfunction.setShared("rab", "delivery", "");
            Index.globalfunction.setShared("rab", "waste", "");

            et_order.setText("");
            tv_item.setText("");
            tv_need.setText("");
            tv_delivery.setText("");
            tv_satuan1.setText("");
            tv_satuan2.setText("");
            tv_satuan3.setText("");
        }
    }

    public void refreshGrid(){
        griditemadapter.clear();
        disetujui = true;
        String datalistbarang = Index.globalfunction.getShared("rab", "listorder", "");
        if(!datalistbarang.equals("")){
            String[] pieces = datalistbarang.trim().split("\\|");
            for(int i=0 ; i < pieces.length ; i++){
                String string = pieces[i];
                String[] parts = string.trim().split("\\~");

                if(parts[5].equals("0")) disetujui = false;

                griditemadapter.add(new ItemAdapter(parts[1], parts[2]));
                griditemadapter.notifyDataSetChanged();
            }
        }
    }

    public void deleteGrid(final int pos){
        String realData = "";
        String datalistbarang = Index.globalfunction.getShared("rab", "listorder", "");
        if(!datalistbarang.equals("")){
            String[] pieces = datalistbarang.trim().split("\\|");
            for(int i=0 ; i < pieces.length ; i++){
                if(i != pos){
                    realData += pieces[i] + "|";
                }
            }
            Index.globalfunction.setShared("rab", "listorder", realData);

            refreshGrid();
        }
    }

    public void alertbox(String title, String message, final Activity activity, final int pos, final int type) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if(type==1) deleteGrid(pos);
                else if(type==2) addGrid(0);
            } });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            } });
        alertDialog.show();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public void createPDF(String data, String kode) throws FileNotFoundException, DocumentException {

        //create document file
        Document doc = new Document();
        try {

            Log.e("PDFCreator", "PDF Path: " + path);

            String namee = "temp.pdf";
            if(!kode.equals(""))
            {
                namee = kode.replace("/","") + ".pdf";
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

                if(!data.equals("")){
                    String[] pieces = data.trim().split("\\|");
                    for(int i=0 ; i < pieces.length ; i++){
                        String string = pieces[i];
                        String[] parts = string.trim().split("\\~");

                        cell = new PdfPCell(new Phrase(parts[1], f));
                        table.addCell(cell);

                        cell = new PdfPCell(new Phrase(parts[2], f));
                        table.addCell(cell);

                        String keterangan = parts[7];
                        if(keterangan.equals("0")) keterangan = " ";

                        cell = new PdfPCell(new Phrase(keterangan, f));
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

    private class createDO extends AsyncTask<String, Void, String> {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdf.format(new Date());

        @Override
        protected String doInBackground(String... urls) {
            try {
                Index.jsonObject = new JSONObject();
                Index.jsonObject.put("userNomor", Index.globalfunction.getShared("user", "nomor", ""));
                Index.jsonObject.put("tanggal", date);
                Index.jsonObject.put("nomor_bangunan", Index.globalfunction.getShared("bangunan", "nomorNow", "0"));
                Index.jsonObject.put("dataDO", Index.globalfunction.getShared("rab", "listorder", ""));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return Index.globalfunction.executePost(urls[0], Index.jsonObject);
        }

        @Override
        protected void onPostExecute(String result)
        {
            Log.d("result", result);
            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(obj.has("success")){
                            String kode = obj.getString("kode");
                            String tanggal = obj.getString("tanggal");
                            String bangunan = obj.getString("bangunan");
                            String project = obj.getString("project");

                            hideLoading();
                            Toast.makeText(getContext(), "Create Delivery Order Success", Toast.LENGTH_LONG).show();

                            if(disetujui)
                            {
                                nomorth = obj.getString("nomor");
                                String datalistbarang = Index.globalfunction.getShared("rab", "listorder", "");

                                Index.globalfunction.setShared("global", "from", "deliveryorder");
                                Index.globalfunction.setShared("global", "print_kode", kode);
                                Index.globalfunction.setShared("global", "print_tanggal", tanggal);
                                Index.globalfunction.setShared("global", "print_bangunan", bangunan);
                                Index.globalfunction.setShared("global", "print_project", project);
                                Index.globalfunction.setShared("global", "print_nomorth", nomorth);
                                Index.globalfunction.setShared("global", "print_data", datalistbarang);

                                Fragment fragment = new Sign();
                                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                transaction.replace(R.id.fragment_container, fragment);
                                transaction.commit();
                            }
                            else
                            {
                                Index.fm.popBackStack(Index.fm.getBackStackEntryCount()-2, 0);
                            }
                        }
                        else
                        {
                            Toast.makeText(getContext(), "Create Delivery Order Failed", Toast.LENGTH_LONG).show();
                        }
                    }
                }

                hideLoading();
            }catch(Exception e)
            {
                e.printStackTrace();
                Toast.makeText(getContext(), "Create Delivery Order Failed", Toast.LENGTH_LONG).show();
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
                Index.jsonObject.put("nomorth", nomorth);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return Index.globalfunction.executePost(urls[0], Index.jsonObject);
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

    class ItemAdapter {
        private String namabarang;
        private String keterangan;


        public ItemAdapter(String namabarang, String keterangan) {
            this.setNamabarang(namabarang);
            this.setKeterangan(keterangan);
        }

        public String getNamaBarang() {
            return namabarang;
        }

        public void setNamabarang(String param) {
            this.namabarang = param;
        }

        public String getKeterangan() {
            return keterangan;
        }

        public void setKeterangan(String param) {
            this.keterangan = param;
        }
    }

    class ItemListAdapter extends ArrayAdapter<ItemAdapter> {

        private List<ItemAdapter> items;
        private int layoutResourceId;
        private Context context;


        public ItemListAdapter(Context context, int layoutResourceId, List<ItemAdapter> items) {
            super(context, layoutResourceId, items);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.items = items;
        }

        public class Holder {
            ItemAdapter adapterItem;
            TextView nama;
            TextView keterangan;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            Holder holder = null;

            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new Holder();
            holder.adapterItem = items.get(position);

            holder.nama = (TextView)row.findViewById(R.id.tv_nama);
            holder.keterangan = (TextView)row.findViewById(R.id.tv_keterangan);

            row.setTag(holder);
            setupItem(holder);

            final Holder finalHolder = holder;
//            row.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                }
//            });

            return row;
        }

        private void setupItem(Holder holder) {
            holder.nama.setText(holder.adapterItem.getNamaBarang());
            holder.keterangan.setText(holder.adapterItem.getKeterangan());
        }
    }
}