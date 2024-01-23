package com.yit.simpleDB.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Table {
    private String name;
    private ColumnNode column;
    private int columnCount = 0;
    private List<List<String>> row = new ArrayList<>();

    private TableIndex index;

    public TableIndex getIndex() {
        return index;
    }

    public void setIndex(TableIndex index) {
        this.index = index;
    }

    public Table(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ColumnNode getColumn() {
        return column;
    }

    public List<List<String>> getRow() {
        return row;
    }

    public int columnSize() {
        return columnCount;
    }

    public int rowSize() {
        return row.size();
    }

    public void addColumn(ColumnNode newColumn) {
        if (column == null) {
            column = newColumn;
        } else {
            ColumnNode node = column;
            for (int i = 0; i < columnCount - 1; i++) {
                node = node.getNext();
            }
            node.setNext(newColumn);
        }
        columnCount++;
    }

//    public int removeColumn(String key) {
//        //TODO:移除数据表列
//        int i = 1;
//        int j = 0;
//        ColumnNode node = column.getNext(); // q=p->next
//        if( i<=0 )
//        {
//            return -1;
//        }
//        while (j < i-1 && node != null)
//        {
//            j++;
//            node=column.getNext();
//        }
//        if(column.getNext()==null)
//        {
//            return -1;
//        }
//        else
//        {
//            if(node == column.getNext())
//            {
//                if(column.getNext()==null)
//                {
//                    return -1;
//                }
//                column.getNext()=
//            }
//        }
//    }

    public int findColumn(String name) {
        int i = 0;
        ColumnNode colNode = getColumn();

        while (colNode != null && !Objects.equals(colNode.getName(), name)) {
            colNode = colNode.getNext();
            i++;
        }

        if (colNode == null) {
            return -1;
        }

        return i;
    }

    /*
    * 使用指定字段从Hash表中查找行。返回符合条件的行索引列表。
    * */
    public List<Integer> findRow(String field, String key) {
        if(index.getMaps().containsKey(field))
        {
           return index.getMap(field).get(key);
        }
        return null;
    }

    /*
    * 二分查找
    * 根据数据表id查找指定的行。返回符合条件的行索引。
    * */
    public int findRow(int key) {
        int low = 0;
        int high = row.size() - 1;
        int mid;
        while (low <= high) {
            mid = (low + high) / 2;
            if (Integer.parseInt(row.get(mid).get(0)) == key) {
                return mid;
            } else if (Integer.parseInt(row.get(mid).get(0)) > key) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return -1;
    }
}
