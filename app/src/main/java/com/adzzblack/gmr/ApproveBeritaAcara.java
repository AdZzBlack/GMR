package com.adzzblack.gmr;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ApproveBeritaAcara extends Fragment implements View.OnClickListener {

    private ImageButton ib_camera;
    private EditText et_keterangan, et_ffl, et_fflawal;
    private TextView tv_fflawal;
    private Button btn_send, btn_approve, btn_disapprove;

    private Bitmap bitmap;

    private ImageView mImageView;

    private String nomor, nomorbangunan, nama, image, keterangan, elevasi, elevasiawal, url;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.beritaacara, container, false);
        getActivity().setTitle("Approve Berita Acara");

        ib_camera = (ImageButton) v.findViewById(R.id.ib_camera);
        tv_fflawal = (TextView) v.findViewById(R.id.tv_fflawal);
        et_keterangan = (EditText) v.findViewById(R.id.et_keterangan);
        et_ffl = (EditText) v.findViewById(R.id.et_ffl);
        et_fflawal = (EditText) v.findViewById(R.id.et_fflawal);
        mImageView = (ImageView) v.findViewById(R.id.imageView);
        btn_send = (Button) v.findViewById(R.id.btn_send);
        btn_approve = (Button) v.findViewById(R.id.btn_approve);
        btn_disapprove = (Button) v.findViewById(R.id.btn_disapprove);

        btn_send.setVisibility(View.GONE);
        btn_approve.setVisibility(View.VISIBLE);
        btn_disapprove.setVisibility(View.VISIBLE);
        et_fflawal.setVisibility(View.VISIBLE);
        tv_fflawal.setVisibility(View.VISIBLE);
        ib_camera.setVisibility(View.GONE);

        et_keterangan.setEnabled(false);
        et_ffl.setEnabled(false);
        et_fflawal.setEnabled(false);

        btn_approve.setOnClickListener(this);
        btn_disapprove.setOnClickListener(this);

        mImageView.setImageResource(0);

        nomor = Index.globalfunction.getShared("elevasi", "nomor", "");
        nomorbangunan = Index.globalfunction.getShared("elevasi", "nomorbangunan", "");
        nama = Index.globalfunction.getShared("elevasi", "nama", "");
        image = Index.globalfunction.getShared("elevasi", "image", "");
        keterangan = Index.globalfunction.getShared("elevasi", "keterangan", "");
        elevasi = Index.globalfunction.getShared("elevasi", "elevasi", "");
        elevasiawal = Index.globalfunction.getShared("elevasi", "elevasiawal", "");

        et_keterangan.setText(keterangan);
        et_ffl.setText(elevasi);
        et_fflawal.setText(elevasiawal);

        mImageView.setOnClickListener(this);

        try {
            final String urlImage = Index.globalfunction.getImageURL() + image;
            Log.d("url", urlImage);

            url = urlImage;

            loadImage(urlImage, mImageView);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return v;
    }
	
    @Override
    public void onResume(){
        super.onResume();

    }

    public void onClick(View v) {
        v.startAnimation(Index.buttoneffect);
        if(v.getId() == R.id.btn_approve){
            Log.d("nomorrrr", nomor);
            String actionUrl = "BeritaAcara/approve/";
            new approve().execute( actionUrl );
        }
        else if(v.getId() == R.id.btn_disapprove){
            Log.d("nomorrrr", nomor);
            String actionUrl = "BeritaAcara/disapprove/";
            new disapprove().execute( actionUrl );
        }
        else if(v.getId() == R.id.imageView){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(url), "image/*");
            startActivity(intent);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void loadImage(final String urlImage, final ImageView image){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
//                    InputStream in = new URL(urlImage).openStream();
//                    bitmap = BitmapFactory.decodeStream(in);
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    bmOptions.inSampleSize = 1; // 1 = 100% if you write 4 means 1/4 = 25%
                    bitmap = BitmapFactory.decodeStream((InputStream)new URL(urlImage).getContent(),
                            null, bmOptions);
                } catch (Exception e) {
                    // log error
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (bitmap != null)
                    image.setImageBitmap(bitmap);
            }
        }.execute();
    }

    private class approve extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                Index.jsonObject = new JSONObject();
                Index.jsonObject.put("nomor", nomor);
                Index.jsonObject.put("nomorbangunan", nomorbangunan);
                Index.jsonObject.put("ffl", elevasi);
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
                                Index.fm.popBackStack(Index.fm.getBackStackEntryCount()-2, 0);
//                                Fragment fragment = new ChooseApprovalElevasi();
//                                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
//                                transaction.replace(R.id.fragment_container, fragment);
//                                transaction.commit();
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

    private class disapprove extends AsyncTask<String, Void, String> {
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
                                Index.fm.popBackStack(Index.fm.getBackStackEntryCount()-2, 0);
//                                Fragment fragment = new ChooseApprovalElevasi();
//                                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
//                                transaction.replace(R.id.fragment_container, fragment);
//                                transaction.commit();
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