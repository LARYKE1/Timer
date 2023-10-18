package com.example.timer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class TimerWorker extends Worker {

    private final static int NOTIFICATION_ID = 0;
    private final static String CHANNEL_ID_TIMER = "channel_timer";
    private final NotificationManager mNotificationManager;
    public final static String KEY_MILLISECONDS_REMAINING =
            "com.zybooks.timer.MILLIS_LEFT";

    public TimerWorker(@NonNull Context context, @NonNull WorkerParameters
            params) {
        super(context, params);

        mNotificationManager=(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data inputData = getInputData();
        long remainingMillis = inputData.getLong(KEY_MILLISECONDS_REMAINING,
                0);

        if (remainingMillis == 0) {
            return Result.failure();
        }

        TimerModel timerModel = new TimerModel();
        timerModel.start(remainingMillis);

        createTimerNotificationChannel();
        while (timerModel.isRunning()) {
            try {

                createTimerNotification(timerModel.toString());

                Thread.sleep(1000);
                if (timerModel.getRemainingMilliseconds() == 0) {
                    timerModel.stop();

                    createTimerNotification("Timer is finished!");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return Result.success();
    }

    private void createTimerNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name =
                    getApplicationContext().getString(R.string.channel_name);
            String description =
                    getApplicationContext().getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_TIMER, name, importance);
            channel.setDescription(description);

            mNotificationManager.createNotificationChannel(channel);
        }
    }

    private void createTimerNotification(String text) {

        Notification notification = new NotificationCompat.Builder(
                getApplicationContext(), CHANNEL_ID_TIMER)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(getApplicationContext().getString(R.string.app_name))
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }
}