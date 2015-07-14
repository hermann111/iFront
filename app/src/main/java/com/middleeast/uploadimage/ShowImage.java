package com.middleeast.uploadimage;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;


public class ShowImage extends Activity {

    private ProgressDialog pd;
    private String urlString="https://s3.amazonaws.com/ishmeet11111/photo_20150422_175737_961483545.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FetchImageIntentService.TRANSACTION_DONE);
        registerReceiver(imageReceiver, intentFilter);

        Intent i = new Intent(this, FetchImageIntentService.class);
        //i.putExtra("url", getIntent().getExtras().getString(urlString));
        i.putExtra("url", urlString);
        startService(i);

        pd = ProgressDialog.show(this,"Fetching Image", "Go intent service go!");
    }

    private BroadcastReceiver imageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String location = intent.getExtras().getString("location");
            if(TextUtils.isEmpty(location)){
                String failedString = "Failed to download image";
                Toast.makeText(context, failedString, Toast.LENGTH_LONG).show();
            }

            File imageFile = new File(location);
            if(!imageFile.exists()){
                pd.dismiss();

                String downloadFail = "Unable to Download file :-(";
                Toast.makeText(context, downloadFail, Toast.LENGTH_LONG);
                return;
            }

            Bitmap b = BitmapFactory.decodeFile(location);
            ImageView iv = (ImageView)findViewById(R.id.remote_image);
            iv.setImageBitmap(b);
            pd.dismiss();
        }
    };
}
