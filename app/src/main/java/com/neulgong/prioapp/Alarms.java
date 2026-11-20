package com.neulgong.prioapp;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

/** 알림 예약과 취소. id 하나당 예약 하나. 같은 id로 다시 예약하면 덮어씀. */
public final class Alarms {
    public static final String CHANNEL = "prio_reminders";
    public static final String EXTRA_ID = "id";
    public static final String EXTRA_AT = "at";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_BODY = "body";
    public static final String EXTRA_DAILY = "daily";
    public static final String EXTRA_HASH = "hash";

    private Alarms() {}

    static void ensureChannel(Context ctx) {
        NotificationManager nm = ctx.getSystemService(NotificationManager.class);
        if (nm.getNotificationChannel(CHANNEL) == null) {
            NotificationChannel ch = new NotificationChannel(CHANNEL, "할 일 알림", NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("타이머 종료, 미룬 일 다시 알림, 취침 전 알림");
            ch.enableVibration(true);
            nm.createNotificationChannel(ch);
        }
    }

    private static Intent baseIntent(Context ctx, String id) {
        Intent i = new Intent(ctx, AlarmReceiver.class);
        // data를 id별로 다르게 해야 PendingIntent가 서로 구분됨
        i.setData(Uri.parse("prio://alarm/" + Uri.encode(id)));
        return i;
    }

    static void schedule(Context ctx, String id, long at, String title, String body, boolean daily, String hash) {
        Intent i = baseIntent(ctx, id);
        i.putExtra(EXTRA_ID, id);
        i.putExtra(EXTRA_AT, at);
        i.putExtra(EXTRA_TITLE, title);
        i.putExtra(EXTRA_BODY, body);
        i.putExtra(EXTRA_DAILY, daily);
        i.putExtra(EXTRA_HASH, hash);
        PendingIntent pi = PendingIntent.getBroadcast(ctx, 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am = ctx.getSystemService(AlarmManager.class);
        boolean exactOk = Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms();
        if (exactOk) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
        } else {
            // 정확한 알람 권한이 없으면 몇 분 늦을 수 있는 방식으로 예약
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
        }
    }

    static void cancel(Context ctx, String id) {
        PendingIntent pi = PendingIntent.getBroadcast(ctx, 0, baseIntent(ctx, id),
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (pi != null) {
            ctx.getSystemService(AlarmManager.class).cancel(pi);
            pi.cancel();
        }
    }
}
