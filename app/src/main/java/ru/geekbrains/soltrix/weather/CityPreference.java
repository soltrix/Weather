package ru.geekbrains.soltrix.weather;

import android.app.Activity;
import android.content.SharedPreferences;

public class CityPreference {

    //Вспомогательный класс для хранения выбранного города
    private SharedPreferences prefs;

    CityPreference(Activity activity) {
        prefs = activity.getPreferences(Activity.MODE_PRIVATE);
    }

    // Возвращаем город по умолчанию, если SharedPreferences пустые
    protected String getCity() {
        return prefs.getString("city", "Yaroslavl");
    }

    protected void setCity(String city) {
        prefs.edit().putString("city", city).apply();
    }
}
