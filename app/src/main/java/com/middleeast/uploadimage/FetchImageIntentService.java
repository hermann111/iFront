package com.middleeast.uploadimage;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Asynchronously handles an intent using a worker thread. Receives a ResultReceiver object and a
 * location through an intent. Tries to fetch the address for the location using a Geocoder, and
 * sends the result to the ResultReceiver.
 */
public class FetchImageIntentService extends IntentService {
    private static final String TAG = "fetch-address";
    private static final String CACHE_FOLDER = "/MRMWR";

    private File cacheDir;

    /**
     * The receiver where results are forwarded from this service.
     */
    protected ResultReceiver mReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        String tmpLocation = Environment.getExternalStorageDirectory().getPath() + CACHE_FOLDER;
        //cacheDir = new File(tmpLocation);
        cacheDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString());
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }
    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public FetchImageIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    /**
     * Tries to get the location address using a Geocoder. If successful, sends an address to a
     * result receiver. If unsuccessful, sends an error message instead.
     * Note: We define a {@link ResultReceiver} in * MainActivity to process content
     * sent from this service.
     *
     * This service calls this method from the default worker thread with the intent that started
     * the service. When this method returns, the service automatically stops.
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        String remoteUrl = intent.getExtras().getString("url");
        String location;
        String filename = remoteUrl.substring(remoteUrl.lastIndexOf(File.separator) + 1);
        File tmp = new File(cacheDir.getPath() + File.separator + filename);

        if (tmp.exists()) {
            location = tmp.getAbsolutePath();
            notifyFinished(location, remoteUrl);
            stopSelf();
            return;
        }
        try {
            URL url = new URL(remoteUrl);
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            if (httpCon.getResponseCode() != 200) {
                throw new Exception("Failed to connect");
            }
            InputStream is = httpCon.getInputStream();
            FileOutputStream fos = new FileOutputStream(tmp);

            byte[] buffer = new byte[1 * 1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }

            //writeStream(is, fos);
            fos.flush();
            fos.close();
            is.close();
            location = tmp.getAbsolutePath();
            notifyFinished(location, remoteUrl);
        } catch (Exception e) {
            Log.e("Service", "Failed!", e);
        }
    }

    public static final String TRANSACTION_DONE ="com.peachpit.TRANSACTION_DONE";
    private void notifyFinished(String location, String remoteUrl){
        Intent i = new Intent(TRANSACTION_DONE);
        i.putExtra("location", location);
        i.putExtra("url", remoteUrl);
        FetchImageIntentService.this.sendBroadcast(i);
    }
}
