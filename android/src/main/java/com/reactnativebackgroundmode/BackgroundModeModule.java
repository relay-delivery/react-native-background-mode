package com.reactnativebackgroundmode;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.AppTask;
import android.content.Intent;
import static android.content.Context.BIND_AUTO_CREATE;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.os.IBinder;
import com.reactnativebackgroundmode.ForegroundService.ForegroundBinder;
import android.util.Log;
import com.facebook.react.bridge.LifecycleEventListener;
import static android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
import org.json.JSONObject;

@ReactModule(name = BackgroundModeModule.NAME)
public class BackgroundModeModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    public static final String NAME = "BackgroundMode";
    private enum Event { ACTIVATE, DEACTIVATE, FAILURE }
    private ForegroundService service;
    // Flag indicates if the app is in background or foreground
    private boolean inBackground = false;

    // Flag indicates if the plugin is enabled or disabled
    private boolean isDisabled = true;

    // Flag indicates if the service is bind
    private boolean isBind = false;

    // Default settings for the notification
    private static JSONObject defaultSettings = new JSONObject();

    public BackgroundModeModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addLifecycleEventListener(this);
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }


    // Used to (un)bind the service to with the activity
    private final ServiceConnection connection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected (ComponentName name, IBinder service)
        {
            Log.d("riderapp", "connect");
            ForegroundBinder binder = (ForegroundBinder) service;
            BackgroundModeModule.this.service = binder.getService();
        }

        @Override
        public void onServiceDisconnected (ComponentName name)
        {
            Log.d("riderapp", "test");
        }
    };

    /**
     * Bind the activity to a background service and put them into foreground
     * state.
     */


     /**
     * Called when the system is about to start resuming a previous activity.
     *
     * @param multitasking Flag indicating if multitasking is turned on for app.
     */
    @Override
    public void onHostPause()
    {
        try {
            inBackground = true;
            Log.d("riderapp", "PAUSE PAUSE PAUSE");
            startService();
        } finally {
            clearKeyguardFlags(getCurrentActivity());
        }
    }

    /**
     * Called when the activity will be destroyed.
     */
    @Override
    public void onHostDestroy()
    {
        Log.d("riderapp", "destroy");
        stopService();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * Called when the activity will start interacting with the user.
     *
     * @param multitasking Flag indicating if multitasking is turned on for app.
     */
    @Override
    public void onHostResume ()
    {
        inBackground = false;
        Log.d("riderapp", "resume");
        stopService();
    }


    /**
     * Bind the activity to a background service and put them into foreground
     * state.
     */
    private void stopService()
    {
        Activity context = getCurrentActivity();
        Intent intent    = new Intent(context, ForegroundService.class);

        if (!isBind) return;

        //fireEvent(Event.DEACTIVATE, null);
        context.unbindService(connection);
        context.stopService(intent);

        isBind = false;
    }

    /**
     * Removes required flags to the window to unlock/wakeup the device.
     */
    private void clearKeyguardFlags (Activity app)
    {
        app.runOnUiThread(() -> app.getWindow().clearFlags(FLAG_DISMISS_KEYGUARD));
    }

    /**
     * Update the default settings for the notification.
     *
     * @param settings The new default settings
     */
    private void setDefaultSettings(JSONObject settings)
    {
        defaultSettings = settings;
    }

    /**
     * Returns the settings for the new/updated notification.
     */
    static JSONObject getSettings () {
        return defaultSettings;
    }

    /**
     * Enable the background mode.
     */
    private void enableMode()
    {
        isDisabled = false;

        if (inBackground) {
            startService();
        }
    }

    /**
     * Disable the background mode.
     */
    private void disableMode()
    {
        stopService();
        isDisabled = true;
    }

    private void startService()
    {
        Activity context =  getCurrentActivity();
        Log.d("riderapp", "start service");

        if (isDisabled || isBind)
            return;

        Log.d("riderapp", "context");
        Log.d("riderapp", String.valueOf(context));

        Intent intent = new Intent(context, ForegroundService.class);

        Log.d("riderapp", "intent");
        Log.d("riderapp", String.valueOf(intent));
        try {
            context.bindService(intent, connection, BIND_AUTO_CREATE);
            Log.d("riderapp", "connection");
            Log.d("riderapp", String.valueOf(connection));

            Log.d("riderapp", "context bind");
            Log.d("riderapp", String.valueOf(context));
            context.startService(intent);
            Log.d("riderapp", "context start");
            Log.d("riderapp", String.valueOf(context));
        } catch (Exception e) {
            Log.d("riderapp", String.valueOf(e));
        }

        isBind = true;

    }

    // Example method
    // See https://reactnative.dev/docs/native-modules-android
    @ReactMethod
    public void multiply(int a, int b, Promise promise) {
      Log.d("riderapp", "The log message");
      enableMode();

    }

    public static native void nativeMultiply(int a, int b);
}
