package com.middleeast.uploadimage;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.telephony.SmsManager;
import android.util.Base64;
import android.util.Log;
import android.util.Xml;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by harmanjeet.s on 3/22/2015.
 */
public class ViolationScreenActivity extends FragmentActivity {

    public EditText mDateTextView;
    public TextView txtSerialNo;
    private TransparentProgressDialog pd;
    String SMS_SENT = "SMS_SENT";
    String SMS_DELIVERED = "SMS_DELIVERED";
    PendingIntent sentPendingIntent;
    PendingIntent deliveredPendingIntent;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        //getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00E3E3")));
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFFFFF")));
        //bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.oman_action_bar));

        setContentView(R.layout.violationscreenactivity_main);
        pd = new TransparentProgressDialog(ViolationScreenActivity.this, R.drawable.loader_green);
        //Modify by Harmanjeet on April 03, 2015
        // To Show Move to Camera ImageView after screen data is saved.
        //findViewById(R.id.btn_moveto_camera).setClickable(false).setVisibility(View.INVISIBLE);
        //findViewById(R.id.moveto_camera).setVisibility(View.INVISIBLE);

        sentPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(SMS_SENT), 0);
        deliveredPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(SMS_DELIVERED), 0);


        findViewById(R.id.image_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                pd.show();
                saveData();
            }
        });

        findViewById(R.id.btn_moveto_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                if(txtSerialNo!=null) {
                    Intent openMainActivity = new Intent("com.middleeast.uploadimage.IMAGEVIDEOACTIVITY");
                    openMainActivity.putExtra("ID", txtSerialNo.getText());
                    startActivity(openMainActivity);
                }
                else
                {
                    Toast.makeText(getApplicationContext(),R.string.serialno_check, Toast.LENGTH_LONG).show();
                }
            }
        });

        // For when the SMS has been sent
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS sent successfully", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(context, "Generic failure cause", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(context, "Service is currently unavailable", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(context, "No pdu provided", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(context, "Radio was explicitly turned off", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SMS_SENT));

        // For when the SMS has been delivered
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SMS_DELIVERED));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_form, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                if(this.getLocalClassName().equals("ViolationScreenActivity")) {
                    //Toast.makeText(this, this.getLocalClassName(), Toast.LENGTH_LONG).show();
                    Intent openMainActivity= new Intent("com.middleeast.uploadimage.HOMESCREEN");
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

            case R.id.ic_action_chat:

                EditText mobileNumber=(EditText) findViewById(R.id.txt_mobile_value);
                String phoneNumber=mobileNumber.getText().toString();
                String smsBody="Hello This is a test automated SMS from MESEC fine system";

                try {

                    // Get the default instance of SmsManager
                    SmsManager smsManager = SmsManager.getDefault();
                    // Send a text based SMS
                    smsManager.sendTextMessage(phoneNumber, null, smsBody, sentPendingIntent, deliveredPendingIntent);


                    //SmsManager.getDefault().sendTextMessage(mobile, null, "Hello This is a test automated SMS from MESEC fine system", null, null);
                } catch (Exception e) {
                    AlertDialog.Builder alertDialogBuilder = new
                            AlertDialog.Builder(this);
                    AlertDialog dialog = alertDialogBuilder.create();
                    dialog.setMessage(e.getMessage());
                    dialog.show();
                }
                return true;

            case R.id.action_email:

                EditText emailAddress=(EditText) findViewById(R.id.txt_email_value);
                String email= emailAddress.getText().toString();

                new SendMailTask(ViolationScreenActivity.this).execute("hermann@hermannsoftware.com", "Satwant@111", email, "MESEC - fine system", "Hello This is a test automated mail from MESEC fine system");
                return true;

            case R.id.action_language:
                restartInLocale();
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }
    @Override
    public void onBackPressed()
    {
        return;
    }

    public void showDatePickerDialog(View v) {

        mDateTextView= (EditText)findViewById(R.id.txtdepartment_date_value);
        DialogFragment newFragment = new DatePickerFragment(mDateTextView);
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void showWroteDatePickerDialog(View v) {

        mDateTextView= (EditText)findViewById(R.id.txt_wrote_date);
        DialogFragment newFragment = new DatePickerFragment(mDateTextView);
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void showReceiptDatePickerDialog(View v) {

        mDateTextView= (EditText)findViewById(R.id.edtxt_receipt_date_value);
        DialogFragment newFragment = new DatePickerFragment(mDateTextView);
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        EditText mDateTextViewLocal;
        public DatePickerFragment()
        {

        }
        DatePickerFragment(EditText textview)
        {
            mDateTextViewLocal=textview;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            Calendar cal = new GregorianCalendar(year, month, day);
            //SimpleDateFormat sdf = new SimpleDateFormat(dd/MM/yyyy);
            //mDateTextViewLocal.setText(sdf.format(cal.getTime()));
            mDateTextViewLocal.setText(String.valueOf(day)+"/"+String.valueOf(month+1)+"/"+String.valueOf(year));
        }
    }

    private void saveData()
    {
        SaveDataTask weatherTask = new SaveDataTask();
        //SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getActivity());
        String defaultValue="harmanjeet singh"; //prefs.getString(getString(R.string.pref_location_key),getString(R.string.pref_default_location_value));

        weatherTask.execute(defaultValue);
    }

  public class SaveDataTask extends AsyncTask<String, Void, String> {

    private final String LOG_TAG = SaveDataTask.class.getSimpleName();

    @Override
    protected String doInBackground(String... params) {

        BufferedReader reader = null;
        StringBuilder stringBuilder=null;

        // If there's no zip code, there's nothing to look up.  Verify size of params.
        if (params.length == 0)
            return null;

        String response;
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection connection = null;
        DataOutputStream outputStream=null;

        //For Local Host URL
        String urlLocalServer ="http://10.0.2.2:3933/getitfastenterprisewcf.ashx/?app=26&actn=76";
        //For Live Server
        String urlLiveServer ="http://search.getit.in/?app=26&actn=76";

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            //final String FORECAST_BASE_URL ="http://api.openweathermap.org/data/2.5/forecast/daily?";
            String inputXML=writeXml();
            Uri builtUri=Uri.parse(urlLiveServer).buildUpon().build();
            URL url=new URL(builtUri.toString());
            String postParameters=inputXML;    // URLEncoder.encode(encodedString, "UTF-8");
            connection=(HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            //conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            connection.setRequestProperty("Content-Type", "application/xml");
            connection.setRequestProperty("Cache-Control","no-cache");
            connection.setFixedLengthStreamingMode(postParameters.getBytes().length);
            OutputStreamWriter outSR=new OutputStreamWriter(connection.getOutputStream());
            outSR.write(postParameters);

            // read the output from the server
            /*reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            stringBuilder = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null)
            {
                stringBuilder.append(line + "\n");
            }*/

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
            if (pd != null) {
                pd.dismiss();
            }

            return null;
        }finally
        {
            // close the reader; this can throw an exception too, so
            // wrap it in another try/catch block.
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException ioe)
                {
                    ioe.printStackTrace();
                }
            }

            if (pd != null) {
                pd.dismiss();
            }
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
             txtSerialNo=(TextView)findViewById(R.id.txtserialno);
            txtSerialNo.setText(value);

            //Modify by Harmanjeet on April 03, 2015
            // To Show Move to Camera ImageView after screen data is saved.
            ((ImageView) findViewById(R.id.btn_moveto_camera)).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.moveto_camera)).setVisibility(View.VISIBLE);
            //}
            // New data is back from the server.  Hooray!
        }
        if (pd != null) {
            pd.dismiss();
        }
    }


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

    private String writeXml(){
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        TextView txtdepartment_date_value=(TextView)findViewById(R.id.txtdepartment_date_value);
        EditText edtxt_department_name=(EditText)findViewById(R.id.edtxt_department_name);
        EditText edtxt_reqion_value=(EditText)findViewById(R.id.edtxt_reqion_value);
        EditText edtxt_state_value=(EditText)findViewById(R.id.edtxt_state_value);
        EditText edtxt_violator_name_value=(EditText)findViewById(R.id.edtxt_violator_name_value);
        EditText edtxt_location_value=(EditText)findViewById(R.id.edtxt_location_value);
        EditText edtxt_activity_type_value=(EditText)findViewById(R.id.edtxt_activity_type_value);
        EditText edtxt_license_number_value=(EditText)findViewById(R.id.edtxt_license_number_value);
        EditText edtext_multi_violation=(EditText)findViewById(R.id.edtext_multi_violation);
        TextView edtext_fine_line1=(TextView)findViewById(R.id.edtext_fine_line1);
        EditText edtext_fine_line1_value=(EditText)findViewById(R.id.edtext_fine_line1_value);
        TextView edtext_fine_line2=(TextView)findViewById(R.id.edtext_fine_line2);
        EditText edtext_fine_line2_value=(EditText)findViewById(R.id.edtext_fine_line2_value);
        EditText edtext_fine_words_value=(EditText)findViewById(R.id.edtext_fine_words_value);
        TextView edtext_notice_period_line1=(TextView)findViewById(R.id.edtext_notice_period_line1);
        TextView edtext_notice_period_line2=(TextView)findViewById(R.id.edtext_notice_period_line2);
        EditText edtext_notice_period_line2_value=(EditText)findViewById(R.id.edtext_notice_period_line2_value);
        TextView edtext_notice_period_line2_next=(TextView)findViewById(R.id.edtext_notice_period_line2_next);
        TextView edtext_notice_period_line3=(TextView)findViewById(R.id.edtext_notice_period_line3);
        TextView txt_wrote_date=(TextView)findViewById(R.id.txt_wrote_date);
        EditText edtxt_recipient_name_value=(EditText)findViewById(R.id.edtxt_recipient_name_value);
        EditText edtxt_signature_recipient_value=(EditText)findViewById(R.id.edtxt_signature_recipient_value);

        EditText edtxt_wrote_name_value=(EditText)findViewById(R.id.edtxt_wrote_name_value);
        EditText edtxt_txt_occupation_value=(EditText)findViewById(R.id.edtxt_txt_occupation_value);
        EditText edtxt_signature_value=(EditText)findViewById(R.id.edtxt_signature_value);
        EditText edtxt_receipt_date_value=(EditText)findViewById(R.id.edtxt_receipt_date_value);
        EditText edtxt_fine_number_value=(EditText)findViewById(R.id.edtxt_fine_number_value);

        EditText edtxt_accountant_name_value=(EditText)findViewById(R.id.edtxt_accountant_name_value);
        EditText edtxt_head_of_department_value=(EditText)findViewById(R.id.edtxt_head_of_department_value);

        String edtext_fine=edtext_fine_line1.getText()+" "+edtext_fine_line1_value.getText()+" "+edtext_fine_line2.getText()+" "+edtext_fine_line2_value.getText();

        String edtext_notice_period=edtext_notice_period_line1.getText()+" "+edtext_fine_line2.getText()+" "+edtext_notice_period_line2_value.getText()+" "+edtext_notice_period_line2_next.getText()+" "+edtext_notice_period_line3.getText();

        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag("", "root");
            //serializer.attribute("", "number", String.valueOf(messages.size()));
            //for (Message msg: messages){
                serializer.startTag("", "violation");
                //serializer.attribute("", "date", msg.getDate());

                serializer.startTag("", "id");
                serializer.text("0");
                serializer.endTag("", "id");
                serializer.startTag("", "serialno");
                serializer.text("0");
                serializer.endTag("", "serialno");
                serializer.startTag("", "date");
                serializer.text(txtdepartment_date_value.getText().toString());
                serializer.endTag("", "date");
                serializer.startTag("", "region");
                serializer.text(edtxt_reqion_value.getText().toString());
                serializer.endTag("", "region");
                serializer.startTag("", "state");
                serializer.text(edtxt_state_value.getText().toString());
                serializer.endTag("", "state");
                serializer.startTag("", "violatorname");
                serializer.text(edtxt_violator_name_value.getText().toString());
                serializer.endTag("", "violatorname");
                serializer.startTag("", "location");
                serializer.text(edtxt_location_value.getText().toString());
                serializer.endTag("", "location");
                serializer.startTag("", "typeofactivity");
                serializer.text(edtxt_activity_type_value.getText().toString());
                serializer.endTag("", "typeofactivity");
                serializer.startTag("", "licensenumber");
                serializer.text(edtxt_license_number_value.getText().toString());
                serializer.endTag("", "licensenumber");
                serializer.startTag("", "violation");
                serializer.text(edtext_multi_violation.getText().toString());
                serializer.endTag("", "violation");
                serializer.startTag("", "fine");
                serializer.text(edtext_fine);
                serializer.endTag("", "fine");
                serializer.startTag("", "amountinletters");
                serializer.text(edtext_fine_words_value.getText().toString());
                serializer.endTag("", "amountinletters");
                serializer.startTag("", "notice");
                serializer.text(edtext_notice_period);
                serializer.endTag("", "notice");
                serializer.startTag("", "wroteindate");
                serializer.text(txt_wrote_date.getText().toString());
                serializer.endTag("", "wroteindate");
                serializer.startTag("", "recipientname");
                serializer.text(edtxt_recipient_name_value.getText().toString());
                serializer.endTag("", "recipientname");
                serializer.startTag("", "recipientsignature");
                serializer.text(edtxt_signature_recipient_value.getText().toString());
                serializer.endTag("", "recipientsignature");
                serializer.startTag("", "wrote");
                serializer.text(edtxt_wrote_name_value.getText().toString());
                serializer.endTag("", "wrote");
                serializer.startTag("", "occupation");
                serializer.text(edtxt_txt_occupation_value.getText().toString());
                serializer.endTag("", "occupation");
                serializer.startTag("", "signature");
                serializer.text(edtxt_signature_value.getText().toString());
                serializer.endTag("", "signature");
                serializer.startTag("", "receipt");
                serializer.text(edtxt_receipt_date_value.getText().toString());
                serializer.endTag("", "receipt");
                serializer.startTag("", "finenumber");
                serializer.text(edtxt_fine_number_value.getText().toString());
                serializer.endTag("", "finenumber");
                serializer.startTag("", "accountantname");
                serializer.text(edtxt_accountant_name_value.getText().toString());
                serializer.endTag("", "accountantname");
                serializer.startTag("", "accountantsignature");
                serializer.text("");
                serializer.endTag("", "accountantsignature");
                serializer.startTag("", "headofdepartment");
                serializer.text(edtxt_head_of_department_value.getText().toString());
                serializer.endTag("", "headofdepartment");
                serializer.startTag("", "note");
                serializer.text("");
                serializer.endTag("", "note");
                serializer.startTag("", "stamp");
                serializer.text("");
                serializer.endTag("", "stamp");
                serializer.startTag("", "ecopy");
                serializer.text("");
                serializer.endTag("", "ecopy");
                serializer.startTag("", "department");
                serializer.text(edtxt_department_name.getText().toString());
                serializer.endTag("", "department");
                serializer.startTag("", "language");
                serializer.text(Locale.getDefault().getLanguage());
                serializer.endTag("", "language");
                serializer.startTag("", "form");
                serializer.text("3");
                serializer.endTag("", "form");
                serializer.endTag("", "violation");
           // }
            serializer.endTag("", "root");
            serializer.endDocument();
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
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

