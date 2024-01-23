package com.yit.simpleDB.ListPage;

import java.util.ArrayList;
import java.util.List;

public class ListItem {
    private List<ListItem> subItems = new ArrayList<>();
    private String text;

    public ListItem() {
    }

    public ListItem(String text) {
        this.text = text;
    }

    public List<ListItem> getSubItems() {
        return subItems;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
