package com.adzzblack.gmr;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormOpname extends Fragment implements View.OnClickListener {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private TextView tv_bangunan;
    private TextView tv_pekerjaan;
    private TextView tv_mandor;
    private TextView tv_luas;
    private TextView tv_satuan;
    private TextView tv_satuan1;
    private TextView tv_progress;
    private EditText et_progress;
    private Button btn_send;
    private Button btn_add;

    private ArrayList<ImageView> mImageView_;
    private ArrayList<ImageButton> mImageButton_;
    private ArrayList<Button> mButton_;
    private ArrayList<String> mPath_;
    private ArrayList<String> mPathRaw_;
    private ArrayList<String> mPhotoName_;
    private String mPath = "";
    private String mPathRaw = "";
    private String photoName = "";
    private ImageButton mImageButton;
    private ImageView mImageView;
    private int ctrImage = 0;

    private String nomor, pekerjaan, bangunan;
    private int oldProgress;

    private Bitmap mImageBitmap;
    private String mCurrentPhotoName = "";
    private String mCurrentPhotoPath = "";
    private String mCurrentPhotoPathRaw = "";

    TableLayout layout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.form_opname, container, false);
        getActivity().setTitle("Form Opname");

        //-----START DECLARE---------------------------------------------------------------------------------------
        nomor = Index.globalfunction.getShared("global", "nomorrab", "");
        pekerjaan = Index.globalfunction.getShared("global", "namarab", "");
        bangunan = Index.globalfunction.getShared("bangunan", "namaNow", "a");

        btn_send = (Button) v.findViewById(R.id.btn_send);
        btn_send.setOnClickListener(this);

        btn_add = (Button) v.findViewById(R.id.btn_add);
        btn_add.setOnClickListener(this);

        tv_bangunan = (TextView) v.findViewById(R.id.tv_bangunan);
        tv_pekerjaan = (TextView) v.findViewById(R.id.tv_pekerjaan);
        tv_mandor = (TextView) v.findViewById(R.id.tv_mandor);
        tv_luas = (TextView) v.findViewById(R.id.tv_luas);
        tv_satuan = (TextView) v.findViewById(R.id.tv_satuan);
        tv_satuan1 = (TextView) v.findViewById(R.id.tv_satuan1);
        tv_progress = (TextView) v.findViewById(R.id.tv_progress);
        et_progress = (EditText) v.findViewById(R.id.et_progress);

        layout = (TableLayout) v.findViewById(R.id.ll);

        mImageView_ = new ArrayList<ImageView>();
        mImageButton_ = new ArrayList<ImageButton>();
        mButton_ = new ArrayList<Button>();
        mPath_ = new ArrayList<String>();
        mPathRaw_ = new ArrayList<String>();
        mPhotoName_ = new ArrayList<String>();

        mImageView = (ImageView) v.findViewById(R.id.imageView);

        mImageButton = (ImageButton) v.findViewById(R.id.ib_camera);
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        Log.i("err", ex.toString());
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        });

        tv_bangunan.setText(bangunan);
        tv_pekerjaan.setText(pekerjaan);

        et_progress.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                try {
                    Float luasProgress = Float.parseFloat(et_progress.getText().toString());
                    Float progress = luasProgress * 100 / Float.parseFloat(tv_luas.getText().toString());

                    int newProgress = Math.round(progress);
                    tv_progress.setText("Progress has reached " + newProgress + "%");
                }
                catch (Exception e)
                {

                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        //-----END DECLARE---------------------------------------------------------------------------------------

        String actionUrl = "Opname/getRabDetail/";
        new get().execute( actionUrl );

        return v;
    }

    private void createNewImage()
    {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("blah", R.drawable.camera);

        final float scale = getResources().getDisplayMetrics().density;
        int pixels = (int) (200 * scale + 0.5f);
        int pixelsPad = (int) (50 * scale + 0.5f);

        TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, pixels);
        TableLayout.LayoutParams layoutParams1 = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        layoutParams1.setMargins(0,0,0,40);

        final ImageView image = new ImageView(getContext());
        final Button btn = new Button(getContext());
        final ImageButton image1 = new ImageButton(getContext());

        image.setLayoutParams(layoutParams);
        image.setBackgroundColor(getResources().getColor(R.color.colorLine));
        image.setImageResource(map.get("blah"));
        image.setVisibility(View.GONE);

        // Adds the view to the layout
        layout.addView(image);
        mImageView_.add(image);

        image1.setLayoutParams(layoutParams);
        image1.setBackgroundColor(getResources().getColor(R.color.colorLine));
        image1.setImageResource(map.get("blah"));
        image1.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image1.setPadding(pixelsPad, pixelsPad, pixelsPad, pixelsPad);
        image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        Log.i("err", ex.toString());
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        });
        // Adds the view to the layout
        layout.addView(image1);
        mImageButton_.add(image1);


        btn.setLayoutParams(layoutParams1);
        btn.setText("Remove Image");
        btn.setVisibility(View.GONE);

        // Adds the view to the layout
        layout.addView(btn);
        mButton_.add(btn);
    }

    public void onClick(View v) {
        v.startAnimation(Index.buttoneffect);
        if(v.getId() == R.id.btn_back){
            Index.fm.popBackStack(Index.fm.getBackStackEntryCount()-2, 0);
        }
        else if(v.getId() == R.id.btn_add){
            createNewImage();
            btn_add.setVisibility(View.GONE);
        }
        else if(v.getId() == R.id.btn_send){
            Float luasProgress = Float.parseFloat(et_progress.getText().toString());
            Float progress = luasProgress * 100 / Float.parseFloat(tv_luas.getText().toString());

            int newProgress = Math.round(progress);
            if(newProgress<0 && newProgress>100)
            {
                Toast.makeText(getContext(), "Invalid input", Toast.LENGTH_LONG).show();
                return;
            }
            else if(newProgress<oldProgress)
            {
                Toast.makeText(getContext(), "Progress less than old progress", Toast.LENGTH_LONG).show();
                return;
            }
            else if(newProgress<0)
            {
                Toast.makeText(getContext(), "Progress couldn't less than 0", Toast.LENGTH_LONG).show();
                return;
            }
            else if(newProgress>100)
            {
                Toast.makeText(getContext(), "Progress more than area", Toast.LENGTH_LONG).show();
                return;
            }

            showLoading();
            photoName = "";
            for(int i=0;i<mPathRaw_.size();i++)
            {
                if(!mPathRaw_.get(i).equals(""))
                {
                    photoName = photoName + mPhotoName_.get(i) + "|";
                    new doFileUpload().execute(decodeFile(mPhotoName_.get(i), mPathRaw_.get(i), 1920, 1080));
                }
            }
            Log.d("name", photoName);
            String actionUrl = "Opname/createOpname/";
            new createOpname().execute( actionUrl );

//            Fragment fragment = new Sign();
//            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
//            transaction.replace(R.id.fragment_container, fragment);
//            transaction.addToBackStack(null);
//            transaction.commit();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {

            Log.d("url", mCurrentPhotoPath);
//                mImageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Uri.parse(mCurrentPhotoPath));
            mImageBitmap = decodeBitmap(mCurrentPhotoPathRaw, 1920, 1080);
            if(mImageView_.size()!=0)
            {
                mImageView_.get(mImageView_.size()-1).setImageBitmap(mImageBitmap);
                mImageButton_.get(mImageButton_.size()-1).setVisibility(View.GONE);
                mButton_.get(mButton_.size()-1).setVisibility(View.VISIBLE);
                mImageView_.get(mImageView_.size()-1).setVisibility(View.VISIBLE);
                mPathRaw_.add(mCurrentPhotoPathRaw);
                mPath_.add(mCurrentPhotoPath);
                mPhotoName_.add(mCurrentPhotoName);
                final int ctr = mPath_.size()-1;
                final int ctrIView = mImageView_.size()-1;
                final int ctrIButton = mImageButton_.size()-1;
                final int ctrButton = mButton_.size()-1;

                mImageView_.get(mImageView_.size()-1).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("ctr", ctr + ", " + ctrIButton + ", " + ctrIView + ", " + ctrButton);
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(mPath_.get(ctr)), "image/*");
                        startActivity(intent);
                    }
                });

                mButton_.get(mButton_.size()-1).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("ctr", ctr + ", " + ctrIButton + ", " + ctrIView + ", " + ctrButton);
