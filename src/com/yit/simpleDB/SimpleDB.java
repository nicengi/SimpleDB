package com.yit.simpleDB;

import com.yit.simpleDB.Data.ColumnNode;
import com.yit.simpleDB.Data.Database;
import com.yit.simpleDB.Data.Table;
import com.yit.simpleDB.Data.TableIndex;
import com.yit.simpleDB.ListPage.ColumnHeader;
import com.yit.simpleDB.ListPage.ListItem;
import com.yit.simpleDB.ListPage.ListPage;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.function.LongPredicate;

public class SimpleDB {
    public static final String CMD_HELP = "help";
    public static final String CMD_CREATE = "create";
    public static final String PARAM_CREATE_DB = "database";
    public static final String PARAM_CREATE_TABLE = "table";
    public static final String PARAM_CREATE_FIELD = "field";
    public static final String CMD_OPEN = "open";
    public static final String PARAM_OPEN_WITH_TABLE = "-t";
    public static final String CMD_COMMIT = "commit";
    public static final String CMD_INSERT = "insert";
    public static final String PARAM_INSERT_INTO = "into";
    public static final String PARAM_INSERT_VALUES = "values";
    public static final String CMD_DELETE = "delete";
    public static final String PARAM_DELETE_WHERE = "where";
    public static final String CMD_UPDATE = "update";
    public static final String PARAM_UPDATE_SET = "set";
    public static final String PARAM_UPDATE_WHERE = "set";
    public static final String CMD_SELECT = "select";
    public static final String PARAM_SELECT_WHERE = "where";
    public static final String CMD_DISPLAY = "display";
    public static final String PARAM_DISPLAY_DATABASES = "databases";
    public static final String PARAM_DISPLAY_TABLE = "table";
    public static final String PARAM_DISPLAY_FIELDS = "fields";
    public static final String CMD_QUIT = "quit";
    public static final String CMD_EXIT = "exit";

    public static final String APP_NAME = "SimpleDB";
    public static final String APP_VERSION = "[Version 1.0.1225]";
    public static final String WorkDirectory = "./data";

    private Map<String, Function<String[], Integer>> commands;

    // 活动的数据库路径
    private String activeDatabasePath = "";

    // 活动的数据库对象
    private Database activeDatabase;

    // 活动的数据表对象
    private Table activeTable;

    public SimpleDB() {


    }

    private void updateActivePath() {
        String path = "";
        if (activeDatabase != null) {
            path += activeDatabase.getName();
        }

        if (activeTable != null) {
            path += "@";
            path += activeTable.getName();
        }
        activeDatabasePath = path;
    }

