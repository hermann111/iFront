package com.middleeast.uploadimage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.app.ProgressDialog;
import android.widget.Toast;

public class CustomizedListView extends Activity {

	// JSON node keys
    static final String KEY_ID = "id";
	static final String KEY_NAME = "name";
	static final String KEY_STATE = "state";
	static final String KEY_DATE = "date";
	static final String KEY_LICENSE = "license";
    private TransparentProgressDialog pd;
    ArrayList<HashMap<String, String>> violationList;
	ListView list;
    LazyAdapter adapter;
    public final static String EXTRA_VIOLATION_ID = "com.middleeast.uploadimage.VIOLATION_ID";
    private String ID="0";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#DA70D6")));

        setContentView(R.layout.main);

		violationList = new ArrayList<HashMap<String, String>>();
        pd = new TransparentProgressDialog(CustomizedListView.this, R.drawable.loader_purple);

        FetchListingTask downloadListingTask=new FetchListingTask(violationList, this);
        downloadListingTask.execute("1");
        pd.show();
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
                if(this.getLocalClassName().equals("CustomizedListView")) {
                    //Toast.makeText(this, R.string.login_admin, Toast.LENGTH_LONG).show();
                    Intent openMainActivity= new Intent("com.middleeast.uploadimage.ADMINISTRATOR");
                    startActivity(openMainActivity);
                }
                else
                {
                    Toast.makeText(this, "Some Other Screen", Toast.LENGTH_LONG).show();
                }
                return true;

            case R.id.ic_action_secure:
                //Toast.makeText(this, R.string.login_admin, Toast.LENGTH_LONG).show();
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

    public class FetchListingTask extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = FetchListingTask.class.getSimpleName();
        ArrayList<HashMap<String, String>> violationListTask;
        Activity activity;

        public FetchListingTask(ArrayList<HashMap<String, String>> violationList, Activity a) {
            this.violationListTask = violationList;
            this.activity=a;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String getListingDataFromJson(String listingJsonStr) throws JSONException {

            try
            {
            JSONObject forecastJson = new JSONObject(listingJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray("list");

            for(int i = 0; i < weatherArray.length(); i++) {

                String id;
                String violator_name;
                String state;
                String date;
                String license_number;

                // Get the JSON object representing the day
                JSONObject listObject = weatherArray.getJSONObject(i);

                id=listObject.getString("ID");
                violator_name = listObject.getString("Violator_name");
                state = listObject.getString("State");
                date = listObject.getString("Date");
                license_number = listObject.getString("License_number");

                // creating new HashMap
                HashMap<String, String> map = new HashMap<String, String>();

                // adding each child node to HashMap key => value
                map.put(KEY_ID, id);
                map.put(KEY_NAME, violator_name);
                map.put(KEY_STATE, state);
                map.put(KEY_DATE, date);
                map.put(KEY_LICENSE, license_number);

                // adding HashList to ArrayList
                violationListTask.add(map);
            }

            Log.v(LOG_TAG, "JSON Parsing: " + listingJsonStr);
               return "empty";

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
                return null;
        }


        }

        @Override
        protected String doInBackground(String... params) {

            // If there's no zip code, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                //return null;
            }
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String listingJsonStr = null;

            String app = "26";
            String actn = "75";

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                //final String FORECAST_BASE_URL ="http://10.0.2.2:3933/getitFastEnterpriseWCF.ashx/?";
                final String FORECAST_BASE_URL ="http://search.getit.in";
                final String APP_PARAM = "app";
                final String ACTN_PARAM = "actn";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(APP_PARAM, app)
                        .appendQueryParameter(ACTN_PARAM, actn)
                        .build();

                java.net.URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream != null) {

                    reader = new BufferedReader(new InputStreamReader(inputStream));
                }

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                }
                else {
                    listingJsonStr = buffer.toString();
                }

                Log.v(LOG_TAG, "Listing string: " + listingJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
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
                return getListingDataFromJson(listingJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            if (result != null) {
                // New data is back from the server.  Hooray!
                //Toast.makeText(activity, "onpost execute", Toast.LENGTH_LONG).show();

                list = (ListView) findViewById(R.id.list);

                // Getting adapter by passing xml data ArrayList
                adapter = new LazyAdapter(activity, violationList);
                list.setAdapter(adapter);
                // Click event for single list row
                list.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        map = violationList.get(position);
                        //Toast.makeText(activity, "harmanjeet singh", Toast.LENGTH_LONG).show();
                        ID = map.get("id");
                        Intent intent = new Intent(activity, Form.class);
                        intent.putExtra(EXTRA_VIOLATION_ID, ID);
                        startActivity(intent);

                    }
                });
            }
                if (pd != null) {
                    pd.dismiss();
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