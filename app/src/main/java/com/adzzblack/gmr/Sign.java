package com.adzzblack.gmr;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ADI on 4/18/2017.
 */

public class Sign extends Fragment {
    protected View v;
    protected ViewGroup c;
    private Paint mPaint;
    private Button btn_clear, btn_back, btn_next;
    private ImageView iv;
    private MyView mView;

    private EditText et_nama;

    private PdfPCell cell;
    private Image bgImage;
    private String path;
    private File dir;
    private File file;

    private String signname;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.sign, container, false);
        getActivity().setTitle("Sign");

        final RelativeLayout rela = (RelativeLayout) v.findViewById(R.id.rl_sign);
        mView = new MyView(getActivity());
        rela.addView(mView);

        et_nama = (EditText) v.findViewById(R.id.et_name);

        btn_clear = (Button) v.findViewById(R.id.btn_clear);
        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mView.clear();
            }
        });

        btn_back = (Button) v.findViewById(R.id.btn_back);
        btn_next = (Button) v.findViewById(R.id.btn_next);

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Index.fm.popBackStack();
            }
        });

        if(Index.globalfunction.getShared("global", "from", "").equals("formBPM"))
        {
            signname = Index.globalfunction.getShared("user", "nama", "");
            et_nama.setText(signname);
            et_nama.setEnabled(false);
            btn_next.setText("Done");
            btn_next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String actionUrl = "BPM/createBPM/";
                    new createBPM().execute( actionUrl );
                }
            });
        }
        else if(Index.globalfunction.getShared("global", "from", "").equals("deliveryorder"))
        {
            signname = Index.globalfunction.getShared("user", "nama", "");
            et_nama.setText(signname);
            et_nama.setEnabled(false);
            btn_next.setText("Create PDF");
            btn_next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(et_nama.getText().toString().equals(""))
                    {
                        Toast.makeText(getContext(), "Name required", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        String actionUrl = "DeliveryOrder/print/";
                        new printDeliveryOrder().execute( actionUrl );

                        try {
                            createPDF();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (DocumentException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);

        //creating new file path
        path = Environment.getExternalStorageDirectory().getAbsolutePath() + Index.globalfunction.pdf_folder;
        dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return v;
    }

    private void saveBitmap() {
        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/temp.jpg";

            Bitmap bitmap = mView.mBitmap;

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            e.printStackTrace();
        }
    }

    private void takeScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            // create bitmap screen capture
            View v1 = getActivity().getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            e.printStackTrace();
        }
    }

    private void openScreenshot(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }

    public void createPDF() throws FileNotFoundException, DocumentException {
        String kode = Index.globalfunction.getShared("global", "print_kode", "");
        String tanggal = Index.globalfunction.getShared("global", "print_tanggal", "");
        String bangunan = Index.globalfunction.getShared("global", "print_bangunan", "");
        String project = Index.globalfunction.getShared("global", "print_project", "");
        String data = Index.globalfunction.getShared("global", "print_data", "");

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

                cell = new PdfPCell(new Phrase("DELIVERY ORDER " + kode, fheader));
                cell.setColspan(3);
                cell.setBorder(Rectangle.NO_BORDER);
                table.addCell(cell);

                PdfPTable pt = new PdfPTable(3);
                pt.setWidthPercentage(100);
                float[] fl = new float[]{10, 5, 85};
                pt.setWidths(fl);

                cell = new PdfPCell(new Phrase("Tanggal", fsubheader));
                cell.setBorder(Rectangle.NO_BORDER);
                pt.addCell(cell);

                cell = new PdfPCell(new Phrase(":", fsubheader));
                cell.setBorder(Rectangle.NO_BORDER);
                pt.addCell(cell);

                cell = new PdfPCell(new Phrase(tanggal, f));
                cell.setBorder(Rectangle.NO_BORDER);
                pt.addCell(cell);

                cell = new PdfPCell(new Phrase("Proyek", fsubheader));
                cell.setBorder(Rectangle.NO_BORDER);
                pt.addCell(cell);

                cell = new PdfPCell(new Phrase(":", fsubheader));
                cell.setBorder(Rectangle.NO_BORDER);
                pt.addCell(cell);

                cell = new PdfPCell(new Phrase(project, f));
                cell.setBorder(Rectangle.NO_BORDER);
                pt.addCell(cell);

                cell = new PdfPCell(new Phrase("Ruangan", fsubheader));
                cell.setBorder(Rectangle.NO_BORDER);
                pt.addCell(cell);

                cell = new PdfPCell(new Phrase(":", fsubheader));
                cell.setBorder(Rectangle.NO_BORDER);
                pt.addCell(cell);

                cell = new PdfPCell(new Phrase(bangunan, f));
                cell.setBorder(Rectangle.NO_BORDER);
                pt.addCell(cell);

                pTable = new PdfPTable(1);
                pTable.setWidthPercentage(100);
                cell = new PdfPCell();
                cell.setColspan(1);
                cell.addElement(pt);
                cell.setBorder(Rectangle.NO_BORDER);
                pTable.addCell(cell);

                cell = new PdfPCell(new Phrase(" ", f));
                cell.setColspan(3);
                cell.setBorder(Rectangle.NO_BORDER);
                table.addCell(cell);

                cell = new PdfPCell();
                cell.setColspan(3);
                cell.addElement(pTable);
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
                PdfPTable pt1 = new PdfPTable(2);
                pt1.setWidthPercentage(100);
                float[] fl1 = new float[]{80, 20};
                pt1.setWidths(fl1);

                cell = new PdfPCell();
                cell.setBorder(Rectangle.NO_BORDER);
                pt1.addCell(cell);

                cell = new PdfPCell(new Phrase("Hormat Saya,", f));
                cell.setBorder(Rectangle.NO_BORDER);
                pt1.addCell(cell);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                mView.mBitmap.compress(Bitmap.CompressFormat.PNG, 100 , stream);

                Image myImg = Image.getInstance(stream.toByteArray());
                bgImage = Image.getInstance(myImg);
                bgImage.setAbsolutePosition(330f, 642f);

                cell = new PdfPCell();
                cell.setBorder(Rectangle.NO_BORDER);
                pt1.addCell(cell);

                cell = new PdfPCell();
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                cell.addElement(bgImage);
                pt1.addCell(cell);

                cell = new PdfPCell();
                cell.setBorder(Rectangle.NO_BORDER);
                pt1.addCell(cell);

                cell = new PdfPCell(new Phrase(signname, fheader));
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                pt1.addCell(cell);

                pTable = new PdfPTable(1);
                pTable.setWidthPercentage(100);
                cell = new PdfPCell();
                cell.setColspan(1);
                cell.addElement(pt1);
                cell.setBorder(Rectangle.NO_BORDER);
                pTable.addCell(cell);

                cell = new PdfPCell();
                cell.setColspan(3);
                cell.addElement(pTable);
                cell.setBorder(Rectangle.NO_BORDER);
                table.addCell(cell);

                doc.add(table);

                Toast.makeText(getContext(), "created PDF", Toast.LENGTH_LONG).show();
                sendpdf(file);
            } catch (DocumentException de) {
                Log.e("PDFCreator", "DocumentException:" + de);
            } finally {
                doc.close();

                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.popBackStack(0, FragmentManager.POP_BACK_STACK_INCLUSIVE);
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

    public class MyView extends View
    {
        private Path    mPath;
        private Paint circlePaint;
        private Path circlePath;
        private Canvas  mCanvas;
        Context context;
        private Paint   mBitmapPaint;
        private Bitmap mBitmap;
        int width,height;

        public MyView(Context c)
        {
            super(c);

            context=c;
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            circlePaint = new Paint();
            circlePath = new Path();
            circlePaint.setAntiAlias(true);
            circlePaint.setColor(Color.BLACK);
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setStrokeJoin(Paint.Join.MITER);
            circlePaint.setStrokeWidth(4f);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            width = w;
            height = h;
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            super.onDraw(canvas);

            canvas.drawBitmap( mBitmap, 0, 0, mBitmapPaint);
            canvas.drawPath( mPath,  mPaint);
            canvas.drawPath( circlePath,  circlePaint);
        }

        public void clear()
        {
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            mPath = new Path();
            invalidate();
        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }

        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                mX = x;
                mY = y;

                circlePath.reset();
                circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
            }
        }

        private void touch_up() {
            mPath.lineTo(mX, mY);
            circlePath.reset();
            // commit the path to our offscreen
            mCanvas.drawPath(mPath,  mPaint);
            // kill this so we don't double draw
            mPath.reset();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }
    }

    private class printDeliveryOrder extends AsyncTask<String, Void, String> {
        String nomorth = Index.globalfunction.getShared("global", "print_nomorth", "");

        @Override
        protected String doInBackground(String... urls) {
            try {
                Index.jsonObject = new JSONObject();
                Index.jsonObject.put("nomorth", nomorth);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Index.globalfunction.setShared("global", "print_nomorth", "");
            return Index.globalfunction.executePost(urls[0], Index.jsonObject);
        }
    }

    private class createBPM extends AsyncTask<String, Void, String> {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdf.format(new Date());

        @Override
        protected String doInBackground(String... urls) {
            try {
                Index.jsonObject = new JSONObject();
                Index.jsonObject.put("nomor_user", Index.globalfunction.getShared("user", "nomor", ""));
                Index.jsonObject.put("tanggal", date);
                Index.jsonObject.put("nomor_thDO", Index.globalfunction.getShared("global", "thdeliveryordernow", "0"));
                Index.jsonObject.put("dataBPM", Index.globalfunction.getShared("global", "detailbpmbaru", ""));
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
                            hideLoading();
                            Toast.makeText(getContext(), "Create BPM Success", Toast.LENGTH_LONG).show();
                            Index.fm.popBackStack(Index.fm.getBackStackEntryCount()-3, 0);
                        }
                        else
                        {
                            Toast.makeText(getContext(), "Create BPM Failed", Toast.LENGTH_LONG).show();
                        }
                    }
                }

                hideLoading();
            }catch(Exception e)
            {
                e.printStackTrace();
                Toast.makeText(getContext(), "Create BPM Failed", Toast.LENGTH_LONG).show();
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
