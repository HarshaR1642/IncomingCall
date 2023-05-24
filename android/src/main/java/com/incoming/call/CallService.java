package com.incoming.call;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.*;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.incoming.R;
import com.incoming.utility.Constants;

public class CallService extends Service {

    Vibrator vibrator;
    Runnable runnable;
    Handler handler;
    Ringtone ringtone;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        Long timeout = bundle.getLong("timeout", Constants.TIME_OUT);

        Notification notification = buildNotification(intent);
        startForeground(Constants.FOREGROUND_SERVICE_ID, notification);
        playRingtone();
        startVibration();
        startTimer(timeout);

        return START_NOT_STICKY;
    }

    private Notification buildNotification(Intent intent) {

        Bundle bundle = intent.getExtras();
        String channelName = bundle.getString("channelName", Constants.NAME);
        String channelId = bundle.getString("channelId", Constants.NAME);
        String component = bundle.getString("component");
        String callerName = bundle.getString("callerName");
        String accessToken = bundle.getString("accessToken");

        RemoteViews customView = new RemoteViews(getPackageName(), R.layout.call_notification);

        if (callerName != null) {
            customView.setTextViewText(R.id.name, callerName);
        }

        Intent notificationIntent = new Intent(this, IncomingCall.class);
        Intent hungupIntent = new Intent(this, HungUpBroadcast.class);
        Intent answerIntent = new Intent(this, AnswerCall.class);

        notificationIntent.putExtra("component", component);
        notificationIntent.putExtra("callerName", callerName);
        notificationIntent.putExtra("accessToken", accessToken);

        answerIntent.putExtra("component", component);
        answerIntent.putExtra("accessToken", accessToken);

        int flag = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, flag);
        PendingIntent hungupPendingIntent = PendingIntent.getBroadcast(this, 0, hungupIntent, flag);
        PendingIntent answerPendingIntent = PendingIntent.getActivity(this, 0, answerIntent, flag);

        customView.setOnClickPendingIntent(R.id.btnAnswer, answerPendingIntent);
        customView.setOnClickPendingIntent(R.id.btnDecline, hungupPendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel(
                    channelId,
                    channelName, NotificationManager.IMPORTANCE_HIGH
            );
            notificationChannel.setSound(null, null);
            notificationChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, channelId);
        notification.setContentTitle(Constants.NAME);
        notification.setTicker(Constants.NAME);
        notification.setContentText(Constants.NAME);
        notification.setSmallIcon(R.drawable.incoming_video_call);
        notification.setCategory(NotificationCompat.CATEGORY_CALL);
        notification.setOngoing(true);
        notification.setFullScreenIntent(pendingIntent, true);
        notification.setStyle(new NotificationCompat.DecoratedCustomViewStyle());
        notification.setCustomContentView(customView);
        notification.setCustomBigContentView(customView);

        return notification.build();
    }

    private void playRingtone() {
        ringtone = RingtoneManager.getRingtone(
                this,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        );
        ringtone.play();
    }

    private void startVibration() {
        long[] vibratePattern = {0, 1000, 1000};

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        stopVibration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(vibratePattern, 0));
        }
    }

    private void stopVibration() {
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    private void startTimer(Long timeout) {
        runnable = new Runnable() {
            @Override
            public void run() {
                stopSelf();
                if (IncomingCall.active) {
                    sendBroadcast(new Intent(Constants.ACTION_END_INCOMING_CALL));
                }
            }
        };
        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(runnable, timeout);
    }

    private void cancelTimer() {
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }

    private void stopRingtone() {
        if (ringtone != null) {
            ringtone.stop();
        }
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Constants.FOREGROUND_SERVICE_ID);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeNotification();
        stopRingtone();
        stopVibration();
        cancelTimer();
    }
}