//                            mImageButton_.remove(ctrIButton);
//                            mImageView_.remove(ctrIView);
//                            mButton_.remove(ctrButton);

                        File f = new File(mPath_.get(ctr));
                        f.delete();
                        f = new File(mPathRaw_.get(ctr));
                        f.delete();

                        mPath_.set(ctr, "");
                        mPathRaw_.set(ctr, "");
                        mPhotoName_.set(ctr, "");
//                            mPath_.remove(ctr);
//                            mPathRaw_.remove(ctr);
                        mImageButton_.get(ctrIButton).setVisibility(View.GONE);
                        mButton_.get(ctrButton).setVisibility(View.GONE);
                        mImageView_.get(ctrIView).setVisibility(View.GONE);
                        layout.removeView(mImageButton_.get(ctrIButton));
                        layout.removeView(mButton_.get(ctrButton));
                        layout.removeView(mImageView_.get(ctrIView));
                        if(mButton_.size()==0)
                        {
                            btn_add.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
            else
            {
                mImageView.setVisibility(View.VISIBLE);
                mImageButton.setVisibility(View.GONE);
                mImageView.setImageBitmap(mImageBitmap);
                mPathRaw = mCurrentPhotoPathRaw;
                mPath = mCurrentPhotoPath;
                mImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(mPath), "image/*");
                        startActivity(intent);
                    }
                });
            }

            createNewImage();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath(), ".GMR");
        if(!storageDir.exists()){
            storageDir.mkdirs();
        }

        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        mCurrentPhotoPathRaw = image.getAbsolutePath();
        mCurrentPhotoName = image.getName();
        return image;
    }

    private String decodeFile(String name, String path,int DESIREDWIDTH, int DESIREDHEIGHT) {
        String strMyImagePath = null;
        Bitmap scaledBitmap = null;

        try {
            // Part 1: Decode image
            Bitmap unscaledBitmap = ScalingUtilities.decodeFile(path, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);

            if (!(unscaledBitmap.getWidth() <= DESIREDWIDTH && unscaledBitmap.getHeight() <= DESIREDHEIGHT)) {
                // Part 2: Scale image
                scaledBitmap = ScalingUtilities.createScaledBitmap(unscaledBitmap, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);
            } else {
                unscaledBitmap.recycle();
                return path;
            }

            // Store to tmp file

            String extr = Environment.getExternalStorageDirectory().toString();
            File mFolder = new File(extr + "/TMMFOLDER");
            if (!mFolder.exists()) {
                mFolder.mkdir();
            }

            String s = name;

            File f = new File(mFolder.getAbsolutePath(), s);

            strMyImagePath = f.getAbsolutePath();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {

                e.printStackTrace();
            } catch (Exception e) {

                e.printStackTrace();
            }

            scaledBitmap.recycle();
        } catch (Throwable e) {
        }

        if (strMyImagePath == null) {
            return path;
        }
        return strMyImagePath;

    }

    private Bitmap decodeBitmap(String path,int DESIREDWIDTH, int DESIREDHEIGHT) {
        String strMyImagePath = null;
        Bitmap scaledBitmap = null;

        try {
            // Part 1: Decode image
            Bitmap unscaledBitmap = ScalingUtilities.decodeFile(path, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);

            if (!(unscaledBitmap.getWidth() <= DESIREDWIDTH && unscaledBitmap.getHeight() <= DESIREDHEIGHT)) {
                // Part 2: Scale image
                scaledBitmap = ScalingUtilities.createScaledBitmap(unscaledBitmap, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);
            } else {
                unscaledBitmap.recycle();
            }
        }
        catch (Throwable e) {
        }

        return  scaledBitmap;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private class get extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                Index.jsonObject = new JSONObject();
                Index.jsonObject.put("nomor_rab", nomor);
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
                            String mandor = (obj.getString("mandor"));
                            String volume = (obj.getString("volume"));
                            String satuan = (obj.getString("satuan"));
                            String progress = (obj.getString("progress"));

                            oldProgress = Integer.parseInt(progress);

                            tv_mandor.setText(mandor);
                            tv_luas.setText(volume);
                            tv_satuan.setText(satuan);
                            tv_satuan.setText(satuan);
                            tv_progress.setText("Progress has reached " + progress + "%");

                            Float progressLuas = Float.parseFloat(volume) * Float.parseFloat(progress) / 100;
                            et_progress.setText(String.valueOf(progressLuas));
                        }
                    }
                }

                hideLoading();
            }catch(Exception e)
            {
                e.printStackTrace();
                Toast.makeText(getContext(), "Load Failed", Toast.LENGTH_LONG).show();
                hideLoading();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            showLoading();
        }
    }

    private class createOpname extends AsyncTask<String, Void, String> {
        Float luasProgress = Float.parseFloat(et_progress.getText().toString());
        Float progress = luasProgress * 100 / Float.parseFloat(tv_luas.getText().toString());

        int newProgress = Math.round(progress);
        String nomor_bangunan = Index.globalfunction.getShared("bangunan", "nomorNow", "");
        String nomor_user = Index.globalfunction.getShared("user", "nomor", "");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdf.format(new Date());

        @Override
        protected String doInBackground(String... urls) {
            try {
                Index.jsonObject = new JSONObject();
                Index.jsonObject.put("nomor_rab", nomor);
                Index.jsonObject.put("nomor_bangunan", nomor_bangunan);
                Index.jsonObject.put("nomor_user", nomor_user);
                Index.jsonObject.put("progress", newProgress);
                Index.jsonObject.put("tanggal", date);
                Index.jsonObject.put("photo", photoName);
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
                        if(obj.has("success")){
                            hideLoading();
                            Index.fm.popBackStack(Index.fm.getBackStackEntryCount()-2, 0);
                        }
                        else
                        {
                            Toast.makeText(getContext(), "Send Opname Failed", Toast.LENGTH_LONG).show();
                            hideLoading();
                        }
                    }
                }
            }catch(Exception e)
            {
                e.printStackTrace();
                Toast.makeText(getContext(), "Send Opname Failed", Toast.LENGTH_LONG).show();
                hideLoading();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            showLoading();
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