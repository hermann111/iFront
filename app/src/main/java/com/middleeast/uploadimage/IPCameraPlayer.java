package com.middleeast.uploadimage;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class IPCameraPlayer extends Activity implements MediaPlayer.OnPreparedListener,
        SurfaceHolder.Callback, IPCameraControllerView.MediaPlayerControl, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener  {

    final static String USERNAME = "";
    final static String PASSWORD = "";
    final static String RTSP_URL_PROFILE1 = "rtsp://59.177.84.43/play1.sdp";
    final static String RTSP_URL_PROFILE2="rtsp://59.177.84.43/play2.sdp";
    final static String RTSP_URL_PROFILE3="rtsp://59.177.84.43/play3.sdp";
    final static String RTSP_URL_PROFILE4="rtsp://59.177.84.43/3gpp";
    private String RTSP_URL=RTSP_URL_PROFILE2;

    private MediaPlayer _mediaPlayer;
    private SurfaceHolder _surfaceHolder;
    IPCameraControllerView controller;
    WifiManager.WifiLock wifiLock;
    private TransparentProgressDialog pd;
    public final static String TAG2 = "TAG2";

    private ImageView           mProfile1Button;
    private ImageView           mProfile2Button;
    private ImageView           mProfile3Button;
    private ImageView           mProfile4Button;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Set up a full-screen black window.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.ip_camera_player);

        mContext=this;
/*
        if (RTSP_URL.startsWith("rtsp://")) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(RTSP_URL));
            startActivity(intent);
        }
        */
       configureVideoView();
    }

    private void configureVideoView()
    {
        // Configure the view that renders live video.

        if(RTSP_URL.length() > 0) {

            SurfaceView surfaceView =(SurfaceView) findViewById(R.id.surfaceView);
            _surfaceHolder = surfaceView.getHolder();
            _surfaceHolder.addCallback(this);
            // _surfaceHolder.setFixedSize(320, 240);

            _mediaPlayer = new MediaPlayer();

            controller = new IPCameraControllerView(this, _mediaPlayer);
            pd = new TransparentProgressDialog(IPCameraPlayer.this, R.drawable.loader_blue);
            pd.show();
        }else
        {
            Toast.makeText(this, R.string.no_video, Toast.LENGTH_LONG).show();
        }
    }

    private View.OnClickListener mProfile1Listener = new View.OnClickListener() {
        public void onClick(View v) {

               RTSP_URL = RTSP_URL_PROFILE1;
               configureVideoView();

        }
    };

    private View.OnClickListener mProfile2Listener = new View.OnClickListener() {
        public void onClick(View v) {

            RTSP_URL = RTSP_URL_PROFILE2;
            playVideo();
            Toast.makeText(mContext, "Profile 2", Toast.LENGTH_LONG).show();
            pd.show();

        }
    };

    private View.OnClickListener mProfile3Listener = new View.OnClickListener() {
        public void onClick(View v) {

            RTSP_URL = RTSP_URL_PROFILE3;
            configureVideoView();
        }
    };

    private View.OnClickListener mProfile4Listener = new View.OnClickListener() {
        public void onClick(View v) {

            RTSP_URL = RTSP_URL_PROFILE4;
            playVideo();
            Toast.makeText(mContext, "Profile 4", Toast.LENGTH_LONG).show();
            pd.show();
        }
    };

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
                if(this.getLocalClassName().equals("IPCameraPlayer")) {
                    //Toast.makeText(this, this.getLocalClassName(), Toast.LENGTH_LONG).show();
                    //Intent openMainActivity= new Intent("com.middleeast.uploadimage.FORM");
                    Intent i = new Intent();
                   // i.putExtra(EXTRA_VIOLATION_ID, Id);
                    i.setClass(this, BatinahRegion.class);
                    if(_mediaPlayer!=null) {
                        _mediaPlayer.release();
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
                if(_mediaPlayer!=null) {
                    _mediaPlayer.release();
                }
                startActivity(openHome);
                return true;

            case R.id.action_language:
                //restartInLocale();
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    /*
SurfaceHolder.Callback
*/

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(controller!=null) {
            controller.show();
        }
        return false;
    }

    @Override
    public void surfaceChanged(
            SurfaceHolder sh, int f, int w, int h) {}

    @Override
    public void surfaceCreated(SurfaceHolder sh) {
        _mediaPlayer.setDisplay(_surfaceHolder);
        playVideo();
        pd.show();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder sh) {
        _mediaPlayer.release();
    }

    /*
MediaPlayer.OnPreparedListener
*/
    @Override
    public void onPrepared(MediaPlayer mp) {

        controller.setMediaPlayer(this);

        controller.setAnchorView((FrameLayout) findViewById(R.id.videoSurfaceContainer));
        _mediaPlayer.start();

        mProfile1Button = (ImageView) controller.findViewById(R.id.btn_video_profile1);
        if (mProfile1Button != null) {
            mProfile1Button.requestFocus();
            mProfile1Button.setOnClickListener(mProfile1Listener);
        }
        mProfile2Button = (ImageView) controller.findViewById(R.id.btn_video_profile2);
        if (mProfile2Button != null) {
            mProfile2Button.requestFocus();
            mProfile2Button.setOnClickListener(mProfile2Listener);
        }
        mProfile3Button = (ImageView) controller.findViewById(R.id.btn_video_profile3);
        if (mProfile3Button != null) {
            mProfile3Button.requestFocus();
            mProfile3Button.setOnClickListener(mProfile3Listener);
        }
        mProfile4Button = (ImageView) controller.findViewById(R.id.btn_video_profile4);
        if (mProfile4Button != null) {
            mProfile4Button.requestFocus();
            mProfile4Button.setOnClickListener(mProfile4Listener);
        }

        if (pd != null) {
            pd.dismiss();
        }
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
        _mediaPlayer.reset();
        return true;
    }

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
        return _mediaPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
       return _mediaPlayer.getDuration();

    }

    @Override
    public boolean isPlaying() {
        return _mediaPlayer.isPlaying();
    }

    @Override
    public void pause() {
        _mediaPlayer.pause();
    }

    @Override
    public void seekTo(int i) {
        _mediaPlayer.seekTo(i);
    }

    @Override
    public void start() {

        _mediaPlayer.start();

        if (pd != null) {
            pd.dismiss();
        }

        Toast.makeText(mContext, RTSP_URL, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void toggleFullScreen() {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        mp.stop();
        Toast.makeText(this, "Stopped : on Video Completion", Toast.LENGTH_LONG).show();
    }
    // End VideoMediaController.MediaPlayerControl

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
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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


    private void playVideo()
    {
        if(_mediaPlayer.isPlaying())
        {
           _mediaPlayer.stop();
            _mediaPlayer.reset();
        }
        Uri source = Uri.parse(RTSP_URL);

        try {

            _mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            // Specify the IP camera's URL and auth headers.
            //_mediaPlayer.setDataSource(context, source, headers);
            _mediaPlayer.setDataSource(mContext, source);
            // Begin the process of setting up a video stream.
            _mediaPlayer.setOnPreparedListener(this);
            _mediaPlayer.prepareAsync();
            wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                    .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

            wifiLock.acquire();

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


}
