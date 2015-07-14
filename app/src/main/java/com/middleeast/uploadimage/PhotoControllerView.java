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
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.AdapterViewFlipper;
import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;

/**
 * Created by harmanjeet.s on 4/19/2015.
 */
public class PhotoControllerView extends FrameLayout {
    private static final String TAG = "PhotoControllerView";
    private AdapterViewFlipperControl  mPlayer;
    private Context             mContext;
    private ViewGroup           mAnchor;
    private View                mRoot;
    private SeekBar             mProgress;
    private TextView            mEndTime, mCurrentTime;
    private boolean             mShowing;
    private boolean             mDragging;
    private static final int    sDefaultTimeout = 3000;
    private static final int    FADE_OUT = 1;
    private static final int    SHOW_PROGRESS = 2;
    private boolean             mUseFastForward;
    private boolean             mFromXml;
    private boolean             mListenersSet;
    private OnClickListener mNextListener, mPrevListener;
    StringBuilder               mFormatBuilder;
    Formatter                   mFormatter;
    private ImageButton         mPauseButton;
    private ImageButton         mFfwdButton;
    private ImageButton         mRewButton;
    private ImageButton         mNextButton;
    private ImageButton         mPrevButton;
    private ImageButton         mFullscreenButton;
    private ImageView           mFormButton;
    private ImageView           mPhotoButton;
    private ImageView           mVideoButton;

    private String[]            mImage;
    private String[]            mVideo;
    private Handler             mHandler = new MessageHandler(this);
    String Id;  //Violation ID
    public final static String EXTRA_VIOLATION_ID = "com.middleeast.uploadimage.VIOLATION_ID";
    public final static String EXTRA_IMAGE = "com.middleeast.uploadimage.IMAGE";
    public final static String EXTRA_VIDEO = "com.middleeast.uploadimage.VIDEO";

    public PhotoControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRoot = null;
        mContext = context;
        mUseFastForward = true;
        mFromXml = true;

