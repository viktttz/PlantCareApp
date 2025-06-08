package com.example.plantcareapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.plantcareapp.models.Plant;

import java.util.List;

@Dao
public interface PlantDao {
    @Query("SELECT * FROM plants")
    List<Plant> getAllPlants();

    @Query("SELECT * FROM plants WHERE id = :id")
    Plant getPlantById(int id);

    @Insert
    long insertPlant(Plant plant);

    @Update
    void updatePlant(Plant plant);

    @Delete
    void deletePlant(Plant plant);
}