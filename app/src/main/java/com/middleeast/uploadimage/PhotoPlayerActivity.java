package com.middleeast.uploadimage;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterViewFlipper;
import android.widget.FrameLayout;
import android.widget.Toast;

import android.content.Context;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.app.Dialog;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.view.animation.RotateAnimation;
import android.view.Gravity;
import android.view.animation.LinearInterpolator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;


public class PhotoPlayerActivity extends Activity implements PhotoControllerView.AdapterViewFlipperControl{

    private static final String DEBUG_TAG = "HttpExample";

    MediaPlayer player;
    PhotoControllerView controller;
    WifiManager.WifiLock wifiLock;
    AdapterViewFlipper flipper;
    //private ProgressDialog pd = null;
    private TransparentProgressDialog pd;
    String Id;  //Violation ID
    public final static String EXTRA_VIOLATION_ID = "com.middleeast.uploadimage.VIOLATION_ID";
    public final static String EXTRA_IMAGE = "com.middleeast.uploadimage.IMAGE";
    public final static String EXTRA_VIDEO = "com.middleeast.uploadimage.VIDEO";

    int counter = 0;
    //int[] images = { R.drawable.splash, R.drawable.touchicon, R.drawable.splash, R.drawable.touchicon };

    String[] image; /* = {"http://d281ayci1f2eci.cloudfront.net/Photo_20150413_173909_-263857253.jpg",
            "http://d281ayci1f2eci.cloudfront.net/Photo_20150413_173727_363496611.jpg",
            "http://d281ayci1f2eci.cloudfront.net/Photo_20150405_213917_2030553425.jpg",
            "http://d281ayci1f2eci.cloudfront.net/Photo_20150405_213932_1814367283.jpg"}; */
    String[] video;

    // Whether there is a Wi-Fi connection.
    private static boolean wifiConnected = false;
    // Whether there is a mobile connection.
    private static boolean mobileConnected = false;

    private boolean isFetchImageServiceCompleted=false;

    private File cacheDir;


    // 监听AdapterViewFlipper
    private View.OnClickListener imageViewListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            //Toast.makeText(getApplicationContext(), "image", 200).show();
            controller.show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        //bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#36ba4e")));

        setContentView(R.layout.activity_photo_player);
        Id=getIntent().getStringExtra(EXTRA_VIOLATION_ID);
        image=getIntent().getStringArrayExtra(EXTRA_IMAGE);
        video=getIntent().getStringArrayExtra(EXTRA_VIDEO);
        flipper = (AdapterViewFlipper) findViewById(R.id.filpper);

        isFetchImageServiceCompleted=false;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FetchImageIntentService.TRANSACTION_DONE);
        registerReceiver(imageReceiver, intentFilter);

        cacheDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString());
        pd = new TransparentProgressDialog(PhotoPlayerActivity.this, R.drawable.loader_green);
