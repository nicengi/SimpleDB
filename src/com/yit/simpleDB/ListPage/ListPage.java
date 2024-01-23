package com.yit.simpleDB.ListPage;

import java.util.ArrayList;
import java.util.List;

public class ListPage {
    private static final String TB_LEFT_TOP = "┌";
    private static final String TB_LEFT_BOTTOM = "└";
    private static final String TB_LEFT_CROSS = "├";

    private static final String TB_RIGHT_TOP = "┐";
    private static final String TB_RIGHT_BOTTOM = "┘";
    private static final String TB_RIGHT_CROSS = "┤";

    private static final String TB_LINE_H = "─";
    private static final String TB_LINE_V = "│";
    private static final String TB_CROSS = "┼";
    private static final String TB_TOP_CROSS = "┬";
    private static final String TB_BOTTOM_CROSS = "┴";

    private List<ColumnHeader> columnHeaders = new ArrayList<>();
    private List<ListItem> listItems = new ArrayList<>();

    public ListPage() {
    }

    public List<ColumnHeader> getColumnHeaders() {
        return columnHeaders;
    }

    public List<ListItem> getListItems() {
        return listItems;
    }

    public void show() {
        printTableLine(TB_LEFT_TOP, TB_TOP_CROSS, TB_RIGHT_TOP, columnHeaders);

        for (ColumnHeader column : columnHeaders) {
            System.out.printf("%s%-" + column.getWidth() + "s", TB_LINE_V, " " + listItemTextHandle(column.getText(), column.getWidth()));
        }
        System.out.println(TB_LINE_V);

        printTableLine(TB_LEFT_CROSS, TB_CROSS, TB_RIGHT_CROSS, columnHeaders);

        for (ListItem item : listItems) {
            ColumnHeader column = columnHeaders.get(0);

            System.out.printf("%s%-" + column.getWidth() + "s", TB_LINE_V, " " + listItemTextHandle(item.getText(), column.getWidth()));

            for (int j = 0; (j < columnHeaders.size() && j < item.getSubItems().size()); j++) {
                ListItem subItem = item.getSubItems().get(j);
                column = columnHeaders.get(j + 1);

                System.out.printf("%s%-" + column.getWidth() + "s", TB_LINE_V, " " + listItemTextHandle(subItem.getText(), column.getWidth()));
            }

            System.out.println(TB_LINE_V);
        }

        printTableLine(TB_LEFT_BOTTOM, TB_BOTTOM_CROSS, TB_RIGHT_BOTTOM, columnHeaders);
    }

    private static void printTableLine(String start, String middle, String end, List<ColumnHeader> columnHeaders) {
        System.out.print(start);
        for (int i = 0; i < columnHeaders.size(); i++) {
            ColumnHeader column = columnHeaders.get(i);
            String line = "";

            for (int j = 0; j < column.getWidth(); j++) {
                line += TB_LINE_H;
            }

            if (i < columnHeaders.size() - 1) {
                line += middle;
            }

            System.out.print(line);
        }
        System.out.println(end);
    }

    private static String listItemTextHandle(String text, int columnWidth) {
        if (text.length() + 1 > columnWidth) {
            text = text.substring(0, columnWidth - 6);
            text += "...";
        }
        return text;
    }
}