    public void run() {
        // 初始化操作
        File file = new File(WorkDirectory);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                System.out.printf("初始化时发生异常：未能创建目录'%s'%n", WorkDirectory);
            }
        }

        commands = new HashMap<>() {{
            put(CMD_HELP, args -> {
//                System.out.println("有关某个命令的详细信息，请键入 HELP 命令名");
                System.out.println(CMD_HELP + "     显示帮助");
                System.out.println(CMD_OPEN + "     打开数据库或表");
                System.out.println("    OPEN [database_name|table_name]");
                System.out.println();
                System.out.println(CMD_CREATE + "   创建数据库、表或字段");
                System.out.println("    CREATE [DATABASE|TABLE|FIELD] name[:field_type]");
                System.out.println();
                System.out.println(CMD_COMMIT + "   提交对数据库的更改，写入到文件");
                System.out.println(CMD_INSERT + "   插入数据");
                System.out.println("    INSERT INTO field_name1,field_name2,... VALUES value1,value2,...");
                System.out.println();
                System.out.println(CMD_DELETE + "   删除数据");
                System.out.println("    DELETE WHERE field_name1=value1");
                System.out.println();
                System.out.println(CMD_UPDATE + "   更新数据");
                System.out.println("    UPDATE SET field_name1,field_name2,... VALUES value1,value2,...");
                System.out.println();
                System.out.println(CMD_SELECT + "   查询数据");
                System.out.println("    SELECT WHERE field_name1=value1");
                System.out.println();
                System.out.println(CMD_DISPLAY + "  列出数据库、数据表中的字段或数据");
                System.out.println("    DISPLAY [DATABASES|TABLE|FIELDS]");
                System.out.println();
                System.out.println(CMD_QUIT + " 返回到上一级");
                System.out.println(CMD_EXIT + " 退出程序");
                System.out.println();

                return 0;
            });

            put(CMD_CREATE, args -> {
                if (!checkArgCount(args, 3)) {
                    return 0;
                }

                String param = args[1].toLowerCase();
                String name;

                switch (param) {
                    case PARAM_CREATE_TABLE:
                        name = args[2];

                        if (name.isEmpty()) {
                            return 0;
                        }

                        File tbFile = new File(String.format("%s/%s/%s.sdb", WorkDirectory, activeDatabase.getName(), name));
                        if (!tbFile.exists()) {
                            try {
                                if (!tbFile.createNewFile()) {
                                    throw new IOException();
                                }
                            } catch (IOException e) {
                                System.out.println("创建数据表失败");
                                return 0;
                            }
                            activeTable = new Table(name);
                            activeTable.setIndex(new TableIndex());
                            activeDatabase.getTables().add(activeTable);
                        } else {
                            System.out.printf("数据表'%s'已存在%n", name);
                        }
                        break;

                    case PARAM_CREATE_FIELD:
                        name = args[2];
                        if (activeTable == null) {
                            System.out.println("无法创建字段，因为没有活动的数据表");
                            return 0;
                        }

                        for (int i = 2; i < args.length; i++) {
                            String[] fieldStr = args[i].split(":");
                            if (fieldStr.length != 2) {
                                System.out.println("'" + args[i] + "'不是有效的参数");
                                return 0;
                            }
                            activeTable.addColumn(new ColumnNode(fieldStr[1], fieldStr[0]));
                            // 给添加的列插入默认值 NULL
                            for (List<String> row : activeTable.getRow()) {
                                row.add("NULL");
                            }
                        }
                        break;

                    case PARAM_CREATE_DB:
                        name = args[2];

                        if (name.isEmpty()) {
                            return 0;
                        }

                        if (activeDatabase != null) {
                            System.out.println("无法创建数据库，请先关闭活动的数据库");
                            return 0;
                        }

                        File dbFile = new File(String.format("%s/%s", WorkDirectory, name));
                        if (!dbFile.exists()) {
                            if (!dbFile.mkdirs()) {
                                System.out.println("创建数据库失败");
                                return 0;
                            }
                            activeDatabase = new Database(name);
                        } else {
                            System.out.printf("数据库'%s'已存在%n", name);
                        }
                        break;
                }

                updateActivePath();
                return 0;
            });

            put(CMD_OPEN, args -> {
                if (!checkArgCount(args, 2)) {
                    return 0;
                }

                String name = args[1];

                if (activeDatabase != null && activeTable != null) {
                    System.out.printf("'%s'已被打开，请键入 QUIT 命令退出%n", activeDatabasePath);
                    return 0;
                }

                if (activeDatabase == null) {
                    File file = new File(String.format("%s/%s", WorkDirectory, name));
                    if (file.exists()) {
                        activeDatabase = new Database(name);
                        if (args.length > 3 && Objects.equals(args[2], PARAM_OPEN_WITH_TABLE)) {
                            String tableName = args[3];
                            String line = CMD_OPEN + " " + tableName;
                            commands.get(CMD_OPEN).apply(line.split(" "));
                        }
                    } else {
                        System.out.printf("数据库'%s'不存在%n", name);
                    }
                } else {
                    File file = new File(String.format("%s/%s/%s.sdb", WorkDirectory, activeDatabase.getName(), name));
                    if (file.exists()) {
                        for (Table table :
                                activeDatabase.getTables()) {
                            if (table.getName().equals(name)) {
                                // 查找数据表是否已经打开
                                activeTable = table;
                                updateActivePath();
                                return 0;
                            }
                        }

                        activeTable = new Table(name);
                        String dbName = activeDatabase.getName();

                        File indexFile = new File(String.format("%s/%s/%s.idx", WorkDirectory, activeDatabase.getName(), name));
//                        String indexFile = String.valueOf(Paths.get(WorkDirectory, dbName, name + ".idx"));
                        if (indexFile.exists()) {
                            try {
                                TableIndex tableIndex = (TableIndex) Util.ObjectInput(indexFile.toString());
                                activeTable.setIndex(tableIndex);
                                activeDatabase.getTables().add(activeTable);
                            } catch (Exception e) {
                                activeTable.setIndex(new TableIndex());
                                activeDatabase.getTables().add(activeTable);
                                System.out.println("读取数据表索引文件失败，将重新建立索引");
                                System.out.println(e.getMessage());
                                System.out.println();
                            }
                        } else {
                            activeTable.setIndex(new TableIndex());
                            activeDatabase.getTables().add(activeTable);
                            System.out.printf("没有找到数据表'%s'的索引文件，将重新建立索引%n", name);
                            System.out.println();
                        }

                        String tableFile = String.valueOf(Paths.get(WorkDirectory, dbName, name + ".sdb"));
                        try (BufferedReader reader = new BufferedReader(new FileReader(tableFile))) {
                            String line;

                            while ((line = reader.readLine()) != null) {
                                if (line.isEmpty() || line.startsWith("--")) {
                                    // 忽略空行和--注释行
                                    continue;
                                }
                                executeCommand(line);
                            }
                        } catch (Exception e) {
                            activeTable = null;
                            System.out.println("读取数据表文件失败");
                            System.out.println(e.getMessage());
                        }
                    } else {
                        System.out.printf("数据表'%s'不存在%n", name);
                    }
                }

                updateActivePath();
                return 0;
            });

            put(CMD_COMMIT, args -> {
                if (activeDatabase == null) {
                    System.out.println("没有打开的数据库");
                    return 0;
                }

                String dbName = activeDatabase.getName();
                for (Table table :
                        activeDatabase.getTables()) {
                    String tableFile = String.valueOf(Paths.get(WorkDirectory, dbName, table.getName() + ".sdb"));
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(tableFile))) {
                        writer.write("--" + APP_NAME + " " + APP_VERSION);
                        writer.newLine();
                        writer.newLine();

                        writer.write("--表的结构'" + table.getName() + "'");
                        writer.newLine();
                        ColumnNode column = table.getColumn();
                        for (int i = 0; i < table.columnSize(); i++) {
                            String line = "CREATE FIELD " + column.getName() + ":" + column.getType();
                            writer.write(line);
                            writer.newLine();
                            column = column.getNext();
                        }

                        writer.newLine();
                        writer.write("--表中的数据");
                        writer.newLine();

                        column = table.getColumn();
                        StringBuilder fields = new StringBuilder(column.getName());
                        for (int i = 1; i < table.columnSize(); i++) {
                            column = column.getNext();
                            fields.append(",").append(column.getName());
                        }

                        for (int i = 0; i < table.rowSize(); i++) {
                            List<String> row = table.getRow().get(i);
                            StringBuilder values = new StringBuilder(row.get(0));
                            for (int j = 1; j < row.size(); j++) {
                                values.append(",").append(row.get(j));
                            }

                            String line = "INSERT INTO " + fields.toString() + " VALUES " + values.toString();
                            writer.write(line);
                            writer.newLine();
                        }

                        writer.flush();
                    } catch (IOException e) {
                        System.out.println("提交失败");
                        System.out.println(e.getMessage());
                        System.out.println(Arrays.toString(e.getStackTrace()));
                    }

                    try {
                        // 写出索引文件
                        String indexFile = String.valueOf(Paths.get(WorkDirectory, dbName, table.getName() + ".idx"));
                        Util.ObjectOutput(table.getIndex(), indexFile);
                    } catch (Exception e) {
                        System.out.println("保存索引失败");
                        System.out.println(e.getMessage());
                        System.out.println(Arrays.toString(e.getStackTrace()));
                    }

                }

                return 0;
            });

            put(CMD_INSERT, args -> {
                if (!checkArgCount(args, 5)) {
                    return 0;
                }

                String paramInto = args[1];
                if (!paramInto.equalsIgnoreCase(PARAM_INSERT_INTO)) {
                    System.out.println("'" + paramInto + "'不是有效的命令");
                    return 0;
                }

                String paramValues = args[3];
                if (!paramValues.equalsIgnoreCase(PARAM_INSERT_VALUES)) {
                    System.out.println("'" + paramValues + "'不是有效的命令");
                    return 0;
                }

                String[] fields = args[2].split(",");
                String[] values = args[4].split(",");

                if (fields.length != values.length) {
                    System.out.println("字段数量必须与值的数量相同");
                    return 0;
                }

                ArrayList<String> row = new ArrayList<>(activeTable.columnSize());
                for (int i = 0; i < activeTable.columnSize(); i++) {
                    //填充占位符
                    row.add("NULL");
                }
                for (int i = 0; i < fields.length; i++) {
                    String field = fields[i];
                    int columnIndex = activeTable.findColumn(field);

                    if (columnIndex == -1) {
                        System.out.println("'" + field + "'未找到字段，插入失败");
                        return 0;
                    }

                    row.set(columnIndex, values[i]);

// 添加索引
String indexKey = values[i];
HashMap<String, List<Integer>> indexMap = activeTable.getIndex().getMap(field);
if (indexMap != null) {
    if (indexMap.containsKey(indexKey)) {
        if (!indexMap.get(indexKey).contains(activeTable.rowSize())) {
            /*
             * 如果存在当前 indexKey 的索引，且索引中没有当前行
             * */
            indexMap.get(indexKey).add(activeTable.rowSize());
        }
    } else {
        indexMap.put(indexKey, new ArrayList<>(List.of(activeTable.rowSize())));
    }
//                        indexMap.get(indexKey).sort(Collections.reverseOrder());
    Collections.sort(indexMap.get(indexKey));
                    }
                }
                activeTable.getRow().add(row);

                return 0;
            });

            put(CMD_DELETE, args -> {
                if (!checkArgCount(args, 3)) {
                    return 0;
                }

                String paramWhere = args[1];
                if (!paramWhere.equalsIgnoreCase(PARAM_DELETE_WHERE)) {
                    System.out.println("'" + paramWhere + "'不是有效的命令");
                    return 0;
                }

                String[] cond = args[2].split("=");
                if (checkArgCount(cond, 2)) {
                    String condField = cond[0];
                    String condKey = cond[1];
                    int rowCount = 0;
                    List<Integer> rowIndexes = activeTable.findRow(condField, condKey);
                    if (rowIndexes != null && !rowIndexes.isEmpty()) {
                        List<Integer> rowIndexesCopy = new ArrayList<>(rowIndexes);
//                        List<String> columnNames = new ArrayList<>();
//
//                        ColumnNode column = activeTable.getColumn();
//                        for (int i = 0; i < activeTable.columnSize(); i++) {
//                            columnNames.add(column.getName());
//                            column = column.getNext();
//                        }

                        // 从Hash表中删除索引
//                        for (int i = 0; i < rowIndexesCopy.size(); i++) {
//                            Integer index = rowIndexes.get(i);
//                            List<String> row = activeTable.getRow().get(index);
//
//                            for (int j = 0; j < row.size(); j++) {
//                                String columnName = columnNames.get(j);
//                                String rowValue = row.get(j);
//
//                                if (activeTable.getIndex().getMaps().containsKey(columnName)) {
//                                    HashMap<String, List<Integer>> indexMap = activeTable.getIndex().getMap(columnName);
//                                    if (indexMap.containsKey(rowValue)) {
//                                        List<Integer> indexes = indexMap.get(rowValue);
//                                        indexes.remove(index);
//                                    }
//                                }
//                            }
//                        }

                        /*
                         * 删除表中的行，因为先删除前面的行会影响到后面行的位置。
                         * 所以要把 rowIndexes 降序排列
                         * 好像不需要了，因为索引直接重建了
                         * */
                        rowIndexesCopy.sort(Collections.reverseOrder());
                        for (Integer index : rowIndexesCopy) {
                            activeTable.getRow().remove((int) index);
                        }

                        /*
                         * 重建索引
                         * */
                        buildIndexes();
                        rowCount = rowIndexesCopy.size();
                    }
                    System.out.println(rowCount + " 条数据已被删除");
                }
                return 0;
            });

            put(CMD_UPDATE, args -> {
                //UPDATE SET id,name VALUES 100,TOMCAT WHERE id=100

                if (!checkArgCount(args, 7)) {
                    return 0;
                }

                String paramSet = args[1];
                if (!paramSet.equalsIgnoreCase(PARAM_UPDATE_SET)) {
                    System.out.println("'" + paramSet + "'不是有效的命令");
                    return 0;
                }

                String paramWhere = args[1];
                if (!paramWhere.equalsIgnoreCase(PARAM_UPDATE_WHERE)) {
                    System.out.println("'" + paramWhere + "'不是有效的命令");
                    return 0;
                }

                String[] cond = args[6].split("=");
                if (checkArgCount(cond, 2)) {
                    String condField = cond[0];
                    String condKey = cond[1];
                    String[] updateValues = args[4].split(",");
                    String[] updateFields = args[2].split(",");


                    int rowCount = 0;
                    List<Integer> rowIndexes = activeTable.findRow(condField, condKey);
                    if (rowIndexes != null) {
                        for (Integer rowIndex : rowIndexes) {
                            List<String> row = activeTable.getRow().get(rowIndex);
                            for (int i = 0; i < updateFields.length; i++) {
                                String updateField = updateFields[i];
                                for (int j = 0; j < row.size(); j++) {
                                    int fieldIndex = activeTable.findColumn(updateField);
                                    if (fieldIndex == j) {
                                        row.set(j, updateValues[i]);
                                    }
                                }
                            }
                        }
                        buildIndexes();
                        rowCount = rowIndexes.size();
                    }

                    System.out.println(rowCount + " 条数据被更新");
                    System.out.println();
                }
                return 0;
            });

            put(CMD_SELECT, args -> {
//                int idx = activeTable.findRow(800);
//                ArrayList<String> row = activeTable.getRow().get(idx);

                if (!checkArgCount(args, 3)) {
                    return 0;
                }

                String paramWhere = args[1];
                if (!paramWhere.equalsIgnoreCase(PARAM_SELECT_WHERE)) {
                    System.out.println("'" + paramWhere + "'不是有效的命令");
                    return 0;
                }
                //Select Where id=100

                ListPage listPage = new ListPage();

                ColumnNode column = activeTable.getColumn();
                for (int i = 0; i < activeTable.columnSize(); i++) {
                    listPage.getColumnHeaders().add(new ColumnHeader(column.getName(), 20));
                    column = column.getNext();
                }

                String[] cond = args[2].split("=");
                if (checkArgCount(cond, 2)) {
                    String condField = cond[0];
                    String condKey = cond[1];
                    int rowCount = 0;
                    List<Integer> rowIndexes = activeTable.findRow(condField, condKey);
                    if (rowIndexes != null) {
                        for (Integer index : rowIndexes) {
                            List<String> row = activeTable.getRow().get(index);
                            ListItem listItem = new ListItem(row.get(0));
                            for (int i = 1; i < row.size(); i++) {
                                ListItem subItem = new ListItem(row.get(i));
                                listItem.getSubItems().add(subItem);
                            }
                            listPage.getListItems().add(listItem);
                        }
                        rowCount = rowIndexes.size();
                    }

                    listPage.show();
                    System.out.println(rowCount + " 条数据");
                    System.out.println();
                }
                return 0;
            });

            put(CMD_DISPLAY, args -> {

                if (!checkArgCount(args, 2)) {
                    return 0;
                }

//                String param = args.length > 1 ? args[1].toLowerCase() : PARAM_DISPLAY_TABLE;
                String param = args[1];
                switch (param) {
                    case PARAM_DISPLAY_DATABASES:
                        if (activeDatabase == null) {
                            ListPage listPageDatabaseList = new ListPage();
                            listPageDatabaseList.getColumnHeaders().add(new ColumnHeader("Databases", 20));

                            File dbDir = new File(WorkDirectory);
                            File[] files = dbDir.listFiles();
                            if (files != null) {
                                for (File file : files) {
                                    if (file.isDirectory()) {
                                        String fileName = file.getName();
                                        listPageDatabaseList.getListItems().add(new ListItem(fileName));
                                    }
                                }
                                listPageDatabaseList.show();
                                System.out.println();
                            }
                        }
                        break;
                    case PARAM_DISPLAY_FIELDS:
                        if (activeTable == null) {
                            System.out.println();
                            return 0;
                        }

                        ListPage listPageField = new ListPage();
                        listPageField.getColumnHeaders().add(new ColumnHeader("Field", 20));
                        listPageField.getColumnHeaders().add(new ColumnHeader("Type", 10));

                        ColumnNode column = activeTable.getColumn();
                        while (column != null) {
                            ListItem listItem = new ListItem(column.getName());
                            ListItem subItem = new ListItem(column.getType());
                            listItem.getSubItems().add(subItem);
                            listPageField.getListItems().add(listItem);
                            column = column.getNext();
                        }
                        listPageField.show();
                        System.out.println();
                        break;

                    case PARAM_DISPLAY_TABLE:
                        if (activeDatabase == null) {
                            return 0;
                        }

                        if (activeTable == null) {
                            ListPage listPageTableList = new ListPage();
                            listPageTableList.getColumnHeaders().add(new ColumnHeader("Tables", 20));

                            File dbDir = new File(String.format("%s/%s", WorkDirectory, activeDatabase.getName()));
                            File[] files = dbDir.listFiles();
                            if (files != null) {
                                for (File file : files) {
                                    if (file.isFile() && file.getName().endsWith(".sdb")) {
                                        String fileName = file.getName();
                                        listPageTableList.getListItems().add(new ListItem(fileName.substring(0, fileName.lastIndexOf("."))));
                                    }
                                }
                                listPageTableList.show();
                                System.out.println();
                            }
                        } else {
                            ListPage listPageTable = new ListPage();

                            ColumnNode tableColumn = activeTable.getColumn();
                            for (int i = 0; i < activeTable.columnSize(); i++) {
                                listPageTable.getColumnHeaders().add(new ColumnHeader(tableColumn.getName(), 20));
                                tableColumn = tableColumn.getNext();
                            }


                            for (int i = 0; i < activeTable.rowSize(); i++) {
                                List<String> row = activeTable.getRow().get(i);
                                ListItem listItem = new ListItem(row.get(0));
                                for (int j = 1; j < row.size(); j++) {
                                    ListItem subItem = new ListItem(row.get(j));
                                    listItem.getSubItems().add(subItem);
                                }
                                listPageTable.getListItems().add(listItem);
                            }
                            listPageTable.show();
                            System.out.println(activeTable.rowSize() + " 条数据");
                            System.out.println();
                        }
                        break;
                    default:
                        System.out.println("'" + args[1] + "'无效参数，请键入 HELP 查看可用的命令列表");
                        break;
                }
                return 0;
            });

            put(CMD_QUIT, args -> {
                if (activeTable != null) {
                    activeTable = null;
                } else if (activeDatabase != null) {
                    System.out.println("确实要关闭数据库吗？所有未提交的修改将会丢失（y/n）:");
                    Scanner scanner = new Scanner(System.in);
                    String line = scanner.nextLine();
                    if (!Objects.equals(line.toLowerCase(), "n")) {
                        activeDatabase.getTables().clear();
                        activeDatabase = null;
                    }
                }
                updateActivePath();
                return 0;
            });
        }};

        System.out.println(APP_NAME + " " + APP_VERSION);
        System.out.println();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print(APP_NAME + " " + activeDatabasePath + ">");
            String line = scanner.nextLine();

            if (line.isEmpty()) {
                continue;
            }

            if (!executeCommand(line)) {
                break;
            }
        }
    }

    private void buildIndexes() {
        activeTable.setIndex(new TableIndex());
        commands.get(CMD_COMMIT).apply(new String[0]);
        String line = CMD_OPEN + " " + activeDatabase.getName() + " -t " + activeTable.getName();
        activeTable = null;
        activeDatabase = null;
        commands.get(CMD_OPEN).apply(line.split(" "));
    }

    private boolean checkArgCount(String[] args, int minCount) {
        if (args.length < minCount) {
            System.out.println("'" + Arrays.toString(args) + "'不是有效的命令，请键入 HELP 查看可用的命令列表");
            return false;
        }
        return true;
    }

    private boolean executeCommand(String line) {
        String[] args = line.split(" ");
        String cmd = args[0].toLowerCase();

        if (Objects.equals(cmd, CMD_EXIT)) {
            // 检查 EXIT 命令，结束程序。
            return false;
        }

        try {
            commands.get(cmd).apply(args);
        } catch (NullPointerException e) {
            System.out.println("'" + cmd + "'不是有效的命令，请键入 HELP 查看可用的命令列表");
            System.out.println();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return true;
    }
}
