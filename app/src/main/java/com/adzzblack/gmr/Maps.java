package com.adzzblack.gmr;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ADI on 4/18/2017.
 */

public class Maps extends Fragment {
    private ItemListAdapter itemadapter;
    private ListView lv_detail;

    protected AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
    private Bitmap bitmap;
    protected View v;
    protected ViewGroup c;
    private Paint mPaint;
    private RelativeLayout rela;
    private int ctrBangunan = -1;
    private int ctrProgress = -1;

    ArrayList<Float> x = new ArrayList<Float>();
    ArrayList<Float> y = new ArrayList<Float>();
    ArrayList<String> name = new ArrayList<String>();
    ArrayList<Float> progress = new ArrayList<Float>();
    ArrayList<String> progressName = new ArrayList<String>();

    ArrayList<ArrayList<Float>> x_ = new ArrayList<ArrayList<Float>>();
    ArrayList<ArrayList<Float>> y_ = new ArrayList<ArrayList<Float>>();
    ArrayList<ArrayList<String>> name_ = new ArrayList<ArrayList<String>>();
    ArrayList<ArrayList<Float>> progress_ = new ArrayList<ArrayList<Float>>();
    ArrayList<ArrayList<String>> progressName_ = new ArrayList<ArrayList<String>>();

    MyView mView;

