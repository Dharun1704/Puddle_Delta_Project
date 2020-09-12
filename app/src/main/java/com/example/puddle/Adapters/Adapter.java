package com.example.puddle.Adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private DatabaseReference reference;
    private ArrayList<Article> articles;
    private Context context;
    ViewPager2 viewPager;
    private ArrayList<Article> bookmarkArticles, firebaseBookmarks;
    private String theme;
    private int np, click = 0, bookmarkNo = 0;
    private static DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
    private static AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();

    private static final String TAG = "Adapter";

    public Adapter(DatabaseReference reference, ArrayList<Article> articles,
                   Context context, ViewPager2 viewPager, String theme, int np) {
        this.reference = reference;
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
                            setBookmarkAnim(holder.doubleTapView, holder.doubleTapBookmark);
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
                reference.child("np").setValue(np);
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

    private void setBookmarkAnim(View view, ImageView imageView) {

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
        firebaseBookmarks = new ArrayList<>();
        DatabaseReference bookmarkRef = reference.child("Bookmarks");
        bookmarkRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookmarkNo =(int) snapshot.getChildrenCount();
                for (DataSnapshot dataBookmarkSnapshot : snapshot.getChildren()) {
                    Article article = new Article();
                    article.setAuthor(dataBookmarkSnapshot.child("author").getValue(String.class));
                    article.setTitle(dataBookmarkSnapshot.child("title").getValue(String.class));
                    article.setDescription(dataBookmarkSnapshot.child("description").getValue(String.class));
                    article.setUrl(dataBookmarkSnapshot.child("url").getValue(String.class));
                    article.setUrlToImage(dataBookmarkSnapshot.child("urlToImage").getValue(String.class));
                    article.setPublishedAt(dataBookmarkSnapshot.child("publishedAt").getValue(String.class));
                    article.setSource(dataBookmarkSnapshot.child("source").getValue(Source.class));

                    firebaseBookmarks.add(article);
                }
                for (int i = 0; i < bookmarkNo; i++) {
                    Log.i(TAG, "addToBookmarksDB: " + firebaseBookmarks.get(i).getAuthor());
                }

                boolean isExisted = false;
                for (int i = 0; i < firebaseBookmarks.size(); i++) {
                    if (firebaseBookmarks.get(i).getUrl().equals(mUrl)) {
                        isExisted = true;
                    }
                }

                if (!isExisted) {
                    Article article = new Article();
                    Source source = new Source();
                    article.setAuthor(mAuthor);
                    article.setTitle(mTitle);
                    article.setDescription(mDesc);
                    article.setUrl(mUrl);
                    article.setUrlToImage(mImage);
                    article.setPublishedAt(mDate);
                    source.setName(mSource);
                    article.setSource(source);
                    firebaseBookmarks.add(article);
                    bookmarkRef.setValue(firebaseBookmarks);
                    if (firebaseBookmarks.isEmpty())
                        Toast.makeText(context, "Unable to add article to bookmarks", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
