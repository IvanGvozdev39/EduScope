package com.eduscope.eduscope.notes;

import java.util.Date;

public class FirebaseModel {
    private String title, content, color;
    private Date date;


    public FirebaseModel() {
    }

    public FirebaseModel(String title, String content, String color) {
        this.title = title;
        this.content = content;
        this.color = color;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }
}