package com.yit.simpleDB.Data;

public class ColumnNode {
    private String type;
    private String name;
    private ColumnNode next;

    public ColumnNode(String type, String name, ColumnNode next) {
        this.type = type;
        this.name = name;
        this.next = next;
    }

    public ColumnNode(String type, String name) {
        this(type, name, null);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ColumnNode getNext() {
        return next;
    }

    public void setNext(ColumnNode next) {
        this.next = next;
    }
}
