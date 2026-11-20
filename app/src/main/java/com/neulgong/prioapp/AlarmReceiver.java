package com.neulgong.prioapp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

/** 예약 시각이 되면 알림을 띄움. 매일 반복 알림이면 다음 날로 다시 예약. */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        String id = intent.getStringExtra(Alarms.EXTRA_ID);
        if (id == null) return;
        String title = intent.getStringExtra(Alarms.EXTRA_TITLE);
        String body = intent.getStringExtra(Alarms.EXTRA_BODY);
        String hash = intent.getStringExtra(Alarms.EXTRA_HASH);
        boolean daily = intent.getBooleanExtra(Alarms.EXTRA_DAILY, false);
        long at = intent.getLongExtra(Alarms.EXTRA_AT, System.currentTimeMillis());

        Alarms.ensureChannel(ctx);
        Intent open = new Intent(ctx, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        open.putExtra(Alarms.EXTRA_HASH, hash);
        PendingIntent contentPi = PendingIntent.getActivity(ctx, id.hashCode(), open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification n = new Notification.Builder(ctx, Alarms.CHANNEL)
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new Notification.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentIntent(contentPi)
                .build();

        boolean allowed = Build.VERSION.SDK_INT < 33
                || ctx.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        if (allowed) {
            ctx.getSystemService(NotificationManager.class).notify(id.hashCode(), n);
        }

        if (daily) {
            long next = at + 86_400_000L;
            long now = System.currentTimeMillis();
            while (next <= now) next += 86_400_000L;
            Alarms.schedule(ctx, id, next, title, body, true, hash);
        }
    }
}
