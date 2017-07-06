package com.adzzblack.gmr;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.FragmentTransaction;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by owners on 17/04/2017.
 */

public class PrivateMessage extends Fragment implements View.OnClickListener {

    private ListView lv_message;
    private EditText et_newmessage;
    private ImageButton ib_sendmessage;
    private Button btn_othermenumessage;
    private PrivateMessageListAdapter messageadapter;
    private LinearLayout rowothermenu, rowselecteditem;
    private RelativeLayout btn_selectphoto, btn_takephoto, btn_selectvideo, btn_takevideo, rowpreviewmedia;
    private ImageView iv_cancelselection, iv_copyselection, iv_deleteselection, iv_replyselection, iv_forwardselection, iv_previewphoto, iv_previewvideo, iv_previewvideo_icontrans;
    private TextView tv_countselection;

    private boolean scroll = false;
    private int counter = 0;
    private boolean clickothermenu = false;
    private boolean isLoading = true;
    private Thread t;
    private Boolean running = true;
    private String previewmedia = "";
    private String replymode = "";

    private ArrayList<String> selectedlist = new ArrayList<String>();

    private String tipe_send = "";
    //For Take Photo
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private String mCurrentPhotoName = "";
    private String mCurrentPhotoPath = "";
    private String mCurrentPhotoPathRaw = "";

    //For Take Video
    static final int REQUEST_VIDEO_CAPTURE = 2;
    private String mCurrentVideoName = "";
    private String mCurrentVideoPath = "";
    private String mCurrentVideoPathRaw = "";

