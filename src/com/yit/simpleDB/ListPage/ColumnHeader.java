package com.yit.simpleDB.ListPage;

public class ColumnHeader {
    private String text;
    private int width;

    public ColumnHeader() {
        width = 20;
    }

    public ColumnHeader(String text, int width) {
        this.text = text;
        this.width = width;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}

