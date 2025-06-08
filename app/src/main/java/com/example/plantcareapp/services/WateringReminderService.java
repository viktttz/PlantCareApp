package com.example.plantcareapp.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.plantcareapp.database.PlantDatabase;
import com.example.plantcareapp.database.PlantDao;
import com.example.plantcareapp.models.Plant;
import com.example.plantcareapp.receivers.WateringReminderReceiver;

import java.util.Calendar;
import java.util.List;

public class WateringReminderService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        scheduleWateringReminders();
        return START_STICKY;
    }

    public static void scheduleReminderForPlant(Context context, Plant plant) {
        if (plant.getNotificationTime() == null) return;

        // Проверка разрешений для Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                scheduleInexactAlarm(context, plant);
                return;
            }
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WateringReminderReceiver.class);
        intent.putExtra("plantId", plant.getId());
        intent.putExtra("plantName", plant.getName());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                plant.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            String[] timeParts = plant.getNotificationTime().split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(plant.getLastWateredDate());
            calendar.add(Calendar.DAY_OF_YEAR, plant.getWateringIntervalDays());
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, plant.getWateringIntervalDays());
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void scheduleInexactAlarm(Context context, Plant plant) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WateringReminderReceiver.class);
        intent.putExtra("plantId", plant.getId());
        intent.putExtra("plantName", plant.getName());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                plant.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            String[] timeParts = plant.getNotificationTime().split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(plant.getLastWateredDate());
            calendar.add(Calendar.DAY_OF_YEAR, plant.getWateringIntervalDays());
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, plant.getWateringIntervalDays());
            }

            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scheduleWateringReminders() {
        new Thread(() -> {
            PlantDao plantDao = PlantDatabase.getInstance(this).plantDao();
            List<Plant> plants = plantDao.getAllPlants();

            for (Plant plant : plants) {
                if (plant.getNotificationTime() != null) {
                    scheduleReminderForPlant(this, plant);
                }
            }
        }).start();
    }

    public static void cancelReminderForPlant(Context context, int plantId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WateringReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                plantId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }
}