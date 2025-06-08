package com.example.plantcareapp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.plantcareapp.models.Plant;

@Database(entities = {Plant.class}, version = 2)
@TypeConverters({Converters.class})
public abstract class PlantDatabase extends RoomDatabase {
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Добавляем новую колонку для времени уведомления
            database.execSQL("ALTER TABLE plants ADD COLUMN notificationTime TEXT DEFAULT '12:00'");
        }
    };
    private static PlantDatabase instance;


    public static synchronized PlantDatabase getInstance(Context context) {
        if (PlantDatabase.instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            PlantDatabase.class, "plant_database")
                    .addMigrations(MIGRATION_1_2) // Добавляем миграцию
                    .fallbackToDestructiveMigration() // Только для разработки!
                    .build();
        }
        return PlantDatabase.instance;
    }

    public abstract PlantDao plantDao();


}