    //For Select Photo & Video
    private int PICK_IMAGE_REQUEST = 3;
    private int PICK_VIDEO_REQUEST = 4;
    String realPath = "";
    String realName = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.privatemessage, container, false);
        String userto_nama = Index.globalfunction.getShared("message", "userto_nama", "");
        String upper_nama = userto_nama.substring(0, 1).toUpperCase() + userto_nama.substring(1);
        getActivity().setTitle("Message to " + upper_nama);

        //-----START DECLARE---------------------------------------------------------------------------------------
        iv_cancelselection = (ImageView) v.findViewById(R.id.iv_cancelselection);
        iv_cancelselection.setOnClickListener(this);
        iv_copyselection = (ImageView) v.findViewById(R.id.iv_copyselection);
        iv_copyselection.setOnClickListener(this);
        iv_deleteselection = (ImageView) v.findViewById(R.id.iv_deleteselection);
        iv_deleteselection.setOnClickListener(this);
        iv_replyselection = (ImageView) v.findViewById(R.id.iv_replyselection);
        iv_replyselection.setOnClickListener(this);
        iv_forwardselection = (ImageView) v.findViewById(R.id.iv_forwardselection);
        iv_forwardselection.setOnClickListener(this);
        tv_countselection = (TextView) v.findViewById(R.id.tv_countselection);
        rowselecteditem = (LinearLayout) v.findViewById(R.id.privatemessage_selecteditem);
        rowpreviewmedia = (RelativeLayout) v.findViewById(R.id.privatemessage_previewmedia);
        iv_previewphoto = (ImageView) v.findViewById(R.id.iv_previewphoto);
        iv_previewphoto.setOnClickListener(this);
        iv_previewvideo = (ImageView) v.findViewById(R.id.iv_previewvideo);
        iv_previewvideo.setOnClickListener(this);
        iv_previewvideo_icontrans = (ImageView) v.findViewById(R.id.iv_previewvideo_icontrans);
        iv_previewvideo_icontrans.setOnClickListener(this);
        et_newmessage = (EditText) v.findViewById(R.id.et_newmessage);
        rowothermenu = (LinearLayout) v.findViewById(R.id.privatemessage_othermenu);
        btn_othermenumessage = (Button) v.findViewById(R.id.btn_othermenumessage);
        btn_othermenumessage.setOnClickListener(this);
        btn_takephoto = (RelativeLayout) v.findViewById(R.id.btn_takephoto);
        btn_takephoto.setOnClickListener(this);
        btn_selectphoto = (RelativeLayout) v.findViewById(R.id.btn_selectphoto);
        btn_selectphoto.setOnClickListener(this);
        btn_takevideo = (RelativeLayout) v.findViewById(R.id.btn_takevideo);
        btn_takevideo.setOnClickListener(this);
        btn_selectvideo = (RelativeLayout) v.findViewById(R.id.btn_selectvideo);
        btn_selectvideo.setOnClickListener(this);
        ib_sendmessage = (ImageButton) v.findViewById(R.id.btn_sendmessage);
        ib_sendmessage.setOnClickListener(this);
        messageadapter = new PrivateMessageListAdapter(getActivity(), R.layout.list_privatemessage, new ArrayList<PrivateMessageAdapter>());
        lv_message = (ListView) v.findViewById(R.id.lv_messsage);
        lv_message.setAdapter(messageadapter);

        lv_message.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) { }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(!isLoading && running){
                    if(firstVisibleItem == 0 && listIsAtTop()){
                        if(scroll){
                            scroll = false;
                            counter += 1;
                            refresh_chat();
                        }
                    }
                }
            }
        });
        //-----END DECLARE---------------------------------------------------------------------------------------

        clickothermenu = false;
        scroll = false;
        counter = 0;
        running = true;
        refresh_chat();

        t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(!Thread.currentThread().isInterrupted() && running){
                        try {
                            Integer private_run_check = Integer.valueOf(Index.globalfunction.getShared("message","private_run_check",""));
                            if(!isLoading && private_run_check < 5)
                            {
                                isLoading = true;
                                String actionUrlget = "Message/getPrivateMessage/";
                                new checkNewMessage().execute( actionUrlget );
                            }
                            Thread.sleep(1000);
                        }catch (InterruptedException e) {
                            running = false;
                            Thread.currentThread().interrupt();
                        }
                    }
                } catch (Exception e) {
                    running = false;
                    Thread.currentThread().interrupt();
                }
            }
        });
        t.start();

        selectedlist.clear();
        if(Index.globalfunction.getShared("message","forwardmessage_nomor", "").equals("done")){
            Index.globalfunction.setShared("message","forwardmessage_nomor", "");
        }
        if(!String.valueOf(Index.globalfunction.getShared("message","forwardmessage_nomor", "").length()).equals("0") && Index.globalfunction.getShared("message","forwardmessage_from", "").equals(Index.globalfunction.getShared("message", "userto_nomor", ""))){
            String message_nomor = Index.globalfunction.getShared("message","forwardmessage_nomor", "");
            Index.globalfunction.setShared("message","forwardmessage_nomor","");
            Index.globalfunction.setShared("message","forwardmessage_from","");
            String[] separated = message_nomor.split("\\,");
            for (int i=0 ; i < separated.length ; i++){
                selectedlist.add(separated[i]);
            }
        }
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        running = false;
        Thread.currentThread().interrupt();
    }

    private boolean listIsAtTop()   {
        if(lv_message.getChildCount() == 0) return true;
        return lv_message.getChildAt(0).getTop() == 0;
    }

    private void clickMessage(String nomor){
        if(selectedlist.contains(nomor)){
            int index = selectedlist.indexOf(nomor);
            selectedlist.remove(index);
        }else {
            selectedlist.add(nomor);
        }
        setSelectedMessage();
    }

    private void setSelectedMessage(){
        if(String.valueOf(selectedlist.size()).equals("0")){
            iv_cancelselection.performClick();
        }else if(selectedlist.size() > 0) {
            if(selectedlist.size() == 1){iv_replyselection.setVisibility(View.VISIBLE);
            }else {iv_replyselection.setVisibility(View.GONE);}
            boolean checkmedia = false;
            boolean checktext = false;
            for(int i=0; i<selectedlist.size(); i++){
                for (int j = 0; j < messageadapter.getCount(); j++) {
                    PrivateMessageAdapter adapter = messageadapter.getItem(j);
                    if (adapter.getNomor().toString().equals(selectedlist.get(i))) {
                        if(adapter.getTipe().equals("2") || adapter.getTipe().equals("3")){checkmedia = true;}
                        if(adapter.getTipe().equals("1") || adapter.getTipe().equals("4")){checktext = true;}
                    }
                }
            }
            if(checkmedia && !checktext){iv_copyselection.setVisibility(View.GONE); iv_deleteselection.setVisibility(View.VISIBLE);}
            if(!checkmedia && checktext){iv_copyselection.setVisibility(View.VISIBLE); iv_deleteselection.setVisibility(View.GONE);}
            if(checkmedia && checktext){iv_copyselection.setVisibility(View.GONE); iv_deleteselection.setVisibility(View.GONE);}
            rowselecteditem.setVisibility(View.VISIBLE);
            iv_forwardselection.setVisibility(View.VISIBLE);
            tv_countselection.setText(String.valueOf(selectedlist.size())); tv_countselection.setTextSize(22); tv_countselection.setVisibility(View.VISIBLE);
        }
        messageadapter.notifyDataSetChanged();
    }

    private void setPreviewMedia(String mediaName){
        if(previewmedia.equals("photo")){
            iv_previewphoto.setImageBitmap(loadImageBitmap(getContext(), mediaName));
            iv_previewphoto.setVisibility(View.VISIBLE);
        }else if(previewmedia.equals("video")){
            iv_previewvideo.setImageBitmap(loadVideoThumbnailBitmap(getContext(), mediaName));
            iv_previewvideo.setVisibility(View.VISIBLE);
            iv_previewvideo_icontrans.setVisibility(View.VISIBLE);
        }
        rowpreviewmedia.setVisibility(View.VISIBLE);
        tv_countselection.setVisibility(View.GONE);
        iv_copyselection.setVisibility(View.GONE);
        iv_deleteselection.setVisibility(View.GONE);
        iv_replyselection.setVisibility(View.GONE);
        iv_forwardselection.setVisibility(View.GONE);
        rowselecteditem.setVisibility(View.VISIBLE);
        btn_othermenumessage.setVisibility(View.GONE);
    }

    private void clearPreviewMedia(){
        previewmedia = "";
        iv_previewphoto.setVisibility(View.GONE);
        iv_previewvideo.setVisibility(View.GONE);
        iv_previewvideo_icontrans.setVisibility(View.GONE);
        rowpreviewmedia.setVisibility(View.GONE);
        btn_othermenumessage.setVisibility(View.VISIBLE);
    }

    private void doDeletePreviewMedia(String previewUrlName){
        File file = new File(getActivity().getFileStreamPath(previewUrlName).getAbsolutePath());
        if(file.exists()){file.delete();}
    }

    private void doDeleteMedia(){
        if(selectedlist.size() > 0){
            for(int i=0; i<selectedlist.size(); i++){
                String urlmedia = "";
                for (int j = 0; j < messageadapter.getCount(); j++) {
                    PrivateMessageAdapter adapter = messageadapter.getItem(j);
                    if (adapter.getNomor().toString().equals(selectedlist.get(i))) {
                        urlmedia = adapter.getUrl();
                    }
                }
                File file = new File(getActivity().getFileStreamPath(urlmedia).getAbsolutePath());
                if(file.exists()){file.delete();}
            }
            iv_cancelselection.performClick();
        }
    }

    private void addSendMessageToAdapter(String nomor, String nama, String et_newmessage, String tipe, String url){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Jakarta"); //same with Timezone server
        sdf.setTimeZone(timeZone);
        String date = sdf.format(new Date());

        messageadapter.add(new PrivateMessageAdapter(nomor, nama, et_newmessage, tipe, url, date, "1"));
        messageadapter.notifyDataSetChanged();
        lv_message.smoothScrollToPosition(messageadapter.getCount() -1);
    }

    public void onClick(View v) {
        v.startAnimation(Index.buttoneffect);
        if (v.getId() == R.id.btn_sendmessage) {
            boolean checksend = true;
            if (tipe_send.equals("2")) {
                addSendMessageToAdapter("", "You", et_newmessage.getText().toString(), "2", mCurrentPhotoName);
                new doFileUpload().execute(decodeFile(mCurrentPhotoPathRaw, 1920, 1080));
            } else if (tipe_send.equals("3")) {
                addSendMessageToAdapter("", "You", et_newmessage.getText().toString(), "3", mCurrentVideoName);
                new doFileUpload().execute(mCurrentVideoPathRaw);
            } else if (tipe_send.equals("4")) {
                addSendMessageToAdapter("", "You", et_newmessage.getText().toString(), "2", realName);
                new doFileUpload().execute(decodeFile(realPath, 1920, 1080));
            } else if (tipe_send.equals("5")) {
                addSendMessageToAdapter("", "You", et_newmessage.getText().toString(), "3", realName);
                new doFileUpload().execute(realPath);
            } else if (tipe_send.equals("") || tipe_send.equals("1") || tipe_send.equals("6")) {
                if (et_newmessage.getText().toString().trim().length() > 0) {
                    if (tipe_send.equals("6")) {
                        selectedlist.clear();
                        addSendMessageToAdapter("", "You", replymode + "-R3p7y-" + et_newmessage.getText().toString(), "4", "");
                        rowselecteditem.setVisibility(View.GONE);
                    } else {
                        tipe_send = "1";
                        addSendMessageToAdapter("", "You", et_newmessage.getText().toString(), "1", "");
                    }
                } else {
                    checksend = false;
                    Toast.makeText(getActivity(), "Message can't be empty!", Toast.LENGTH_SHORT).show();
                }
            }
            if (!tipe_send.equals("") && checksend) {
                String actionUrl = "Message/sendPrivateMessage/";
                new sendMessage().execute(actionUrl);
                et_newmessage.setText("");
                for (int j = 0; j < messageadapter.getCount(); j++) {
                    PrivateMessageAdapter adapter = messageadapter.getItem(j);
                    if (adapter.getStatus().toString().equals("1") && !adapter.getNama().equals("You")) {
                        adapter.setStatus("2");
                    }
                }
            }
        } else if (v.getId() == R.id.btn_othermenumessage) {
            if (!clickothermenu) {
                rowothermenu.setVisibility(View.VISIBLE);
                clickothermenu = true;
            } else if (clickothermenu) {
                rowothermenu.setVisibility(View.GONE);
                clickothermenu = false;
            }
        } else if (v.getId() == R.id.btn_takephoto) {
            btn_othermenumessage.performClick();

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {  // Error occurred while creating the File
                    Log.i("err", ex.toString());
                }

                if (photoFile != null) {  // Continue only if the File was successfully created
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        } else if (v.getId() == R.id.btn_takevideo) {
            btn_othermenumessage.performClick();

            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            if (takeVideoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                File videoFile = null;
                try {
                    videoFile = createVideoFile();
                } catch (IOException ex) { //System.out.println( "Error creating file." );
                    Log.i("err", ex.toString());
                }

                if (videoFile != null) {
                    takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(videoFile));
                    takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0); // 1-> high quality with higher file size  0-> lower quality and lower file size
                    takeVideoIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 15000000L);
                    takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10); //int second
                    startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
                }
            }
        } else if (v.getId() == R.id.btn_selectphoto) {
            btn_othermenumessage.performClick();
            Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
            getIntent.setType("image/*");

            Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");

            Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

            startActivityForResult(chooserIntent, PICK_IMAGE_REQUEST);
        } else if (v.getId() == R.id.btn_selectvideo) {
            btn_othermenumessage.performClick();
            Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
            getIntent.setType("video/*");

            Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("video/*");

            Intent chooserIntent = Intent.createChooser(getIntent, "Select Video");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

            startActivityForResult(chooserIntent, PICK_VIDEO_REQUEST);
        } else if (v.getId() == R.id.iv_cancelselection) {
            if (!previewmedia.equals("")) {
                if (tipe_send.equals("2")) {
                    doDeletePreviewMedia(mCurrentPhotoName);
                    btn_takephoto.clearFocus();
                }
                if (tipe_send.equals("3")) {
                    doDeletePreviewMedia(mCurrentVideoName);
                    btn_takevideo.clearFocus();
                }
                if (tipe_send.equals("4") || tipe_send.equals("5")) {
                    doDeletePreviewMedia(realName);
                    btn_selectphoto.clearFocus();
                    btn_selectvideo.clearFocus();
                }
                clearPreviewMedia();
            } else {
                replymode = "";
                tv_countselection.setText("0");
                tv_countselection.setTextSize(22);
                tv_countselection.setVisibility(View.VISIBLE);
                iv_copyselection.setVisibility(View.GONE);
                iv_deleteselection.setVisibility(View.GONE);
                iv_replyselection.setVisibility(View.GONE);
                iv_forwardselection.setVisibility(View.GONE);
                selectedlist.clear();
                messageadapter.notifyDataSetChanged();
            }
            rowselecteditem.setVisibility(View.GONE);
        } else if (v.getId() == R.id.iv_copyselection) {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            String tampselection = "";
            if (selectedlist.size() > 0) {
                if (String.valueOf(selectedlist.size()).equals("1")) {
                    for (int j = 0; j < messageadapter.getCount(); j++) {
                        PrivateMessageAdapter adapter = messageadapter.getItem(j);
                        if (adapter.getNomor().toString().equals(selectedlist.get(0))) {
                            tampselection = adapter.getMessage();
                            if (adapter.getTipe().equals("4")) {
                                String[] separated = adapter.getMessage().split("\\-R3p7y-");
                                tampselection = separated[1];
                            }
                        }
                    }
                } else {
                    ArrayList<Integer> unsortedList = new ArrayList<Integer>();
                    for (String stringValue : selectedlist) {
                        unsortedList.add(Integer.parseInt(stringValue));
                    }
                    Collections.sort(unsortedList);
                    System.out.println("Sorted ArrayList in Java - Ascending order : " + unsortedList);
                    for (int i = 0; i < messageadapter.getCount(); i++) {
                        PrivateMessageAdapter adapter = messageadapter.getItem(i);
                        for (int j = 0; j < unsortedList.size(); j++) {
                            if (adapter.getNomor().toString().equals(String.valueOf(unsortedList.get(j)))) {
                                String tv_nama = adapter.getNama();
                                if (tv_nama.equals("You")) {
                                    tv_nama = Index.globalfunction.getShared("user", "username", "");
                                    tv_nama = tv_nama.substring(0, 1).toUpperCase() + tv_nama.substring(1);
                                }
                                String tv_date = adapter.getTime();
                                try {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    TimeZone timeZone = TimeZone.getTimeZone("Asia/Jakarta"); //change to date Timezone server - Asia/Jakarta
                                    sdf.setTimeZone(timeZone);
                                    Date newdate = sdf.parse(tv_date);
                                    tv_date = sdf.format(newdate);

                                    SimpleDateFormat datetimeformat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                                    timeZone = TimeZone.getTimeZone(TimeZone.getDefault().getID()); //from Timezone server to Timezone app
                                    datetimeformat.setTimeZone(timeZone);
                                    newdate = sdf.parse(tv_date);
                                    tv_date = datetimeformat.format(newdate);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                String tv_message = adapter.getMessage();
                                if (adapter.getTipe().equals("4")) {
                                    String[] separated = adapter.getMessage().split("\\-R3p7y-");
                                    tv_message = separated[1];
                                }
                                tampselection = tampselection + "[" + tv_date + "] " + tv_nama + ": " + tv_message + '\n';
                            }
                        }
                    }
                }
                android.content.ClipData clip = android.content.ClipData.newPlainText("Private Message", tampselection);
                clipboard.setPrimaryClip(clip);
                if (String.valueOf(selectedlist.size()).equals("1")) {
                    Toast.makeText(getActivity(), "Copied to Clipboard", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), String.valueOf(selectedlist.size()) + " Messages Copied", Toast.LENGTH_SHORT).show();
                }
                iv_cancelselection.performClick();
            }
        } else if (v.getId() == R.id.iv_deleteselection) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
            dialog.setCancelable(false);
            if (String.valueOf(selectedlist.size()).equals("1")) {
                String nama = "";
                for (int j = 0; j < messageadapter.getCount(); j++) {
                    PrivateMessageAdapter adapter = messageadapter.getItem(j);
                    if (adapter.getNomor().toString().equals(selectedlist.get(0))) {
                        nama = adapter.getNama();
                    }
                }
                dialog.setTitle("Delete message from " + nama + "?");
            } else {
                dialog.setTitle("Delete " + String.valueOf(selectedlist.size()) + " messages?");
            }
            dialog.setMessage("[!] Delete media from phone.");
            dialog.setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            dialog.setNegativeButton("DELETE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    doDeleteMedia();
                }
            });
            final AlertDialog alert = dialog.create();
            alert.show();
        } else if (v.getId() == R.id.iv_replyselection) {
            tv_countselection.setText("Reply Message");
            tv_countselection.setTextSize(18);
            tv_countselection.setVisibility(View.VISIBLE);
            iv_copyselection.setVisibility(View.GONE);
            iv_deleteselection.setVisibility(View.GONE);
            iv_replyselection.setVisibility(View.GONE);
            iv_forwardselection.setVisibility(View.GONE);
            replymode = selectedlist.get(0);
            tipe_send = "6";
        }else if(v.getId() == R.id.iv_forwardselection){
            if (selectedlist.size() > 0) {
                String tampnomor = "";
                for (int i = 0; i < selectedlist.size(); i++) {
                    if(tampnomor.equals("")){
                        tampnomor = selectedlist.get(i);
                    }else {
                        tampnomor = tampnomor + "," + selectedlist.get(i);
                    }
                }
                Index.globalfunction.setShared("message","forwardmessage_from", Index.globalfunction.getShared("message", "userto_nomor", ""));
                Index.globalfunction.setShared("message","forwardmessage_nomor", tampnomor);

                Fragment fragment = new ChooseUser();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        }else if(v.getId() == R.id.iv_previewphoto){
            if(tipe_send.equals("2")){ doOpenImage(mCurrentPhotoName);
            }else if(tipe_send.equals("4")){ doOpenImage(realName); }
        }else if(v.getId() == R.id.iv_previewvideo || v.getId() == R.id.iv_previewvideo_icontrans){
            if(tipe_send.equals("3")){ doOpenVideo(mCurrentVideoName);
            }else if(tipe_send.equals("5")){ doOpenVideo(realName); }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    /*--------------------------MESSAGE----------------------*/
    private class sendMessage extends AsyncTask<String, Void, String> {
        String userfrom_nomor = Index.globalfunction.getShared("user","id","");
        String userto_nomor = Index.globalfunction.getShared("message","userto_nomor","");
        String new_message = et_newmessage.getText().toString();
        String url = null;

        @Override
        protected String doInBackground(String... urls) {
            try {
                Index.jsonObject = new JSONObject();
                Index.jsonObject.put("userfrom_nomor", userfrom_nomor);
                Index.jsonObject.put("userto_nomor", userto_nomor);
                if(tipe_send.equals("6")){
                    Index.jsonObject.put("new_message", replymode + "-R3p7y-" + new_message);
                }else {
                    Index.jsonObject.put("new_message", new_message);
                }
                if(tipe_send.equals("1")){
                    Index.jsonObject.put("tipe_send", "1");
                }else if (tipe_send.equals("2") || tipe_send.equals("4")){
                    Index.jsonObject.put("tipe_send", "2");
                    if(tipe_send.equals("2")){
                        url = mCurrentPhotoName;
                    }else {
                        url = realName;
                    }
                }else if (tipe_send.equals("3") || tipe_send.equals("5")){
                    Index.jsonObject.put("tipe_send", "3");
                    if(tipe_send.equals("3")){
                        url = mCurrentVideoName;
                    }else {
                        url = realName;
                    }
                }else if(tipe_send.equals("6")){
                    Index.jsonObject.put("tipe_send", "4");
                }
                Index.jsonObject.put("url", url);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return Index.globalfunction.executePost(urls[0],Index.jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray jsonarray = new JSONArray(result);
                for (int i = 0; i < jsonarray.length(); i++) {
                    JSONObject obj = jsonarray.getJSONObject(i);
                    String success = obj.getString("success");
                    if(success.equals("true")){
                        String nomor = obj.getString("nomor");
                        int itemcounter = messageadapter.getCount() - 1;
                        PrivateMessageAdapter item = messageadapter.getItem(itemcounter);
                        item.setNomor(nomor);
                        messageadapter.notifyDataSetChanged();

                        lv_message.smoothScrollToPosition(messageadapter.getCount() - 1);
                        if(!tipe_send.equals("1")) { clearPreviewMedia();}
                        if(tipe_send.equals("6")){ iv_cancelselection.performClick(); }
                    }else{
                        Toast.makeText(getContext(), "Failed send message", Toast.LENGTH_LONG).show();
                    }
                }
                isLoading = false;
            }catch(Exception e)
            {
                e.printStackTrace();
                Toast.makeText(getContext(), "Failed send message", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isLoading = true;
            hideLoading();
        }
    }

    public void refresh_chat(){
        String actionUrl = "Message/getPrivateMessage/";
        new getMessage().execute( actionUrl );
        actionUrl = "Message/readPrivateMessage/";
        new readPrivateMessage().execute(actionUrl);
    }

    private class getMessage extends AsyncTask<String, Void, String> {
        String userfrom_nomor = Index.globalfunction.getShared("user","id","");
        String userto_nomor = Index.globalfunction.getShared("message","userto_nomor","");

        @Override
        protected String doInBackground(String... urls) {
            try {
                Index.jsonObject = new JSONObject();
                Index.jsonObject.put("limit", counter);
                Index.jsonObject.put("userfrom_nomor", userfrom_nomor);
                Index.jsonObject.put("userto_nomor", userto_nomor);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return Index.globalfunction.executePost(urls[0],Index.jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            if(counter == 0){
                fillMessageStart(result);
            }else{
                fillMessageNext(result);
            }
            setSelectedMessage();
            hideLoading();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isLoading = true;
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

    private void fillMessageStart(String result){
        try {
            JSONArray jsonarray = new JSONArray(result);
            if(jsonarray.length() > 0){
                for (int i = jsonarray.length() - 1; i >= 0; i--) {
                    JSONObject obj = jsonarray.getJSONObject(i);
                    if(!obj.has("query")){
                        String nomor = (obj.getString("nomor"));
                        String userfrom_nomor = (obj.getString("userfrom_nomor"));
                        String userfrom_nama = (obj.getString("userfrom_nama"));
                        String userto_nomor = (obj.getString("userto_nomor"));
                        String userto_nama = (obj.getString("userto_nama"));
                        String message = (obj.getString("message"));
                        String url = (obj.getString("url"));
                        String tipe = (obj.getString("tipe"));
                        String date = (obj.getString("waktu"));
                        String status = (obj.getString("status_read"));

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date newdate = sdf.parse(date);
                        date = sdf.format(newdate);
                        String userid = Index.globalfunction.getShared("user","id","");
                        if (userid.equals(userfrom_nomor)) {
                            userfrom_nama = "You";
                            messageadapter.add(new PrivateMessageAdapter(nomor, userfrom_nama, message, tipe, url, date, status));
                            messageadapter.notifyDataSetChanged();
                        } else {
                            //status = "1";
                            String upper_nama = userfrom_nama.substring(0, 1).toUpperCase() + userfrom_nama.substring(1);
                            messageadapter.add(new PrivateMessageAdapter(nomor, upper_nama, message, tipe, url, date, status));
                            messageadapter.notifyDataSetChanged();
                        }

                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {isLoading = false;}
                                },
                                1000);

                        lv_message.setSelection(messageadapter.getCount() - (counter * 10) - 1);
                    }
                }
            }
            if(jsonarray.length() < 10){
                scroll = false;
            }
            else {
                scroll = true;
            }

            hideLoading();
        }catch(Exception e)
        {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed load message", Toast.LENGTH_LONG).show();
            hideLoading();
        }
    }

    private void fillMessageNext(String result){
        try {
            JSONArray jsonarray = new JSONArray(result);
            if(jsonarray.length() > 0){
                for (int i = 0; i < jsonarray.length(); i++) {
                    JSONObject obj = jsonarray.getJSONObject(i);
                    if(!obj.has("query")){
                        String nomor = (obj.getString("nomor"));
                        String userfrom_nomor = (obj.getString("userfrom_nomor"));
                        String userfrom_nama = (obj.getString("userfrom_nama"));
                        String userto_nomor = (obj.getString("userto_nomor"));
                        String userto_nama = (obj.getString("userto_nama"));
                        String message = (obj.getString("message"));
                        String url = (obj.getString("url"));
                        String tipe = (obj.getString("tipe"));
                        String date = (obj.getString("waktu"));
                        String status = (obj.getString("status_read"));

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date newdate = sdf.parse(date);
                        date = sdf.format(newdate);
                        String userid = Index.globalfunction.getShared("user","id","");
                        if (userid.equals(userfrom_nomor)) {
                            userfrom_nama = "You";
                            messageadapter.insert(new PrivateMessageAdapter(nomor, userfrom_nama, message, tipe, url, date, status), 0);
                            messageadapter.notifyDataSetChanged();
                        } else {
                            //status = "1";
                            String upper_nama = userfrom_nama.substring(0, 1).toUpperCase() + userfrom_nama.substring(1);
                            messageadapter.insert(new PrivateMessageAdapter(nomor, upper_nama, message, tipe, url, date, status), 0);
                            messageadapter.notifyDataSetChanged();
                        }

                        lv_message.setSelection(messageadapter.getCount() - (counter * 10));

                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() { isLoading = false; }
                                },
                                1000);
                    }
                }
            }
            if(jsonarray.length() < 10){
                scroll = false;
            }
            else {
                scroll = true;
            }

            hideLoading();
        }catch(Exception e)
        {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed load message", Toast.LENGTH_LONG).show();
            hideLoading();
        }
    }

    private class readPrivateMessage extends AsyncTask<String, Void, String> {
        String userfrom_nomor = Index.globalfunction.getShared("user","id","");
        String userto_nomor = Index.globalfunction.getShared("message","userto_nomor","");

        @Override
        protected String doInBackground(String... urls) {
            try {
                Index.jsonObject = new JSONObject();
                Index.jsonObject.put("userfrom_nomor", userfrom_nomor);
                Index.jsonObject.put("userto_nomor", userto_nomor);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return Index.globalfunction.executePost(urls[0],Index.jsonObject);
        }
    }

    private class checkNewMessage extends AsyncTask<String, Void, String> {
        String userfrom_nomor = Index.globalfunction.getShared("user","id","");
        String userto_nomor = Index.globalfunction.getShared("message","userto_nomor","");

        @Override
        protected String doInBackground(String... urls) {
            try {
                Index.jsonObject = new JSONObject();
                Index.jsonObject.put("limit", 0);
                Index.jsonObject.put("userfrom_nomor", userfrom_nomor);
                Index.jsonObject.put("userto_nomor", userto_nomor);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return Index.globalfunction.executePost(urls[0],Index.jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(!obj.has("query")){
                            String nomor = (obj.getString("nomor"));
                            String userfrom_nomor = (obj.getString("userfrom_nomor"));
                            String userfrom_nama = (obj.getString("userfrom_nama"));
                            String userto_nomor = (obj.getString("userto_nomor"));
                            String userto_nama = (obj.getString("userto_nama"));
                            String message = (obj.getString("message"));
                            String url = (obj.getString("url"));
                            String tipe = (obj.getString("tipe"));
                            String date = (obj.getString("waktu"));
                            String status = (obj.getString("status_read"));

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date newdate = sdf.parse(date);
                            date = sdf.format(newdate);
                            String userid = Index.globalfunction.getShared("user","id","");
                            if (userid.equals(userfrom_nomor)) {
                                userfrom_nama = "You";
                            } else {
                                for (int j = 0; j < messageadapter.getCount(); j++) {
                                    PrivateMessageAdapter adapter = messageadapter.getItem(j);
                                    if(adapter.getNama().toString().equals("You")){
                                        adapter.setStatus("2");
                                        messageadapter.notifyDataSetChanged();
                                    }
                                }
                                Boolean kembar = false;
                                for (int j = 0; j < messageadapter.getCount(); j++) {
                                    PrivateMessageAdapter adapter = messageadapter.getItem(j);
                                    if(adapter.getNomor().equals(nomor)){
                                        Log.d("kembar", "kembar");
                                        kembar = true;
                                        break;
                                    }
                                }
                                if(!kembar){
                                    String upper_nama = userfrom_nama.substring(0, 1).toUpperCase() + userfrom_nama.substring(1);
                                    messageadapter.add(new PrivateMessageAdapter(nomor, upper_nama, message, tipe, url, date, status));
                                    messageadapter.notifyDataSetChanged();
                                    lv_message.smoothScrollToPosition(messageadapter.getCount() -1);
                                }
                                String actionUrlread = "Message/readPrivateMessage/";
                                new readPrivateMessage().execute(actionUrlread);
                            }
                        }
                    }
                }
            }catch(Exception e) { e.printStackTrace(); }
            isLoading = false;
            Integer private_run_check = Integer.valueOf(Index.globalfunction.getShared("message","private_run_check","")) + 1;
            Index.globalfunction.setShared("message","private_run_check", String.valueOf(private_run_check));
        }
    }

    /** Show an event in the LogCat view, for debugging */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == getActivity().RESULT_OK) {
                tipe_send = "2";
                //copy to app
                try { copyMedia(decodeFile(mCurrentPhotoPathRaw, 1920, 1080), mCurrentPhotoName); }
                catch (IOException e) { e.printStackTrace(); }
                previewmedia = "photo";
                setPreviewMedia(mCurrentPhotoName);
            } else if (resultCode == getActivity().RESULT_CANCELED) { // user cancelled Image capture
                Toast.makeText(getContext(), "User cancelled image capture", Toast.LENGTH_SHORT).show();
            } else { // failed to capture image
                Toast.makeText(getContext(), "Sorry! Failed to capture image", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_VIDEO_CAPTURE) {
            if (resultCode == getActivity().RESULT_OK) {
                tipe_send = "3";
                //copy to app
                try { copyMedia(mCurrentVideoPathRaw, mCurrentVideoName); }
                catch (IOException e) { e.printStackTrace(); }
                previewmedia = "video";
                setPreviewMedia(mCurrentVideoName);
            } else if (resultCode == getActivity().RESULT_CANCELED) { // user cancelled recording
                Toast.makeText(getActivity(), "User cancelled video recording", Toast.LENGTH_SHORT).show();
            } else { // failed to record video
                Toast.makeText(getActivity(), "Sorry! Failed to record video", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            realPath = MediaFilePath.getPath(getActivity().getBaseContext(), data.getData());
            File f = new File(realPath);
            realName = f.getName();
            tipe_send = "4";
            //copy to app
            try { copyMedia(decodeFile(realPath, 1920, 1080), realName); }
            catch (IOException e) { e.printStackTrace(); }
            previewmedia = "photo";
            setPreviewMedia(realName);
        } else if (requestCode == PICK_VIDEO_REQUEST && resultCode == getActivity().RESULT_OK){
            realPath = MediaFilePath.getPath(getActivity().getBaseContext(), data.getData());
            File f = new File(realPath);
            realName = f.getName();

            File file = new File(realPath);
            long length = file.length();
            length = length/1024;
            if(length > 5000){ //more than 5 KB
                Toast.makeText(getActivity(), "Sorry, video size too large.", Toast.LENGTH_LONG).show();
            }else {
                tipe_send = "5";
                //copy to app
                try { copyMedia(realPath, realName); }
                catch (IOException e) { e.printStackTrace(); }
                previewmedia = "video";
                setPreviewMedia(realName);
            }
        }
    }

    /*-----------------------TAKE PHOTO-----------------------*/
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath(), "GMR");

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

    private String decodeFile(String path,int DESIREDWIDTH, int DESIREDHEIGHT) {
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

            String s = null;
            if(tipe_send.equals("2")){
                s = mCurrentPhotoName;
            }else if(tipe_send.equals("4")){
                s = realName;
            }

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
        } catch (Throwable e) { }

        if (strMyImagePath == null) { return path; }
        return strMyImagePath;
    }

    /*-----------------------------TAKE VIDEO-----------------------------*/
    private File createVideoFile() throws IOException {
        // Create an video file name
        String timeStamp = new SimpleDateFormat( "yyyyMMdd_HHmmss" ).format( new Date() );
        String videoFileName = "MP4_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath(), "GMR");
        if(!storageDir.exists()){
            storageDir.mkdirs();
        }
        File video = File.createTempFile(
                videoFileName,  /* prefix */
                ".mp4",         /* suffix */
                storageDir      /* directory */
        );

        mCurrentVideoPath = "file:" + video.getAbsolutePath();
        mCurrentVideoPathRaw = video.getAbsolutePath();
        mCurrentVideoName = video.getName();
        return video;
    }

    private void copyMedia(String selectedImagePath, String selectedRealName) throws IOException {
        isLoading = true; showLoading();
        File storageDir = new File(getContext().getFilesDir().getAbsolutePath());
        File newFile = new File(storageDir.getAbsolutePath(), selectedRealName);
        InputStream in = new FileInputStream(selectedImagePath);
        OutputStream out = new FileOutputStream(newFile.getAbsolutePath());

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
        isLoading = false; hideLoading();
    }

    private void saveImage(Context context, Bitmap b, String imageName) {
        FileOutputStream foStream;
        try {
            foStream = context.openFileOutput(imageName, Context.MODE_PRIVATE);
            b.compress(Bitmap.CompressFormat.JPEG, 75, foStream);
            foStream.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public Bitmap loadImageBitmap(Context context, String imageName) {
        Bitmap bitmap = null;
        FileInputStream fiStream;
        try {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inSampleSize = 1 / 4; // 1 = 100% if you write 4 means 1/4 = 25%
            bmOptions.inPreferredConfig = Bitmap.Config.RGB_565;
            fiStream    = context.openFileInput(imageName);
            bitmap      = BitmapFactory.decodeStream(fiStream, null, bmOptions);
            fiStream.close();
        } catch (Exception e) { e.printStackTrace(); }
        return bitmap;
    }

    public Bitmap loadVideoThumbnailBitmap(Context context, String videoName){
        Bitmap bitmap = null;
        try {
            Uri location = Uri.fromFile(context.getFileStreamPath(videoName));
            MediaMetadataRetriever media = new MediaMetadataRetriever();
            ParcelFileDescriptor parcel = ParcelFileDescriptor.open(new File(location.getPath()),ParcelFileDescriptor.MODE_READ_ONLY); //Permission for load file on this application
            media.setDataSource(parcel.getFileDescriptor());
            bitmap = media.getFrameAtTime(0 , MediaMetadataRetriever.OPTION_CLOSEST );
        } catch (FileNotFoundException e) { e.printStackTrace(); }
        return bitmap;
    }

    public void doOpenImage(String imageName){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(Uri.parse("content://com.adzzblack.gmr/" + imageName), "image/*");
        startActivity(intent);
    }

    public void doOpenVideo(String videoName){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(Uri.parse("content://com.adzzblack.gmr/" + videoName), "video/*");
        startActivity(intent);
    }

    class PrivateMessageAdapter {
        private String nomor;
        private String nama;
        private String message;
        private String tipe;
        private String url;
        private String time;
        private String status;

        public PrivateMessageAdapter(String nomor, String nama, String message, String tipe, String url, String time, String status) {
            this.setNomor(nomor);
            this.setNama(nama);
            this.setMessage(message);
            this.setTipe(tipe);
            this.setUrl(url);
            this.setTime(time);
            this.setStatus(status);
        }

        public String getNomor() {
            return nomor;
        }

        public void setNomor(String nomor) {
            this.nomor = nomor;
        }

        public String getNama() {
            return nama;
        }

        public void setNama(String nama) {
            this.nama = nama;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getTipe() {
            return tipe;
        }

        public void setTipe(String tipe) {
            this.tipe = tipe;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    class PrivateMessageListAdapter extends ArrayAdapter<PrivateMessageAdapter> {

        private List<PrivateMessageAdapter> items;
        private int layoutResourceId;
        private Context context;

        public PrivateMessageListAdapter(Context context, int layoutResourceId, List<PrivateMessageAdapter> items) {
            super(context, layoutResourceId, items);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.items = items;
        }

        public class Holder {
            PrivateMessageAdapter adapterMessage;
            RelativeLayout item_list, contentmessage, back_item_list;
            TextView unreadmessage, date, message, timein, timeout, status, namereply, messagereply;
            LinearLayout datecontent, messagedesc, contentmessagereply;
            ImageView foto, video, videoicon, fotoreply, videoreply;
            Bitmap bitmap;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            Holder holder = null;

            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new Holder();
            holder.adapterMessage = items.get(position);

            holder.item_list = (RelativeLayout)row.findViewById(R.id.privatemessage_listitem);
            holder.back_item_list = (RelativeLayout) row.findViewById(R.id.privatemessage_backcontentmessage);
            holder.message = (TextView)row.findViewById(R.id.tv_message);
            holder.datecontent = (LinearLayout)row.findViewById(R.id.privatemessage_date);
            holder.date = (TextView)row.findViewById(R.id.tv_datemessage);
            holder.unreadmessage = (TextView) row.findViewById(R.id.tv_unreadmessage);
            holder.timein = (TextView)row.findViewById(R.id.tv_timemessagein);
            holder.timeout = (TextView)row.findViewById(R.id.tv_timemessageout);
            holder.status = (TextView)row.findViewById(R.id.tv_messageread);
            holder.messagedesc = (LinearLayout) row.findViewById(R.id.privatemessage_desc);
            holder.contentmessage = (RelativeLayout) row.findViewById(R.id.privatemessage_contentmessage);
            holder.foto = (ImageView) row.findViewById(R.id.iv_photomessage);
            holder.video = (ImageView) row.findViewById(R.id.iv_videomessage);
            holder.videoicon = (ImageView) row.findViewById(R.id.iv_videomessage_icontrans);
            holder.contentmessagereply = (LinearLayout) row.findViewById(R.id.privatemessage_contentmessagereply);
            holder.namereply = (TextView) row.findViewById(R.id.tv_messagereplyfrom);
            holder.messagereply = (TextView) row.findViewById(R.id.tv_messagereply);
            holder.fotoreply = (ImageView) row.findViewById(R.id.iv_photomessagereply);
            holder.videoreply = (ImageView) row.findViewById(R.id.iv_videomessagereply);

            row.setTag(holder);
            setupItem(holder);

            final Holder finalHolder = holder;
            holder.back_item_list.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    v.startAnimation(Index.listeffect);
                    clickMessage(finalHolder.adapterMessage.getNomor());
                    return true;
                }
            });

            holder.back_item_list.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.startAnimation(Index.listeffect);
                    clickMessage(finalHolder.adapterMessage.getNomor());
                }
            });

            return row;
        }

        private int getPosition(final Holder holder){
            int position = 0;
            for (int j = 0; j < messageadapter.getCount(); j++) {
                PrivateMessageAdapter adapter = messageadapter.getItem(j);
                if(adapter.getNomor().toString().equals(holder.adapterMessage.getNomor())){ position = j; }
            }
            return position;
        }

        private void setClickMessage(final Holder holder){
            holder.message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.startAnimation(Index.listeffect);
                    clickMessage(holder.adapterMessage.getNomor());
                }
            });
            holder.message.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    v.startAnimation(Index.listeffect);
                    clickMessage(holder.adapterMessage.getNomor());
                    return true;
                }
            });
        }

        private void setClickMediaMessage(final Holder holder){
            holder.contentmessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.startAnimation(Index.listeffect);
                    clickMessage(holder.adapterMessage.getNomor());
                }
            });
            holder.contentmessage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    v.startAnimation(Index.listeffect);
                    clickMessage(holder.adapterMessage.getNomor());
                    return true;
                }
            });
        }

        private void setPic(final Holder holder, final String urlImage){
            class DownloadImage extends AsyncTask<String, Void, Bitmap> {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    isLoading = true;
                    showLoading();
                }

                private Bitmap downloadImageBitmap(String sUrl) {
                    Bitmap bitmap = null;
                    try {
                        InputStream inputStream = new URL(sUrl).openStream();   // Download Image from URL
                        bitmap = BitmapFactory.decodeStream(inputStream);       // Decode Bitmap
                        inputStream.close();
                    } catch (Exception e) { e.printStackTrace(); }
                    return bitmap;
                }

                @Override
                protected Bitmap doInBackground(String... params) { return downloadImageBitmap(urlImage); }

                protected void onPostExecute(Bitmap result) {
                    File file = new File(getContext().getFileStreamPath(holder.adapterMessage.getUrl()).getAbsolutePath());
                    if(file.exists()){file.delete();}
                    saveImage(getContext(), result, holder.adapterMessage.getUrl());
                    holder.foto.setImageBitmap(loadImageBitmap(getContext(), holder.adapterMessage.getUrl()));
                    isLoading = false;
                    hideLoading();
                    messageadapter.notifyDataSetChanged();
                }
            }

            boolean checkFile = false;
            final File storageDir = new File(getContext().getFileStreamPath(holder.adapterMessage.getUrl()).getAbsolutePath());
            if(storageDir.exists()){
                checkFile = true;
                holder.foto.setImageBitmap(loadImageBitmap(getContext(), holder.adapterMessage.getUrl()));
            }else{
                File checkfile = new File(getActivity().getFileStreamPath(holder.adapterMessage.getUrl()).getAbsolutePath());
                if(!holder.adapterMessage.getNama().equals("You") && holder.adapterMessage.getStatus().equals("1") && !checkfile.exists()){
                    new DownloadImage().execute(urlImage);
                    isLoading = false; hideLoading();
                }
            }

            final boolean finalCheckFile = checkFile;
            holder.foto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!finalCheckFile){ new DownloadImage().execute(urlImage); }
                    if(!isLoading && running){ doOpenImage(holder.adapterMessage.getUrl()); }
                }
            });
        }

        private void setVid(final Holder holder, final String urlVideo){
            class DownloadVideo extends AsyncTask<Void, Void, Void> {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    isLoading = true;
                    showLoading();
                }

                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        URL url = new URL(urlVideo);
                        URLConnection connection = url.openConnection();
                        InputStream inputStream = new BufferedInputStream(url.openStream(), 10240);
                        File filesDir = new File(getContext().getFilesDir().getAbsolutePath());
                        File filesFile = new File(filesDir, holder.adapterMessage.getUrl());
                        FileOutputStream outputStream = new FileOutputStream(filesFile);

                        byte buffer[] = new byte[1024];
                        int dataSize;
                        int loadedSize = 0;
                        while ((dataSize = inputStream.read(buffer)) != -1) {
                            loadedSize += dataSize;
                            outputStream.write(buffer, 0, dataSize);
                        }
                        outputStream.close();
                    }catch (IOException e){ e.printStackTrace(); }
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    holder.video.setImageBitmap(loadVideoThumbnailBitmap(getContext(), holder.adapterMessage.getUrl()));
                    holder.videoicon.setVisibility(View.VISIBLE);
                    isLoading = false;
                    hideLoading();
                    messageadapter.notifyDataSetChanged();
                }
            }

            boolean checkFile = false;
            final File storageDir = new File(getContext().getFileStreamPath(holder.adapterMessage.getUrl()).getAbsolutePath());
            if(storageDir.exists()){
                checkFile = true;
                holder.video.setImageBitmap(loadVideoThumbnailBitmap(getContext(), holder.adapterMessage.getUrl()));
                holder.videoicon.setVisibility(View.VISIBLE);
            }else {
                File checkfile = new File(getActivity().getFileStreamPath(holder.adapterMessage.getUrl()).getAbsolutePath());
                if(!holder.adapterMessage.getNama().equals("You") && holder.adapterMessage.getStatus().equals("1") && !checkfile.exists()){
                    new DownloadVideo().execute();
                    isLoading = false; hideLoading();
                }
            }

            final boolean finalCheckFile = checkFile;
            holder.video.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!finalCheckFile){ new DownloadVideo().execute(); }
                    if(!isLoading && running){ doOpenVideo(holder.adapterMessage.getUrl()); }
                }
            });
        }

        private void setupItem(final Holder holder) {
            String date = holder.adapterMessage.getTime();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                TimeZone timeZone = TimeZone.getTimeZone("Asia/Jakarta"); //change to date Timezone server - Asia/Jakarta
                sdf.setTimeZone(timeZone);
                Date newdate = sdf.parse(date);

                SimpleDateFormat sdfTimezone = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                timeZone = TimeZone.getTimeZone(TimeZone.getDefault().getID()); //from Timezone server to Timezone app
                sdfTimezone.setTimeZone(timeZone);
                String sDateInTimeZone = sdfTimezone.format(newdate); // Convert to String first
                Date dateInTimeZone = sdf.parse(sDateInTimeZone); // Create a new Date object
                date = sdf.format(dateInTimeZone);

                SimpleDateFormat dateformat = new SimpleDateFormat("EEE, dd MMM yyyy");
                Date d_today_db = sdfTimezone.parse(date);
                String s_today_db = dateformat.format(d_today_db);

                //get today date
                String s_today_system = dateformat.format(new Date());
                Date d_today_system = dateformat.parse(s_today_system);
                s_today_system = dateformat.format(d_today_system);

                //get yesterday date
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DATE, -1);
                String s_yesterday_system = dateformat.format(c.getTime());
                Date d_yesterday_system = dateformat.parse(s_yesterday_system);
                s_yesterday_system = dateformat.format(d_yesterday_system);

                //show time
                SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm");
                Date dt_today_db = sdfTimezone.parse(date);
                String st_today_db = timeformat.format(dt_today_db);
                String time = st_today_db;

                holder.timein.setText("(" + time + ")");
                holder.timeout.setText("(" + time + ")");

                //show date
                int position = 0;
                String s_before_today_db = "";
                for (int j = 0; j < messageadapter.getCount(); j++) {
                    PrivateMessageAdapter adapter = messageadapter.getItem(j);
                    if(adapter.getNomor().toString().equals(holder.adapterMessage.getNomor())) {
                        if (j > 0){
                            position = j - 1;
                            adapter = messageadapter.getItem(position);
                            s_before_today_db = adapter.getTime();
                        }
                    }
                }
                if(!s_before_today_db.equals("")){
                    Date beforedate = sdf.parse(s_before_today_db);
                    s_before_today_db = sdf.format(beforedate);
                    Date d_before_today_db = sdf.parse(s_before_today_db);
                    s_before_today_db = dateformat.format(d_before_today_db);

                    if(!s_before_today_db.equals(s_today_db)) {
                        if (s_today_db.equals(s_today_system)) {
                            holder.date.setText("Today");
                            holder.date.setVisibility(View.VISIBLE);
                        } else if (s_today_db.equals(s_yesterday_system)) {
                            holder.date.setText("Yesterday");
                            holder.date.setVisibility(View.VISIBLE);
                        } else {
                            holder.date.setText(s_today_db);
                            holder.date.setVisibility(View.VISIBLE);
                        }
                    }
                }
                if(String.valueOf(position).equals("0") && s_before_today_db.equals("")){
                    if (s_today_db.equals(s_today_system)) {
                        holder.date.setText("Today");
                        holder.date.setVisibility(View.VISIBLE);
                    } else if (s_today_db.equals(s_yesterday_system)) {
                        holder.date.setText("Yesterday");
                        holder.date.setVisibility(View.VISIBLE);
                    } else {
                        holder.date.setText(s_today_db);
                        holder.date.setVisibility(View.VISIBLE);
                    }
                }
            } catch(Exception e) { e.printStackTrace(); }

            RelativeLayout.LayoutParams Rparams = (RelativeLayout.LayoutParams) holder.contentmessage.getLayoutParams();
            if(holder.adapterMessage.getNama().equals("You")){
                Rparams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                holder.contentmessage.setLayoutParams(Rparams);
                if(holder.adapterMessage.getStatus().equals("2")){ holder.status.setVisibility(View.VISIBLE); }
            }else {
                boolean check = false;
                if(holder.adapterMessage.getStatus().equals("1")){
                    PrivateMessageAdapter adapter = messageadapter.getItem(getPosition(holder) - 1); //check before this list
                    if(adapter.getNama().equals("You") || (!adapter.getNama().equals("You") && adapter.getStatus().equals("2"))){check = true; }
                }
                if(check) {
                    holder.unreadmessage.setVisibility(View.VISIBLE);
                    int count_unread = 0;
                    for (int j = 0; j < messageadapter.getCount(); j++) {
                        PrivateMessageAdapter adapter = messageadapter.getItem(j);
                        if (adapter.getStatus().toString().equals("1") && !adapter.getNama().equals("You")) {count_unread += 1;}
                    }
                    if (count_unread > 1) {holder.unreadmessage.setText("  " + count_unread + " unread messages  ");}
                    lv_message.smoothScrollToPosition(getPosition(holder));
                }
                if (Build.VERSION.SDK_INT >= 21) { holder.contentmessage.setBackground(getResources().getDrawable(R.drawable.in_message));
                } else { holder.contentmessage.setBackgroundDrawable(getResources().getDrawable(R.drawable.in_message)); }
                holder.messagedesc.setVisibility(View.GONE);
            }

            if(holder.adapterMessage.getTipe().equals("1") || !holder.adapterMessage.getMessage().equals("")){
                holder.message.setVisibility(View.VISIBLE);
                holder.message.setText(holder.adapterMessage.getMessage());
                holder.message.setTextIsSelectable(true);
                int ctrmsg = holder.adapterMessage.getMessage().trim().length();
                DisplayMetrics dm = new DisplayMetrics();
                WindowManager windowManager = (WindowManager) getContext().getSystemService(WINDOW_SERVICE);
                windowManager.getDefaultDisplay().getMetrics(dm);
                int widthInDP = Math.round(dm.widthPixels / dm.density);
                holder.message.setMaxWidth(widthInDP);
                //if(ctrmsg > 35){ holder.message.setMaxWidth(400);}
            }else { holder.message.setVisibility(View.GONE); }

            if(holder.adapterMessage.getTipe().equals("1")){
                setClickMessage(holder);
            }else if(holder.adapterMessage.getTipe().equals("2")){
                holder.foto.setVisibility(View.VISIBLE);
                final String urlImage = Index.globalfunction.getImageURL() + holder.adapterMessage.getUrl();
                setPic(holder, urlImage);
                setClickMediaMessage(holder);
            }else if(holder.adapterMessage.getTipe().equals("3")){
                holder.video.setVisibility(View.VISIBLE);
                final String urlVideo = Index.globalfunction.getImageURL() + holder.adapterMessage.getUrl();
                setVid(holder, urlVideo);
                setClickMediaMessage(holder);
            }else if(holder.adapterMessage.getTipe().equals("4")){
                String[] separated = holder.adapterMessage.getMessage().split("\\-R3p7y-");
                final int nomorreply = Integer.parseInt(separated[0]);
                holder.message.setText(separated[1]);
                holder.contentmessagereply.setVisibility(View.VISIBLE);
                int positionreply = -1;
                for (int j = 0; j < messageadapter.getCount(); j++) {
                    PrivateMessageAdapter adapter = messageadapter.getItem(j);
                    if (adapter.getNomor().toString().equals(String.valueOf(nomorreply))) { positionreply = j;}
                }
                if(positionreply == -1){
                    new AsyncTask<String, Void, String>() {
                        @Override
                        protected String doInBackground(String... urls) {
                            try {
                                Index.jsonObject = new JSONObject();
                                Index.jsonObject.put("nomor", String.valueOf(nomorreply));
                            } catch (JSONException e) { e.printStackTrace(); }

                            return Index.globalfunction.executePost(urls[0],Index.jsonObject);
                        }
                        @Override
                        protected void onPostExecute(String result) {
                            try {
                                JSONArray jsonarray = new JSONArray(result);
                                for (int i = 0; i < jsonarray.length(); i++) {
                                    JSONObject obj = jsonarray.getJSONObject(i);
                                    if(!obj.has("query")){
                                        String userfrom_nomor = (obj.getString("userfrom_nomor"));
                                        String userfrom_nama = (obj.getString("userfrom_nama"));
                                        String message = (obj.getString("message"));
                                        String url = (obj.getString("url"));
                                        String tipe = (obj.getString("tipe"));
                                        holder.namereply.setText("You");
                                        String userid = Index.globalfunction.getShared("user","id","");
                                        if (!userid.equals(userfrom_nomor)) {
                                            String upper_nama = userfrom_nama.substring(0, 1).toUpperCase() + userfrom_nama.substring(1);
                                            holder.namereply.setText(upper_nama);
                                        }
                                        if(message.length() > 30){ message = message.substring(0, 30) + "..."; }
                                        if(tipe.equals("1")){ holder.messagereply.setText(message);
                                        }else if(tipe.equals("2")){
                                            holder.messagereply.setText("[Photo]" + '\n' + message);
                                            holder.fotoreply.setVisibility(View.VISIBLE);
                                            File storageDir = new File(getContext().getFileStreamPath(url).getAbsolutePath());
                                            if(storageDir.exists()){holder.fotoreply.setImageBitmap(loadImageBitmap(getContext(), url));}
                                        }else if(tipe.equals("3")){
                                            holder.messagereply.setText("[Video]" + '\n' + message);
                                            holder.videoreply.setVisibility(View.VISIBLE);
                                            File storageDir = new File(getContext().getFileStreamPath(url).getAbsolutePath());
                                            if(storageDir.exists()){holder.videoreply.setImageBitmap(loadVideoThumbnailBitmap(getContext(), url));}
                                        }else if(tipe.equals("4")){
                                            String[] separated = message.split("\\-R3p7y-");
                                            holder.messagereply.setText(separated[1]);
                                        }
                                    }else{
                                        Toast.makeText(getContext(), "Failed get message", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }catch(Exception e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), "Failed get message", Toast.LENGTH_LONG).show();
                            }
                        }
                    }.execute("Message/getSpecificPrivateMessage/");

                }else {
                    holder.namereply.setText(messageadapter.items.get(positionreply).getNama());
                    String tampmsg = messageadapter.items.get(positionreply).getMessage();
                    if(messageadapter.items.get(positionreply).getMessage().length() > 30) {
                        tampmsg = messageadapter.items.get(positionreply).getMessage().substring(0, 30) + "...";
                    }
                    if(messageadapter.items.get(positionreply).getTipe().equals("1")){
                        holder.messagereply.setText(tampmsg);
                    }else if(messageadapter.items.get(positionreply).getTipe().equals("2")){
                        holder.messagereply.setText("[Photo]" + '\n' + tampmsg);
                        holder.fotoreply.setVisibility(View.VISIBLE);
                        File storageDir = new File(getContext().getFileStreamPath(messageadapter.items.get(positionreply).getUrl()).getAbsolutePath());
                        if(storageDir.exists()){holder.fotoreply.setImageBitmap(loadImageBitmap(getContext(), messageadapter.items.get(positionreply).getUrl()));}
                    }else if(messageadapter.items.get(positionreply).getTipe().equals("3")){
                        holder.messagereply.setText("[Video]" + '\n' + tampmsg);
                        holder.videoreply.setVisibility(View.VISIBLE);
                        File storageDir = new File(getContext().getFileStreamPath(messageadapter.items.get(positionreply).getUrl()).getAbsolutePath());
                        if(storageDir.exists()){holder.videoreply.setImageBitmap(loadVideoThumbnailBitmap(getContext(), messageadapter.items.get(positionreply).getUrl()));}
                    }else if(messageadapter.items.get(positionreply).getTipe().equals("4")){
                        separated = tampmsg.split("\\-R3p7y-");
                        holder.messagereply.setText(separated[1]);
                    }
                }
                final int finalPosition = positionreply;
                holder.contentmessagereply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(finalPosition == -1){
                            Toast.makeText(getContext(), "The message still not shown on this page, please scroll up!", Toast.LENGTH_LONG).show();
                        }else {
                            lv_message.smoothScrollToPosition(finalPosition);
                        }
                    }
                });
            }

            if(selectedlist.size() > 0){
                for(int i=0; i<selectedlist.size(); i++){
                    if(holder.adapterMessage.getNomor().equals(selectedlist.get(i))){
                        if(holder.adapterMessage.getNama().equals("You")){
                            if (Build.VERSION.SDK_INT >= 21) { holder.contentmessage.setBackground(getResources().getDrawable(R.drawable.selected_out_message));
                            } else { holder.contentmessage.setBackgroundDrawable(getResources().getDrawable(R.drawable.selected_out_message)); }
                        }else {
                            if (Build.VERSION.SDK_INT >= 21) { holder.contentmessage.setBackground(getResources().getDrawable(R.drawable.selected_in_message));
                            } else { holder.contentmessage.setBackgroundDrawable(getResources().getDrawable(R.drawable.selected_in_message)); }
                        }
                        holder.item_list.setBackgroundColor(Color.parseColor("#cde7f0"));
                        holder.datecontent.setBackgroundColor(Color.WHITE);
                    }
                }
            }
        }
    }
}