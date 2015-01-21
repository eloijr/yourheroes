package com.runze.yourheroes.db;

import java.io.Serializable;

/**
 * Created by Eloi Jr on 03/01/2015.
 */
public class Person implements Serializable {

    private int id;
    private String name;
    private String description;
    private String URLDetail;
    private String landscapeSmallImageUrl;
    private String standardXLargeImageUrl;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getURLDetail() {
        return URLDetail;
    }

    public void setURLDetail(String URLDetail) {
        this.URLDetail = URLDetail;
    }

    public String getLandscapeSmallImageUrl() {
        return landscapeSmallImageUrl;
    }

    public void setLandscapeSmallImageUrl(String landscapeSmallImageUrl) {
        this.landscapeSmallImageUrl = landscapeSmallImageUrl;
    }

    public String getStandardXLargeImageUrl() {
        return standardXLargeImageUrl;
    }

    public void setStandardXLargeImageUrl(String standardXLargeImageUrl) {
        this.standardXLargeImageUrl = standardXLargeImageUrl;
    }

}
