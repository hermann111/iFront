package com.middleeast.uploadimage;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterViewFlipper;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;


public class PhotoPlayerGlide extends Activity implements PhotoControllerView.AdapterViewFlipperControl{

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

    public final static String TAG="Glide";

    int counter = 0;
    String[] image;
    String[] video;

    // Whether there is a Wi-Fi connection.
    private static boolean wifiConnected = false;
    // Whether there is a mobile connection.
    private static boolean mobileConnected = false;

    // 监听AdapterViewFlipper
    private OnClickListener imageViewListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
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
        PhotoPlayerGlide activity;

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
            //Glide.with(context).load(images[position]).into(imageView);
            //Glide.with(this).load("http://goo.gl/gEgYUd").into(imageView);

            pd = new TransparentProgressDialog(PhotoPlayerGlide.this, R.drawable.loader_green);
            pd.show();

           /* Glide.with(context)
                    .load(images[position])
                    .error(R.drawable.logo_48)
                    .into(new GlideDrawableImageViewTarget(imageView) {
                        @Override
                        public void onResourceReady(GlideDrawable drawable, GlideAnimation anim) {
                            super.onResourceReady(drawable, anim);
                            if(pd!=null)
                                pd.dismiss();
                        }


                    });
        */

            Glide.with(context).load(images[position]).listener(new RequestListener<String, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                    Log.i(TAG, "Listener onException: " + e.toString());
                    if(pd!=null)
                        pd.dismiss();
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    Log.i(TAG, "onResourceReady with resource = " + resource);
                    Log.i(TAG, "onResourceReady from memory cache = " + isFromMemoryCache);
                    if (pd != null)
                        pd.dismiss();
                    return false;
                }
            })
                    .error(R.drawable.logo_48)
                    .into(imageView);

            // 设置显示的动画效果
            //animation = AnimationUtils.loadAnimation(context, R.anim.alpha1);
            //imageView.setAnimation(animation);

            imageView.setOnClickListener(listener);
            return imageView;
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
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
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

}
