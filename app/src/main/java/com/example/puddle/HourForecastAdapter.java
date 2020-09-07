package com.example.puddle;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puddle.WeatherModel.Hourly;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class HourForecastAdapter extends RecyclerView.Adapter<HourForecastAdapter.ViewHolder> {

    Context context;
    ArrayList<Hourly> data;
    int tempMode;

    public HourForecastAdapter(Context context, ArrayList<Hourly> data, int tempMode) {
        this.context = context;
        this.data = data;
        this.tempMode = tempMode;
    }

    @NonNull
    @Override
    public HourForecastAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hour_forecast, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HourForecastAdapter.ViewHolder holder, int position) {
        Hourly hourly = data.get(position);

        String temp = String.valueOf((int) WeatherActivity.convertKelvinToTemp(hourly.getTemp(), tempMode));
        if (tempMode == 1)
            temp += " °C";
        else if (tempMode == 2)
            temp += " °F";
        holder.temp.setText(temp);
        holder.time.setText(WeatherActivity.convertUnixToHour(hourly.getDt(), 1));

        Picasso
                .get()
                .load("https://openweathermap.org/img/wn/" +
                        hourly.getWeather().get(0).getIcon() + ".png")
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView time, temp;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            time = itemView.findViewById(R.id.forecastHourTime);
            temp = itemView.findViewById(R.id.forecastHourTemp);
            image = itemView.findViewById(R.id.forecastHourImage);
        }
    }
}
