package com.incoming.call;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.incoming.R;
import com.incoming.utility.Constants;

public class IncomingCall extends AppCompatActivity {
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
        setContentView(R.layout.call_fullscreen);

        Bundle bundle = getIntent().getExtras();
        TextView name = findViewById(R.id.name);
        LinearLayout acceptButton = findViewById(R.id.acceptButton);
        LinearLayout declineButton = findViewById(R.id.declineButton);

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

        String callerName = bundle.getString("callerName");
        if (callerName != null) {
            name.setText(callerName);
        }

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Constants.ACTION_END_INCOMING_CALL);
        MBroadcastReceiver mBroadcastReceiver = new MBroadcastReceiver();
        registerReceiver(mBroadcastReceiver, mIntentFilter);

        acceptButton.setOnClickListener(view -> {
            stopService(new Intent(getApplicationContext(), CallService.class));
            Intent answerIntent = new Intent(getApplicationContext(), AnswerCall.class);
            String component = bundle.getString("component");
            String accessToken = bundle.getString("accessToken");
            answerIntent.putExtra("component", component);
            answerIntent.putExtra("accessToken", accessToken);
            startActivity(answerIntent);
            finishAndRemoveTask();
        });

        declineButton.setOnClickListener(view -> {
            stopService(new Intent(getApplicationContext(), CallService.class));
            finishAndRemoveTask();
        });
    }

    public class MBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.ACTION_END_INCOMING_CALL)) {
                finishAndRemoveTask();
            }
        }
    }
}
