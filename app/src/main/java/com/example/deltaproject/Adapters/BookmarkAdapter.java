package com.example.deltaproject.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deltaproject.DateTime;
import com.example.deltaproject.NewsDetailActivity;
import com.example.deltaproject.NewsModel.Article;
import com.example.deltaproject.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Article> articles;

    public BookmarkAdapter(Context context, ArrayList<Article> articles) {
        this.context = context;
        this.articles = articles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.slider_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Article model = articles.get(position);

        if( model.getUrlToImage() == null || model.getUrlToImage().isEmpty()) {
            holder.imageView.setImageResource(R.drawable.icon_news);
        } else {
            Picasso
                    .get()
                    .load(model.getUrlToImage())
                    .fit()
                    .centerCrop()
                    .into(holder.imageView);
        }

        holder.title.setText(model.getTitle());
        holder.desc.setText(model.getDescription());
        holder.time.setText(" \u2022 " + DateTime.DateToTimeFormat(model.getPublishedAt()));
        holder.source.setText(model.getSource().getName());

        Drawable unwrapped = AppCompatResources.getDrawable(context, R.drawable.background2);
        assert unwrapped != null;
        Drawable wrapped = DrawableCompat.wrap(unwrapped);
        DrawableCompat.setTint(wrapped, Color.parseColor("#1E90FF"));
        holder.openArticle.setBackground(wrapped);

        holder.openArticle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(context, NewsDetailActivity.class);

                i.putExtra("url", model.getUrl());
                i.putExtra("title", model.getTitle());
                i.putExtra("description", model.getDescription());
                i.putExtra("img", model.getUrlToImage());
                i.putExtra("date", model.getPublishedAt());
                i.putExtra("source", model.getSource().getName());
                i.putExtra("author", model.getAuthor());

                Pair<View, String> pair = Pair.create( holder.imageView, ViewCompat.getTransitionName(holder.imageView));
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        (Activity) context,
                        pair
                );
                context.startActivity(i, optionsCompat.toBundle());
            }
        });
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView title, desc, source, time;
        ImageView imageView;
        Button openArticle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.sliderTitle);
            desc = itemView.findViewById(R.id.sliderDesc);
            source = itemView.findViewById(R.id.sliderSource);
            time = itemView.findViewById(R.id.sliderTime);
            imageView = itemView.findViewById(R.id.sliderPhoto);
            openArticle = itemView.findViewById(R.id.openArticle);
        }
    }
}
