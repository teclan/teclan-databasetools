package database.tools;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.javalite.activejdbc.DB;

import database.tools.service.MenuService;

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

    private static Console console = System.console();

    public static void main(String[] args) {

        setDriverAndUrl();

        setDbInfo();

        setFilePath();

        openDatabase();

        handle();

        closeDatabase();

    }

    private static void setDriverAndUrl() {
        int choose = 0;
        do {
            MenuService.showMenu();
            try {
                choose = Integer.valueOf(console.readLine("选择数据库类型: "));
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

    }

    private static void setDbInfo() {
        do {
            host = console.readLine("主机: ");
            port = console.readLine("端口: ");
            dbName = console.readLine("数据库名: ");
            schema = console.readLine("模式: ");
            userName = console.readLine("用户: ");
            password = console.readLine("密码: ");

            System.out.println(String.format(
                    "数据库信息：主机:%s\t端口:%s\t数据库名:%s\t模式:%s\t用户:%s\t密码:%s", host,
                    port, dbName, schema, userName, password));

            confirm = console.readLine("确认? (Y/N): ");
        } while ("N".equals(confirm) || "n".equals(confirm));
    }

    private static void setFilePath() {
        do {
            filePath = console.readLine("文件路径 :");
            if (new File(filePath).exists()) {
                break;
            } else {
                System.out.println("文件不存在，请重新选择！");
            }
        } while (true);
    }

    private static void handle() {

        // String table = console
        // .readLine("基础表名 (users1,users2,...usersn,则基础表名为 users):");
        // int count = Integer.valueOf(console.readLine("表个数，(从 1
        // 开始到小于等于表个数):"));

        String sql = console.readLine("sql tmplate :");

        try {

            new DB(connect).exec(sql, 1, readFileByBytes(new File(filePath)));
        } catch (Exception e) {
            System.out.println(e);
        }
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
