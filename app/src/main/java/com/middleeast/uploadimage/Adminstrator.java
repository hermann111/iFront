package com.middleeast.uploadimage;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.print.PrintHelper;
import android.telecom.TelecomManager;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.UUID;

// Testing Version Control
public class Adminstrator extends Activity implements CustomModalDialogFragment.NoticeDialogListener {
    AdminControllerView controller;
    TelephonyManager telephonyManager;
    private TransparentProgressDialog pd;
    private static String imie=null;
    private static String uuid=null;
    private static final String INSTALLATION = "INSTALLATION";
    private final String LOG_TAG = Adminstrator.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ActionBar actionBar = getActionBar();
       actionBar.setDisplayHomeAsUpEnabled(false);
        //actionBar.setTitle(getResources().getString(R.string.app_name));
       //actionBar.setSubtitle(getResources().getString(R.string.admin_subtitle));

        setContentView(R.layout.activity_adminstrator);

        if(!Util.isNetworkAvailable(this)) {
            showNoticeDialog();
        }

            //Commented the code to not show the overlay on splash screen
            controller = new AdminControllerView(this);
            controller.setAnchorView((FrameLayout) findViewById(R.id.videoSurfaceContainer));
            controller.show();

            findViewById(R.id.imgview_login_inspector).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    Intent openResultList = new Intent("com.middleeast.uploadimage.HOMESCREEN");
                    startActivity(openResultList);
                }
            });

            findViewById(R.id.imgview_login_admin).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    Intent openResultList = new Intent("com.middleeast.uploadimage.CUSTOMIZEDLISTVIEW");
                    startActivity(openResultList);
                }
            });

            findViewById(R.id.imgview_ip_camera).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    Intent openResultList = new Intent(getApplicationContext(), Region.class);
                    startActivity(openResultList);
                }
            });
            imie = readIMIENumber();
            File installation = new File(this.getFilesDir(), INSTALLATION);

            pd = new TransparentProgressDialog(Adminstrator.this, R.drawable.loader_pink);
            pd.show();

            if (!installation.exists()) {
                uuid = Installation.id(this);
                RegisterInstallationTask registerInstallationTask = new RegisterInstallationTask();
                registerInstallationTask.execute(imie, uuid);
            } else {
                try {
                    uuid = Installation.readInstallationFile(installation);
                } catch (IOException ex) {
                    Log.e(LOG_TAG, "Error ", ex);
                }

                ValidateInstallationTask validateInstallationTask = new ValidateInstallationTask();
                validateInstallationTask.execute(imie, uuid);
            }

        Toast.makeText(this, "Administrator Activity", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!Util.isNetworkAvailable(this)) {
            showNoticeDialog();
        }
    }

    public void showNoticeDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new CustomModalDialogFragment();
        dialog.show(getFragmentManager(), "NoticeDialogFragment");
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
       //finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        String mailBody="\t\n" +
                "\n" +
                "    <!DOCTYPE html PUBLIC \n" +
                "\"-//W3C//DTD XHTML 1.0 \n" +
                "Transitional//EN\" \n" +
                "\"http://www.w3.org/TR/xhtml1/DTD/\n" +
                "xhtml1-transitional.dtd\">\n" +
                "    <html \n" +
                "xmlns=\"http://www.w3.org/1999/xht\n" +
                "ml\">\n" +
                "     <head>\n" +
                "      <meta http-equiv=\"Content-\n" +
                "Type\" content=\"text/html; \n" +
                "charset=UTF-8\" />\n" +
                "      <title>Demystifying Email \n" +
                "Design</title>\n" +
                "      <meta name=\"viewport\" \n" +
                "content=\"width=device-width, \n" +
                "initial-scale=1.0\"/>\n" +
                "    </head>\n" +
                "    <body style=\"margin: 0; \n" +
                "padding: 0;\">\n" +
                "     <table border=\"1\" \n" +
                "cellpadding=\"0\" cellspacing=\"0\" \n" +
                "width=\"100%\">\n" +
                "      <tr>\n" +
                "       <td>\n" +
                "         <table align=\"center\" \n" +
                "border=\"1\" cellpadding=\"0\" \n" +
                "cellspacing=\"0\" width=\"600\" \n" +
                "style=\"border-collapse: \n" +
                "collapse;\">\n" +
                "           <tr>\n" +
                "             <td align=\"center\" \n" +
                "bgcolor=\"#70bbd9\" style=\"padding: \n" +
                "40px 0 30px 0;\">\n" +
                "              <img \n" +
                "src=\"images/h1.gif\" alt=\"Creating \n" +
                "Email Magic\" width=\"300\" \n" +
                "height=\"230\" style=\"display: \n" +
                "block;\" />\n" +
                "             </td>\n" +
                "           </tr>\n" +
                "           <tr>\n" +
                "             <td \n" +
                "bgcolor=\"#ffffff\" style=\"padding: \n" +
                "40px 30px 40px 30px;\">\n" +
                "               <table border=\"1\" \n" +
                "cellpadding=\"0\" cellspacing=\"0\" \n" +
                "width=\"100%\">\n" +
                "                <tr>\n" +
                "                 <td>\n" +
                "                  Lorem ipsum \n" +
                "dolor sit amet!\n" +
                "                 </td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                 <td \n" +
                "style=\"padding: 20px 0 30px 0;\">\n" +
                "                  Lorem ipsum \n" +
                "dolor sit amet, consectetur \n" +
                "adipiscing elit. In tempus \n" +
                "adipiscing felis, sit amet \n" +
                "blandit ipsum volutpat sed. Morbi \n" +
                "porttitor, eget accumsan dictum, \n" +
                "nisi libero ultricies ipsum, in \n" +
                "posuere mauris neque at erat.\n" +
                "                 </td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                 <td>\n" +
                "                   <table \n" +
                "border=\"1\" cellpadding=\"0\" \n" +
                "cellspacing=\"0\" width=\"100%\">\n" +
                "                    <tr>\n" +
                "                     <td \n" +
                "width=\"260\" valign=\"top\">\n" +
                "                      <table \n" +
                "border=\"1\" cellpadding=\"0\" \n" +
                "cellspacing=\"0\" width=\"100%\">\n" +
                "                       <tr>\n" +
                "                        <td>\n" +
                "                         <img \n" +
                "src=\"images/left.gif\" alt=\"\" \n" +
                "width=\"100%\" height=\"140\" \n" +
                "style=\"display: block;\" />\n" +
                "                        </td>\n" +
                "                       </tr>\n" +
                "                       <tr>\n" +
                "                        <td \n" +
                "style=\"padding: 25px 0 0 0;\">\n" +
                "                         Lorem \n" +
                "ipsum dolor sit amet, consectetur \n" +
                "adipiscing elit. In tempus \n" +
                "adipiscing felis, sit amet \n" +
                "blandit ipsum volutpat sed. Morbi \n" +
                "porttitor, eget accumsan dictum, \n" +
                "nisi libero ultricies ipsum, in \n" +
                "posuere mauris neque at erat.\n" +
                "                        </td>\n" +
                "                       </tr>\n" +
                "                      </table>\n" +
                "                     </td>\n" +
                "                     <td \n" +
                "style=\"font-size: 0; line-height: \n" +
                "0;\" width=\"20\">\n" +
                "                      &nbsp;\n" +
                "                     </td>\n" +
                "                     <td \n" +
                "width=\"260\" valign=\"top\">\n" +
                "                      <table \n" +
                "border=\"1\" cellpadding=\"0\" \n" +
                "cellspacing=\"0\" width=\"100%\">\n" +
                "                       <tr>\n" +
                "                        <td>\n" +
                "                         <img \n" +
                "src=\"images/right.gif\" alt=\"\" \n" +
                "width=\"100%\" height=\"140\" \n" +
                "style=\"display: block;\" />\n" +
                "                        </td>\n" +
                "                       </tr>\n" +
                "                       <tr>\n" +
                "                        <td \n" +
                "style=\"padding: 25px 0 0 0;\">\n" +
                "                         Lorem \n" +
                "ipsum dolor sit amet, consectetur \n" +
                "adipiscing elit. In tempus \n" +
                "adipiscing felis, sit amet \n" +
                "blandit ipsum volutpat sed. Morbi \n" +
                "porttitor, eget accumsan dictum, \n" +
                "nisi libero ultricies ipsum, in \n" +
                "posuere mauris neque at erat.\n" +
                "                        </td>\n" +
                "                       </tr>\n" +
                "                      </table>\n" +
                "                     </td>\n" +
                "                    </tr>\n" +
                "                   </table>\n" +
                "                 </td>\n" +
                "                </tr>\n" +
                "               </table>\n" +
                "             </td>\n" +
                "           </tr>\n" +
                "           <tr>\n" +
                "             <td \n" +
                "bgcolor=\"#ee4c50\" style=\"padding: \n" +
                "30px 30px 30px 30px;\">\n" +
                "               <table border=\"1\" \n" +
                "cellpadding=\"0\" cellspacing=\"0\" \n" +
                "width=\"100%\">\n" +
                "                 <td width=\"75%\">\n" +
                "                  &reg; Someone, \n" +
                "somewhere 2013<br/>\n" +
                "                  Unsubscribe to \n" +
                "this newsletter instantly\n" +
                "                 </td>\n" +
                "                 <td \n" +
                "align=\"right\">\n" +
                "                  <table \n" +
                "border=\"0\" cellpadding=\"0\" \n" +
                "cellspacing=\"0\">\n" +
                "                   <tr>\n" +
                "                    <td>\n" +
                "                     <a \n" +
                "href=\"http://www.twitter.com/\">\n" +
                "                      <img \n" +
                "src=\"images/tw.gif\" alt=\"Twitter\" \n" +
                "width=\"38\" height=\"38\" \n" +
                "style=\"display: block;\" \n" +
                "border=\"0\" />\n" +
                "                     </a>\n" +
                "                    </td>\n" +
                "                    <td \n" +
                "style=\"font-size: 0; line-height: \n" +
                "0;\" width=\"20\">&nbsp;</td>\n" +
                "                    <td>\n" +
                "                     <a \n" +
                "href=\"http://www.twitter.com/\">\n" +
                "                      <img \n" +
                "src=\"images/fb.gif\" \n" +
                "alt=\"Facebook\" width=\"38\" \n" +
                "height=\"38\" style=\"display: \n" +
                "block;\" border=\"0\" />\n" +
                "                     </a>\n" +
                "                    </td>\n" +
                "                   </tr>\n" +
                "                  </table>\n" +
                "                 </td>\n" +
                "               </table>\n" +
                "             </td>\n" +
                "           </tr>\n" +
                "         </table>\n" +
                "       </td>\n" +
                "      </tr>\n" +
                "     </table>\n" +
                "    </body>\n" +
                "    </html>\n" +
                "\n";

        switch (item.getItemId()) {
             case R.id.action_print:
                 doPhotoPrint();
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

    private void doPhotoPrint() {
        PrintHelper photoPrinter = new PrintHelper(getApplicationContext());
        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.splashone);
        photoPrinter.printBitmap("splash.jpg - test print", bitmap);
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

    public static class Installation {
        private static String sID = null;

        public synchronized static String id(Context context) {
            if (sID == null) {
                File installation = new File(context.getFilesDir(), INSTALLATION);
                try {
                    if (!installation.exists())
                        writeInstallationFile(installation);
                    sID = readInstallationFile(installation);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return sID;
        }

        private static String readInstallationFile(File installation) throws IOException {
            RandomAccessFile f = new RandomAccessFile(installation, "r");
            byte[] bytes = new byte[(int) f.length()];
            f.readFully(bytes);
            f.close();
            return new String(bytes);
        }

        private static void writeInstallationFile(File installation) throws IOException {
            FileOutputStream out = new FileOutputStream(installation);
            String id = UUID.randomUUID().toString();
            out.write(id.getBytes());
            out.close();
        }
    }

    private String readIMIENumber()
    {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId().toString();
    }

    private void validateInstallation()
    {
        ValidateInstallationTask validateInstallationTask = new ValidateInstallationTask();
        validateInstallationTask.execute(imie, uuid);
    }

    public class ValidateInstallationTask extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = ValidateInstallationTask.class.getSimpleName();

        @Override
        protected String doInBackground(String... params) {

             StringBuilder stringBuilder=null;

            // If there's no zip code, there's nothing to look up.  Verify size of params.
            if (params.length == 0)
                return null;

            String IMIE=params[0];
            String UUID=params[1];

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection connection = null;

            //For Live Server
            String urlLiveServer ="http://search.getit.in/?app=26&actn=82" +"&imie=" +IMIE + "&uuid=" + UUID;

            try {
                Uri builtUri=Uri.parse(urlLiveServer).buildUpon().build();
                URL url=new URL(builtUri.toString());
                connection=(HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/xml");
                connection.setRequestProperty("Cache-Control", "no-cache");

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    return readStream(connection.getInputStream());
                }

                return stringBuilder.toString();

            }catch (Exception ex) {
                Log.e(LOG_TAG, "Error ", ex);

            }finally
            {
               if (pd != null) {
                    pd.dismiss();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            if (result != null) {

                String value=result.replace("<root><valid>","");
                value=value.replace("</valid></root>","");

                if(value.toUpperCase().equals("FALSE")) {
                    Toast.makeText(getApplicationContext(), "Your Installation is not Licensed", Toast.LENGTH_LONG).show();
                    finish();
                }

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

    public class RegisterInstallationTask extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = RegisterInstallationTask.class.getSimpleName();

        @Override
        protected String doInBackground(String... params) {

            StringBuilder stringBuilder=null;

            // If there's no zip code, there's nothing to look up.  Verify size of params.
            if (params.length == 0)
                return null;

            String IMIE=params[0];
            String UUID=params[1];

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection connection = null;

            //For Live Server
            String urlLiveServer ="http://search.getit.in/?app=26&actn=81" +"&imie=" +IMIE + "&uuid=" + UUID;

            try {
                Uri builtUri=Uri.parse(urlLiveServer).buildUpon().build();
                URL url=new URL(builtUri.toString());
                connection=(HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/xml");
                connection.setRequestProperty("Cache-Control", "no-cache");

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    return readStream(connection.getInputStream());
                }

                return stringBuilder.toString();

            }catch (Exception ex) {
                Log.e(LOG_TAG, "Error ", ex);

            }finally
            {
                if (pd != null) {
                    pd.dismiss();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            if (result != null) {

                String value=result.replace("<root><ID>","");
                value=value.replace("</ID></root>","");

                if(Integer.parseInt(value) > 0) {
                    Toast.makeText(getApplicationContext(), "Your Device is successfully registered", Toast.LENGTH_LONG).show();
                    validateInstallation();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Your Device failed to registered!! Try again", Toast.LENGTH_LONG).show();
                    finish();
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

