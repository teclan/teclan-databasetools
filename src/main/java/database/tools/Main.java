package database.tools;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.javalite.activejdbc.DB;

import database.tools.service.MenuService;
import teclan.utils.FileUtils;

public class Main {

    private static String driver;
    private static String url;

    private static String host;
    private static String port;
    private static String dbName;
    private static String schema;
    private static String userName;
    private static String password;

    private static String confirm = "Y";
    private static String connect = "default";
    private static String filePath;

    private static String baseTable;
    private static int    tableCount;
    private static int    countPerTable;

    private static int seq;

    private static Console console = System.console();

    public static void main(String[] args) {

        seq = setDriverAndUrl();

        setDbInfo();

        openDatabase();

        handle();

        closeDatabase();

    }

    private static int setDriverAndUrl() {
        int choose = 0;
        do {
            MenuService.showMenu();
            try {
                choose = Integer.valueOf(console.readLine("选择数据库类型: "));

                if (choose > 0 && choose < 6) {
                    break;
                }
            } catch (Exception e) {
                System.out.println("无效选择，请重新操作！");
            }
        } while (choose == 0 || choose > 6);

        switch (choose) {
            case 1:
                driver = "oracle.jdbc.driver.OracleDriver";
                url = "jdbc:oracle:thin:@%s:%d:%s";
                break;

            case 2:
                driver = "dm.jdbc.driver.DmDriver";
                url = "jdbc:dm://%s:%d/%s";
                break;

            case 3:
                driver = "com.mysql.jdbc.Driver";
                url = "jdbc:mysql://%s:%d/%s";
                break;

            case 4:
                driver = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
                url = "jdbc:microsoft:sqlserver://%s:%d;DatabaseName=%s";
                break;

            case 5:

                driver = "COM.ibm.db2os390.sqlj.jdbc.DB2SQLJDriver";
                url = "jdbc:db2://%s:%s/%s";
                break;

            default:
                System.out.println("无效选择！");
                break;
        }

        return choose;

    }

    private static void setDbInfo() {
        do {
            host = console.readLine("主机 >> ");
            port = console.readLine("端口 >> ");
            dbName = console.readLine("数据库名 >> ");
            schema = console.readLine("模式 >> ");
            userName = console.readLine("用户 >> ");
            password = console.readLine("密码 >> ");

            confirm = console.readLine("确认? (Y/N) >> ");
        } while ("N".equals(confirm.toUpperCase()));
    }

    private static File setFilePath() {
        do {
            filePath = console.readLine("文件路径 >>");
            if (new File(filePath).exists()) {
                break;
            } else {
                System.out.println("文件不存在，请重新选择！");
            }
        } while (true);

        return new File(filePath);
    }

    private static void handle() {
        int choose = 0;

        do {

            do {
                System.out.println("========= 选择操作 ============");
                System.out.println("1、自定义sql");
                System.out.println("2、批量插入数据");
                try {
                    choose = Integer.valueOf(console.readLine("请选择 >> "));
                    if (choose > 0 && choose < 3) {
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("无效选择，请重新操作！");
                }
            } while (true);

            if (choose == 1) {
                excuteSql();
            } else if (choose == 2) {
                insert();
            }

            confirm = console.readLine("是否退出? (Y/N) >> ");

            if ("Y".equals(confirm.toUpperCase())) {
                return;
            }

        } while (true);

    }

    private static void excuteSql() {

        String sql = console.readLine("输入sql,语句最后不需要分号 >> ");

        try {
            new DB(connect).exec(sql);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static void insert() {

        setFilePath();

        baseTable = console.readLine("基础表名 >> ");
        tableCount = Integer.valueOf(console.readLine("表个数 >> "));
        countPerTable = Integer.valueOf(console.readLine("每个表多少条记录 >> "));

        String[] content = FileUtils.getContent(new File("sql/insert.sql"))
                .split(",");

        String[] columns = new String[content.length];
        Object[] values = new Object[content.length];

        List<String> paramaters = new ArrayList<String>();

        for (int i = 0; i < content.length; i++) {
            paramaters.add("?");
        }

        for (int m = 1; m <= tableCount; m++) {

            for (int n = 1; n <= countPerTable; n++) {

                for (int j = 0; j < content.length; j++) {
                    String[] tmp = content[j].split("#");

                    if (tmp.length < 2) {
                        System.out.println("sql/insert.sql 配置错误，缺少字段，在第 " + j
                                + 1 + " 行附近");
                        return;
                    }

                    columns[j] = tmp[0];

                    if ("time".equals(tmp[1].toLowerCase())) {
                        values[j] = new Date().getTime();
                    } else if ("file".equals(tmp[1].toLowerCase())) {
                        values[j] = readFileByBytes(new File(filePath));
                    } else if ("i".equals(tmp[1].toLowerCase())) {
                        values[j] = n;
                    } else {
                        values[j] = tmp[1];
                    }
                }

                try {

                    // DB2 and mysql
                    if (seq == 3 || seq == 5) {
                        new DB(connect).exec(
                                "insert into " + schema + "." + baseTable + m
                                        + " (" + String.join(",", columns)
                                        + ") values ("
                                        + String.join(",", paramaters) + ")",
                                values);

                    } else {
                        new DB(connect).exec(
                                "insert into \"" + schema + "\".\"" + baseTable
                                        + m + "\" (" + String.join(",", columns)
                                        + ") values ("
                                        + String.join(",", paramaters) + ")",
                                values);
                    }

                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }

        System.out.println("执行完毕！");
    }

    private static byte[] readFileByBytes(File file) {
        InputStream in = null;
        byte[] bytes = new byte[(int) file.length()];
        try {
            in = new FileInputStream(file);
            in.read(bytes);
            in.close();
            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                }
            }
        }
    }

    private static boolean openDatabase() {
        try {
            new DB(connect).open(driver,
                    String.format(url, host, Integer.valueOf(port), dbName),
                    userName, password);
            System.out.println("数据库连接成功!");
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    private static void closeDatabase() {
        new DB(connect).close();
        System.out.println("数据库关闭成功!");
    }

}
