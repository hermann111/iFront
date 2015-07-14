/*
 * Copyright 2010-2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.middleeast.uploadimage;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.middleeast.uploadimage.models.TransferModel;
import com.middleeast.uploadimage.network.TransferController;
import com.amazonaws.services.s3.AmazonS3Client;

import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.io.UnsupportedEncodingException;


/* 
 * Activity where the user can see the history of transfers, go to the downloads
 * page, or upload images/videos.
 *
 * The reason we separate image and videos is for compatibility with Android versions
 * that don't support multiple MIME types. We only allow videos and images because
 * they are nice for demonstration
 */

public class ImageVideoActivity extends Activity {
    private boolean exists = false;
    private boolean checked = false;
    private static final int REFRESH_DELAY = 500;
    private static final int SELECT_IMAGE = 100;
    private static final int CAPTURE_IMAGE = 200;
    private static final int SELECT_VIDEO = 300;
    private static final int CAPTURE_VIDEO = 400;

    private final String LOG_TAG = ImageVideoActivity.class.getSimpleName();

    private Timer mTimer;
    private LinearLayout mLayout;
    private TransferModel[] mModels = new TransferModel[0];
    public String serialNo="0";

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#38B0DE")));

        setContentView(R.layout.imagevideoactivity_main);

        Intent i=getIntent();
        serialNo= i.getExtras().getString("ID");

        mLayout = (LinearLayout) findViewById(R.id.transfers_layout);
        new CheckBucketExists().execute();

