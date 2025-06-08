package com.example.plantcareapp.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "plants")
public class Plant {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String description;
    private String imagePath;
    private Date lastWateredDate;
    private int wateringIntervalDays;
    private String notificationTime;

    public Plant(String name, String description, String imagePath, Date lastWateredDate, int wateringIntervalDays, String notificationTime) {
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
        this.lastWateredDate = lastWateredDate;
        this.wateringIntervalDays = wateringIntervalDays;
        this.notificationTime = notificationTime;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Date getLastWateredDate() {
        return lastWateredDate;
    }

    public void setLastWateredDate(Date lastWateredDate) {
        this.lastWateredDate = lastWateredDate;
    }

    public int getWateringIntervalDays() {
        return wateringIntervalDays;
    }

    public void setWateringIntervalDays(int wateringIntervalDays) {
        this.wateringIntervalDays = wateringIntervalDays;
    }

    public String getNotificationTime() {
        return notificationTime;
    }

    public void setNotificationTime(String notificationTime) {
        this.notificationTime = notificationTime;
    }
}