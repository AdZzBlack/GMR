package com.adzzblack.gmr;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Galery extends Fragment implements View.OnClickListener{
    private GridView imageGrid;
    private ImageListAdapter imageadapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.galery, container, false);
        getActivity().setTitle("Photo");

        //-----START DECLARE---------------------------------------------------------------------------------------
        // It have to be matched with the directory in SDCard
        imageGrid = (GridView) v.findViewById(R.id.gridview);
        imageadapter = new ImageListAdapter(getActivity(), R.layout.list_image, new ArrayList<ImageAdapter>());

        imageGrid.setAdapter(imageadapter);

        String actionUrl = "BeritaAcara/getPasang/";
        new search().execute( actionUrl );

        return v;
    }

    private void loadImage(final String urlImage, final String photo, final String nameImage, final int ctr) {
        final Bitmap[] bitmap = new Bitmap[1];
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    bmOptions.inSampleSize = 1; // 1 = 100% if you write 4 means 1/4 = 25%
                    bitmap[0] = BitmapFactory.decodeStream((InputStream)new URL(urlImage).getContent(),
                            null, bmOptions);
                } catch (Exception e) {
                    // log error
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (bitmap[0] != null)
                {
                    int s = bitmap[0].getWidth();
                    if(bitmap[0].getWidth()>bitmap[0].getHeight())
                    {
                        s= bitmap[0].getHeight();
                    }

                    Bitmap dst = Bitmap.createBitmap(bitmap[0], 0, 0, s, s);
                    imageadapter.add(new ImageAdapter(dst, urlImage, nameImage));
                    imageadapter.notifyDataSetChanged();
                    ImageStorage.saveToSdCard(bitmap[0], photo);

                    if(ctr==0)
                    {
                        hideLoading();
                    }
//                    showImage(bitmap[0],photo,keterangan,ctr,urlImage);
                }
                else
                {
                    Log.d("tes1", urlImage);
                    if(ctr==0)
                    {
                        hideLoading();
                    }
                }
            }
        }.execute();
    }

    public void onClick(View v) {
        v.startAnimation(Index.buttoneffect);
    }

    @Override
    public void onResume() {
        super.onResume();
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

    private class search extends AsyncTask<String, Void, String> {
        String nomor_bangunan = Index.globalfunction.getShared("bangunan", "nomorNow", "");
        @Override
        protected String doInBackground(String... urls) {
            try {
                Index.jsonObject = new JSONObject();
                Index.jsonObject.put("nomor_bangunan", nomor_bangunan);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return Index.globalfunction.executePost(urls[0], Index.jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("photo", result + "a");
            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    showLoading();
                    for (int i = jsonarray.length() - 1; i >= 0; i--) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(!obj.has("query")){
                            String photo = (obj.getString("photo"));
                            final String urlImage = Index.globalfunction.getImageURL() + photo;
                            final String nameImage = photo;

                            if(ImageStorage.checkifImageExists(photo))
                            {
                                File file = ImageStorage.getImage("/"+photo);
                                String path = file.getAbsolutePath();
                                if (path != null){
                                    Bitmap b = BitmapFactory.decodeFile(path);
                                    int s = b.getWidth();
                                    if(b.getWidth()>b.getHeight())
                                    {
                                        s= b.getHeight();
                                    }
                                    Bitmap dst = Bitmap.createBitmap(b, 0, 0, s, s);
                                    imageadapter.add(new ImageAdapter(dst, urlImage, nameImage));
                                    if(i==0)
                                    {
                                        hideLoading();
                                    }
//                                    showImage(b, photo, keterangan, i, urlImage);
                                }
                            }
                            else
                            {
                                loadImage(urlImage, photo, nameImage, i);
                            }
                        }
                        else
                        {
                            hideLoading();
                        }
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
//                Toast.makeText(getContext(), "Load Item Failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    public class ImageListAdapter extends ArrayAdapter<ImageAdapter> {

        private List<ImageAdapter> items;
        private int layoutResourceId;
        private Context context;

        public ImageListAdapter(Context context, int layoutResourceId, List<ImageAdapter> items) {
            super(context, layoutResourceId, items);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.items = items;
        }

        public List<ImageAdapter> getItems() {
            return items;
        }

        public class Holder {
            ImageAdapter adapterItem;
            ImageView iv_image;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            Holder holder = null;

            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new Holder();
            holder.adapterItem = items.get(position);

            holder.iv_image = (ImageView) row.findViewById(R.id.imageView);
            final Holder finalHolder = holder;

            holder.iv_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(finalHolder.adapterItem.getUrl()), "image/*");
                    startActivity(intent);
                }
            });


            row.setTag(holder);
            setupItem(holder);

            return row;
        }

        private void setupItem(Holder holder) {
            holder.iv_image.setImageBitmap(holder.adapterItem.getBitmap());
        }
    }

    public class ImageAdapter {

        private String image;
        private String imageUrl;
        private String name;
        private Bitmap bitmap;

        public ImageAdapter(Bitmap bitmap, String url, String name)
        {
            this.setBitmap((bitmap));
            this.setUrl(url);
            this.setName(name);
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public void setBitmap(Bitmap b) {
            this.bitmap = b;
        }

        public String getUrl() {
            return imageUrl;
        }

        public void setUrl(String param) {
            this.imageUrl = param;
        }

        public String getName() {
            return name;
        }

        public void setName(String param) {
            this.name = param;
        }
    }
}