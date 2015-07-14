package com.middleeast.uploadimage;

        import android.content.Context;
        import android.content.Intent;
        import android.os.Handler;
        import android.os.Message;
        import android.util.AttributeSet;
        import android.util.Log;
        import android.view.Gravity;
        import android.view.KeyEvent;
        import android.view.LayoutInflater;
        import android.view.MotionEvent;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.FrameLayout;
        import android.widget.ImageButton;
        import android.widget.ImageView;
        import android.widget.ProgressBar;
        import android.widget.SeekBar;
        import android.widget.TextView;

        import java.lang.ref.WeakReference;
        import java.util.Formatter;
        import java.util.Locale;

/**
 * Created by harmanjeet.s on 4/23/2015.
 */
public class FormControllerView extends FrameLayout {

    private static final String TAG = "FormControllerView";
    private Context mContext;
    private ViewGroup mAnchor;
    private View mRoot;
    private boolean             mShowing;
    private boolean             mDragging;
    private static final int    sDefaultTimeout = 10000;
    private static final int    FADE_OUT = 1;
    private static final int    SHOW_PROGRESS = 2;
    private Handler             mHandler = new MessageHandler(this);
    private ImageView           mFormButton;
    private ImageView           mPhotoButton;
    private ImageView           mVideoButton;

    private String[]            mImage;
    private String[]            mVideo;
    String Id;  //Violation ID
    public final static String EXTRA_VIOLATION_ID = "com.middleeast.uploadimage.VIOLATION_ID";
    public final static String EXTRA_IMAGE = "com.middleeast.uploadimage.IMAGE";
    public final static String EXTRA_VIDEO = "com.middleeast.uploadimage.VIDEO";

    public FormControllerView(Context context) {
        super(context);
        mContext = context;
    }

    public FormControllerView(Context context,String violationID, String[] image,String...video) {
        this(context);

        Id=violationID;
        mImage=image;
        mVideo=video;

        Log.i(TAG, TAG);
    }

    /**
     * Set the view that acts as the anchor for the control view.
     * This can for example be a VideoView, or your Activity's main view.
     * @param view The view to which to anchor the controller when it is visible.
     */
    public void setAnchorView(ViewGroup view) {
        mAnchor = view;

        LayoutParams frameParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        removeAllViews();
        View v = makeControllerView();
        addView(v, frameParams);
    }

    /**
     * Create the view that holds the widgets that control playback.
     * Derived classes can override this to create their own.
     * @return The controller view.
     * @hide This doesn't work as advertised
     */
    protected View makeControllerView() {
        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot = inflate.inflate(R.layout.form_controller, null);
        initControllerView(mRoot);

        return mRoot;
    }

    @Override
    public void onFinishInflate() {
        if (mRoot != null)
            initControllerView(mRoot);
    }


    private void initControllerView(View v) {

        mFormButton = (ImageView) v.findViewById(R.id.btn_ic_action_read);
        if (mFormButton != null) {
            mFormButton.requestFocus();
            mFormButton.setOnClickListener(mFormListener);
        }

        mPhotoButton = (ImageView) v.findViewById(R.id.btn_ic_action_picture);
        if (mPhotoButton != null) {
            mPhotoButton.requestFocus();
            mPhotoButton.setOnClickListener(mPhotoListener);
        }

        mVideoButton = (ImageView) v.findViewById(R.id.btn_ic_action_video);
        if (mVideoButton != null) {
            mVideoButton.requestFocus();
            mVideoButton.setOnClickListener(mVideoListener);
        }
    }


    /**
     * Show the controller on screen. It will go away
     * automatically after 3 seconds of inactivity.
     */
    public void show() {
        show(sDefaultTimeout);
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 'timeout' milliseconds of inactivity.
     * @param timeout The timeout in milliseconds. Use 0 to show
     * the controller until hide() is called.
     */
    public void show(int timeout) {
        if (!mShowing && mAnchor != null) {

             LayoutParams tlp = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM
            );

            mAnchor.addView(this, tlp);
            mShowing = true;
        }

        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        Message msg = mHandler.obtainMessage(FADE_OUT);
        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }
    }

    /**
     * Remove the controller from the screen.
     */
    public void hide() {
        if (mAnchor == null) {
            return;
        }

        try {
            mAnchor.removeView(this);
        } catch (IllegalArgumentException ex) {
            Log.w("MediaController", "already removed");
        }
        mShowing = false;
    }

    private OnClickListener mFormListener = new OnClickListener() {
        public void onClick(View v) {
            if(!mContext.toString().contains("Form")) {
                Intent i = new Intent();
                i.putExtra(EXTRA_VIOLATION_ID, Id);
                i.setClass(v.getContext(), Form.class);
                v.getContext().startActivity(i);
            }
        }
    };

    private OnClickListener mPhotoListener = new OnClickListener() {
        public void onClick(View v) {
            if(!mContext.toString().contains("PhotoPlayerActivity")) {

                Intent i = new Intent();
                i.putExtra(EXTRA_VIOLATION_ID, Id);
                i.putExtra(EXTRA_IMAGE, mImage);
                i.putExtra(EXTRA_VIDEO, mVideo);
                i.setClass(v.getContext(), PhotoPlayerGlide.class);
                v.getContext().startActivity(i);
            }
        }
    };

    private OnClickListener mVideoListener = new OnClickListener() {
        public void onClick(View v) {
            if(!mContext.toString().contains("VideoPlayerActivity")) {
                Intent i = new Intent();
                i.putExtra(EXTRA_VIOLATION_ID, Id);
                i.putExtra(EXTRA_IMAGE, mImage);
                i.putExtra(EXTRA_VIDEO, mVideo);
                i.setClass(v.getContext(), VideoPlayerActivity.class);
                v.getContext().startActivity(i);
            }
        }
    };

    private static class MessageHandler extends Handler {
        private final WeakReference<FormControllerView> mView;

        MessageHandler(FormControllerView view) {
            mView = new WeakReference<FormControllerView>(view);
        }
        @Override
        public void handleMessage(Message msg) {
            FormControllerView view = mView.get();
            if (view == null ) {
                return;
            }

            int pos;
            switch (msg.what) {
                case FADE_OUT:
                    //commented by Harmanjeet on May 10, 2015
                    //view.hide();
                    break;
                case SHOW_PROGRESS:
                    pos =0; // view.setProgress();
                    if (!view.mDragging && view.mShowing) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    }
}

