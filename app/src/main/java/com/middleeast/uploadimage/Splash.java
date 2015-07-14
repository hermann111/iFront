package com.middleeast.uploadimage;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;
import android.widget.Toast;


/**
 * Created by harmanjeet.s on 3/6/2015.
 */
public class Splash extends Activity {

    SplashControllerView controller;
    MediaPlayer ourSong;

    @Override
    protected  void onCreate(Bundle TravisBenBach)
    {
        super.onCreate(TravisBenBach);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.splash);

        //Commented the code to not show the overlay on splash screen
        controller = new SplashControllerView(this);
        controller.setAnchorView((FrameLayout) findViewById(R.id.splashSurfaceContainer));
        controller.show();

        FrameLayout framelayout=(FrameLayout) findViewById(R.id.splashSurfaceContainer);
        framelayout.setOnTouchListener(new OnTouchListener(){
               @Override
               public boolean onTouch(View v, MotionEvent event){
                   Intent openMainActivity= new Intent("com.middleeast.uploadimage.ADMINISTRATOR");
                   startActivity(openMainActivity);
                   return true;
               }
           });

        ourSong =MediaPlayer.create(Splash.this,R.raw.albi);
       // ourSong.start();
     /*   Thread timer = new Thread()
        {
            public void run()
            {
                try
                {
                    sleep(15000);

                }catch(InterruptedException e)
                {
                    e.printStackTrace();
                }finally {

                    Intent openMainActivity= new Intent("com.middleeast.uploadimage.ADMINISTRATOR");
                    startActivity(openMainActivity);
                }
            }
        };

        timer.start();*/
    }
    protected void onPause()
    {
        super.onPause();
        //ourSong.release();
        finish();
    }

}
