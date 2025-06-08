package com.example.plantcareapp.activities;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.plantcareapp.R;
import com.example.plantcareapp.database.PlantDatabase;
import com.example.plantcareapp.database.PlantDao;
import com.example.plantcareapp.models.Plant;
import com.example.plantcareapp.receivers.WateringReminderReceiver;
import com.example.plantcareapp.utils.PlantCareInfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PlantDetailsActivity extends AppCompatActivity {
    private Plant plant;
    private PlantDao plantDao;
    private TextView notificationTimeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_details);

        int plantId = getIntent().getIntExtra("plantId", -1);
        if (plantId == -1) {
            finish();
            return;
        }

        plantDao = PlantDatabase.getInstance(this).plantDao();
        notificationTimeTextView = findViewById(R.id.notification_time);
        Button changeTimeButton = findViewById(R.id.change_time_button);
        Button changeIntervalButton = findViewById(R.id.change_interval_button);

        changeTimeButton.setOnClickListener(v -> showTimePicker());
        changeIntervalButton.setOnClickListener(v -> showIntervalDialog());

        loadPlant(plantId);
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        try {
            String[] timeParts = plant.getNotificationTime().split(":");
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
            calendar.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
        } catch (Exception e) {
            calendar.set(Calendar.HOUR_OF_DAY, 12);
            calendar.set(Calendar.MINUTE, 0);
        }

        TimePickerDialog timePicker = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    String newTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    plant.setNotificationTime(newTime);
                    notificationTimeTextView.setText("Время уведомления: " + newTime);
                    savePlantChanges();
                    setupWateringReminder();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);

        timePicker.setTitle("Выберите время уведомления");
        timePicker.show();
    }

    private void showIntervalDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Изменить интервал полива")
                .setItems(new CharSequence[]{"Каждый день", "Каждые 2 дня", "Раз в неделю", "Другой интервал"},
                        (dialog, which) -> {
                            switch (which) {
                                case 0: plant.setWateringIntervalDays(1); break;
                                case 1: plant.setWateringIntervalDays(2); break;
                                case 2: plant.setWateringIntervalDays(7); break;
                                case 3: showCustomIntervalDialog(); break;
                            }
                            savePlantChanges();
                            setupWateringReminder();
                        })
                .show();
    }

    private void showCustomIntervalDialog() {
        // Реализация диалога для ввода произвольного интервала
    }

    private void setupWateringReminder() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, WateringReminderReceiver.class);
        intent.putExtra("plantId", plant.getId());
        intent.putExtra("plantName", plant.getName());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                plant.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );


        try {
            String[] timeParts = plant.getNotificationTime().split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);


            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY * plant.getWateringIntervalDays(),
                    pendingIntent
            );
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка установки напоминания", Toast.LENGTH_SHORT).show();
        }
    }

    private void deletePlant() {
        new AlertDialog.Builder(this)
                .setTitle("Удаление растения")
                .setMessage("Вы уверены, что хотите удалить это растение?")
                .setPositiveButton("Удалить", (dialog, which) -> deletePlant(plant))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deletePlant(Plant plant) {
        // Отменяем уведомление для этого растения
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, WateringReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                plant.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);

        // Удаляем из базы данных
        new Thread(() -> {
            plantDao.deletePlant(plant);
            runOnUiThread(() -> {
                Toast.makeText(this, "Растение удалено", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    private void loadPlant(int plantId) {
        new Thread(() -> {
            plant = plantDao.getPlantById(plantId);
            runOnUiThread(this::setupViews);
        }).start();
    }

    private void setupViews() {
        if (plant == null) {
            finish();
            return;
        }

        TextView nameTextView = findViewById(R.id.plant_name);
        TextView descriptionTextView = findViewById(R.id.plant_description);
        TextView lastWateredTextView = findViewById(R.id.last_watered_date);
        TextView careInfoTextView = findViewById(R.id.care_info);
        Button waterButton = findViewById(R.id.water_button);
        Button deleteButton = findViewById(R.id.delete_button);

        nameTextView.setText(plant.getName());
        descriptionTextView.setText(plant.getDescription());

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        lastWateredTextView.setText(sdf.format(plant.getLastWateredDate()));

        String careInfo = PlantCareInfo.getCareInfo(plant.getName());
        careInfoTextView.setText(careInfo);

        notificationTimeTextView.setText("Время уведомления: " +
                (plant.getNotificationTime() != null ? plant.getNotificationTime() : "не установлено"));

        waterButton.setOnClickListener(v -> waterPlant());
        deleteButton.setOnClickListener(v -> deletePlant());
    }

    private void waterPlant() {
        plant.setLastWateredDate(new Date());
        new Thread(() -> {
            plantDao.updatePlant(plant);
            runOnUiThread(() -> {
                Toast.makeText(this, "Растение полито!", Toast.LENGTH_SHORT).show();
                setupWateringReminder();
                finish();
            });
        }).start();
    }

    private void savePlantChanges() {
        new Thread(() -> {
            plantDao.updatePlant(plant);
            runOnUiThread(() ->
                    Toast.makeText(this, "Изменения сохранены", Toast.LENGTH_SHORT).show());
        }).start();
    }
}