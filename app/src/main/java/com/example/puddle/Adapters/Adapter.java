package com.example.puddle.Adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.provider.ContactsContract;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.puddle.DateTime;
import com.example.puddle.NewsDatabase;
import com.example.puddle.NewsDetailActivity;
import com.example.puddle.NewsModel.Article;
import com.example.puddle.NewsModel.Source;
import com.example.puddle.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.logging.LogRecord;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private ArrayList<Article> articles;
    private Context context;
    ViewPager2 viewPager;
    private ArrayList<Article> bookmarkArticles;
    private String theme;
    private int np, click = 0;;
    private boolean isBookmarked;
    private static DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
    private static AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();

    public Adapter(ArrayList<Article> articles, Context context, ViewPager2 viewPager, String theme, int np) {
        this.articles = articles;
        this.context = context;
        this.viewPager = viewPager;
        this.theme = theme;
        this.np = np;
    }

    @NonNull
    @Override
    public Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.slider_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint({"SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull Adapter.ViewHolder holder, int position) {
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

        switch (theme) {
            case "Violet-blue": {
                Drawable unwrapped = AppCompatResources.getDrawable(context, R.drawable.background2);
                assert unwrapped != null;
                Drawable wrapped = DrawableCompat.wrap(unwrapped);
                DrawableCompat.setTint(wrapped, Color.parseColor("#8A2BE2"));
                holder.openArticle.setBackground(wrapped);
                break;
            }
            case "Deep Sea": {
                Drawable unwrapped = AppCompatResources.getDrawable(context, R.drawable.background2);
                assert unwrapped != null;
                Drawable wrapped = DrawableCompat.wrap(unwrapped);
                DrawableCompat.setTint(wrapped, Color.parseColor("#1E90FF"));
                holder.openArticle.setBackground(wrapped);
                break;
            }
            case "Dark": {
                Drawable unwrapped = AppCompatResources.getDrawable(context, R.drawable.background2);
                assert unwrapped != null;
                Drawable wrapped = DrawableCompat.wrap(unwrapped);
                DrawableCompat.setTint(wrapped, Color.parseColor("#FF4500"));
                holder.openArticle.setBackground(wrapped);
                break;
            }
        }

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click++;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (click == 2) {
                            bookmark(holder.doubleTapView, holder.doubleTapBookmark);
                            addToBookmarksDB(model.getAuthor(), model.getTitle(), model.getDescription(),
                                    model.getUrl(), model.getUrlToImage(),
                                    model.getPublishedAt(), model.getSource().getName());
                        }
                        click = 0;
                    }
                }, 200);
            }
        });

        holder.openArticle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                np++;
                SharedPreferences newsPoints = context.getSharedPreferences("newsPoints", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = newsPoints.edit();
                editor.putInt("np", np);
                editor.apply();
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

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, desc, source, time;
        ImageView imageView, doubleTapBookmark;
        Button openArticle;
        View doubleTapView;
        NestedScrollView nsv;

        @SuppressLint("ClickableViewAccessibility")
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nsv = itemView.findViewById(R.id.sliderNsv);
            title = itemView.findViewById(R.id.sliderTitle);
            desc = itemView.findViewById(R.id.sliderDesc);
            source = itemView.findViewById(R.id.sliderSource);
            time = itemView.findViewById(R.id.sliderTime);
            imageView = itemView.findViewById(R.id.sliderPhoto);
            openArticle = itemView.findViewById(R.id.openArticle);
            doubleTapBookmark = itemView.findViewById(R.id.doubleTapBookmark);
            doubleTapView = itemView.findViewById(R.id.doubleTapBackground);
        }
    }

    private void bookmark(View view, ImageView imageView) {

        view.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.VISIBLE);

        view.setScaleX(0.1f);
        view.setScaleY(0.1f);
        view.setAlpha(1f);
        imageView.setScaleX(0.1f);
        imageView.setScaleY(0.1f);

        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator bgScaleYAnim = ObjectAnimator.ofFloat(view, "scaleY", 0.1f, 1f);
        bgScaleYAnim.setDuration(500);
        bgScaleYAnim.setInterpolator(decelerateInterpolator);

        ObjectAnimator bgScaleXAnim = ObjectAnimator.ofFloat(view, "scaleX", 0.1f, 1f);
        bgScaleXAnim.setDuration(500);
        bgScaleXAnim.setInterpolator(decelerateInterpolator);

        ObjectAnimator bgAlphaAnim = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        bgAlphaAnim.setDuration(500);
        bgAlphaAnim.setStartDelay(350);
        bgAlphaAnim.setInterpolator(decelerateInterpolator);

        ObjectAnimator imgScaleUpYAnim = ObjectAnimator.ofFloat(imageView, "scaleY", 0.1f, 1f);
        imgScaleUpYAnim.setDuration(300);
        imgScaleUpYAnim.setInterpolator(decelerateInterpolator);

        ObjectAnimator imgScaleUpXAnim = ObjectAnimator.ofFloat(imageView, "scaleX", 0.1f, 1f);
        imgScaleUpXAnim.setDuration(300);
        imgScaleUpXAnim.setInterpolator(decelerateInterpolator);

        ObjectAnimator imgScaleDownYAnim = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 0f);
        imgScaleDownYAnim.setDuration(300);
        imgScaleDownYAnim.setInterpolator(accelerateInterpolator);

        ObjectAnimator imgScaleDownXAnim = ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 0f);
        imgScaleDownXAnim.setDuration(300);
        imgScaleDownXAnim.setInterpolator(accelerateInterpolator);

        animatorSet.playTogether(bgScaleYAnim, bgScaleXAnim, bgAlphaAnim, imgScaleUpYAnim, imgScaleUpXAnim);
        animatorSet.play(imgScaleDownYAnim).with(imgScaleDownXAnim).after(imgScaleUpYAnim);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                resetAnimation(view, imageView);
            }
        });
        animatorSet.start();
    }

    private void resetAnimation(View view, ImageView imageView) {
        view.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);
    }

    private void addToBookmarksDB(String mAuthor, String mTitle, String mDesc,
                                  String mUrl, String mImage, String mDate, String mSource) {

        bookmarkArticles = new ArrayList<>();
        NewsDatabase db = new NewsDatabase(context);
        Cursor data = db.getData();
        while (data.moveToNext()) {
            String author = data.getString(1);
            String title = data.getString(2);
            String desc = data.getString(3);
            String url = data.getString(4);
            String urltoimage = data.getString(5);
            String publish = data.getString(6);
            String source = data.getString(7);
            Article article = new Article();
            Source source1 = new Source();
            article.setAuthor(author);
            article.setTitle(title);
            article.setDescription(desc);
            article.setUrl(url);
            article.setUrlToImage(urltoimage);
            article.setPublishedAt(publish);
            source1.setName(source);
            article.setSource(source1);
            bookmarkArticles.add(article);
        }

        boolean isExisted = false;
        for (int i = 0; i < bookmarkArticles.size(); i++) {
            if (bookmarkArticles.get(i).getUrl().equals(mUrl)) {
                isExisted = true;
            }
        }

        if (!isExisted) {
            boolean insertData = db.addData(mAuthor, mTitle, mDesc, mUrl, mImage, mDate, mSource);
            if (!insertData)
                Toast.makeText(context, "Unable to add article to bookmarks", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(context, "Article already exists in bookmarks.", Toast.LENGTH_SHORT).show();
    }
}