    int mapWidth = 0;
    int mapHeight = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.main_fragment, container, false);
        getActivity().setTitle("Map");

        itemadapter = new ItemListAdapter(getActivity(), R.layout.list_detailmap, new ArrayList<ItemAdapter>());
        lv_detail = (ListView) v.findViewById(R.id.lv_detail);
        lv_detail.setAdapter(itemadapter);

        rela = (RelativeLayout) v.findViewById(R.id.tes);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE); // the results will be higher than using the activity context object or the getWindowManager() shortcut
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        Log.d("pixel4", screenWidth + ", " + screenHeight);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);

        String actionUrl = "Maps/getMaps/";
        new getMap().execute( actionUrl );

        return v;
    }

    public void setMap(String url)
    {
        ImageView iv = (ImageView) v.findViewById(R.id.imageView);
        Log.d("pixel2", iv.getWidth() + ", " + iv.getHeight());

        try {
            final String urlImage = Index.globalfunction.getServerImageURL() + url;
            Log.d("url", urlImage);

            url = urlImage;

            loadImage(urlImage, iv);

        } catch (Exception e) {
            e.printStackTrace();
        }

        mView = new MyView(getActivity());
        rela.addView(mView);

        final RelativeLayout rela_ = rela;
        final ImageView iv_ = iv;
        final ListView lv_ = lv_detail;
        rela.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rela_.layout(0,0,iv_.getWidth(),iv_.getHeight());

                rela_.setDrawingCacheEnabled(true);
                rela_.buildDrawingCache();
                Bitmap bmp = Bitmap.createBitmap(rela_.getDrawingCache());

                rela_.layout(0,0,iv_.getWidth(),iv_.getHeight() + lv_.getHeight());
                rela_.buildDrawingCache();
                rela_.setDrawingCacheEnabled(false);

                String pathofBmp = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bmp,"temp.png", "drawing");
                Log.d("photooo", pathofBmp);
                Uri bmpUri = Uri.parse(pathofBmp);

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(bmpUri.toString()), "image/*");
                startActivity(intent);
            }
        });
        Log.d("pixel1", rela.getWidth() + ", " + rela.getHeight());
    }

    public class MyView extends View
    {
        private Canvas  mCanvas;
        Context context;
        private Paint   mBitmapPaint;
        private Bitmap mBitmap;

        Paint paint = null;
        public MyView(Context c)
        {
            super(c);
            paint = new Paint();

            context=c;

            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        }


        @Override
        protected void onDraw(Canvas canvas)
        {
            super.onDraw(canvas);

            Paint wallpaintbase = new Paint();
            wallpaintbase.setColor(Color.RED);
            wallpaintbase.setStrokeWidth(5);
            wallpaintbase.setStyle(Paint.Style.STROKE);

            Log.d("ctr", x_.size() + "");
            for(int z=0; z<x_.size();z++) {
                if(ctrBangunan==z || ctrBangunan==-1)
                {
                    x = x_.get(z);
                    y = y_.get(z);

                    Path wallpathbase = new Path();
                    wallpathbase.reset();
                    if(x.size()>=1)
                    {
                        wallpathbase.moveTo(x.get(0), y.get(0));
                        for (int i = 1; i < x.size(); i++) {
                            wallpathbase.lineTo(x.get(i), y.get(i));
                        }
                        canvas.drawPath(wallpathbase, wallpaintbase);
                    }

                    drawPercentage(canvas, Float.parseFloat("100"), x_.get(z), y_.get(z), 0);
                    if(progress_.size()>z)
                    {
                        if(progress_.get(z).size()>z)
                        {
                            Log.d("ppp", progressName_.get(z).size() + " " + z);
                            for(int w=0; w<progress_.get(z).size();w++) {
                                if(ctrProgress==w+1 || ctrProgress==-1)
                                    drawPercentage(canvas, progress_.get(z).get(w), x_.get(z), y_.get(z), w+1);
                            }
                        }
                    }
                }
            }
        }

        public Bitmap getBitmap()
        {
            this.setDrawingCacheEnabled(true);
            this.buildDrawingCache();
            Bitmap bmp = Bitmap.createBitmap(this.getDrawingCache());
            this.setDrawingCacheEnabled(false);
            return bmp;
        }
    }

    public void setInformation()
    {
        String information = "";
        itemadapter.add(new ItemAdapter("-1", "Lihat semua bangunan", "", "", -1));
        itemadapter.notifyDataSetChanged();

        for(int z=0; z<x_.size();z++) {
            for(int w=0; w<name_.get(z).size();w++) {
                information = information + "<b>" + name_.get(z).get(w) + "</b><br />";
                itemadapter.add(new ItemAdapter(String.valueOf(z), name_.get(z).get(w), "", "", -1));
                itemadapter.notifyDataSetChanged();
            }

            if(progress_.size()>z)
            {
                if(progress_.get(z).size()>z)
                {
                    for(int w=0; w<progress_.get(z).size();w++) {
                        information = information + "     " + progressName_.get(z).get(w) + " : " + progress_.get(z).get(w) + "%<br />";
                        itemadapter.add(new ItemAdapter(String.valueOf(z), "", progressName_.get(z).get(w), progress_.get(z).get(w) + "%", (w+1)));
                        itemadapter.notifyDataSetChanged();
                    }
                }
            }
        }
//        tvketerangan.setText(Html.fromHtml(information));
    }

    public void drawPercentage(Canvas canvas, Float percentage, ArrayList<Float> x_, ArrayList<Float> y_, int color)
    {
        Paint wallpaint = new Paint();
        if(color==0) wallpaint.setColor(Color.GRAY);
        else if(color==1) wallpaint.setColor(Color.RED);
        else if(color==2) wallpaint.setColor(Color.rgb(255,165,0));
        else if(color==3) wallpaint.setColor(Color.YELLOW);
        else if(color==4) wallpaint.setColor(Color.GREEN);
        else if(color==5) wallpaint.setColor(Color.BLUE);
        else if(color==6) wallpaint.setColor(Color.MAGENTA);
        else if(color==7) wallpaint.setColor(Color.WHITE);
        else if(color==8) wallpaint.setColor(Color.WHITE);
        wallpaint.setAlpha(150);
        wallpaint.setStyle(Paint.Style.FILL);

//        for(int z=0; z<x_.size();z++)
//        {
            x = x_;
            y = y_;

            Path wallpath = new Path();
            wallpath.reset(); // only needed when reusing this path for a new build

            float yMin = (float) 10000;
            float yMax = (float) 0;
            float xBefore = (float) 0;
            float yBefore = (float) 0;
            for(int i=0;i<x.size();i++)
            {
                if(y.get(i)<yMin) yMin = y.get(i);
                if(y.get(i)>yMax) yMax = y.get(i);
            }
            float diff = yMax-yMin;
            Log.d("ymax", yMax + "");
            Log.d("ymin", yMin + "");
            Log.d("diffbefore", diff + "");
            diff = diff * percentage /100;
            yMax = yMin + diff;
            Log.d("diff", diff + "");

            boolean isStarted = false;
            float xStart = 0;
            float yStart = 0;

            for(int i=0;i<x.size();i++)
            {
                if(y.get(i)>yMax)
                {
                    if(xBefore>0 && yBefore>0)
                    {
                        float m =(y.get(i) - yBefore) / (x.get(i) - xBefore);
                        float xNew = (yMax - yBefore + (m * xBefore)) / m;

                        float tempXMin;
                        float tempXMax;
                        if(x.get(i)<=xBefore)
                        {
                            tempXMin = x.get(i);
                            tempXMax = xBefore;
                        }
                        else
                        {
                            tempXMin = xBefore;
                            tempXMax = x.get(i);
                        }

                        float tempYMin;
                        float tempYMax;
                        if(y.get(i)<=yBefore)
                        {
                            tempYMin = y.get(i);
                            tempYMax = yBefore;
                        }
                        else
                        {
                            tempYMin = yBefore;
                            tempYMax = y.get(i);
                        }

                        if(tempXMin==tempXMax)
                        {
                            if(yMax>=tempYMin && yMax<=tempYMax)
                            {
                                Log.d("xy1", tempXMin + ", " + yMax);
                                if(!isStarted)
                                {
                                    xStart = tempXMin;
                                    yStart = yMax;
                                    isStarted = true;
                                }
                                wallpath.lineTo(tempXMin, yMax);
                            }
                        }
                        else if(xNew>=tempXMin && xNew<=tempXMax)
                        {
                            Log.d("xy4", xNew + ", " + yMax);
                            if(!isStarted)
                            {
                                xStart = xNew;
                                yStart = yMax;
                                isStarted = true;
                            }
                            wallpath.lineTo(xNew, yMax);
                        }
                    }
                }
                else
                {
                    if(yBefore>yMax)
                    {
                        float m =(yBefore - y.get(i)) / (xBefore - x.get(i));
                        float xNew = (yMax - y.get(i) + (m * x.get(i))) / m;

                        float tempXMin;
                        float tempXMax;
                        if(x.get(i)<=xBefore)
                        {
                            tempXMin = x.get(i);
                            tempXMax = xBefore;
                        }
                        else
                        {
                            tempXMin = xBefore;
                            tempXMax = x.get(i);
                        }
                        if(xNew>=tempXMin && xNew<=tempXMax)
                        {
                            Log.d("xy3", xNew + ", " + yMax);
                            wallpath.lineTo(xNew, yMax);
                            if(!isStarted)
                            {
                                xStart = xNew;
                                yStart = yMax;
                                isStarted = true;
                            }
                        }
                    }

                    Log.d("xy2", x.get(i) + ", " + y.get(i));
                    wallpath.lineTo(x.get(i), y.get(i));
                    if(!isStarted)
                    {
                        xStart = x.get(i);
                        yStart = y.get(i);
                        isStarted = true;
                    }
                }

                xBefore = x.get(i);
                yBefore = y.get(i);
            }
            wallpath.lineTo(xStart, yStart);

            canvas.drawPath(wallpath, wallpaint);
//        }
    }

    private void loadImage(final String urlImage, final ImageView image){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    bmOptions.inSampleSize = 1; // 1 = 100% if you write 4 means 1/4 = 25%
                    bitmap = BitmapFactory.decodeStream((InputStream)new URL(urlImage).getContent(),
                            null, bmOptions);
                    mapWidth = bitmap.getWidth();
                    mapHeight = bitmap.getHeight();

                } catch (Exception e) {
                    // log error
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (bitmap != null)
                    image.setImageBitmap(bitmap);
                    mapWidth = image.getWidth();
                    mapHeight = image.getHeight();
            }
        }.execute();
    }

    private class getMap extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                Index.jsonObject = new JSONObject();
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
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(!obj.has("error")){
                            String name1 = obj.getString("name");
                            String koordinat = obj.getString("koordinat");
                            String progress1 = obj.getString("progressbangunan");
                            String progressnama1 = obj.getString("progressnama");
                            String denah = obj.getString("denah");
                            String parent = obj.getString("isparent");
                            String childname = obj.getString("childName");
                            String childkoordinat = obj.getString("childkoordinat");
                            String childprogress = obj.getString("childprogressbangunan");
                            String childprogressnama = obj.getString("childprogressnama");

                            if(childkoordinat.equals("0"))
                            {
                                String[] pieces = koordinat.trim().split("\\|");
                                x = new ArrayList<Float>();
                                y = new ArrayList<Float>();
                                for(int j=0 ; j < pieces.length ; j++) {
                                    String string = pieces[j];
                                    String[] parts = string.trim().split("\\,");

                                    Float newX = Float.parseFloat(parts[0]) * 1080 / 1090;
                                    Float newY = Float.parseFloat(parts[1]) * 602 / 550;

                                    x.add(newX);
                                    y.add(newY);
                                }
                                x_.add(x);
                                y_.add(y);

                                if(!name1.equals(""))
                                {
                                    String[] pieces0 = name1.trim().split("\\|");
                                    name = new ArrayList<String>();
                                    for(int j=0 ; j < pieces0.length ; j++) {
                                        String string = pieces0[j];

                                        name.add(string);
                                    }
                                    name_.add(name);
                                }

                                if(!progress1.equals(""))
                                {
                                    String[] pieces1 = progress1.trim().split("\\|");
                                    progress = new ArrayList<Float>();
                                    for(int j=0 ; j < pieces1.length ; j++) {
                                        String string = pieces1[j];

                                        Float newProgress = Float.parseFloat(string);

                                        progress.add(newProgress);
                                    }
                                    progress_.add(progress);
                                }

                                if(!progressnama1.equals(""))
                                {
                                    String[] pieces2 = progressnama1.trim().split("\\|");
                                    progressName = new ArrayList<String>();
                                    for(int j=0 ; j < pieces2.length ; j++) {
                                        String string = pieces2[j];

                                        progressName.add(string);
                                    }
                                    progressName_.add(progressName);
                                }
                            }
                            else
                            {
                                String[] pieces_ = childkoordinat.trim().split("\\@");
                                for(int k=0 ; k < pieces_.length ; k++) {
                                    String[] pieces = pieces_[k].trim().split("\\|");
                                    x = new ArrayList<Float>();
                                    y = new ArrayList<Float>();

                                    for(int j=0 ; j < pieces.length ; j++) {
                                        String string = pieces[j];
                                        Log.d("pecah2", string);
                                        if(!string.equals(""))
                                        {
                                            String[] parts = string.trim().split("\\,");

                                            Float newX = Float.parseFloat(parts[0]) * 1080 / 1090;
                                            Float newY = Float.parseFloat(parts[1]) * 602 / 550;

                                            x.add(newX);
                                            y.add(newY);
                                        }
                                    }
                                    x_.add(x);
                                    y_.add(y);
                                }

                                String[] pieces0_ = childname.trim().split("\\@");
                                for(int k=0 ; k < pieces0_.length ; k++) {
                                    String[] pieces = pieces0_[k].trim().split("\\|");
                                    name = new ArrayList<String>();

                                    for(int j=0 ; j < pieces.length ; j++) {
                                        String string = pieces[j];
                                        if(!string.equals(""))
                                        {
                                            String newName = string;
                                            name.add(newName);
                                        }
                                    }
                                    name_.add(name);
                                }

                                String[] pieces1_ = childprogress.trim().split("\\@");
                                for(int k=0 ; k < pieces1_.length ; k++) {
                                    String[] pieces = pieces1_[k].trim().split("\\|");
                                    progress = new ArrayList<Float>();

                                    for(int j=0 ; j < pieces.length ; j++) {
                                        String string = pieces[j];
                                        if(!string.equals(""))
                                        {
                                            Float newProgress = Float.parseFloat(string);
                                            progress.add(newProgress);
                                        }
                                    }
                                    progress_.add(progress);
                                }

                                String[] pieces2_ = childprogressnama.trim().split("\\@");
                                for(int k=0 ; k < pieces2_.length ; k++) {
                                    String[] pieces = pieces2_[k].trim().split("\\|");
                                    progressName = new ArrayList<String>();

                                    for(int j=0 ; j < pieces.length ; j++) {
                                        String string = pieces[j];
                                        if(!string.equals(""))
                                        {
                                            String newProgressName = string;
                                            progressName.add(newProgressName);
                                        }
                                    }
                                    progressName_.add(progressName);
                                }
                            }

                            setMap(denah);
                            setInformation();
                        }
                        else
                        {
                            Toast.makeText(getContext(), "Get Data Failed", Toast.LENGTH_LONG).show();
                        }
                    }
                }

                hideLoading();
            }catch(Exception e)
            {
                e.printStackTrace();
                Toast.makeText(getContext(), "Get Data Failed", Toast.LENGTH_LONG).show();
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

    class ItemAdapter {

        private String nomor;
        private String namabangunan;
        private String namaprogress;
        private String progress;
        private int color;


        public ItemAdapter(String nomor, String namabangunan, String namaprogress, String progress, int color) {
            this.setNomor(nomor);
            this.setNamabangunan(namabangunan);
            this.setNamaprogress(namaprogress);
            this.setProgress(progress);
            this.setColor(color);
        }

        public String getNomor() {
            return nomor;
        }

        public void setNomor(String param) {this.nomor = param; }

        public String getNamaprogress() {
            return namaprogress;
        }

        public void setNamaprogress(String param) {this.namaprogress = param; }

        public String getNamabangunan() {
            return namabangunan;
        }

        public void setNamabangunan(String param) {this.namabangunan = param; }

        public String getProgress() {
            return progress;
        }

        public void setProgress(String param) {this.progress = param; }

        public int getColor() {
            return color;
        }

        public void setColor(int param) {this.color = param; }
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
            TextView tv_namabangunan;
            TextView tv_namaprogress;
            TextView tv_progress;
            TextView tv_color;
            LinearLayout ll_bangunan;
            LinearLayout ll_progress;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View row = convertView;
            Holder holder = null;

            if(row==null)
            {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);
            }

            holder = new Holder();
            holder.adapterItem = items.get(position);

            holder.tv_namabangunan = (TextView)row.findViewById(R.id.tv_namabangunan);
            holder.tv_namaprogress = (TextView)row.findViewById(R.id.tv_namaprogress);
            holder.tv_progress = (TextView)row.findViewById(R.id.tv_progress);
            holder.tv_color = (TextView)row.findViewById(R.id.tv_color);
            holder.ll_bangunan = (LinearLayout) row.findViewById(R.id.ll_bangunan);
            holder.ll_progress = (LinearLayout) row.findViewById(R.id.ll_progress);

            row.setTag(holder);
            setupItem(holder);

            final Holder finalHolder = holder;
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ctrBangunan = Integer.parseInt(finalHolder.adapterItem.getNomor());
                    ctrProgress = finalHolder.adapterItem.getColor();
                    rela.removeView(mView);
                    mView = new MyView(getActivity());
                    rela.addView(mView);
                    lv_detail.setSelection(position);
                    lv_detail.setItemChecked(position, true);
                    itemadapter.notifyDataSetChanged();
                }
            });

            return row;
        }

        private void setupItem(Holder holder) {
            if(holder.adapterItem.getNamabangunan().equals(""))
            {
                holder.ll_bangunan.setVisibility(View.GONE);
                holder.ll_progress.setVisibility(View.VISIBLE);
                holder.tv_namaprogress.setText(holder.adapterItem.getNamaprogress());
                holder.tv_progress.setText(holder.adapterItem.getProgress());

                if(holder.adapterItem.getColor()==0) holder.tv_color.setBackgroundColor(Color.GRAY);
                else if(holder.adapterItem.getColor()==1) holder.tv_color.setBackgroundColor(Color.RED);
                else if(holder.adapterItem.getColor()==2) holder.tv_color.setBackgroundColor(Color.rgb(255,165,0));
                else if(holder.adapterItem.getColor()==3) holder.tv_color.setBackgroundColor(Color.YELLOW);
                else if(holder.adapterItem.getColor()==4) holder.tv_color.setBackgroundColor(Color.GREEN);
                else if(holder.adapterItem.getColor()==5) holder.tv_color.setBackgroundColor(Color.BLUE);
                else if(holder.adapterItem.getColor()==6) holder.tv_color.setBackgroundColor(Color.MAGENTA);
                else if(holder.adapterItem.getColor()==7) holder.tv_color.setBackgroundColor(Color.WHITE);
                else if(holder.adapterItem.getColor()==8) holder.tv_color.setBackgroundColor(Color.WHITE);
            }
            else
            {
                holder.ll_bangunan.setVisibility(View.VISIBLE);
                holder.ll_progress.setVisibility(View.GONE);
                holder.tv_namabangunan.setText(holder.adapterItem.getNamabangunan());
            }
        }
    }
}
