package com.middleeast.uploadimage;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.widget.TextView;
import android.content.Context;
public class Form extends Activity {

    public final static String EXTRA_VIOLATION_ID = "com.middleeast.uploadimage.VIOLATION_ID";
    public final static String EXTRA_IMAGE = "com.middleeast.uploadimage.IMAGE";
    public final static String EXTRA_VIDEO = "com.middleeast.uploadimage.VIDEO";
    String Id;
    String [] form=new String[26];;
    String [] image;
    String [] video;
    Intent fetchImageIntentService;

    View rootView;
    FormControllerView controller;
    Context mContext;
    private TransparentProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
       // actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#36ba4e")));

        setContentView(R.layout.activity_form);
        mContext=this;
        Id=getIntent().getStringExtra(EXTRA_VIOLATION_ID);


            pd = new TransparentProgressDialog(Form.this, R.drawable.loader_pink);
            FetchViolationTask violationTask = new FetchViolationTask();
            pd.show();
            violationTask.execute(Id);


        FrameLayout framelayout=(FrameLayout) findViewById(R.id.formSurfaceContainer);
        framelayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                controller.show();
                return true;
            }
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FetchImageIntentService.TRANSACTION_DONE);
        //registerReceiver(imageReceiver, intentFilter);

        fetchImageIntentService = new Intent(this, FetchImageIntentService.class);

  /*    findViewById(R.id.btn_ic_action_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                Intent intent=new Intent("com.middleeast.uploadimage.PHOTOPLAYERACTIVITY");
                intent.putExtra(EXTRA_VIOLATION_ID, Id);
                intent.putExtra(EXTRA_IMAGE, image);
                intent.putExtra(EXTRA_VIDEO, video);
                startActivity(intent);
            }
        });

        findViewById(R.id.btn_ic_action_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent=new Intent("com.middleeast.uploadimage.VIDEOPLAYERACTIVITY");
                intent.putExtra(EXTRA_VIOLATION_ID, Id);
                intent.putExtra(EXTRA_IMAGE, image);
                intent.putExtra(EXTRA_VIDEO, video);
                startActivity(intent);
            }
        });
*/

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
                if(this.getLocalClassName().equals("Form")) {
                    //Toast.makeText(this, this.getLocalClassName(), Toast.LENGTH_LONG).show();
                    Intent openMainActivity= new Intent("com.middleeast.uploadimage.CUSTOMIZEDLISTVIEW");
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

    public class FetchViolationTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchViolationTask.class.getSimpleName();

        /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getViolationDataFromJson(String violationJsonStr) throws JSONException {

            JSONObject  violationJsonObject = new JSONObject(violationJsonStr);

            JSONArray   imageJSONArray = violationJsonObject.getJSONArray("image");
            JSONArray   videoJSONArray = violationJsonObject.getJSONArray("video");

            form[0]=violationJsonObject.getString("Department");
            form[1]=violationJsonObject.getString("ID");
            form[2]=violationJsonObject.getString("Date");
            form[3]=violationJsonObject.getString("Region");
            form[4]=violationJsonObject.getString("State");
            form[5]=violationJsonObject.getString("Violator_name");
            form[6]=violationJsonObject.getString("Location");
            form[7]=violationJsonObject.getString("Type_of_activity");
            form[8]=violationJsonObject.getString("License_number");
            form[9]=violationJsonObject.getString("Violation");
            form[10]=violationJsonObject.getString("Fine");
            form[11]=violationJsonObject.getString("Amount_in_letters");
            form[12]=violationJsonObject.getString("Notice");
            form[13]=violationJsonObject.getString("Wrote_in_date");
            form[14]=violationJsonObject.getString("Recipient_name");
            form[15]=violationJsonObject.getString("Recipient_signature");
            form[16]=violationJsonObject.getString("Wrote");
            form[17]=violationJsonObject.getString("Occupation");
            form[18]=violationJsonObject.getString("Signature");
            form[19]=violationJsonObject.getString("Receipt");
            form[20]=violationJsonObject.getString("Fine_number");
            form[21]=violationJsonObject.getString("Accountant_name");
            form[22]=violationJsonObject.getString("Accountant_signature");
            form[23]=violationJsonObject.getString("Head_of_Department");
            form[24]=violationJsonObject.getString("Language");
            form[25]=violationJsonObject.getString("Form");

            image = new String[imageJSONArray.length()];
            for(int i = 0; i < imageJSONArray.length(); i++) {

                // For now, using the format "Day, description, hi/low"
                String id;
                String url;

                // Get the JSON object representing the day
                JSONObject imageObject = imageJSONArray.getJSONObject(i);

                id = imageObject.getString("ID");
                url = imageObject.getString("URL");

                image[i] = url;
            }

            video = new String[videoJSONArray.length()];
            for(int i = 0; i < videoJSONArray.length(); i++) {

                // For now, using the format "Day, description, hi/low"
                String id;
                String url;

                // Get the JSON object representing the day
                JSONObject videoObject = videoJSONArray.getJSONObject(i);

                id = videoObject.getString("ID");
                url = videoObject.getString("URL");

                video[i] = url;
            }

            for (String s : image) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }

            return form;
        }

        @Override
        protected String[] doInBackground(String... params) {

            // If there's no zip code, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String violationJsonStr = null;

            String app = "26";
            String actn = "74";

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String FORECAST_BASE_URL ="http://search.getit.in/?";
                final String APP_PARAM = "app";
                final String ACTN_PARAM = "actn";
                final String ID_PARAM = "id";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(APP_PARAM, app)
                        .appendQueryParameter(ACTN_PARAM, actn)
                        .appendQueryParameter(ID_PARAM, params[0])
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                violationJsonStr = buffer.toString();

                Log.v(LOG_TAG, "Violation string: " + violationJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.

                if (pd != null) {
                    pd.dismiss();
                }

                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getViolationDataFromJson(violationJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {

            for (int i = 0; i < image.length; i++) {

                fetchImageIntentService.putExtra("url", image[i]);
                startService(fetchImageIntentService);
            }

            if (result != null) {

                //Commented the code to not show the overlay on splash screen
                controller = new FormControllerView(mContext,Id, image, video);
                controller.setAnchorView((FrameLayout) findViewById(R.id.formSurfaceContainer));
                controller.show();

                if(!Locale.getDefault().getLanguage().contains(result[24])) {
                    Toast.makeText(mContext,Locale.getDefault().getLanguage() + "-"+result[24],Toast.LENGTH_LONG ).show();

                    if (result[24].contains("ar"))   // mContext.get) mContext.getResources().getConfiguration().locale
                    {
                        onLocale_Changed("ar");
                    } else {
                        onLocale_Changed("en");
                    }
                }

                if(result[25].contains("1"))
                {
                    ((TextView) findViewById(R.id.txtform_violation)).setText(R.string.txtform_alarm_violation);
                }
                else if(result[25].contains("2"))
                {
                    ((TextView) findViewById(R.id.txtform_violation)).setText(R.string.txt_laboratory);
                }
                else if(result[25].contains("3"))
                {
                    ((TextView) findViewById(R.id.txtform_violation)).setText(R.string.txtform_violation);
                }
                else if(result[25].contains("4"))
                {
                    ((TextView) findViewById(R.id.txtform_violation)).setText(R.string.txt_close_property);
                }
                else if(result[25].contains("5"))
                {
                    ((TextView) findViewById(R.id.txtform_violation)).setText(R.string.txt_material);
                }

                // New data is back from the server.  Hooray!
                TextView txtSerialNo = (TextView)findViewById(R.id.txtserialno);
                txtSerialNo.setText(result[1]);
                TextView txtdepartment_date_value=(TextView)findViewById(R.id.txtdepartment_date_value);
                txtdepartment_date_value.setText(result[2]);
                TextView edtxt_department_name=(TextView)findViewById(R.id.edtxt_department_name);
                edtxt_department_name.setText(result[0]);
                TextView edtxt_reqion_value=(TextView)findViewById(R.id.edtxt_reqion_value);
                edtxt_reqion_value.setText(result[3]);
                TextView edtxt_state_value=(TextView)findViewById(R.id.edtxt_state_value);
                edtxt_state_value.setText(result[4]);
                TextView edtxt_violator_name_value=(TextView)findViewById(R.id.edtxt_violator_name_value);
                edtxt_violator_name_value.setText(result[5]);
                TextView edtxt_location_value=(TextView)findViewById(R.id.edtxt_location_value);
                edtxt_location_value.setText(result[6]);
                TextView edtxt_activity_type_value=(TextView)findViewById(R.id.edtxt_activity_type_value);
                edtxt_activity_type_value.setText(result[7]);
                TextView edtxt_license_number_value=(TextView)findViewById(R.id.edtxt_license_number_value);
                edtxt_license_number_value.setText(result[8]);
                TextView edtext_multi_violation=(TextView)findViewById(R.id.edtext_multi_violation);
                edtext_multi_violation.setText(result[9]);
                TextView edtext_fine=(TextView)findViewById(R.id.edtext_fine_line1);
                edtext_fine.setText(result[10]);
                TextView edtext_fine_words_value=(TextView)findViewById(R.id.edtext_fine);
                edtext_fine_words_value.setText(result[11]);
                TextView edtext_notice_period=(TextView)findViewById(R.id.edtext_notice_period);
                edtext_notice_period.setText(result[12]);
                TextView txt_wrote_date=(TextView)findViewById(R.id.txt_wrote_date);
                txt_wrote_date.setText(result[13]);
                TextView edtxt_recipient_name_value=(TextView)findViewById(R.id.edtxt_recipient_name_value);
                edtxt_recipient_name_value.setText(result[14]);
                TextView edtxt_signature_recipient_value=(TextView)findViewById(R.id.edtxt_signature_recipient_value);
                edtxt_signature_recipient_value.setText(result[15]);
                TextView edtxt_wrote_name_value=(TextView)findViewById(R.id.edtxt_wrote_name_value);
                edtxt_wrote_name_value.setText(result[16]);
                TextView edtxt_txt_occupation_value=(TextView)findViewById(R.id.edtxt_txt_occupation_value);
                edtxt_txt_occupation_value.setText(result[17]);
                TextView edtxt_signature_value=(TextView)findViewById(R.id.edtxt_signature_value);
                edtxt_signature_value.setText(result[18]);
                TextView edtxt_receipt_date_value=(TextView)findViewById(R.id.edtxt_receipt_date_value);
                edtxt_receipt_date_value.setText(result[19]);
                TextView edtxt_fine_number_value=(TextView)findViewById(R.id.edtxt_fine_number_value);
                edtxt_fine_number_value.setText(result[20]);
                TextView edtxt_accountant_name_value=(TextView)findViewById(R.id.edtxt_accountant_name_value);
                edtxt_accountant_name_value.setText(result[21]);
                TextView edtxt_head_of_department_value=(TextView)findViewById(R.id.edtxt_head_of_department_value);
                edtxt_head_of_department_value.setText(result[23]);

            }

            if (pd != null) {
                pd.dismiss();
            }
        }
    }

    private void onLocale_Changed(String locale){
        Locale myLocale = new Locale(locale);
        // set the new locale
        Locale.setDefault(myLocale);
        Configuration config = new Configuration();
        config.locale = myLocale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        // refresh UI - get values from localized resources
        ((TextView) findViewById(R.id.txtcountry)).setText(R.string.txtcountry);
        ((TextView) findViewById(R.id.txtministry)).setText(R.string.txtministry);
        ((TextView) findViewById(R.id.txtdepartment)).setText(R.string.txtdepartment);
        ((TextView) findViewById(R.id.txtform_violation)).setText(R.string.txtform_violation);
        ((TextView) findViewById(R.id.txtserial)).setText(R.string.txtserial);
        ((TextView) findViewById(R.id.txtdepartment_date)).setText(R.string.txtdepartment_date);
        ((TextView) findViewById(R.id.txtdepartment_name)).setText(R.string.txtdepartment_name);
        ((TextView) findViewById(R.id.txt_reqion)).setText(R.string.txt_reqion);
        ((TextView) findViewById(R.id.txt_state)).setText(R.string.txt_state);
        ((TextView) findViewById(R.id.txt_violator_name)).setText(R.string.txt_violator_name);
        ((TextView) findViewById(R.id.txt_location)).setText(R.string.txt_location);
        ((TextView) findViewById(R.id.txt_activity_type)).setText(R.string.txt_activity_type);
        ((TextView) findViewById(R.id.txt_license_number)).setText(R.string.txt_license_number);
        ((TextView) findViewById(R.id.txt_violation)).setText(R.string.txt_violation);
        ((TextView) findViewById(R.id.edtext_fine_line1)).setText(R.string.edtext_fine_line1);
//        ((TextView) findViewById(R.id.edtext_fine_line2)).setText(R.string.edtext_fine_line2);
        ((TextView) findViewById(R.id.edtext_fine)).setText(R.string.text_fine_words);
        ((TextView) findViewById(R.id.edtext_notice_period)).setText(R.string.edtext_notice_period_line1);
        //((TextView) findViewById(R.id.edtext_notice_period_line2)).setText(R.string.edtext_notice_period_line2);
       // ((TextView) findViewById(R.id.edtext_notice_period_line2_next)).setText(R.string.edtext_notice_period_line2_next);
        //((TextView) findViewById(R.id.edtext_notice_period_line3)).setText(R.string.edtext_notice_period_line3);
        ((TextView) findViewById(R.id.txt_wrotedate)).setText(R.string.txt_wrote_date);
        ((TextView) findViewById(R.id.txt_recipient_name)).setText(R.string.txt_recipient_name);
        ((TextView) findViewById(R.id.txt_signature_recipient)).setText(R.string.txt_signature_recipient);
        ((TextView) findViewById(R.id.txt_wrote_name)).setText(R.string.txt_wrote_name);
        ((TextView) findViewById(R.id.txt_occupation)).setText(R.string.txt_occupation);
        ((TextView) findViewById(R.id.txt_signature)).setText(R.string.txt_signature);
        ((TextView) findViewById(R.id.txt_receipt)).setText(R.string.txt_receipt);
        ((TextView) findViewById(R.id.txt_receipt_date)).setText(R.string.txt_receipt_date);
        ((TextView) findViewById(R.id.txt_fine_number)).setText(R.string.txt_fine_number);
        ((TextView) findViewById(R.id.txt_accountant_name)).setText(R.string.txt_accountant_name);
        ((TextView) findViewById(R.id.txt_head_of_department)).setText(R.string.txt_head_of_department);
        ((TextView) findViewById(R.id.txt_note)).setText(R.string.txt_note);
        ((TextView) findViewById(R.id.txt_stamp)).setText(R.string.txt_stamp);
        ((TextView) findViewById(R.id.txt_ecopy)).setText(R.string.txt_ecopy);

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

}
