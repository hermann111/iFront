package com.middleeast.uploadimage;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;


import java.util.Locale;


/**
 * Created by harmanjeet.s on 3/21/2015.
 */
public class Region extends Activity {

    RegionControllerView controller;

    @Override
    protected void onCreate(Bundle TravisBenBach) {
        super.onCreate(TravisBenBach);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#36ba4e")));

        setContentView(R.layout.region);

        //Commented the code to not show the overlay on splash screen
        controller = new RegionControllerView(this);
        controller.setAnchorView((FrameLayout) findViewById(R.id.regionContainer));
        controller.show();


        findViewById(R.id.batinah_region).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent openMainActivity = new Intent("com.middleeast.uploadimage.BATINAHREGION");
                startActivity(openMainActivity);
            }
        });
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
                if (this.getLocalClassName().equals("Region")) {
                    //Toast.makeText(this, this.getLocalClassName(), Toast.LENGTH_LONG).show();
                    Intent openMainActivity = new Intent("com.middleeast.uploadimage.ADMINISTRATOR");
                    startActivity(openMainActivity);
                } else {
                    Toast.makeText(this, "Some Other Screen", Toast.LENGTH_LONG).show();
                }
                return true;

            case R.id.ic_action_secure:
                Intent openHome = new Intent("com.middleeast.uploadimage.ADMINISTRATOR");
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

    @Override
    public void onBackPressed()
    {
        return;
    }
}
