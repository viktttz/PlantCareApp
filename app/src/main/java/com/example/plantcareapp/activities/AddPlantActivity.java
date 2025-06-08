package com.example.plantcareapp.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.plantcareapp.R;
import com.example.plantcareapp.database.PlantDatabase;
import com.example.plantcareapp.database.PlantDao;
import com.example.plantcareapp.models.Plant;
import com.example.plantcareapp.utils.PlantCareInfo;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddPlantActivity extends AppCompatActivity {
    private EditText nameEditText;
    private EditText descriptionEditText;
    private Button lastWateredButton;
    private Button notificationTimeButton;
    private Calendar lastWateredDate = Calendar.getInstance();
    private String notificationTime = "12:00"; // Время уведомления по умолчанию
    private PlantDao plantDao;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plant);

        plantDao = PlantDatabase.getInstance(this).plantDao();

        nameEditText = findViewById(R.id.plant_name);
        descriptionEditText = findViewById(R.id.plant_description);
        lastWateredButton = findViewById(R.id.last_watered_button);
        notificationTimeButton = findViewById(R.id.notification_time_button);


        updateLastWateredButtonText();

        lastWateredButton.setOnClickListener(v -> showDatePickerDialog());
        notificationTimeButton.setOnClickListener(v -> showTimePickerDialog());

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> savePlant());
    }

    private void updateLastWateredButtonText() {
        lastWateredButton.setText(String.format(Locale.getDefault(),
                "%02d.%02d.%d",
                lastWateredDate.get(Calendar.DAY_OF_MONTH),
                lastWateredDate.get(Calendar.MONTH) + 1,
                lastWateredDate.get(Calendar.YEAR)));
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    lastWateredDate.set(year, month, dayOfMonth);
                    updateLastWateredButtonText();
                },
                lastWateredDate.get(Calendar.YEAR),
                lastWateredDate.get(Calendar.MONTH),
                lastWateredDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        try {
            String[] timeParts = notificationTime.split(":");
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
            calendar.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
        } catch (Exception e) {
            calendar.set(Calendar.HOUR_OF_DAY, 12);
            calendar.set(Calendar.MINUTE, 0);
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    notificationTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    notificationTimeButton.setText(getString(R.string.notification_time_label, notificationTime));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.setTitle("Выберите время напоминания");
        timePickerDialog.show();
    }

    private void savePlant() {
        String name = nameEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Введите название растения", Toast.LENGTH_SHORT).show();
            return;
        }

        int wateringInterval = PlantCareInfo.getDefaultWateringInterval(name);
        Plant plant = new Plant(
                name,
                description,
                "", // Путь к изображению (можно добавить позже)
                lastWateredDate.getTime(),
                wateringInterval,
                notificationTime // Добавляем время уведомления
        );

        new Thread(() -> {
            long plantId = plantDao.insertPlant(plant);
            plant.setId((int) plantId);
            runOnUiThread(() -> {
                Toast.makeText(AddPlantActivity.this, "Растение добавлено", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}