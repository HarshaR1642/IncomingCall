package com.incoming.call;

import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.react.ReactFragment;
import com.incoming.R;
import com.incoming.utility.Constants;

public class AnswerCall extends AppCompatActivity {

    public static boolean active = false;

    @Override
    protected void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        active = false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            //Some devices need the code below to work when the device is locked
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(AppCompatActivity.KEYGUARD_SERVICE);
            if (keyguardManager.isDeviceLocked()) {
                KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("Incoming:unLock");
                keyguardLock.disableKeyguard();
            }
        }
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        );

        if (IncomingCall.active) {
            sendBroadcast(new Intent(Constants.ACTION_END_INCOMING_CALL));
        }

        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Constants.ACTION_END_ACTIVE_CALL);
        MBroadcastReceiver mBroadcastReceiver = new MBroadcastReceiver();
        registerReceiver(mBroadcastReceiver, mIntentFilter);

        stopService(new Intent(this, CallService.class));
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1000);
        setContentView(R.layout.call_accept);
        Bundle bundle = getIntent().getExtras();
        String component = bundle.getString("component");
        ReactFragment reactNativeFragment = new ReactFragment.Builder()
                .setComponentName(component)
                .setLaunchOptions(bundle)
                .build();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.reactNativeFragment, reactNativeFragment)
                .commit();
    }

    public class MBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.ACTION_END_ACTIVE_CALL)) {
                finishAndRemoveTask();
            }
        }
    }
}
