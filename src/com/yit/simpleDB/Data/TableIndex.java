package com.yit.simpleDB.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TableIndex implements Serializable {

    private final HashMap<String, HashMap<String, List<Integer>>> maps = new HashMap<>();

    public HashMap<String, HashMap<String, List<Integer>>> getMaps() {
        return maps;
    }

    /*
    * 查找指定字段的索引Hash表，字段索引不存在时将创建。
    * */
    public HashMap<String, List<Integer>> getMap(String key) {
        if (maps.containsKey(key)) {
            return maps.get(key);
        }
        maps.put(key, new HashMap<>());
        return maps.get(key);
    }
}
