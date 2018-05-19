package com.hd.screencapture;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.hd.screencapture.config.ScreenCaptureConfig;
import com.hd.screencapture.help.ScreenCaptureFragment;
import com.hd.screencapture.help.Utils;
import com.hd.screencapture.observer.CaptureObserver;
import com.hd.screencapture.observer.ScreenCaptureObserver;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by hd on 2018/5/14 .
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ScreenCapture {

    private static final String TAG = "Screen-Capture";

    public static ScreenCapture with(@NonNull AppCompatActivity activity) {
        if (activity.isFinishing() || activity.isDestroyed()) {
            throw new RuntimeException("current activity is not running state !");
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            throw new RuntimeException("the sdk version less than 21 equipment does not provide this function !");
        }
        if(!Utils.isExternalStorageReady()){
            Log.e(TAG,"current no storage space");
        }
        return new ScreenCapture(activity);
    }

    private ScreenCaptureFragment screenCaptureFragment;

    private CaptureObserver observer;

    private ScreenCapture(@NonNull AppCompatActivity activity) {
        //add lifecycle observer
        observer = new ScreenCaptureObserver(this);
        activity.getLifecycle().addObserver((ScreenCaptureObserver) observer);
        //init the main capture work fragment
        screenCaptureFragment = getScreenCaptureFragment(activity);
        screenCaptureFragment.addObserver(observer);
        //init default config
        setConfig(ScreenCaptureConfig.initDefaultConfig(activity));
    }

    private ScreenCaptureFragment getScreenCaptureFragment(@NonNull Activity activity) {
        ScreenCaptureFragment screenCaptureFragment = findScreenCaptureFragment(activity);
        boolean isNewInstance = screenCaptureFragment == null;
        if (isNewInstance) {
            screenCaptureFragment = new ScreenCaptureFragment();
            FragmentManager fragmentManager = activity.getFragmentManager();
            fragmentManager.beginTransaction().add(screenCaptureFragment, TAG).commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
        return screenCaptureFragment;
    }

    private ScreenCaptureFragment findScreenCaptureFragment(@NonNull Activity activity) {
        return (ScreenCaptureFragment) activity.getFragmentManager().findFragmentByTag(TAG);
    }

    public ScreenCapture setConfig(@NonNull ScreenCaptureConfig config) {
        if (config.getVideoConfig() == null)
            throw new RuntimeException("you must set the capture video config before start capture ");
        screenCaptureFragment.setConfig(config);
        observer.initConfig(config);
        return this;
    }

    public void startCapture() {
        startCapture(-1);
    }

    public void startCapture(long duration) {
        screenCaptureFragment.startCapture();
        if (duration > 0) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    stopCapture();
                }
            }, duration);
        }
    }

    public void stopCapture() {
        screenCaptureFragment.stopCapture();
    }

}