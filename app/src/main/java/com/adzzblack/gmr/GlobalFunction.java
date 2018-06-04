package com.adzzblack.gmr;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

/**
 * Created by ADI on 4/6/2017.
 */

public class GlobalFunction {
    private static Context context;
    private static String hostUrl;
    private static String uploadUrl;
    private static String uploadPdfUrl;
    private static String imageUrl;
    private static String serverimageUrl;

    private static JSONObject jsonObject;
    private static String result;
    private static String classDialog;

    public static void setResult(String result) { GlobalFunction.result = result; }
    public static void setHostUrl(String hostUrl) { GlobalFunction.hostUrl = hostUrl; }
    public static void setUploadUrl(String uploadUrl) { GlobalFunction.uploadUrl = uploadUrl; }
    public static void setImageUrl(String imageUrl) { GlobalFunction.imageUrl = imageUrl; }
    public static void setServerImageUrl(String serverimageUrl) { GlobalFunction.serverimageUrl = serverimageUrl; }
    public static void setClassDialog(String classDialog) { GlobalFunction.classDialog = classDialog; }
    public static void setJsonObject(JSONObject jsonObject) { GlobalFunction.jsonObject = jsonObject; }

    public static String getResult() { return result; }
    public static String getHostURL() {
        return hostUrl;
    }
    public static String getUploadURL() { return uploadUrl;}
    public static String getUploadPDFURL() { return uploadPdfUrl;}
    public static String getImageURL() { return imageUrl;}
    public static String getServerImageURL() { return serverimageUrl;}
    public static String getClassDialog() { return classDialog; }
    public static JSONObject getJsonObject() { return jsonObject; }

    private static SharedPreferences usersharedpreferences, serversharedpreferences, bangunansharedpreferences, elevasisharedpreferences, globalsharedpreferences, rabsharedpreferences, messagesharedpreferences;

    public static String pdf_folder = "/GMR/PDF Files";

    public GlobalFunction(Context mContext)
    {
        context = mContext;
        serversharedpreferences = mContext.getSharedPreferences("server", Context.MODE_PRIVATE);
        bangunansharedpreferences = mContext.getSharedPreferences("bangunan", Context.MODE_PRIVATE);
        usersharedpreferences = mContext.getSharedPreferences("user", Context.MODE_PRIVATE);
        elevasisharedpreferences = mContext.getSharedPreferences("elevasi", Context.MODE_PRIVATE);
        globalsharedpreferences = mContext.getSharedPreferences("position", Context.MODE_PRIVATE);
        rabsharedpreferences = mContext.getSharedPreferences("rab", Context.MODE_PRIVATE);
        messagesharedpreferences = mContext.getSharedPreferences("message", Context.MODE_PRIVATE);

        String url = getShared("server", "servernow", "");
        hostUrl = "http://" + url + "/wsGMR/gmr/index.php/api/";
        uploadUrl = "http://" + url + "/wsGMR/upload.php";
        uploadPdfUrl = "http://" + url + "/gmr/upload.php";
        imageUrl = "http://" + url + "/wsGMR/uploads/";
        serverimageUrl = "http://" + url + "/gmr/uploads/";
    }

    public static String delimeter(String txt)
    {
        DecimalFormat format=new DecimalFormat("#,###.00");

        if(txt.equals("null")) return "-";
        double Raw = Double.parseDouble(txt);

        if(Raw == (long) Raw)
            return String.format("%d",(long)Raw);
        else
            return String.format("%s",Raw);

//        String result = String.valueOf(format.format(Raw));
//        return result;
    }

