package com.example.plantcareapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.plantcareapp.utils.NotificationHelper;

public class WateringReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String plantName = intent.getStringExtra("plantName");
        NotificationHelper.showWateringNotification(context, plantName);
    }
}