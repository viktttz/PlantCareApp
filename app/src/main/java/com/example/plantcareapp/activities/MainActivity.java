package com.example.plantcareapp.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.plantcareapp.R;
import com.example.plantcareapp.adapters.PlantsAdapter;
import com.example.plantcareapp.database.PlantDatabase;
import com.example.plantcareapp.database.PlantDao;
import com.example.plantcareapp.models.Plant;
import com.example.plantcareapp.services.WateringReminderService;
import com.example.plantcareapp.utils.NotificationHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;
import android.app.AlarmManager;

public class MainActivity extends AppCompatActivity implements PlantsAdapter.OnPlantActionListener {
    private static final int REQUEST_CODE_EXACT_ALARM = 123;
    private RecyclerView plantsRecyclerView;
    private PlantsAdapter plantsAdapter;
    private PlantDao plantDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NotificationHelper.createNotificationChannel(this);
        startService(new Intent(this, WateringReminderService.class));

        plantsRecyclerView = findViewById(R.id.plants_recycler_view);
        plantsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        plantDao = PlantDatabase.getInstance(this).plantDao();

        FloatingActionButton addPlantButton = findViewById(R.id.add_plant_button);
        addPlantButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddPlantActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkExactAlarmPermission();
    }

    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                try {
                    startActivityForResult(intent, REQUEST_CODE_EXACT_ALARM);
                } catch (ActivityNotFoundException e) {
                    // Обработка случая, когда действие недоступно
                    Toast.makeText(this, "Функция точных будильников недоступна", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EXACT_ALARM) {
            checkExactAlarmPermission();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlants();
    }

    @Override
    public void onPlantClick(Plant plant) {
        Intent intent = new Intent(this, PlantDetailsActivity.class);
        intent.putExtra("plantId", plant.getId());
        startActivity(intent);
    }

    @Override
    public void onPlantDelete(Plant plant) {
        new AlertDialog.Builder(this)
                .setTitle("Удаление растения")
                .setMessage("Вы уверены, что хотите удалить " + plant.getName() + "?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    new Thread(() -> {
                        plantDao.deletePlant(plant);
                        runOnUiThread(() -> {
                            loadPlants();
                            Toast.makeText(this, "Растение удалено", Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void loadPlants() {
        new Thread(() -> {
            List<Plant> plants = plantDao.getAllPlants();
            runOnUiThread(() -> {
                if (plantsAdapter == null) {
                    plantsAdapter = new PlantsAdapter(plants, this);
                    plantsRecyclerView.setAdapter(plantsAdapter);
                } else {
                    plantsAdapter.setPlants(plants);
                }
            });
        }).start();
    }
}