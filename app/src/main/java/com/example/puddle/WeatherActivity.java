package com.example.puddle;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.puddle.WeatherApi.ApiClient;
import com.example.puddle.WeatherApi.ApiInterface;
import com.example.puddle.WeatherModel.CityName;
import com.example.puddle.WeatherModel.Hourly;
import com.example.puddle.WeatherModel.WeatherResult;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class WeatherActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        SettingsBottomSheet.BottomSheetListener {

    public static final String APP_ID = "c61a7dd65dcd5fcd58fc2b9a77febbbc";
    private long backPressedTime;
    private Toast backToast;
    private int gps = 0;
    private int network = 0;
    private int datePattern = 0;
    private int hourPattern = 0;
    private static final String TAG = "WeatherActivity";
    private int tempMode = 1;
    private int visMode = 1;
    private int windMode = 1;
    private int pressMode = 1;
    private boolean searchMode = false, permission;
    private String searchLat, searchLon;

    private DrawerLayout weatherLayout;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager hLayoutManager;
    private HourForecastAdapter adapter;
    private SwipeRefreshLayout refreshLayout;
    private NestedScrollView nsv;
    private RelativeLayout errorLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    ArrayList<Hourly> hourData;
    Toolbar toolbar;

    ImageView mMainImage, errorImage;
    TextView mTemp, mLocation, mWind, mPressure, mHumidity, mDewPoint, mClouds, mSunRise, mSunSet, mVisibility,
            mAppName, mMain, mDescription, mRain, userDisplay;
    TextView[] forecastDate, forecastDay, forecastDesc, forecastMaxTemp, forecastMinTemp;
    ImageView[] forecastImage;
    Button getCurrLocWtr;
    View[] btnView;

    String exclude = "minutely";
    private Context mContext;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        weatherLayout = findViewById(R.id.weatherLayout);
        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setRefreshing(true);
        refreshLayout.setColorSchemeColors(Color.BLACK);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        toolbar.setBackgroundColor(Color.parseColor("#001020"));
        toolbar.setForegroundGravity(Gravity.START | Gravity.CENTER);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#001020"));
        window.setNavigationBarColor(Color.parseColor("#001020"));

        mAppName = findViewById(R.id.appName);
        mAppName.setText("Puddle");
        mAppName.setTextColor(Color.WHITE);
        mAppName.setForegroundGravity(Gravity.START | Gravity.CENTER);

        recyclerView = findViewById(R.id.hourForecastLayout);
        nsv = findViewById(R.id.weatherDetailLayout);
        errorLayout = findViewById(R.id.errorLayout);
        errorImage = findViewById(R.id.errorImage);
        errorImage.setImageResource(R.drawable.oops);
        hLayoutManager = new LinearLayoutManager(WeatherActivity.this, LinearLayoutManager.HORIZONTAL, false);
        mMain = findViewById(R.id.weatherMain);
        mDescription = findViewById(R.id.weatherDescription);
        mMainImage = findViewById(R.id.weatherImage);
        mTemp = findViewById(R.id.weatherTemp);
        mLocation = findViewById(R.id.weatherLocation);
        mWind = findViewById(R.id.weatherWind);
        mPressure = findViewById(R.id.weatherPress);
        mHumidity = findViewById(R.id.weatherHumidity);
        mDewPoint = findViewById(R.id.WeatherDewPoint);
        mClouds = findViewById(R.id.weatherClouds);
        mVisibility = findViewById(R.id.weatherVisibility);
        mSunRise = findViewById(R.id.weatherSunRise);
        mSunSet = findViewById(R.id.weatherSunSet);
        mRain = findViewById(R.id.weatherRain);
        getCurrLocWtr = findViewById(R.id.getCurrLocBtn);
        getCurrLocWtr.setVisibility(View.GONE);
        btnView = new View[2];
        btnView[0] = findViewById(R.id.btnTopView);
        btnView[0].setVisibility(View.GONE);
        btnView[1] = findViewById(R.id.btnBtnView);
        btnView[1].setVisibility(View.GONE);

        //assign and initialize forecast items
        assignForecastItems();
        //request permission
        requestPermission();
        //on refreshing
        onRefresh();

        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);
        SharedPreferences userProfileName = getSharedPreferences("userProfileName", MODE_PRIVATE);
        String user = userProfileName.getString("userName", "");
        userDisplay = navigationView.getHeaderView(0).findViewById(R.id.userDisplayText);
        userDisplay.setText(user);

        toggle = new ActionBarDrawerToggle(this, weatherLayout, R.string.open, R.string.close);
        weatherLayout.addDrawerListener(toggle);
        toggle.syncState();

        Intent intent = getIntent();
        searchMode = intent.getBooleanExtra("searchMode", false);
        searchLat = intent.getStringExtra("searchLat");
        searchLon = intent.getStringExtra("searchLon");

        if (searchMode) {
            getCurrLocWtr.setVisibility(View.VISIBLE);
            btnView[0].setVisibility(View.VISIBLE);
            btnView[1].setVisibility(View.VISIBLE);
            getWeatherInformation(searchLat, searchLon);
        }

        getCurrLocWtr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchMode = false;
                findLocation(getBaseContext());
                getCurrLocWtr.setVisibility(View.GONE);
                btnView[0].setVisibility(View.GONE);
                btnView[1].setVisibility(View.GONE);
            }
        });
    }

    private void assignForecastItems() {

        forecastDay = new TextView[6];
        forecastDay[0] = findViewById(R.id.forecastDay1);
        forecastDay[1] = findViewById(R.id.forecastDay2);
        forecastDay[2] = findViewById(R.id.forecastDay3);
        forecastDay[3] = findViewById(R.id.forecastDay4);
        forecastDay[4] = findViewById(R.id.forecastDay5);
        forecastDay[5] = findViewById(R.id.forecastDay6);

        forecastDate = new TextView[6];
        forecastDate[0] = findViewById(R.id.forecastDate1);
        forecastDate[1] = findViewById(R.id.forecastDate2);
        forecastDate[2] = findViewById(R.id.forecastDate3);
        forecastDate[3] = findViewById(R.id.forecastDate4);
        forecastDate[4] = findViewById(R.id.forecastDate5);
        forecastDate[5] = findViewById(R.id.forecastDate6);

        forecastDesc = new TextView[6];
        forecastDesc[0] = findViewById(R.id.forecastDesc1);
        forecastDesc[1] = findViewById(R.id.forecastDesc2);
        forecastDesc[2] = findViewById(R.id.forecastDesc3);
        forecastDesc[3] = findViewById(R.id.forecastDesc4);
        forecastDesc[4] = findViewById(R.id.forecastDesc5);
        forecastDesc[5] = findViewById(R.id.forecastDesc6);

        forecastImage = new ImageView[6];
        forecastImage[0] = findViewById(R.id.forecastImage1);
        forecastImage[1] = findViewById(R.id.forecastImage2);
        forecastImage[2] = findViewById(R.id.forecastImage3);
        forecastImage[3] = findViewById(R.id.forecastImage4);
        forecastImage[4] = findViewById(R.id.forecastImage5);
        forecastImage[5] = findViewById(R.id.forecastImage6);

        forecastMaxTemp = new TextView[6];
        forecastMaxTemp[0] = findViewById(R.id.forecastMaxTemp1);
        forecastMaxTemp[1] = findViewById(R.id.forecastMaxTemp2);
        forecastMaxTemp[2] = findViewById(R.id.forecastMaxTemp3);
        forecastMaxTemp[3] = findViewById(R.id.forecastMaxTemp4);
        forecastMaxTemp[4] = findViewById(R.id.forecastMaxTemp5);
        forecastMaxTemp[5] = findViewById(R.id.forecastMaxTemp6);

        forecastMinTemp = new TextView[6];
        forecastMinTemp[0] = findViewById(R.id.forecastMinTemp1);
        forecastMinTemp[1] = findViewById(R.id.forecastMinTemp2);
        forecastMinTemp[2] = findViewById(R.id.forecastMinTemp3);
        forecastMinTemp[3] = findViewById(R.id.forecastMinTemp4);
        forecastMinTemp[4] = findViewById(R.id.forecastMinTemp5);
        forecastMinTemp[5] = findViewById(R.id.forecastMinTemp6);
    }

    private void requestPermission() {
        Dexter.withContext(this)
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                        if (ActivityCompat.checkSelfPermission(WeatherActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(WeatherActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        permission = true;
                        //check network status
                        checkData();
                        if (network == 1) {
                            //check gps status
                            checkGPSStatus();
                            if (gps == 1) {
                                findLocation(getBaseContext());
                            }
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        Snackbar.make(weatherLayout, "Permission Denied. Enable Permission manually by going to " +
                                "Settings -> apps -> Puddle -> Permissions -> location. Then restart the app!", Snackbar.LENGTH_LONG).show();
                        refreshLayout.setRefreshing(false);
                        refreshLayout.setEnabled(false);
                        permission = false;
                    }
                }).check();
    }

    private void findLocation(Context con) {
        Log.d("Find Location", "in find_location");
        this.mContext = con;
        locationManager = (LocationManager) con.getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        List<String> providers = locationManager.getProviders(true);
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(provider, 1000, 0,
                    new LocationListener() {

                        public void onLocationChanged(Location location) {
                        }

                        public void onProviderDisabled(String provider) {
                        }

                        public void onProviderEnabled(String provider) {
                        }

                        public void onStatusChanged(String provider, int status,
                                                    Bundle extras) {
                        }
                    });
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                String latitude = String.valueOf(location.getLatitude());
                String longitude = String.valueOf(location.getLongitude());
                getWeatherInformation(latitude, longitude);
            }
        }
    }

    private void checkGPSStatus() {
        LocationManager locationManager = null;
        boolean gps_enabled = false;
        boolean network_enabled = false;
        if ( locationManager == null ) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ignored){}
        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ignored){}

        if (gps_enabled && network_enabled) {
            gps = 1;
        }

        if ( !gps_enabled && !network_enabled) {
            refreshLayout.setRefreshing(false);
            AlertDialog.Builder dialog = new AlertDialog.Builder(WeatherActivity.this);
            dialog.setMessage("GPS not enabled");
            dialog.setPositiveButton("Enable", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //this will navigate user to the device location settings screen
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            AlertDialog alert = dialog.create();
            alert.setCanceledOnTouchOutside(false);
            alert.show();
            refreshLayout.setRefreshing(true);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i = getIntent();
                    startActivity(i);
                    finish();
                }
            }, 15000);
        }
    }

    private void checkData() {
        ConnectivityManager cm = (ConnectivityManager) WeatherActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            network = 1;
            errorLayout.setVisibility(View.GONE);
            nsv.setVisibility(View.VISIBLE);
        }
        else {
            network = 0;
            refreshLayout.setRefreshing(false);
            errorLayout.setVisibility(View.VISIBLE);
            nsv.setVisibility(View.GONE);
        }

    }

    private void onRefresh() {
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checkData();
                if (network == 1){
                    checkGPSStatus();
                    if (gps == 1){
                        findLocation(getBaseContext());
                        Snackbar.make(weatherLayout, "Page Refreshed", Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @SuppressLint("DefaultLocale")
    public void getWeatherInformation(final String latitude, final String longitude) {
        ApiInterface mService = ApiClient.getWeatherRetrofit().create(ApiInterface.class);
        Call<WeatherResult> call;
        if (searchMode) {
            call = mService.getWeather(searchLat, searchLon, exclude, APP_ID);
        }
        else
            call = mService.getWeather(String.valueOf(latitude), String.valueOf(longitude), exclude, APP_ID);

        call.enqueue(new Callback<WeatherResult>() {
            @Override
            public void onResponse(Call<WeatherResult> call, Response<WeatherResult> response) {

                //load units
                SharedPreferences tempMODE = getSharedPreferences("TEMPERATUREMODE", Context.MODE_PRIVATE);
                tempMode = tempMODE.getInt("tempMode", 1);
                SharedPreferences windMODE = getSharedPreferences("WINDMODE", Context.MODE_PRIVATE);
                windMode = windMODE.getInt("windMode", 1);
                SharedPreferences pressMODE = getSharedPreferences("PRESSUREMODE", Context.MODE_PRIVATE);
                pressMode = pressMODE.getInt("pressMode", 1);
                SharedPreferences visMODE = getSharedPreferences("VISIBILITYMODE", Context.MODE_PRIVATE);
                visMode = visMODE.getInt("visMode", 1);

                //load image
                Picasso.get()
                        .load("https://openweathermap.org/img/wn/" +
                                response.body().getCurrent().getWeather().get(0).getIcon() +
                                ".png")
                        .into(mMainImage);

                //set texts
                String temperature = String.valueOf((int) convertKelvinToTemp(response.body().getCurrent().getTemp(), tempMode));
                if (tempMode == 1)
                    temperature += " °C";
                else if (tempMode == 2)
                    temperature += " °F";
                mTemp.setText(temperature);

                mMain.setText(response.body().getCurrent().getWeather().get(0).getMain());

                String description = "|";
                description += response.body().getCurrent().getWeather().get(0).getDescription();
                for (int j = 0; j < description.length(); j++) {
                    if (description.charAt(j) == ' ') {
                        description = description.substring(0,1).toUpperCase() + description.substring(1,j).toLowerCase() + " " +
                                description.substring(j+1, j+2).toUpperCase() + description.substring(j+2).toLowerCase();
                    }
                }
                mDescription.setText(description);

                String windSpeed = String.format("%.2f",convertWindSpeed(response.body().getCurrent().getWind_speed(), windMode));
                if (windMode == 1)
                    windSpeed += " km/h";
                else if (windMode == 2)
                    windSpeed += " mph";
                else if (windMode == 3)
                    windSpeed += " knots";
                mWind.setText(windSpeed);

                String pressure = " ";

                if (pressMode == 1) {
                    pressure = (int) convertPressure(response.body().getCurrent().getPressure(), pressMode) + " hPa";
                                    }
                else if (pressMode == 2) {
                    pressure = (float) convertPressure(response.body().getCurrent().getPressure(), pressMode) + " mmHg";
                }
                else if (pressMode == 3) {
                    pressure = (float) convertPressure(response.body().getCurrent().getPressure(), pressMode) + " inHg";
                }
                mPressure.setText(pressure);

                String humidity = String.valueOf(response.body().getCurrent().getHumidity());
                humidity += "%";
                mHumidity.setText(humidity);

                String dewPoint = String.format("%.2f",(float) convertKelvinToTemp(response.body().getCurrent().getDew_point(), tempMode));
                if (tempMode == 1)
                    dewPoint += " °C";
                else if (tempMode == 2)
                    dewPoint += " °F";
                mDewPoint.setText(dewPoint);

                String visibility = " ";
                if (visMode == 1) {
                    visibility = String.valueOf((int)convertVisibility(response.body().getCurrent().getVisibility() / 1000, visMode));
                    visibility += " km";
                }
                else if (visMode ==  2) {
                    visibility = String.valueOf((int)convertVisibility(response.body().getCurrent().getVisibility() / 1000, visMode));
                    visibility += " m";
                }
                else if (visMode == 3) {
                    visibility = String.format("%.2f",convertVisibility(response.body().getCurrent().getVisibility() / 1000, visMode));
                    visibility += " mi";
                }
                mVisibility.setText(visibility);

                mClouds.setText(String.valueOf(response.body().getCurrent().getClouds()));

                hourPattern = 0;
                mSunRise.setText(convertUnixToHour(response.body().getCurrent().getSunrise(),hourPattern));
                mSunSet.setText(convertUnixToHour(response.body().getCurrent().getSunset(),hourPattern));

                String rain = (String.valueOf(response.body().getDaily().get(0).getRain()));
                rain += " mm";
                mRain.setText(rain);
                if (mRain.getText().equals(" mm")) {
                    rain = "0 mm";
                    mRain.setText(rain);
                }

                //stop refreshing
                refreshLayout.setRefreshing(false);

                //get forecast
                // >>>set day
                datePattern = 0;
                forecastDay[0].setText("Today");
                for (int i = 1; i <= 5; i++) {
                    forecastDay[i].setText(convertUnixToDate(response.body().getDaily().get(i).getDt(),datePattern));
                }

                // >>>set date
                datePattern = 1;
                for (int i = 0; i < 6; i++) {
                    forecastDate[i].setText(convertUnixToDate(response.body().getDaily().get(i).getDt(),datePattern));
                }

                // >>>set description
                for (int i = 0; i < 6; i++) {
                    String dailyDesc = response.body().getDaily().get(i).getWeather().get(0).getDescription();
                    if (dailyDesc.length() >= 7) {
                        forecastDesc[i].setEllipsize(TextUtils.TruncateAt.MARQUEE);
                        forecastDesc[i].setSelected(true);
                        forecastDesc[i].setMarqueeRepeatLimit(5);
                    }
                    dailyDesc = dailyDesc.substring(0,1).toUpperCase() + dailyDesc.substring(1).toLowerCase();
                    for (int j = 0; j < dailyDesc.length(); j++) {
                        if (dailyDesc.charAt(j) == ' ') {
                            dailyDesc = dailyDesc.substring(0,1).toUpperCase() + dailyDesc.substring(1,j).toLowerCase() + " " +
                                    dailyDesc.substring(j+1, j+2).toUpperCase() + dailyDesc.substring(j+2).toLowerCase();
                        }
                    }
                    forecastDesc[i].setText(dailyDesc);
                }

                // >>>set image
                for (int i = 0; i < 6; i++) {
                    Picasso.get()
                            .load("https://openweathermap.org/img/wn/" +
                                    response.body().getDaily().get(i).getWeather().get(0).getIcon() + ".png")
                            .into(forecastImage[i]);
                }

                // >>>set max and min temp
                for (int i = 0; i < 6; i++) {
                    String max = String.valueOf((int) convertKelvinToTemp(response.body().getDaily().get(i).getTemp().getMax(), tempMode));
                    if (tempMode == 1)
                        max += " °C";
                    else if (tempMode == 2)
                        max += " °F";
                    forecastMaxTemp[i].setText(max);

                    String min = String.valueOf((int) convertKelvinToTemp(response.body().getDaily().get(i).getTemp().getMin(), tempMode));
                    if (tempMode == 1)
                        min += " °C";
                    else if (tempMode == 2)
                        min += " °F";
                    forecastMinTemp[i].setText(min);
                }

                //set hourly forecast
                adapter = null;
                hourData = new ArrayList<>(response.body().getHourly());
                adapter = new HourForecastAdapter(WeatherActivity.this, hourData, tempMode);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(hLayoutManager);

                //stop refreshing
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<WeatherResult> call, Throwable t) {
                Toast.makeText(WeatherActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        Call<CityName> callC;

        if (searchMode) {
            callC = mService.getCityName(searchLat, searchLon, APP_ID);
        }
        else
            callC = mService.getCityName(String.valueOf(latitude), String.valueOf(longitude), APP_ID);

        callC.enqueue(new Callback<CityName>() {
            @Override
            public void onResponse(Call<CityName> call, Response<CityName> response) {
                mLocation.setText(response.body().getName());
            }

            @Override
            public void onFailure(Call<CityName> call, Throwable t) {
                Toast.makeText(WeatherActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("SimpleDateFormat")
    private static String convertUnixToDate(long dt, int pattern) {
        Date date = new Date(dt*1000L);
        SimpleDateFormat simpleDateFormat = null;
        if (pattern == 0) {
            simpleDateFormat = new SimpleDateFormat("E");
        }
        else if (pattern == 1) {
            simpleDateFormat = new SimpleDateFormat("MMM dd");
        }
        assert simpleDateFormat != null;
        return simpleDateFormat.format(date);
    }

    @SuppressLint("SimpleDateFormat")
    public static String convertUnixToHour(long sunDt, int pattern) {
        Date date = new Date(sunDt*1000L);
        SimpleDateFormat simpleDateFormat = null;
        if (pattern == 0) {
            simpleDateFormat = new SimpleDateFormat("hh:mm a");
        }
        else if (pattern == 1) {
            simpleDateFormat = new SimpleDateFormat("hh a");
        }
        assert simpleDateFormat != null;
        return simpleDateFormat.format(date);
    }

    private void setCustomUnits(String[] units) {
        switch (units[0]) {
            case "C":
                tempMode = 1;
                SharedPreferences tempMODE = getSharedPreferences("TEMPERATUREMODE", Context.MODE_PRIVATE);
                SharedPreferences.Editor editorTM = tempMODE.edit();
                editorTM.putInt("tempMode", tempMode);
                editorTM.apply();
                break;
            case "F":
                tempMode = 2;
                tempMODE = getSharedPreferences("TEMPERATUREMODE", Context.MODE_PRIVATE);
                editorTM = tempMODE.edit();
                editorTM.putInt("tempMode", tempMode);
                editorTM.apply();
                break;
        }
        switch (units[1]) {
            case "Kilometers per hour - km/h":
                windMode = 1;
                SharedPreferences windMODE = getSharedPreferences("WINDMODE", Context.MODE_PRIVATE);
                SharedPreferences.Editor editorWM = windMODE.edit();
                editorWM.putInt("windMode", windMode);
                editorWM.apply();
                break;
            case "Miles per hour - mph":
                windMode = 2;
                windMODE = getSharedPreferences("WINDMODE", Context.MODE_PRIVATE);
                editorWM = windMODE.edit();
                editorWM.putInt("windMode", windMode);
                editorWM.apply();
                break;
            case "Nautical miles per hour - kts":
                windMode = 3;
                windMODE = getSharedPreferences("WINDMODE", Context.MODE_PRIVATE);
                editorWM = windMODE.edit();
                editorWM.putInt("windMode", windMode);
                editorWM.apply();
                break;
        }
        switch (units[2]) {
            case "HectoPascals - hPa":
                pressMode = 1;
                SharedPreferences pressMODE = getSharedPreferences("PRESSUREMODE", Context.MODE_PRIVATE);
                SharedPreferences.Editor editorPM = pressMODE.edit();
                editorPM.putInt("pressMode", pressMode);
                editorPM.apply();
                break;
            case "Millimeters of Mercury - mmHg":
                pressMode = 2;
                pressMODE = getSharedPreferences("PRESSUREMODE", Context.MODE_PRIVATE);
                editorPM = pressMODE.edit();
                editorPM.putInt("pressMode", pressMode);
                editorPM.apply();
                break;
            case "Inches of Mercury - inHg":
                pressMode = 3;
                pressMODE = getSharedPreferences("PRESSUREMODE", Context.MODE_PRIVATE);
                editorPM = pressMODE.edit();
                editorPM.putInt("pressMode", pressMode);
                editorPM.apply();
                break;
        }
        switch (units[3]) {
            case "Kilometer - km":
                visMode = 1;
                SharedPreferences visMODE = getSharedPreferences("VISIBILITYMODE", Context.MODE_PRIVATE);
                SharedPreferences.Editor editorVM = visMODE.edit();
                editorVM.putInt("visMode", visMode);
                editorVM.apply();
                break;
            case "Meter - m":
                visMode = 2;
                visMODE = getSharedPreferences("VISIBILITYMODE", Context.MODE_PRIVATE);
                editorVM = visMODE.edit();
                editorVM.putInt("visMode", visMode);
                editorVM.apply();
                break;
            case "Miles - mi":
                visMode = 3;
                visMODE = getSharedPreferences("VISIBILITYMODE", Context.MODE_PRIVATE);
                editorVM = visMODE.edit();
                editorVM.putInt("visMode", visMode);
                editorVM.apply();
                break;
        }
    }

    public static double convertKelvinToTemp(double value, int tempMode) {
        if (tempMode == 1)
            return value - 273.15;
        else if (tempMode == 2)
            return ((value - 273.15) * 1.8) + 32;
        return 0;
    }

    public static double convertWindSpeed(double value, int WindMode) {
        if (WindMode == 1)
            return value;
        else if (WindMode == 2)
            return value * 0.621371;
        else if (WindMode == 3)
            return value * 0.539957;
        return 0;
    }

    public static double convertPressure(int value, int pressMode) {
        if (pressMode == 1)
            return value;
        else if (pressMode == 2)
            return (value * 0.75);
        else if (pressMode == 3)
            return (value *  0.03);
        return 0;
    }

    public static double convertVisibility(int value, int visMode) {
        if (visMode == 1)
            return value;
        else if (visMode == 2)
            return value * 1000;
        else if (visMode == 3)
            return (value * 0.621371);
        return 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_weather, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (permission) {
            int id = item.getItemId();

            switch (id) {
                case R.id.city:

                    Intent i = new Intent(WeatherActivity.this, WeatherCitySearchActivity.class);
                    startActivity(i);
                    overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);

                    break;

                case android.R.id.home:
                    weatherLayout.openDrawer(GravityCompat.START);
                    break;

                case R.id.settings:
                    SettingsBottomSheet bottomSheet = new SettingsBottomSheet();
                    bottomSheet.show(getSupportFragmentManager(), "SettingBottomSheet");
                    break;

                case R.id.sensor:
                    int np;
                    SharedPreferences newsPoints = getSharedPreferences("newsPoints", Context.MODE_PRIVATE);
                    np = newsPoints.getInt("np", 0);
                    if (np > 100) {
                        Intent intent = new Intent(WeatherActivity.this, SensorActivity.class);
                        startActivity(intent);
                    }
                    else {
                        Snackbar.make(weatherLayout, "You must be a \"Front Bencher\" to unlock this feature", Snackbar.LENGTH_LONG)
                                .show();
                    }
                    break;

            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent i;
        switch (id) {
            case R.id.news:
                i = new Intent(this, NewsActivity.class);
                startActivity(i);
                finish();
                break;
            case R.id.weather:
                weatherLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.profile:
                i = new Intent(this, ProfileActivity.class);
                startActivity(i);
                break;
        }
        weatherLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClicked(String[] text) {
        setCustomUnits(text);
        findLocation(getBaseContext());
    }

    @Override
    public void onBackPressed() {

        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast.cancel();
            super.onBackPressed();
            return;
        }
        else {
            backToast = Toast.makeText(getBaseContext(), "Press again to exit", Toast.LENGTH_SHORT);
            backToast.show();
        }

        backPressedTime = System.currentTimeMillis();
    }
}