    public static String getVersion(Context _context)
    {
        String version = "";
        try
        {
            PackageInfo pInfo = _context.getPackageManager().getPackageInfo(_context.getPackageName(), 0);
            version = pInfo.versionName;
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    // Execute POST JSON and Retrieve Data JSON
    public static String executePost(String targetURL, JSONObject jsonObject){
        String url = getShared("server", "servernow", "");
        hostUrl = "http://" + url + "/wsGMR/gmr/index.php/api/";

        Log.d("host", hostUrl + targetURL);

        InputStream inputStream = null;
        String result = "";
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            HttpParams httpParameters = httpclient.getParams();

            //modified by Tonny @04-Jun-2018 connection timeout dijadikan 10 detik supaya data yg banyak bisa diretrieve
            HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
            HttpConnectionParams.setSoTimeout(httpParameters, 10000);
//            HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
//            HttpConnectionParams.setSoTimeout(httpParameters, 3000);
            HttpConnectionParams.setTcpNoDelay(httpParameters, true);

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost( hostUrl + targetURL );

            // 3. convert JSONObject to JSON to String
            String json = jsonObject.toString();

            // 4. ** Alternative way to convert Person object to JSON string usin Jackson Lib
            // ObjectMapper mapper = new ObjectMapper();
            // json = mapper.writeValueAsString(person);

            // 5. set json to StringEntity
            StringEntity stringEntity = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(stringEntity);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
//            Log.d("InputStream", e.getLocalizedMessage());
            Log.d("InputStream", "error");
        }

        // 11. return result
        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }

    public static String getShared(String preference, String key, String defaultValue) {
        if (preference.equals("user")) return usersharedpreferences.getString(key, defaultValue);
        else if (preference.equals("server")) return serversharedpreferences.getString(key, defaultValue);
        else if (preference.equals("bangunan")) return bangunansharedpreferences.getString(key, defaultValue);
        else if (preference.equals("elevasi")) return elevasisharedpreferences.getString(key, defaultValue);
        else if (preference.equals("global")) return globalsharedpreferences.getString(key, defaultValue);
        else if (preference.equals("rab")) return rabsharedpreferences.getString(key, defaultValue);
        else if (preference.equals("message")) return messagesharedpreferences.getString(key, defaultValue);

        return "";
    }

    public static void setShared(String preference, String key, String param) {
        if (preference.equals("user"))
        {
            SharedPreferences.Editor editor = usersharedpreferences.edit();
            editor.putString(key, param);
            editor.commit();
        }
        else if (preference.equals("server"))
        {
            SharedPreferences.Editor editor = serversharedpreferences.edit();
            editor.putString(key, param);
            editor.commit();
        }
        else if (preference.equals("bangunan"))
        {
            SharedPreferences.Editor editor = bangunansharedpreferences.edit();
            editor.putString(key, param);
            editor.commit();
        }
        else if (preference.equals("elevasi"))
        {
            SharedPreferences.Editor editor = elevasisharedpreferences.edit();
            editor.putString(key, param);
            editor.commit();
        }
        else if (preference.equals("global"))
        {
            SharedPreferences.Editor editor = globalsharedpreferences.edit();
            editor.putString(key, param);
            editor.commit();
        }
        else if (preference.equals("rab"))
        {
            SharedPreferences.Editor editor = rabsharedpreferences.edit();
            editor.putString(key, param);
            editor.commit();
        }
        else if (preference.equals("message"))
        {
            SharedPreferences.Editor editor = messagesharedpreferences.edit();
            editor.putString(key, param);
            editor.commit();
        }
    }

    public static void clearShared(String preference) {
        if (preference.equals("user")) usersharedpreferences.edit().clear().commit();
        else if (preference.equals("server")) serversharedpreferences.edit().clear().commit();
        else if (preference.equals("bangunan")) bangunansharedpreferences.edit().clear().commit();
        else if (preference.equals("elevasi")) elevasisharedpreferences.edit().clear().commit();
        else if (preference.equals("global")) globalsharedpreferences.edit().clear().commit();
        else if (preference.equals("rab")) rabsharedpreferences.edit().clear().commit();
        else if (preference.equals("message")) messagesharedpreferences.edit().clear().commit();
    }
}
