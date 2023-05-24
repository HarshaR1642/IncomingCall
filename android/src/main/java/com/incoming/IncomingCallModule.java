package com.incoming;

import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.incoming.call.AnswerCall;
import com.incoming.call.CallService;
import com.incoming.call.IncomingCall;
import com.incoming.utility.Constants;

import javax.annotation.Nonnull;

public class IncomingCallModule extends ReactContextBaseJavaModule {

    ReactContext reactContext;

    public IncomingCallModule(@Nonnull ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @NonNull
    @Override
    public String getName() {
        return Constants.NAME;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @ReactMethod
    public void showIncomingCall(ReadableMap options) {
        this.reactContext.stopService(
                new Intent(
                        this.reactContext,
                        CallService.class
                )
        );
        Long timeout = Math.round(options.getDouble("timeout"));
        Intent intent = new Intent(this.reactContext, CallService.class);
        intent.putExtra("channelName", options.getString("channelName"));
        intent.putExtra("channelId", options.getString("channelId"));
        intent.putExtra("timeout", timeout);
        intent.putExtra("component", options.getString("component"));
        intent.putExtra("callerName", options.getString("callerName"));
        intent.putExtra("accessToken", options.getString("accessToken"));
        this.reactContext.startForegroundService(intent);
    }

    @ReactMethod
    public void endCall() {
        this.reactContext.stopService(
                new Intent(
                        this.reactContext,
                        CallService.class
                )
        );

        if (IncomingCall.active) {
            this.reactContext.sendBroadcast(new Intent(Constants.ACTION_END_INCOMING_CALL));
        }
        if (AnswerCall.active) {
            this.reactContext.sendBroadcast(new Intent(Constants.ACTION_END_ACTIVE_CALL));
        }
    }
}
