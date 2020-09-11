package com.example.puddle;

import com.example.puddle.NewsModel.Article;

import java.util.ArrayList;

public class User {

    private String email;
    private String username;
    private String password;
    private int np;
    private ArrayList<Article> bookmarks;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getNp() {
        return np;
    }

    public void setNp(int np) {
        this.np = np;
    }

    public ArrayList<Article> getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(ArrayList<Article> bookmarks) {
        this.bookmarks = bookmarks;
    }
}
