package ru.geekbrains.soltrix.weather;

import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private Handler handler;
    private TextView city;
    private TextView temp;
    private TextView sky;
    protected TextView gradus;
    private TextView detailsText;
    private TextView data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        temp = findViewById(R.id.temperature);
        temp.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueCyr-UltraLight.otf"));
        sky = findViewById(R.id.sky);
        sky.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/weather.ttf"));
        gradus = findViewById(R.id.gradus);
        gradus.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueCyr-UltraLight.otf"));
        gradus.setText("\u00b0C");
        detailsText = findViewById(R.id.details);
        city = findViewById(R.id.city);
        data = findViewById(R.id.data);

        updateWeatherData(new CityPreference(MainActivity.this).getCity());
    }

    //меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    //обработка нажатия пункта меню
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) showInputDialog();
        return true;
    }

    //показать диалог выбора города
    private void showInputDialog() {
        AlertDialog.Builder chooseCity = new AlertDialog.Builder(this);
        chooseCity.setIcon(R.mipmap.ic_launcher);
        chooseCity.setTitle(R.string.choose_city);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        chooseCity.setView(input);
        chooseCity.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String city = input.getText().toString();
                updateWeatherData(city);
                new CityPreference(MainActivity.this).setCity(city);
            }
        });
        chooseCity.show();
    }

    //Обновление/загрузка погодных данных
    private void updateWeatherData(final String city) {
        new Thread() {
            public void run() {
                final JSONObject json = WeatherData.getJSONData(MainActivity.this, city);
                if (json == null) {
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.place_not_found),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        public void run() {
                            renderWeather(json);
                        }
                    });
                }
            }
        }.start();
    }

    //Обработка загруженных данных
    private void renderWeather(JSONObject json) {
        try {
            city.setText(json.getString("name").toUpperCase(Locale.US) + ", "
                    + json.getJSONObject("sys").getString("country"));

            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");
            detailsText.setText(details.getString("description").toUpperCase(Locale.US) + "\n" + getResources().getString(R.string.humidity)
                    + ": " + main.getString("humidity") + "%" + "\n" + getResources().getString(R.string.pressure)
                    + ": " + main.getString("pressure") + " hPa");
            detailsText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            detailsText.setLineSpacing(0,1.4f);

            temp.setText(String.format("%.1f", main.getDouble("temp")));

            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = df.format(new Date(json.getLong("dt") * 1000));
            data.setText(getResources().getString(R.string.last_update) + " " + updatedOn);

            setWeatherIcon(details.getInt("id"), json.getJSONObject("sys").getLong("sunrise") * 1000,
                    json.getJSONObject("sys").getLong("sunset") * 1000);

        } catch (Exception e) {
            Log.e("Weather", "One or more fields not found in the JSON data");
        }
    }

    //Подстановка нужной иконки
    private void setWeatherIcon(int actualId, long sunrise, long sunset) {
        int id = actualId / 100;
        String icon = "";
        if (actualId == 800) {
            long currentTime = new Date().getTime();
            if (currentTime >= sunrise && currentTime < sunset) {
                icon = MainActivity.this.getString(R.string.weather_sunny);
            } else {
                icon = MainActivity.this.getString(R.string.weather_clear_night);
            }
        } else {
            Log.d("SimpleWeather", "id " + id);
            switch (id) {
                case 2:
                    icon = MainActivity.this.getString(R.string.weather_thunder);
                    break;
                case 3:
                    icon = MainActivity.this.getString(R.string.weather_drizzle);
                    break;
                case 5:
                    icon = MainActivity.this.getString(R.string.weather_rainy);
                    break;
                case 6:
                    icon = MainActivity.this.getString(R.string.weather_snowy);
                    break;
                case 7:
                    icon = MainActivity.this.getString(R.string.weather_foggy);
                    break;
                case 8:
                    icon = MainActivity.this.getString(R.string.weather_cloudy);
                    break;
            }
        }
        sky.setText(icon);
    }
}
