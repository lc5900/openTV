package com.lc5900.tv.model;

/**
 * Created by liuchun on 2017/11/29.
 */

public class TvMenu {
    private int id;
    private String name;
    private String[] urls;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getUrls() {
        return urls;
    }

    public void setUrls(String[] urls) {
        this.urls = urls;
    }
}
