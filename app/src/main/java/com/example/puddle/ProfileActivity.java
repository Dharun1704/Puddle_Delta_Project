package com.example.puddle;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.collection.ArraySet;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.puddle.Adapters.BookmarkAdapter;
import com.example.puddle.NewsModel.Article;
import com.example.puddle.NewsModel.Source;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private ViewPager2 bookmarkLayout;
    private NestedScrollView nestedScrollView;
    private CompositePageTransformer pageTransformer;
    private BookmarkAdapter adapter;
    private NewsDatabase newsDb;
    private LinearLayout dvlModePassword, dvlModeMain;
    private DatabaseReference reference;

    TextView appTitle, userLvl, newsPoint, newsTheme, noBookmarkFound, bookmarksHD, userDisplay;
    RelativeLayout NewsThemeLayout;
    EditText dvlPassword, dvlCustomNp;
    ImageButton deleteBookmark;

    ArrayAdapter<String> theme;
    ArrayList<Article> bookmarkArticle;
    String[] fTheme;
    int np;
    int selected[] = {0};
    boolean isDeveloper, isBookmarkLayoutOn = false;

    private static final String TAG = "ProfileActivity";
    private ArrayList<Article> firebaseBookmarks;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        SharedPreferences userProfileName = getSharedPreferences("userProfileName", MODE_PRIVATE);
        String user = userProfileName.getString("userName", "");
        reference = FirebaseDatabase.getInstance().getReference("Users").child(user);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#5098A9"));
        window.setNavigationBarColor(Color.parseColor("#205888"));

        toolbar.setBackgroundColor(Color.parseColor("#5098A9"));

        appTitle = findViewById(R.id.appName);
        appTitle.setText("Puddle");
        appTitle.setTextColor(Color.WHITE);

        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);
        userDisplay = navigationView.getHeaderView(0).findViewById(R.id.userDisplayText);
        userDisplay.setText(user);

        drawerLayout = findViewById(R.id.profile_layout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        newsDb = new NewsDatabase(this);
        nestedScrollView = findViewById(R.id.nestedProfileView);
        dvlModePassword = findViewById(R.id.developerModePassword);
        dvlModeMain = findViewById(R.id.developerModeMain);
        NewsThemeLayout = findViewById(R.id.newsTheme);
        newsTheme = findViewById(R.id.newsThemeText);
        userLvl = findViewById(R.id.profile_level);
        newsPoint = findViewById(R.id.profile_np);
        dvlPassword = findViewById(R.id.developerPassword);
        dvlCustomNp = findViewById(R.id.customNP);

        noBookmarkFound = findViewById(R.id.noBookmarks);
        deleteBookmark = findViewById(R.id.deleteBookmark);
        bookmarksHD = findViewById(R.id.bookmarks_hd);
        bookmarkLayout = findViewById(R.id.bookmark_news_slider);
        bookmarkLayout.setClipToPadding(false);
        bookmarkLayout.setClipChildren(false);
        bookmarkLayout.setOffscreenPageLimit(3);
        bookmarkLayout.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        pageTransformer = new CompositePageTransformer();
        pageTransformer.addTransformer(new MarginPageTransformer(40));
        pageTransformer.addTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float r = 1 - Math.abs(position);
                page.setScaleY(0.85f + r * 0.15f);
            }
        });
        bookmarkLayout.setPageTransformer(pageTransformer);
        bookmarkLayout.setSaveFromParentEnabled(false);

        nestedScrollView.setVisibility(View.VISIBLE);

        SharedPreferences developer = getSharedPreferences("Developer", Context.MODE_PRIVATE);
        isDeveloper = developer.getBoolean("isDeveloper", false);

        if (isDeveloper) {
            dvlModeMain.setVisibility(View.VISIBLE);
        }
        else {
            dvlModeMain.setVisibility(View.GONE);
            dvlModePassword.setVisibility(View.GONE);
        }

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                np = snapshot.child("np").getValue(Integer.class);
                assignLevel(np);
                Log.i(TAG, "onCreate: " + np);
                newsPoint.setText(String.valueOf(np));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        assignLevel(np);
        Log.i(TAG, "onCreate2: " + np);
        newsPoint.setText(String.valueOf(np));

        fTheme = new String[2];
        fTheme[0] = "Violet-Blue";
        fTheme[1] = "Dark";

        SharedPreferences sNewsTheme = getSharedPreferences("sNewsTheme", MODE_PRIVATE);
        fTheme[0] = sNewsTheme.getString("fNewsTheme", "Violet-Blue");

        newsTheme.setText(fTheme[0]);

        SharedPreferences Selected = getSharedPreferences("NewsTheme", MODE_PRIVATE);
        selected[0] = Selected.getInt("OptionSelectedNewsTheme", 0);

        theme = new ArrayAdapter<>(ProfileActivity.this, R.layout.dialog_item);
        theme.add("Violet-Blue");
        theme.add("Deep Sea");
        theme.add("Dark");

        NewsThemeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences Selected = getSharedPreferences("NewsTheme", MODE_PRIVATE);
                selected[0] = Selected.getInt("OptionSelectedNewsTheme", 0);

                new AlertDialog.Builder(ProfileActivity.this)
                        .setTitle("Select News Theme")
                        .setSingleChoiceItems(theme, selected[0], new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                selected[0] = which;

                                SharedPreferences Selected = getSharedPreferences("NewsTheme", MODE_PRIVATE);
                                SharedPreferences.Editor editor = Selected.edit();
                                editor.putInt("OptionSelectedNewsTheme", selected[0]);
                                editor.apply();
                            }
                        })
                        .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                fTheme[0] = theme.getItem(selected[0]);

                                SharedPreferences sNewsTheme = getSharedPreferences("sNewsTheme", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sNewsTheme.edit();
                                editor.putString("fNewsTheme", fTheme[0]);
                                editor.apply();

                                newsTheme.setText(fTheme[0]);
                                checkLvlFeatures();
                            }
                        })
                        .show();
            }

        });

        dvlCustomNp.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId2, KeyEvent event2) {
                if (actionId2 == EditorInfo.IME_ACTION_SEARCH || actionId2 == EditorInfo.IME_ACTION_DONE ||
                        event2 != null &&
                                event2.getAction() == KeyEvent.ACTION_DOWN &&
                                event2.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (event2 == null || !event2.isShiftPressed()) {

                        np = Integer.parseInt(view.getText().toString());
                        reference.child("np").setValue(np);
                        newsPoint.setText(String.valueOf(np));
                        assignLevel(np);
                        dvlCustomNp.setText("");
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isBookmarkLayoutOn) {
            noBookmarkFound.setVisibility(View.GONE);
            bookmarkLayout.setVisibility(View.GONE);
            bookmarksHD.setVisibility(View.GONE);
            deleteBookmark.setVisibility(View.GONE);
            nestedScrollView.setVisibility(View.VISIBLE);
        }
        else {
            super.onBackPressed();
        }
    }

    private void assignLevel(int nps) {
        if (nps >= 0 && nps <= 50) {
            userLvl.setText("Noob Master");
        }
        else if (nps > 50 && nps <= 100) {
            userLvl.setText("Reader");
        }
        else if (nps > 100 && nps <= 300) {
            userLvl.setText("Front Bencher");
        }
        else if (nps > 300 && nps <= 500) {
            userLvl.setText("Prodigy");
        }
        else if (nps > 500 ) {
            userLvl.setText("News Wizard");
        }
    }

    private void checkLvlFeatures() {
        String s = newsTheme.getText().toString();
        if (np < 51 && s.equals("Deep Sea")) {
            Toast.makeText(ProfileActivity.this, "Theme unavailable for this level. Current theme set is the latest theme available", Toast.LENGTH_LONG).show();
            selected[0] = 0;
            SharedPreferences Selected = getSharedPreferences("NewsTheme", MODE_PRIVATE);
            SharedPreferences.Editor editorS = Selected.edit();
            editorS.putInt("OptionSelectedNewsTheme", selected[0]);
            editorS.apply();
            fTheme[0] = theme.getItem(selected[0]);
            SharedPreferences sNewsTheme = getSharedPreferences("sNewsTheme", MODE_PRIVATE);
            SharedPreferences.Editor editorT = sNewsTheme.edit();
            editorT.putString("fNewsTheme", fTheme[0]);
            editorT.apply();

            newsTheme.setText(fTheme[0]);
        }

        if (np < 51 && s.equals("Dark")) {
            Toast.makeText(ProfileActivity.this, "Theme unavailable for this level. Current theme set is the latest theme available", Toast.LENGTH_LONG).show();
            selected[0] = 0;
            SharedPreferences Selected = getSharedPreferences("NewsTheme", MODE_PRIVATE);
            SharedPreferences.Editor editorS = Selected.edit();
            editorS.putInt("OptionSelectedNewsTheme", selected[0]);
            editorS.apply();
            fTheme[0] = theme.getItem(selected[0]);
            SharedPreferences sNewsTheme = getSharedPreferences("sNewsTheme", MODE_PRIVATE);
            SharedPreferences.Editor editorT = sNewsTheme.edit();
            editorT.putString("fNewsTheme", fTheme[0]);
            editorT.apply();

            newsTheme.setText(fTheme[0]);
        }

        if (np > 50 && np < 101 && s.equals("Dark")) {
            Toast.makeText(ProfileActivity.this, "Theme unavailable for this level. Current theme set is the latest theme available", Toast.LENGTH_LONG).show();
            selected[0] = 1;
            SharedPreferences Selected = getSharedPreferences("NewsTheme", MODE_PRIVATE);
            SharedPreferences.Editor editorS = Selected.edit();
            editorS.putInt("OptionSelectedNewsTheme", selected[0]);
            editorS.apply();
            fTheme[0] = theme.getItem(selected[0]);
            SharedPreferences sNewsTheme = getSharedPreferences("sNewsTheme", MODE_PRIVATE);
            SharedPreferences.Editor editorT = sNewsTheme.edit();
            editorT.putString("fNewsTheme", fTheme[0]);
            editorT.apply();

            newsTheme.setText(fTheme[0]);
        }

    }

    private void setInFirebase(int np) {
        reference.child("np").setValue(np);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
        }

        else if (item.getItemId() == R.id.profile_reset) {
            np = 0;
            reference.child("np").setValue(np);
            newsPoint.setText(String.valueOf(np));
            isDeveloper = false;
            SharedPreferences developer = getSharedPreferences("Developer", Context.MODE_PRIVATE);
            SharedPreferences.Editor editorD = developer.edit();
            editorD.putBoolean("isDeveloper", isDeveloper);
            editorD.apply();
            selected[0] = 0;
            SharedPreferences Selected = getSharedPreferences("NewsTheme", MODE_PRIVATE);
            SharedPreferences.Editor editorS = Selected.edit();
            editorS.putInt("OptionSelectedNewsTheme", selected[0]);
            editorS.apply();
            fTheme[0] = theme.getItem(selected[0]);
            SharedPreferences sNewsTheme = getSharedPreferences("sNewsTheme", MODE_PRIVATE);
            SharedPreferences.Editor editorT = sNewsTheme.edit();
            editorT.putString("fNewsTheme", fTheme[0]);
            editorT.apply();

            newsTheme.setText(fTheme[0]);
            Intent i = getIntent();
            startActivity(i);
            finish();
        }

        else if (item.getItemId() == R.id.developMode) {
            dvlModePassword.setVisibility(View.VISIBLE);
            dvlPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE ||
                            event != null &&
                                    event.getAction() == KeyEvent.ACTION_DOWN &&
                                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        if (event == null || !event.isShiftPressed()) {
                            String password = v.getText().toString();
                            if (password.equals("puddle")) {
                                checkDeveloperPassword();
                            }
                            else {
                                Snackbar.make(drawerLayout, "Incorrect Password", Snackbar.LENGTH_LONG).show();
                                dvlModePassword.setVisibility(View.GONE);
                            }
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        else if (item.getItemId() == R.id.viewBookmarks) {
            isBookmarkLayoutOn = true;
            nestedScrollView.setVisibility(View.GONE);

            getDataFromDatabase();

            deleteBookmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = bookmarkLayout.getCurrentItem();
                    reference.child("Bookmarks").child(String.valueOf(position)).removeValue();
                    bookmarkLayout.setCurrentItem(position, true);
                }
            });

        }

        else if (item.getItemId() == R.id.userLogOut) {
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        return true;
    }

    private void checkDeveloperPassword() {
        isDeveloper = true;
        SharedPreferences developer = getSharedPreferences("Developer", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorD = developer.edit();
        editorD.putBoolean("isDeveloper", isDeveloper);
        editorD.apply();

        dvlModePassword.setVisibility(View.GONE);
        dvlModeMain.setVisibility(View.VISIBLE);

        dvlCustomNp.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId2, KeyEvent event2) {
                if (actionId2 == EditorInfo.IME_ACTION_SEARCH || actionId2 == EditorInfo.IME_ACTION_DONE ||
                        event2 != null &&
                                event2.getAction() == KeyEvent.ACTION_DOWN &&
                                event2.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (event2 == null || !event2.isShiftPressed()) {

                        np = Integer.parseInt(view.getText().toString());
                        Log.i(TAG, "onEditorAction: " + np);
                        setInFirebase(np);
                        newsPoint.setText(String.valueOf(np));
                        assignLevel(np);
                        dvlCustomNp.setText("");
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void getDataFromDatabase() {
        firebaseBookmarks = new ArrayList<>();
        DatabaseReference bookmarkRef = reference.child("Bookmarks");
        bookmarkRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
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

                if (firebaseBookmarks.size() == 0) {
                    noBookmarkFound.setVisibility(View.VISIBLE);
                    bookmarkLayout.setVisibility(View.GONE);
                    deleteBookmark.setVisibility(View.GONE);
                    bookmarksHD.setVisibility(View.GONE);
                } else {
                    noBookmarkFound.setVisibility(View.GONE);
                    bookmarksHD.setVisibility(View.VISIBLE);
                    deleteBookmark.setVisibility(View.VISIBLE);
                    bookmarkLayout.setVisibility(View.VISIBLE);
                    adapter = new BookmarkAdapter(ProfileActivity.this, firebaseBookmarks);
                    bookmarkLayout.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
                i = new Intent(this, WeatherActivity.class);
                startActivity(i);
                finish();
                break;
            case R.id.profile:
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.settings:
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
