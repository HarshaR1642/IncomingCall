package com.incoming.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.incoming.utility.Constants;

public class HungUpBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (IncomingCall.active) {
            context.sendBroadcast(new Intent(Constants.ACTION_END_INCOMING_CALL));
        }

        Intent stopIntent = new Intent(context, CallService.class);
        context.stopService(stopIntent);
    }
}