        findViewById(R.id.image_select).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!checked) {
                    Toast.makeText(getApplicationContext(), "Please wait a moment...",
                            Toast.LENGTH_SHORT).show();
                }
                else if (!exists) {
                    Toast.makeText(getApplicationContext(), "You must first create the bucket",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, SELECT_IMAGE);
                }

            }
        });

        findViewById(R.id.video_select).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!checked) {
                    Toast.makeText(getApplicationContext(), "Please wait a moment...",
                            Toast.LENGTH_SHORT).show();
                }
                else if (!exists) {
                    Toast.makeText(getApplicationContext(), "You must first create the bucket",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("video/*");
                    startActivityForResult(intent, SELECT_VIDEO);
                }

            }
        });

        findViewById(R.id.image_capture).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checked) {
                    Toast.makeText(getApplicationContext(), "Please wait a moment...",
                            Toast.LENGTH_SHORT).show();
                } else if (!exists) {
                    Toast.makeText(getApplicationContext(), "You must first create the bucket",
                            Toast.LENGTH_SHORT).show();
                } else {

                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    // Ensure that there's a camera activity to handle the intent
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        // Create the File where the photo should go
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                            // Error occurred while creating the File...
                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            String fileURI = Uri.fromFile(photoFile).toString();

                            String fileName= fileURI.substring(fileURI.lastIndexOf("/")+1);
                            //String Url="https://s3.amazonaws.com/ishmeet11111/"+fileName;
                            String Url= "http://d281ayci1f2eci.cloudfront.net/" + fileName;
                            String mimeType="1";
                            SaveFileTask savefileTask = new SaveFileTask();
                            savefileTask.execute(Url, mimeType);

                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("fileURI", fileURI).apply();
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                            startActivityForResult(takePictureIntent, CAPTURE_IMAGE);
                        }
                    }

                }
            }
        });

        findViewById(R.id.video_capture).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checked) {
                    Toast.makeText(getApplicationContext(), "Please wait a moment...",
                            Toast.LENGTH_SHORT).show();
                } else if (!exists) {
                    Toast.makeText(getApplicationContext(), "You must first create the bucket",
                            Toast.LENGTH_SHORT).show();
                } else {

                    Intent takePictureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    // Ensure that there's a camera activity to handle the intent
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        // Create the File where the photo should go
                        File photoFile = null;
                        try {
                            photoFile = createVideoFile();
                        } catch (IOException ex) {
                            // Error occurred while creating the File...
                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            String fileURI = Uri.fromFile(photoFile).toString();
                            String fileName= fileURI.substring(fileURI.lastIndexOf("/")+1);
                            //String Url="https://s3.amazonaws.com/ishmeet11111/"+fileName;
                            String Url= "http://d281ayci1f2eci.cloudfront.net/" + fileName;
                            String mimeType="2";
                            SaveFileTask savefileTask = new SaveFileTask();
                            savefileTask.execute(Url, mimeType);
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("fileURI", fileURI).apply();
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                            startActivityForResult(takePictureIntent, CAPTURE_VIDEO);
                        }
                    }

                }
            }
        });

        // make timer that will refresh all the transfer views
        mTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                ImageVideoActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        syncModels();
                        for (int i = 0; i < mLayout.getChildCount(); i++) {
                            ((TransferView) mLayout.getChildAt(i)).refresh();
                        }
                    }
                });
            }
        };
        mTimer.schedule(task, 0, REFRESH_DELAY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                if(this.getLocalClassName().equals("ImageVideoActivity")) {
                    //Toast.makeText(this, this.getLocalClassName(), Toast.LENGTH_LONG).show();
                    Intent openMainActivity= new Intent("com.middleeast.uploadimage.VIOLATIONSCREENACTIVITY");
                    startActivity(openMainActivity);
                }
                else
                {
                    Toast.makeText(this, "Some Other Screen", Toast.LENGTH_LONG).show();
                }
                return true;

            case R.id.ic_action_secure:
                Intent openHome=new Intent("com.middleeast.uploadimage.ADMINISTRATOR");
                //openHome.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                startActivity(openHome);
                return true;

            case R.id.action_language:
                restartInLocale();
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    /*
     * When we get a Uri back from the gallery, upload the associated
     * image/video
     */
    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {

        if(reqCode==CAPTURE_IMAGE && resCode == Activity.RESULT_OK) {

            String name = PreferenceManager.getDefaultSharedPreferences(this).getString("fileURI","defaultValue");
            Uri uri=Uri.parse(name);
            if (uri != null) {
                TransferController.upload(this, uri, null);
            }

        }else  if(reqCode==CAPTURE_VIDEO && resCode == Activity.RESULT_OK) {

            String name = PreferenceManager.getDefaultSharedPreferences(this).getString("fileURI","defaultValue");
            Uri uri=Uri.parse(name);
            if (uri != null) {
                TransferController.upload(this, uri, null);
            }
        }else  if(reqCode==SELECT_IMAGE && resCode == Activity.RESULT_OK) {

            Uri uri = data.getData();
            if (uri != null) {

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "photo_" + timeStamp;
                // Toast.makeText(getApplicationContext(), imageFileName,Toast.LENGTH_LONG).show();
                imageFileName=ArabicToEnglishNumber(imageFileName) +  ".jpg";
                String Url= "http://d281ayci1f2eci.cloudfront.net/" + imageFileName;
                String mimeType="1";
                SaveFileTask savefileTask = new SaveFileTask();
                savefileTask.execute(Url, mimeType);

                TransferController.upload(this, uri, imageFileName);
            }

        }else  if(reqCode==SELECT_VIDEO && resCode == Activity.RESULT_OK) {

            Uri uri = data.getData();
            if (uri != null) {

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "movie_" + timeStamp;
                // Toast.makeText(getApplicationContext(), imageFileName,Toast.LENGTH_LONG).show();
                imageFileName=ArabicToEnglishNumber(imageFileName) +  ".mp4";
                String Url= "http://d281ayci1f2eci.cloudfront.net/" + imageFileName;
                String mimeType="2";
                SaveFileTask savefileTask = new SaveFileTask();
                savefileTask.execute(Url, mimeType);

                TransferController.upload(this, uri, imageFileName);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncModels();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mTimer.purge();
    }

    /* makes sure that we are up to date on the transfers */
    private void syncModels() {
        TransferModel[] models = TransferModel.getAllTransfers();
        if (mModels.length != models.length) {
            // add the transfers we haven't seen yet
            for (int i = mModels.length; i < models.length; i++) {
                mLayout.addView(new TransferView(this, models[i]), 0);
            }
            mModels = models;
        }
    }

    private class CreateBucket extends AsyncTask<Object, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Object... params) {
            AmazonS3Client sS3Client = Util.getS3Client(getApplicationContext());
            if (!Util.doesBucketExist()) {
                Util.createBucket();
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                Toast.makeText(getApplicationContext(), "Bucket already exists", Toast.LENGTH_SHORT)
                        .show();
            }
            else {
                Toast.makeText(getApplicationContext(), "Bucket successfully created!",
                        Toast.LENGTH_SHORT).show();
            }
            exists = true;
        }
    }

    private class CheckBucketExists extends AsyncTask<Object, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Object... params) {
            AmazonS3Client sS3Client = Util.getS3Client(getApplicationContext());
            return Util.doesBucketExist();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            checked = true;
            exists = result;
        }
    }

    private class DeleteBucket extends AsyncTask<Object, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Object... params) {
            AmazonS3Client sS3Client = Util.getS3Client(getApplicationContext());
            if (Util.doesBucketExist()) {
                Util.deleteBucket();
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                Toast.makeText(getApplicationContext(), "Bucket does not exist", Toast.LENGTH_SHORT)
                        .show();
            }
            else {
                Toast.makeText(getApplicationContext(), "Bucket successfully deleted!",
                        Toast.LENGTH_SHORT).show();
            }
            exists = false;
        }
    }

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "photo_" + timeStamp + "_";
       // Toast.makeText(getApplicationContext(), imageFileName,Toast.LENGTH_LONG).show();
        imageFileName=ArabicToEnglishNumber(imageFileName);
        //Toast.makeText(getApplicationContext(), imageFileNameT,Toast.LENGTH_LONG).show();
        File storageDir = Environment.getExternalStorageDirectory();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private String ArabicToEnglishNumber(String strArabic)
    {
        if(strArabic == null)
            return strArabic;

        String  result=strArabic.replaceAll("١", "1");
                result=result.replaceAll("٢", "2");
                result=result.replaceAll("٣", "3");
                result=result.replaceAll("٤", "4");
                result=result.replaceAll("٥", "5");
                result=result.replaceAll("٦", "6");
                result=result.replaceAll("٧", "7");
                result=result.replaceAll("٨", "8");
                result=result.replaceAll("٩", "9");
                result=result.replaceAll("٠", "0");

        return result;
    }
    private File createVideoFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "movie_" + timeStamp + "_";
        imageFileName=ArabicToEnglishNumber(imageFileName);
        File storageDir = Environment.getExternalStorageDirectory();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".mp4",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private static String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }

    private String writeXml(String serialNo, String FileName){
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag("", "root");
            serializer.startTag("", "file");
            serializer.startTag("", "id");
            serializer.text(serialNo);
            serializer.endTag("", "id");
            serializer.startTag("", "url");
            serializer.text(FileName);
            serializer.endTag("", "url");
            serializer.endTag("", "file");
            serializer.endTag("", "root");
            serializer.endDocument();
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public class SaveFileTask extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = SaveFileTask.class.getSimpleName();

        @Override
        protected String doInBackground(String... params) {

            StringBuilder stringBuilder=null;

            // If there's no zip code, there's nothing to look up.  Verify size of params.
            if (params.length == 0)
                return null;

            String response;
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection connection = null;

            //For Local Host URL
            String urlLocalServer ="http://10.0.2.2:3933/getitfastenterprisewcf.ashx/?app=26&actn=77";
            //For Live Server
            String urlLiveServer ="http://search.getit.in/?app=26&actn=77";

            try {

                Uri builtUri=Uri.parse(urlLiveServer).buildUpon().build();
                URL url=new URL(builtUri.toString());
                String postParameters="<root><file><id>" +serialNo + "</id><url>"+params[0] +"</url><type>"+params[1] +"</type></file></root>"; //"id=" +serialNo +"url="+ params[0];
                connection=(HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                //connection.setRequestProperty("Content-Type", "application/xml");
                connection.setRequestProperty("Cache-Control","no-cache");
                connection.setFixedLengthStreamingMode(postParameters.getBytes().length);
                OutputStreamWriter outSR=new OutputStreamWriter(connection.getOutputStream());
                outSR.write(postParameters);
                outSR.flush();
                outSR.close();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    return readStream(connection.getInputStream());
                }

                return stringBuilder.toString();
                // CHUNKING Code End
            }catch (Exception ex) {
                Log.e(LOG_TAG, "Error ", ex);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(),"Post Execute", Toast.LENGTH_LONG).show();
            if (result != null) {
                //for(String dayForecastStr : result) {
                String value=result.replace("<root><ID>","");
                value=value.replace("</ID></root>","");
                Toast.makeText(getApplicationContext(),value ,Toast.LENGTH_LONG).show();
            }
        }


    }

    @Override
    public void onBackPressed()
    {
        return;
    }

    private void restartInLocale()
    {
        Locale locale;
        if(this.getResources().getConfiguration().locale.getDisplayLanguage().contains("English"))   // mContext.get) mContext.getResources().getConfiguration().locale
        {
            locale = new Locale("ar");
        }
        else
        {
            locale = new Locale("en");
        }

        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        Resources resources = getResources();
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        recreate();
    }
}
