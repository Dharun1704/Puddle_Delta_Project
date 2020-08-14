package com.example.deltaproject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.deltaproject.WeatherApi.ApiClient;
import com.example.deltaproject.WeatherApi.ApiInterface;
import com.example.deltaproject.WeatherModel.WeatherResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.deltaproject.WeatherActivity.APP_ID;

public class WeatherCitySearchActivity extends AppCompatActivity {

    private MaterialSearchBar searchBar;

    private List<String> lstCities;
    private double lat, lon;
    private boolean searchMode = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.city_search_layout);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#001020"));

        searchBar = findViewById(R.id.searchBar);

        lstCities = new ArrayList<>();
        try {
            StringBuilder builder = new StringBuilder();
            InputStream is = getResources().openRawResource(R.raw.city_list);
            GZIPInputStream gzipInputStream = new GZIPInputStream(is);

            InputStreamReader reader = new InputStreamReader(gzipInputStream);
            BufferedReader in = new BufferedReader(reader);

            String read;
            while ((read = in.readLine()) != null) {
                builder.append(read);
                lstCities = new Gson().fromJson(builder.toString(), new TypeToken<List<String>>(){}.getType());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        searchBar.setEnabled(true);
        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<String> suggest = new ArrayList<>();
                for (String search : lstCities) {
                    if (search.toLowerCase().contains(searchBar.getText().toLowerCase())) {
                        suggest.add(search);
                    }
                }
                searchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                searchBar.setLastSuggestions(lstCities);
                getCityCoord(text.toString());
                searchMode = true;
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

        searchBar.setLastSuggestions(lstCities);
    }

    private void getCityCoord(String cityName) {
        ApiInterface mService = ApiClient.getWeatherRetrofit().create(ApiInterface.class);

        Call<WeatherResult> call = mService.getCityCoord(cityName, APP_ID);

        call.enqueue(new Callback<WeatherResult>() {
            @Override
            public void onResponse(Call<WeatherResult> call, Response<WeatherResult> response) {
                assert response.body() != null;
                lat = response.body().getCoord().getLat();
                lon = response.body().getCoord().getLon();
                Intent intent = new Intent(WeatherCitySearchActivity.this, WeatherActivity.class);
                intent.putExtra("searchMode", searchMode);
                intent.putExtra("searchLat", Double.toString(lat));
                intent.putExtra("searchLon", Double.toString(lon));
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Call<WeatherResult> call, Throwable t) {
                Toast.makeText(WeatherCitySearchActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_up);
    }
}
