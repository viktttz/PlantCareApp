package com.example.plantcareapp.utils;

import java.util.HashMap;
import java.util.Map;

public class PlantCareInfo {
    private static final Map<String, String> plantCareMap = new HashMap<>();

    static {
        plantCareMap.put("Кактус", "Полив: раз в 2-3 недели. Освещение: яркое. Температура: комнатная.");
        plantCareMap.put("Фикус", "Полив: умеренный, когда верхний слой почвы подсохнет. Освещение: яркое, но не прямое солнце.");
        plantCareMap.put("Орхидея", "Полив: раз в неделю, методом погружения. Освещение: яркое, рассеянное. Влажность: высокая.");
        plantCareMap.put("Сансевиерия", "Полив: редкий, раз в 2-3 недели. Освещение: любое. Очень неприхотлива.");
        plantCareMap.put("Монстера", "Полив: регулярный, но без застоя воды. Освещение: яркое, рассеянное. Любит опрыскивания.");
    }

    public static String getCareInfo(String plantName) {
        return plantCareMap.getOrDefault(plantName, "Информация по уходу за этим растением пока недоступна.");
    }

    public static int getDefaultWateringInterval(String plantName) {
        switch (plantName) {
            case "Кактус": return 21;
            case "Фикус": return 7;
            case "Орхидея": return 7;
            case "Сансевиерия": return 14;
            case "Монстера": return 5;
            default: return 7;
        }
    }
}