/*
        if(!isFetchImageServiceCompleted) {
            pd.show();
        }else{
            if (pd != null) {
                pd.dismiss();
            }
        }
*/
    }

    @Override
    public void onStart () {
        super.onStart();

        // Update the user's network flag settings
          updateConnectedFlags();

        // Update the user's network flag settings
            loadImage();

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
                if(this.getLocalClassName().equals("PhotoPlayerActivity")) {
                    //Toast.makeText(this, this.getLocalClassName(), Toast.LENGTH_LONG).show();
                    //Intent openMainActivity= new Intent("com.middleeast.uploadimage.FORM");
                    Intent i = new Intent();
                    i.putExtra(EXTRA_VIOLATION_ID, Id);
                    i.setClass(this, Form.class);
                    startActivity(i);
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        controller.show();
        return false;
    }

    // Implement VideoMediaController.MediaPlayerControl

     @Override
    public boolean isFlipping() {
        return flipper.isFlipping();
    }

    @Override
    public void stopFlipping() {
        flipper.stopFlipping();
    }

    @Override
    public void startFlipping() {
        if (wifiConnected || mobileConnected){
            // AsyncTask subclass
            flipper.startFlipping();
        } else {
            Toast.makeText(this, "No Internet is available", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void showPrevious() {

        if (wifiConnected || mobileConnected){
            // AsyncTask subclass
            flipper.showPrevious();
        } else {
            Toast.makeText(this, "No Internet is available", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void showNext() {

        if (wifiConnected || mobileConnected){
            // AsyncTask subclass
            flipper.showNext();
        } else {
            Toast.makeText(this, "No Internet is available", Toast.LENGTH_LONG).show();
        }

    }

    // End VideoMediaController.MediaPlayerControl

    public class FlipperAdapter extends BaseAdapter {
        private String[] images;
        private Context context;
        private OnClickListener listener;
        Animation animation;
        PhotoPlayerActivity activity;

        public FlipperAdapter(Context context, String[] image, OnClickListener listener) {
            this.images = image;
            this.context = context;
            this.listener = listener;

        }

        public void setListener(OnClickListener listener) {
            this.listener = listener;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return images.length;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return images[position];
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            final ImageView imageView = new ImageView(context);
            imageView.setScaleType(ScaleType.CENTER_CROP);
            //imageView.setImageResource(images[position]);

            // Show the ProgressDialog on this thread
            //pd = ProgressDialog.show(PhotoPlayerActivity.this, "Working..", "Downloading Data...", true, false);


            String remoteUrl = images[position];
            String location;
            String filename = remoteUrl.substring(remoteUrl.lastIndexOf(File.separator) + 1);
            File tmp = new File(cacheDir.getPath() + File.separator + filename);

            if (tmp.exists()) {
                location = tmp.getAbsolutePath();
                Bitmap b = BitmapFactory.decodeFile(location);
                imageView.setImageBitmap(b);

                if (pd != null) {
                    pd.dismiss();
                }
            }
            else {
                DownloadImageTask downloadImageTask = new DownloadImageTask(imageView);
                downloadImageTask.execute(images[position]);
            }

            // 设置显示的动画效果
            //animation = AnimationUtils.loadAnimation(context, R.anim.alpha1);
            //imageView.setAnimation(animation);

            imageView.setOnClickListener(listener);
            return imageView;
        }
    }


    public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {

                return downloadUrl(urls[0]);
            } catch (IOException e) {

                if (pd != null) {
                    pd.dismiss();
                }

                return null;
            }
        }

        protected void onPostExecute(Bitmap result) {

            if(result !=null) {
                bmImage.setImageBitmap(result);
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Failed to load Image.. try again", Toast.LENGTH_LONG).show();
            }
            // Pass the result data back to the main activity

            if (pd != null) {
                pd.dismiss();
            }
        }

        // Given a URL, establishes an HttpUrlConnection and retrieves
        // the web page content as a InputStream, which it returns as
        // a string.
        private Bitmap downloadUrl(String myurl) throws IOException {

            InputStream is = null;

            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(DEBUG_TAG, "The response is: " + response);
                is = conn.getInputStream();

                // Convert the InputStream into a bitmap
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                return bitmap;

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
    }


    public class TransparentProgressDialog extends Dialog {

        private ImageView iv;

        public TransparentProgressDialog(Context context, int resourceIdOfImage) {
            super(context, R.style.TransparentProgressDialog);
            WindowManager.LayoutParams wlmp = getWindow().getAttributes();
            wlmp.gravity = Gravity.CENTER_HORIZONTAL;
            getWindow().setAttributes(wlmp);
            setTitle(null);
            setCancelable(false);
            setOnCancelListener(null);
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            iv = new ImageView(context);
            iv.setImageResource(resourceIdOfImage);
            layout.addView(iv, params);
            addContentView(layout, params);
        }

        @Override
        public void show() {
            super.show();
            RotateAnimation anim = new RotateAnimation(0.0f, 360.0f , Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
            anim.setInterpolator(new LinearInterpolator());
            anim.setRepeatCount(Animation.INFINITE);
            anim.setDuration(3000);
            iv.setAnimation(anim);
            iv.startAnimation(anim);
        }
    }

    @Override
    public void onBackPressed()
    {
        return;
    }

    // Checks the network connection and sets the wifiConnected and mobileConnected
    // variables accordingly.
    public void updateConnectedFlags() {

        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            wifiConnected = false;
            mobileConnected = false;
        }
    }

    // Uses AsyncTask subclass to download the XML feed from stackoverflow.com.
    public void loadImage() {
        if (wifiConnected || mobileConnected){
            // AsyncTask subclass

            controller = new PhotoControllerView(this,Id, image, video);
            pd = new TransparentProgressDialog(PhotoPlayerActivity.this, R.drawable.loader_green);
            controller.setMediaPlayer(this);
            controller.setAnchorView((FrameLayout) findViewById(R.id.photoSurfaceContainer));
            if (image.length >0) {
                flipper.setAdapter(new FlipperAdapter(this, image, imageViewListener));
            }else
            {
                Toast.makeText(this,"Images Failed to Load..try again...", Toast.LENGTH_LONG).show();
            }
            // 开始滑动
            //flipper.startFlipping();

        } else {
            Toast.makeText(this, "No Internet is available", Toast.LENGTH_LONG).show();
        }
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

    private BroadcastReceiver imageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        isFetchImageServiceCompleted=true;

        }
    };
}
