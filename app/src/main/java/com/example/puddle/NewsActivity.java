package com.example.puddle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.puddle.Adapters.Adapter;
import com.example.puddle.NewsApi.ApiClient;
import com.example.puddle.NewsApi.ApiInterface;
import com.example.puddle.NewsModel.Article;
import com.example.puddle.NewsModel.News;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    public static final String API_KEY = "034142bf356249c885fba9ae4e2b1f6e";
    private int np = 0;
    private long backPressedTime;
    private Toast backToast;
    private ViewPager2 viewPager2;
    private ArrayList<Article> articles = new ArrayList<>();
    private Adapter adapter;
    private RelativeLayout errorLayout;
    private LinearLayout ViewPagerLayout,subCatLayout, dialogLayout;
    private DrawerLayout drawerLayout;
    private SwipeRefreshLayout refreshLayout;
    private HorizontalScrollView categoryLayout;
    CompositePageTransformer compositePageTransformer;
    Toolbar toolbar;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private Dialog levelDialog;

    Button[] categories;
    ImageButton goToStart;
    View[] views;
    TextView[] sub_categories;
    String country = "in", theme;
    ImageView errorImage;
    TextView errorText, errorTextMessage, appTitle;
    int button, categoryIn, categoryOut, view_bg, bg_dark;
    boolean isNetwork = true, isFirstTimePopUp = false;
    Drawable background, wrapped, wrapped2, wrapped3, unwrapped, unwrapped2, unwrapped3;
    TextView dialog_level;
    ImageView dialogCloseImg;
    boolean[] isPopUpDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        errorLayout = findViewById(R.id.errorLayout);
        refreshLayout = findViewById(R.id.newsRefreshLayout);
        refreshLayout.setRefreshing(true);
        refreshLayout.setColorSchemeColors(Color.BLACK);
        ViewPagerLayout = findViewById(R.id.viewPagerLayout);
        categoryLayout = findViewById(R.id.category_layout);
        subCatLayout = findViewById(R.id.subCategoryLayout);
        subCatLayout.setVisibility(View.GONE);

        views = new View[4];
        views[0] = findViewById(R.id.view1);
        views[1] = findViewById(R.id.view2);
        views[1].setVisibility(View.INVISIBLE);
        views[2] = findViewById(R.id.view3);
        views[2].setVisibility(View.INVISIBLE);
        views[3] = findViewById(R.id.view4);
        views[3].setVisibility(View.INVISIBLE);

        appTitle = findViewById(R.id.appName);
        appTitle.setText("Puddle");
        appTitle.setTextColor(Color.WHITE);
        errorImage = findViewById(R.id.errorImage);
        errorText = findViewById(R.id.errorText);
        errorTextMessage = findViewById(R.id.errorTextMessage);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);

        drawerLayout = findViewById(R.id.newsDrawer);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        goToStart = findViewById(R.id.goToStart);
        goToStart.setVisibility(View.GONE);
        goToStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager2.setCurrentItem(0,true);
            }
        });

        viewPager2 = findViewById(R.id.news_slider);
        viewPager2.setClipToPadding(false);
        viewPager2.setClipChildren(false);
        viewPager2.setOffscreenPageLimit(3);
        viewPager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float r = 1 - Math.abs(position);
                page.setScaleY(0.85f + r * 0.15f);
            }
        });

        viewPager2.setPageTransformer(compositePageTransformer);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if(position == 0)
                    goToStart.setVisibility(View.GONE);
                else
                    goToStart.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                boolean enable = false;
                if (state == ViewPager2.SCROLL_STATE_IDLE)
                    enable = true;
                if (refreshLayout != null)
                    refreshLayout.setEnabled(enable);
            }
        });

        // check network
        checkData();

        newsCategory();
        //check and set theme
        setNewsTheme();
        for (int i = 0; i < 8; i++) {
            categories[i].setOnClickListener(categoryClicked);
        }
        if (isNetwork)
            loadNews("", country);

        onRefresh();
        checkAndDisplayDialog();
    }

    private void setNewsTheme() {
        SharedPreferences sNewsTheme = getSharedPreferences("sNewsTheme", MODE_PRIVATE);
        theme = sNewsTheme.getString("fNewsTheme", "Violet-Blue");

        switch (theme) {
            case "Violet-Blue": {
                background = ContextCompat.getDrawable(NewsActivity.this, R.drawable.background_violetblue);
                button = Color.parseColor("#8A2BE2");
                categoryIn = Color.parseColor("#8A2BE2");
                categoryOut = Color.parseColor("#505078");
                view_bg = Color.parseColor("#9370DB");

                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.parseColor("#784278"));
                window.setNavigationBarColor(Color.parseColor("#5656A6"));

                toolbar.setBackgroundColor(Color.parseColor("#784278"));

                unwrapped = AppCompatResources.getDrawable(NewsActivity.this, R.drawable.background);
                assert unwrapped != null;
                wrapped = DrawableCompat.wrap(unwrapped);
                DrawableCompat.setTint(wrapped, categoryOut);

                unwrapped2 = AppCompatResources.getDrawable(NewsActivity.this, R.drawable.background2);
                assert unwrapped2 != null;
                wrapped2 = DrawableCompat.wrap(unwrapped2);
                DrawableCompat.setTint(wrapped2, button);


                unwrapped3 = AppCompatResources.getDrawable(NewsActivity.this, R.drawable.background3);
                assert unwrapped3 != null;
                wrapped3 = DrawableCompat.wrap(unwrapped3);
                DrawableCompat.setTint(wrapped3, categoryIn);

                drawerLayout.setBackground(background);
                goToStart.setBackground(unwrapped2);
                categories[0].setBackground(unwrapped3);
                for (int i = 1; i < 8; i++) {
                    categories[i].setBackground(unwrapped);
                }
                for (int i = 0; i < 4; i++) {
                    views[i].setBackgroundColor(view_bg);
                }
                navigationView.setBackground(ContextCompat.getDrawable(NewsActivity.this,R.drawable.background_violetblue));

                break;
            }
            case "Deep Sea": {
                background = ContextCompat.getDrawable(NewsActivity.this, R.drawable.background_bluegradiant2);
                button = Color.parseColor("#1E90FF");
                categoryIn = Color.parseColor("#1E90FF");
                categoryOut = Color.parseColor("#4682B4");
                view_bg = Color.parseColor("#1E90FF");

                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.parseColor("#5098A9"));
                window.setNavigationBarColor(Color.parseColor("#205888"));

                toolbar.setBackgroundColor(Color.parseColor("#5098A9"));

                unwrapped = AppCompatResources.getDrawable(NewsActivity.this, R.drawable.background);
                assert unwrapped != null;
                wrapped = DrawableCompat.wrap(unwrapped);
                DrawableCompat.setTint(wrapped, categoryOut);

                unwrapped2 = AppCompatResources.getDrawable(NewsActivity.this, R.drawable.background2);
                assert unwrapped2 != null;
                wrapped2 = DrawableCompat.wrap(unwrapped2);
                DrawableCompat.setTint(wrapped2, categoryIn);

                unwrapped3 = AppCompatResources.getDrawable(NewsActivity.this, R.drawable.background3);
                assert unwrapped3 != null;
                wrapped3 = DrawableCompat.wrap(unwrapped3);
                DrawableCompat.setTint(wrapped3, categoryIn);

                drawerLayout.setBackground(background);
                goToStart.setBackground(wrapped2);
                categories[0].setBackground(unwrapped3);
                for (int i = 1; i < 8; i++) {
                    categories[i].setBackground(wrapped);
                }
                for (int i = 0; i < 4; i++) {
                    views[i].setBackgroundColor(view_bg);
                }
                navigationView.setBackground(ContextCompat.getDrawable(NewsActivity.this,R.drawable.background_bluegradiant2));
                break;
            }
            case "Dark":
                bg_dark = Color.parseColor("#252525");
                button = Color.parseColor("#FF4500");
                categoryIn = Color.parseColor("#FF4500");
                categoryOut = Color.parseColor("#696969");
                view_bg = Color.parseColor("#FF6347");

                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.parseColor("#252525"));
                window.setNavigationBarColor(Color.parseColor("#252525"));

                toolbar.setBackgroundColor(Color.parseColor("#252525"));

                unwrapped = AppCompatResources.getDrawable(NewsActivity.this, R.drawable.background);
                assert unwrapped != null;
                wrapped = DrawableCompat.wrap(unwrapped);
                DrawableCompat.setTint(wrapped, categoryOut);

                unwrapped2 = AppCompatResources.getDrawable(NewsActivity.this, R.drawable.background2);
                assert unwrapped2 != null;
                wrapped2 = DrawableCompat.wrap(unwrapped2);
                DrawableCompat.setTint(wrapped2, button);

                unwrapped3 = AppCompatResources.getDrawable(NewsActivity.this, R.drawable.background3);
                assert unwrapped3 != null;
                wrapped3 = DrawableCompat.wrap(unwrapped3);
                DrawableCompat.setTint(wrapped3, categoryIn);

                drawerLayout.setBackgroundColor(bg_dark);
                navigationView.setBackgroundColor(bg_dark);
                goToStart.setBackground(wrapped2);
                categories[0].setBackground(unwrapped3);
                for (int i = 1; i < 8; i++) {
                    categories[i].setBackground(wrapped);
                }
                for (int i = 0; i < 4; i++) {
                    views[i].setBackgroundColor(view_bg);
                }
                break;
        }
    }

    private void checkAndDisplayDialog() {
        String level = "none";
        isPopUpDone = new boolean[4];

        SharedPreferences newsPoints = getSharedPreferences("newsPoints", Context.MODE_PRIVATE);
        np = newsPoints.getInt("np", 0);

        SharedPreferences PopUpDone = getSharedPreferences("popUpDone", Context.MODE_PRIVATE);
        isPopUpDone[0] = PopUpDone.getBoolean("readerDone", false);
        isPopUpDone[1] = PopUpDone.getBoolean("frontBencherDone", false);
        isPopUpDone[2] = PopUpDone.getBoolean("prodigyDone", false);
        isPopUpDone[3] = PopUpDone.getBoolean("newsWizardDone", false);

        if (np == 51) {
            if (!isPopUpDone[0]) {
                level = "Reader";
                isFirstTimePopUp = true;
                isPopUpDone[0] = true;
                SharedPreferences.Editor editor = PopUpDone.edit();
                editor.putBoolean("readerDone", true);
                editor.apply();

            }
        }
        else if (np == 101) {
            if (!isPopUpDone[1]) {
                level = "Front Bencher";
                isFirstTimePopUp = true;
                isPopUpDone[1] = true;
                SharedPreferences.Editor editor = PopUpDone.edit();
                editor.putBoolean("frontBencherDone", true);
                editor.apply();
            }
        }
        else if (np == 301) {
            if (!isPopUpDone[2]) {
                level = "Prodigy";
                isFirstTimePopUp = true;
                isPopUpDone[2] = true;
                SharedPreferences.Editor editor = PopUpDone.edit();
                editor.putBoolean("prodigyDone", true);
                editor.apply();
            }
        }
        else if (np == 501) {
            if (!isPopUpDone[3]) {
                level = "News Wizard";
                isFirstTimePopUp = true;
                isPopUpDone[3] = true;
                SharedPreferences.Editor editor = PopUpDone.edit();
                editor.putBoolean("newsWizardDone", true);
                editor.apply();
            }
        }
        else
            isFirstTimePopUp = false;

        if (isFirstTimePopUp) {
            levelDialogPopUp(level);
        }
    }

    private void levelDialogPopUp(String level) {

        levelDialog = new Dialog(this);
        levelDialog.setContentView(R.layout.level_dialog);

        dialog_level = levelDialog.findViewById(R.id.dialog_levelText);
        dialogCloseImg = levelDialog.findViewById(R.id.dialog_closeImg);
        dialogLayout = levelDialog.findViewById(R.id.dialog_layout);
        if (theme.equals("Dark")) {
            dialogLayout.setBackgroundColor(Color.parseColor("#252525"));
        }
        else {
            dialogLayout.setBackground(background);
        }

        dialog_level.setText(level);
        dialogCloseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                levelDialog.dismiss();
            }
        });
        Objects.requireNonNull(levelDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        levelDialog.show();
    }

    private void checkData() {
        ConnectivityManager cm = (ConnectivityManager) NewsActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            errorLayout.setVisibility(View.GONE);
            ViewPagerLayout.setVisibility(View.VISIBLE);
            categoryLayout.setVisibility(View.VISIBLE);
            isNetwork = true;
        }
        else {
            refreshLayout.setRefreshing(false);
            errorLayout.setVisibility(View.VISIBLE);
            ViewPagerLayout.setVisibility(View.GONE);
            categoryLayout.setVisibility(View.GONE);
            isNetwork = false;
        }
    }

    private void onRefresh() {
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checkData();
                if (isNetwork) {
                    country = "in";
                    loadNews("", country);
                    categories[0].setBackground(wrapped3);
                    categories[0].setTextColor(Color.WHITE);
                    categories[0].setTypeface(categories[0].getTypeface(), Typeface.BOLD);
                    for (int i = 1; i < 8; i++) {
                        categories[i].setBackground(wrapped);
                        categories[i].setTextColor(Color.parseColor("#B6B6B6"));
                    }
                    subCatLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_news, menu);

        final androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) menu.findItem(R.id.search).getActionView();
        MenuItem menuItem = menu.findItem(R.id.search);

        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appTitle.setVisibility(View.GONE);
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                appTitle.setVisibility(View.VISIBLE);
                return false;
            }
        });

        searchView.setQueryHint("Search News...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query.length() > 2)
                    loadNews(query, country);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                loadNews(newText, country);
                return false;
            }
        });
        menuItem.getIcon().setVisible(false, false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            drawerLayout.openDrawer(GravityCompat.START);
        }
        return true;
    }

    public void newsCategory(){
        categories = new Button[8];
        categories[0] = findViewById(R.id.topHd);
        categories[0].setTextColor(Color.WHITE);
        categories[1] = findViewById(R.id.global);
        categories[2] = findViewById(R.id.Business);
        categories[3] = findViewById(R.id.Entertainment);
        categories[4] = findViewById(R.id.health);
        categories[5] = findViewById(R.id.Science);
        categories[6] = findViewById(R.id.Sports);
        categories[7] = findViewById(R.id.technology);

        sub_categories = new TextView[4];
        sub_categories[0] = findViewById(R.id.tv1);
        sub_categories[1] = findViewById(R.id.tv2);
        sub_categories[2] = findViewById(R.id.tv3);
        sub_categories[3] = findViewById(R.id.tv4);
    }

    View.OnClickListener categoryClicked = new View.OnClickListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public void onClick(View v) {
            Button btn_clicked = (Button) v;
            String text = btn_clicked.getText().toString();

            switch (text) {
                case "Headlines":
                    btn_clicked.setBackground(wrapped3);
                    btn_clicked.setTextColor(Color.WHITE);
                    btn_clicked.setTypeface(btn_clicked.getTypeface(), Typeface.BOLD);
                    for (int i = 1; i < 7; i++) {
                        categories[i].setBackground(wrapped);
                        categories[i].setTextColor(Color.parseColor("#B6B6B6"));
                    }
                    subCatLayout.setVisibility(View.GONE);

                    loadNews("", country);
                    break;

                case "Global":
                    btn_clicked.setBackground(wrapped3);
                    btn_clicked.setTextColor(Color.WHITE);
                    btn_clicked.setTypeface(btn_clicked.getTypeface(), Typeface.BOLD);
                    for (int i = 0; i < 8; i++) {
                        if(i != 1) {
                            categories[i].setBackground(wrapped);
                            categories[i].setTextColor(Color.parseColor("#B6B6B6"));
                        }
                    }
                    country = "us";
                    loadNews("", country);
                    subCatLayout.setVisibility(View.VISIBLE);
                    sub_categories[2].setVisibility(View.VISIBLE);
                    sub_categories[3].setVisibility(View.VISIBLE);
                    sub_categories[0].setText("US");
                    sub_categories[1].setText("UK");
                    sub_categories[2].setText("Australia");
                    sub_categories[3].setVisibility(View.GONE);

                    sub_categories[0].setTextColor(Color.WHITE);
                    sub_categories[0].setTypeface(sub_categories[0].getTypeface(), Typeface.BOLD);
                    for (int j = 1; j < 4; j++) {
                        sub_categories[j].setTextColor(Color.parseColor("#B6B6B6"));
                        views[j].setVisibility(View.INVISIBLE);
                    }
                    country = "us";
                    loadNews("", country);

                    for (int i = 0; i < 4; i++) {
                        int x = i;
                        sub_categories[i].setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                TextView subCat = (TextView) v;
                                subCat.setTextColor(Color.WHITE);
                                subCat.setTypeface(subCat.getTypeface(), Typeface.BOLD);
                                if (x == 0) {
                                    views[0].setVisibility(View.VISIBLE);
                                    for (int j = 1; j < 4; j++) {
                                        sub_categories[j].setTextColor(Color.parseColor("#B6B6B6"));
                                        views[j].setVisibility(View.INVISIBLE);
                                    }
                                    country = "us";
                                    loadNews("", country);
                                }

                                if (x == 1) {
                                    views[1].setVisibility(View.VISIBLE);
                                    for (int j = 0; j < 4; j++) {
                                        if (j != 1) {
                                            sub_categories[j].setTextColor(Color.parseColor("#B6B6B6"));
                                            views[j].setVisibility(View.INVISIBLE);
                                        }
                                    }
                                    country = "gb";
                                    loadNews("", country);
                                }

                                if (x == 2) {
                                    views[2].setVisibility(View.VISIBLE);
                                    for (int j = 0; j < 4; j++) {
                                        if (j != 2) {
                                            sub_categories[j].setTextColor(Color.parseColor("#B6B6B6"));
                                            views[j].setVisibility(View.INVISIBLE);
                                        }
                                    }
                                    country = "au";
                                    loadNews("", country);
                                }
                                return true;
                            }
                        });
                    }

                    break;

                case "Business":
                    btn_clicked.setBackground(wrapped3);
                    btn_clicked.setTextColor(Color.WHITE);
                    btn_clicked.setTypeface(btn_clicked.getTypeface(), Typeface.BOLD);
                    for (int i = 0; i < 8; i++) {
                        if(i != 2) {
                            categories[i].setBackground(wrapped);
                            categories[i].setTextColor(Color.parseColor("#B6B6B6"));
                        }
                    }
                    subCatLayout.setVisibility(View.GONE);
                    country = "in";
                    loadCustomNews(country, "business");
                    break;

                case "Entertainment":
                    btn_clicked.setBackground(wrapped3);
                    btn_clicked.setTextColor(Color.WHITE);
                    btn_clicked.setTypeface(btn_clicked.getTypeface(), Typeface.BOLD);
                    for (int i = 0; i < 8; i++) {
                        if(i != 3) {
                            categories[i].setBackground(wrapped);
                            categories[i].setTextColor(Color.parseColor("#B6B6B6"));
                        }
                    }
                    subCatLayout.setVisibility(View.VISIBLE);
                    sub_categories[0].setText("India");
                    sub_categories[1].setText("Hollywood");
                    sub_categories[2].setVisibility(View.GONE);
                    sub_categories[3].setVisibility(View.GONE);

                    sub_categories[0].setTextColor(Color.WHITE);
                    views[0].setVisibility(View.VISIBLE);
                    for (int j = 1; j < 4; j++) {
                        sub_categories[j].setTextColor(Color.parseColor("#B6B6B6"));
                        views[j].setVisibility(View.INVISIBLE);
                    }
                    country = "in";
                    loadCustomNews(country, "entertainment");

                    for (int i = 0; i < 2; i++) {
                        int x = i;
                        sub_categories[i].setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                TextView subCat = (TextView) v;
                                subCat.setTextColor(Color.WHITE);
                                subCat.setTypeface(subCat.getTypeface(), Typeface.BOLD);
                                if (x == 0) {
                                    views[0].setVisibility(View.VISIBLE);
                                    for (int j = 1; j < 4; j++) {
                                        sub_categories[j].setTextColor(Color.parseColor("#B6B6B6"));
                                        views[j].setVisibility(View.INVISIBLE);
                                    }
                                    country = "in";
                                    loadCustomNews(country, "entertainment");
                                }

                                if (x == 1) {
                                    views[1].setVisibility(View.VISIBLE);
                                    for (int j = 0; j < 4; j++) {
                                        if (j != 1) {
                                            sub_categories[j].setTextColor(Color.parseColor("#B6B6B6"));
                                            views[j].setVisibility(View.INVISIBLE);
                                        }
                                    }
                                    country = "us";
                                    loadCustomNews(country, "entertainment");
                                }
                                return true;
                            }
                        });
                    }

                    break;

                case "Health":
                    btn_clicked.setBackground(wrapped3);
                    btn_clicked.setTextColor(Color.WHITE);
                    btn_clicked.setTypeface(btn_clicked.getTypeface(), Typeface.BOLD);
                    for (int i = 0; i < 8; i++) {
                        if(i != 4) {
                            categories[i].setBackground(wrapped);
                            categories[i].setTextColor(Color.parseColor("#B6B6B6"));
                        }
                    }

                    subCatLayout.setVisibility(View.VISIBLE);
                    sub_categories[0].setText("India");
                    sub_categories[1].setText("Global");
                    sub_categories[2].setVisibility(View.GONE);
                    sub_categories[3].setVisibility(View.GONE);

                    sub_categories[0].setTextColor(Color.WHITE);
                    views[0].setVisibility(View.VISIBLE);
                    for (int j = 1; j < 4; j++) {
                        sub_categories[j].setTextColor(Color.parseColor("#B6B6B6"));
                        views[j].setVisibility(View.INVISIBLE);
                    }
                    country = "in";
                    loadCustomNews(country, "health");

                    for (int i = 0; i < 2; i++) {
                        int x = i;
                        sub_categories[i].setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                TextView subCat = (TextView) v;
                                subCat.setTextColor(Color.WHITE);
                                subCat.setTypeface(subCat.getTypeface(), Typeface.BOLD);
                                if (x == 0) {
                                    views[0].setVisibility(View.VISIBLE);
                                    for (int j = 1; j < 4; j++) {
                                        sub_categories[j].setTextColor(Color.parseColor("#B6B6B6"));
                                        views[j].setVisibility(View.INVISIBLE);
                                    }
                                    country = "in";
                                    loadCustomNews(country, "health");
                                }

                                if (x == 1) {
                                    views[1].setVisibility(View.VISIBLE);
                                    for (int j = 0; j < 4; j++) {
                                        if (j != 1) {
                                            sub_categories[j].setTextColor(Color.parseColor("#B6B6B6"));
                                            views[j].setVisibility(View.INVISIBLE);
                                        }
                                    }
                                    country = "us";
                                    loadCustomNews(country, "health");
                                }
                                return true;
                            }
                        });
                    }
                    break;

                case "Science":
                    btn_clicked.setBackground(wrapped3);
                    btn_clicked.setTextColor(Color.WHITE);
                    btn_clicked.setTypeface(btn_clicked.getTypeface(), Typeface.BOLD);
                    for (int i = 0; i < 8; i++) {
                        if(i != 5) {
                            categories[i].setBackground(wrapped);
                            categories[i].setTextColor(Color.parseColor("#B6B6B6"));
                        }
                    }
                    subCatLayout.setVisibility(View.VISIBLE);
                    sub_categories[0].setText("India");
                    sub_categories[1].setText("Global");
                    sub_categories[2].setVisibility(View.GONE);
                    sub_categories[3].setVisibility(View.GONE);

                    sub_categories[0].setTextColor(Color.WHITE);
                    views[0].setVisibility(View.VISIBLE);
                    for (int j = 1; j < 4; j++) {
                        sub_categories[j].setTextColor(Color.parseColor("#B6B6B6"));
                        views[j].setVisibility(View.INVISIBLE);
                    }
                    country = "in";
                    loadCustomNews(country, "science");

                    for (int i = 0; i < 2; i++) {
                        int x = i;
                        sub_categories[i].setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                TextView subCat = (TextView) v;
                                subCat.setTextColor(Color.WHITE);
                                subCat.setTypeface(subCat.getTypeface(), Typeface.BOLD);
                                if (x == 0) {
                                    views[0].setVisibility(View.VISIBLE);
                                    for (int j = 1; j < 4; j++) {
                                        sub_categories[j].setTextColor(Color.parseColor("#B6B6B6"));
                                        views[j].setVisibility(View.INVISIBLE);
                                    }
                                    country = "in";
                                    loadCustomNews(country, "science");
                                }

                                if (x == 1) {
                                    views[1].setVisibility(View.VISIBLE);
                                    for (int j = 0; j < 4; j++) {
                                        if (j != 1) {
                                            sub_categories[j].setTextColor(Color.parseColor("#B6B6B6"));
                                            views[j].setVisibility(View.INVISIBLE);
                                        }
                                    }
                                    country = "us";
                                    loadCustomNews(country, "science");
                                }
                                return true;
                            }
                        });
                    }
                    break;

                case "Sports":
                    btn_clicked.setBackground(wrapped3);
                    btn_clicked.setTextColor(Color.WHITE);
                    btn_clicked.setTypeface(btn_clicked.getTypeface(), Typeface.BOLD);
                    for (int i = 0; i < 8; i++) {
                        if(i != 6) {
                            categories[i].setBackground(wrapped);
                            categories[i].setTextColor(Color.parseColor("#B6B6B6"));
                        }
                    }
                    subCatLayout.setVisibility(View.VISIBLE);
                    sub_categories[3].setVisibility(View.VISIBLE);
                    sub_categories[2].setVisibility(View.VISIBLE);
                    sub_categories[0].setText("All");
                    sub_categories[1].setText("Cricket");
                    sub_categories[2].setText("Football");
                    sub_categories[3].setText("Basketball");

                    sub_categories[0].setTextColor(Color.WHITE);
                    views[0].setVisibility(View.VISIBLE);
                    for (int j = 1; j < 4; j++) {
                        sub_categories[j].setTextColor(Color.parseColor("#B6B6B6"));
                        views[j].setVisibility(View.INVISIBLE);
                    }
                    loadNews("sports", "");

                    for (int i = 0; i < 4; i++) {
                        int x = i;
                        sub_categories[i].setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                TextView subCat = (TextView) v;
                                subCat.setTextColor(Color.WHITE);
                                subCat.setTypeface(subCat.getTypeface(), Typeface.BOLD);
                                if (x == 0) {
                                    views[0].setVisibility(View.VISIBLE);
                                    for (int j = 1; j < 4; j++) {
                                        sub_categories[j].setTextColor(Color.parseColor("#B6B6B6"));
                                        views[j].setVisibility(View.INVISIBLE);
                                    }
                                    loadNews("sports", "");
                                }

                                if (x == 1) {
                                    views[1].setVisibility(View.VISIBLE);
                                    for (int j = 0; j < 4; j++) {
                                        if (j != 1) {
                                            sub_categories[j].setTextColor(Color.parseColor("#B6B6B6"));
                                            views[j].setVisibility(View.INVISIBLE);
                                        }
                                    }
                                    loadNews("cricket", "");
                                }

                                if (x == 2) {
                                    views[2].setVisibility(View.VISIBLE);
                                    for (int j = 0; j < 4; j++) {
                                        if (j != 2) {
                                            sub_categories[j].setTextColor(Color.parseColor("#B6B6B6"));
                                            views[j].setVisibility(View.INVISIBLE);
                                        }
                                    }
                                    loadNews("football", "");
                                }

                                if (x == 3) {
                                    views[3].setVisibility(View.VISIBLE);
                                    for (int j = 0; j < 4; j++) {
                                        if (j != 3) {
                                            sub_categories[j].setTextColor(Color.parseColor("#B6B6B6"));
                                            views[j].setVisibility(View.INVISIBLE);
                                        }
                                    }
                                    loadNews("basketball", "");
                                }

                                return true;
                            }
                        });
                    }

                    break;

                case "Technology":
                    btn_clicked.setBackground(wrapped3);
                    btn_clicked.setTextColor(Color.WHITE);
                    btn_clicked.setTypeface(btn_clicked.getTypeface(), Typeface.BOLD);
                    for (int i = 0; i < 8; i++) {
                        if(i != 7) {
                            categories[i].setBackground(wrapped);
                            categories[i].setTextColor(Color.parseColor("#B6B6B6"));
                        }
                    }
                    subCatLayout.setVisibility(View.VISIBLE);
                    sub_categories[0].setText("India");
                    sub_categories[1].setText("Global");
                    sub_categories[2].setVisibility(View.GONE);
                    sub_categories[3].setVisibility(View.GONE);

                    sub_categories[0].setTextColor(Color.WHITE);
                    views[0].setVisibility(View.VISIBLE);
                    for (int j = 1; j < 4; j++) {
                        sub_categories[j].setTextColor(Color.parseColor("#B6B6B6"));
                        views[j].setVisibility(View.INVISIBLE);
                    }
                    country = "in";
                    loadCustomNews(country, "technology");

                    for (int i = 0; i < 2; i++) {
                        int x = i;
                        sub_categories[i].setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                TextView subCat = (TextView) v;
                                subCat.setTextColor(Color.WHITE);
                                subCat.setTypeface(subCat.getTypeface(), Typeface.BOLD);
                                if (x == 0) {
                                    views[0].setVisibility(View.VISIBLE);
                                    for (int j = 1; j < 4; j++) {
                                        sub_categories[j].setTextColor(Color.parseColor("#B6B6B6"));
                                        views[j].setVisibility(View.INVISIBLE);
                                    }
                                    country = "in";
                                    loadCustomNews(country, "technology");
                                }

                                if (x == 1) {
                                    views[1].setVisibility(View.VISIBLE);
                                    for (int j = 0; j < 4; j++) {
                                        if (j != 1) {
                                            sub_categories[j].setTextColor(Color.parseColor("#B6B6B6"));
                                            views[j].setVisibility(View.INVISIBLE);
                                        }
                                    }
                                    country = "us";
                                    loadCustomNews(country,"technology");
                                }
                                return true;
                            }
                        });
                    }
                    break;
            }
        }
    };

    public void loadNews(final String keyword, final String country){

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<News> call;
        if(keyword.length() > 0){
            call = apiInterface.getNewsSearch(keyword,"en", "publishedAt", API_KEY);
        } else {
            call = apiInterface.getNews(country, API_KEY);
        }
        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if(response.isSuccessful() && response.body().getArticles() != null){

                    if(!articles.isEmpty())
                        articles.clear();

                    articles = response.body().getArticles();
                    SharedPreferences newsPoints = getSharedPreferences("newsPoints", Context.MODE_PRIVATE);
                    np = newsPoints.getInt("np", 0);
                    adapter = new Adapter(articles, NewsActivity.this, viewPager2, theme, np);
                    viewPager2.setAdapter(adapter);

                    refreshLayout.setRefreshing(false);

                } else {
                    Toast.makeText(NewsActivity.this, "No Article!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
            }
        });
    }

    public void loadCustomNews(final String country, final String category){

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<News> call;
        call = apiInterface.getCustomNews(country, category, API_KEY);
        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if(response.isSuccessful() && response.body().getArticles() != null){

                    if(!articles.isEmpty())
                        articles.clear();

                    articles = response.body().getArticles();
                    SharedPreferences newsPoints = getSharedPreferences("newsPoints", Context.MODE_PRIVATE);
                    np = newsPoints.getInt("np", 0);
                    adapter = new Adapter(articles, NewsActivity.this, viewPager2, theme, np);
                    viewPager2.setAdapter(adapter);

                } else {
                    Toast.makeText(NewsActivity.this, "No Article!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent i;
        switch (id) {
            case R.id.news:
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.weather:
                i = new Intent(this, WeatherActivity.class);
                startActivity(i);
                finish();
                break;
            case R.id.profile:
                i = new Intent(this, ProfileActivity.class);
                startActivity(i);
                break;
            case R.id.settings:
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
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