        Log.i(TAG, TAG);
    }

    public PhotoControllerView(Context context, boolean useFastForward) {
        super(context);
        mContext = context;
        mUseFastForward = useFastForward;

        Log.i(TAG, TAG);
    }
    public PhotoControllerView(Context context) {
        super(context);
        mContext = context;
        Log.i(TAG, TAG);
    }
    public PhotoControllerView(Context context,String violationID, String[] image,String...video) {
        this(context, true);
        Id=violationID;
        mImage=image;
        mVideo=video;
        setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Handle next click here

                if (mPlayer == null) {
                    return;
                }

                mPlayer.showNext();
                show(sDefaultTimeout);
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Handle previous click here

                if (mPlayer == null) {
                    return;
                }

                mPlayer.showPrevious();
                show(sDefaultTimeout);
            }
        });

        Log.i(TAG, TAG);
    }

    @Override
    public void onFinishInflate() {
        if (mRoot != null)
            initControllerView(mRoot);
    }

    public void setMediaPlayer(AdapterViewFlipperControl player) {
        mPlayer = player;
        updatePausePlay();
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
        mRoot = inflate.inflate(R.layout.media_controller_green, null);

        initControllerView(mRoot);

        return mRoot;
    }

    private void initControllerView(View v) {
        mPauseButton = (ImageButton) v.findViewById(R.id.pause);
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mPauseListener);
        }

        mFullscreenButton = (ImageButton) v.findViewById(R.id.fullscreen);
        if (mFullscreenButton != null) {
            mFullscreenButton.requestFocus();
            //mFullscreenButton.setOnClickListener(mFullscreenListener);
        }

        mFfwdButton = (ImageButton) v.findViewById(R.id.ffwd);
        if (mFfwdButton != null) {
            //mFfwdButton.setOnClickListener(mFfwdListener);
            if (!mFromXml) {
                mFfwdButton.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
            }
        }

        mRewButton = (ImageButton) v.findViewById(R.id.rew);
        if (mRewButton != null) {
            //mRewButton.setOnClickListener(mRewListener);
            if (!mFromXml) {
                mRewButton.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
            }
        }

        // By default these are hidden. They will be enabled when setPrevNextListeners() is called
        mNextButton = (ImageButton) v.findViewById(R.id.next);
        if(mNextButton!=null) {
            //mNextButton.setOnClickListener(mNextListener);
            if (!mFromXml && !mListenersSet) {
                mNextButton.setVisibility(View.GONE);
            }
        }
        mPrevButton = (ImageButton) v.findViewById(R.id.prev);
        if(mPrevButton != null) {
            //mPrevButton.setOnClickListener(mPrevListener);
            if (!mFromXml && !mListenersSet) {
                mPrevButton.setVisibility(View.GONE);
            }
        }
        mProgress = (SeekBar) v.findViewById(R.id.mediacontroller_progress);
        if (mProgress != null) {
            if (mProgress instanceof SeekBar) {
                ProgressBar seeker = (SeekBar) mProgress;
                //seeker.setOnSeekBarChangeListener(mSeekListener);
            }
            mProgress.setMax(1000);
        }

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

        mEndTime = (TextView) v.findViewById(R.id.time);
        mCurrentTime = (TextView) v.findViewById(R.id.time_current);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        installPrevNextListeners();
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

            if (mPauseButton != null) {
                mPauseButton.requestFocus();
            }

            LayoutParams tlp = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM
            );

            mAnchor.addView(this, tlp);
            mShowing = true;
        }
        updatePausePlay();

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

    public boolean isShowing() {
        return mShowing;
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

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        show(sDefaultTimeout);
        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(sDefaultTimeout);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mPlayer == null) {
            return true;
        }

        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode ==  KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                doPauseResume();
                show(sDefaultTimeout);
                if (mPauseButton != null) {
                    mPauseButton.requestFocus();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !mPlayer.isFlipping()) {
                mPlayer.startFlipping();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mPlayer.isFlipping()) {
                mPlayer.stopFlipping();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide();
            }
            return true;
        }

        show(sDefaultTimeout);
        return super.dispatchKeyEvent(event);
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
                i.setClass(v.getContext(), PhotoPlayerActivity.class);
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

    private OnClickListener mPauseListener = new OnClickListener() {
        public void onClick(View v) {
            doPauseResume();
            show(sDefaultTimeout);
        }
    };

    public void updatePausePlay() {
        if (mRoot == null || mPauseButton == null || mPlayer == null) {
            return;
        }

        if (mPlayer.isFlipping()) {
            mPauseButton.setImageResource(R.drawable.ic_media_pause);
        } else {
            mPauseButton.setImageResource(R.drawable.ic_media_play);
        }
    }

    private void doPauseResume() {
        if (mPlayer == null) {
            return;
        }

        if (mPlayer.isFlipping()) {
            mPlayer.stopFlipping();
        } else {
            mPlayer.startFlipping();
        }
        updatePausePlay();
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (mPauseButton != null) {
            mPauseButton.setEnabled(enabled);
        }
        if (mFfwdButton != null) {
            mFfwdButton.setEnabled(enabled);
        }
        if (mRewButton != null) {
            mRewButton.setEnabled(enabled);
        }
        if (mNextButton != null) {
            mNextButton.setEnabled(enabled && mNextListener != null);
        }
        if (mPrevButton != null) {
            mPrevButton.setEnabled(enabled && mPrevListener != null);
        }
        if (mProgress != null) {
            mProgress.setEnabled(enabled);
        }
       super.setEnabled(enabled);
    }

    private void installPrevNextListeners() {
        if (mNextButton != null) {
            mNextButton.setOnClickListener(mNextListener);
            mNextButton.setEnabled(mNextListener != null);
        }

        if (mPrevButton != null) {
            mPrevButton.setOnClickListener(mPrevListener);
            mPrevButton.setEnabled(mPrevListener != null);
        }
    }

    public void setPrevNextListeners(OnClickListener next, OnClickListener prev) {
        mNextListener = next;
        mPrevListener = prev;
        mListenersSet = true;

        if (mRoot != null) {
            installPrevNextListeners();

            if (mNextButton != null && !mFromXml) {
                mNextButton.setVisibility(View.VISIBLE);
            }
            if (mPrevButton != null && !mFromXml) {
                mPrevButton.setVisibility(View.VISIBLE);
            }
        }
    }

    public interface AdapterViewFlipperControl {
        void    startFlipping();
        void    stopFlipping();
        void    showPrevious();
        void    showNext();
        boolean isFlipping();
    }
    private static class MessageHandler extends Handler {
        private final WeakReference<PhotoControllerView> mView;

        MessageHandler(PhotoControllerView view) {
            mView = new WeakReference<PhotoControllerView>(view);
        }
        @Override
        public void handleMessage(Message msg) {
            PhotoControllerView view = mView.get();
            if (view == null || view.mPlayer == null) {
                return;
            }

            int pos;
            switch (msg.what) {
                case FADE_OUT:
                    view.hide();
                    break;
                case SHOW_PROGRESS:
                    pos =0; // view.setProgress();
                    if (!view.mDragging && view.mShowing && view.mPlayer.isFlipping()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    }
}