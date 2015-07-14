package com.middleeast.uploadimage;

import java.io.IOException;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.app.Dialog;
import android.net.wifi.WifiManager;
import android.content.Context;
import android.net.wifi.WifiManager.WifiLock;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.view.animation.RotateAnimation;
import android.view.Gravity;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;
import android.util.Log;


public class VideoPlayerActivity extends Activity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, VideoControllerView.MediaPlayerControl {

    SurfaceView videoSurface;
    MediaPlayer player;
    VideoControllerView controller;
    SurfaceHolder videoHolder;
    WifiLock wifiLock;
    String Id;  //Violation ID
    public final static String EXTRA_VIOLATION_ID = "com.middleeast.uploadimage.VIOLATION_ID";
    public final static String EXTRA_IMAGE = "com.middleeast.uploadimage.IMAGE";
    public final static String EXTRA_VIDEO = "com.middleeast.uploadimage.VIDEO";
    public final static String TAG2 = "TAG2";

    String[] image;
    String [] video;
    private TransparentProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#38B0DE")));

        setContentView(R.layout.activity_video_player);
        Id=getIntent().getStringExtra(EXTRA_VIOLATION_ID);
        image =getIntent().getStringArrayExtra(EXTRA_IMAGE);
        video =getIntent().getStringArrayExtra(EXTRA_VIDEO);

        if(video.length > 0) {
            videoSurface = (SurfaceView) findViewById(R.id.videoSurface);
            videoHolder = videoSurface.getHolder();
            videoHolder.addCallback(this);

            player = new MediaPlayer();
            player.setOnErrorListener(this);

            controller = new VideoControllerView(this, player, Id, image, video);
            pd = new TransparentProgressDialog(VideoPlayerActivity.this, R.drawable.loader_blue);
        }else
        {
            Toast.makeText(this, R.string.no_video, Toast.LENGTH_LONG).show();
        }
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
                if(this.getLocalClassName().equals("VideoPlayerActivity")) {
                    //Toast.makeText(this, this.getLocalClassName(), Toast.LENGTH_LONG).show();
                    //Intent openMainActivity= new Intent("com.middleeast.uploadimage.FORM");
                    Intent i = new Intent();
                    i.putExtra(EXTRA_VIOLATION_ID, Id);
                    i.setClass(this, Form.class);
                    if(player!=null) {
                        player.release();
                    }
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
                if(player!=null) {
                    player.release();
                }
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
        if(controller!=null) {
            controller.show();
        }
        return false;
    }

    // Implement SurfaceHolder.Callback
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    	playVideo();
        pd.show();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        
    }
    // End SurfaceHolder.Callback

    // Implement MediaPlayer.OnPreparedListener
    @Override
    public void onPrepared(MediaPlayer mp) {

        controller.setMediaPlayer(this);
        controller.setAnchorView((FrameLayout) findViewById(R.id.videoSurfaceContainer));
        player.start();
        if (pd != null) {
            pd.dismiss();
        }
    }

    // Harmanjeet April 26, 2015
    // This method never called even after completion of video.. need to check later on. -
     @Override
     public void onCompletion (MediaPlayer mp)
     {
         mp.stop();
         Toast.makeText(this, "Stopped : on Video Completion", Toast.LENGTH_LONG).show();
     }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // ... react appropriately ...
        // The MediaPlayer has moved to the Error state, must be reset!

        switch (what){
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.e(TAG2, "unknown media playback error");
                Toast.makeText(this, "unknown media playback error", Toast.LENGTH_LONG).show();
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.e(TAG2, "server connection died");
                Toast.makeText(this, "server connection died", Toast.LENGTH_LONG).show();
            default:
                Log.e(TAG2, "generic audio playback error");
                Toast.makeText(this, "generic audio playback error", Toast.LENGTH_LONG).show();
                break;
        }

        switch (extra){
            case MediaPlayer.MEDIA_ERROR_IO:
                Log.e(TAG2, "IO media error");
                Toast.makeText(this, "IO media error", Toast.LENGTH_LONG).show();
                break;
            case MediaPlayer.MEDIA_ERROR_MALFORMED:
                Log.e(TAG2, "media error, malformed");
                Toast.makeText(this, "media error, malformed", Toast.LENGTH_LONG).show();
                break;
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                Log.e(TAG2, "unsupported media content");
                Toast.makeText(this, "unsupported media content", Toast.LENGTH_LONG).show();
                break;
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                Log.e(TAG2, "media timeout error");
                Toast.makeText(this, "media timeout error", Toast.LENGTH_LONG).show();
                break;
            default:
                Log.e(TAG2, "unknown playback error");
                Toast.makeText(this, "unknown playback error", Toast.LENGTH_LONG).show();
                break;
        }

        if (pd != null) {
            pd.dismiss();
        }
        player.reset();
        return true;
    }


    // End MediaPlayer.OnPreparedListener

    // Implement VideoMediaController.MediaPlayerControl
    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return player.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public void pause() {
        player.pause();
    }

    @Override
    public void seekTo(int i) {
        player.seekTo(i);
    }

    @Override
    public void start() {
        player.start();
        if (pd != null) {
            pd.dismiss();
        }
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void toggleFullScreen() {
        
    }
    // End VideoMediaController.MediaPlayerControl

    private void playVideo()
    {
       /* String[] images={"http://www.boisestatefootball.com/sites/default/files/videos/original/01%20-%20coach%20pete%20bio_4.mp4","http://d281ayci1f2eci.cloudfront.net/movie_20150417_035011_263300719.mp4",
                "http://d281ayci1f2eci.cloudfront.net/Movie_20150405_213941_1889107471.mp4", "http://d281ayci1f2eci.cloudfront.net/movie_20150417_081015_263300719.mp4"};
        */
        player.setDisplay(videoHolder);
        //player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        try {
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            //player.setDataSource(this, Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"));

                player.setDataSource(video[0]);
                player.prepareAsync();
                wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                        .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

                wifiLock.acquire();
                player.setOnPreparedListener(this);


        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
