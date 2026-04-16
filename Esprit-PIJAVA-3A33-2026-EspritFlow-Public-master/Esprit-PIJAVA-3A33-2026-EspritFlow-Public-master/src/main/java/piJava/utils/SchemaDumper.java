package piJava.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

public class SchemaDumper {
    public static void main(String[] args) {
        try {
            Connection conn = MyDataBase.getInstance().getConnection();
            if (conn != null) {
                DatabaseMetaData metaData = conn.getMetaData();
                dumpTable(metaData, "salle");
                dumpTable(metaData, "seance");
            } else {
                System.out.println("No connection.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void dumpTable(DatabaseMetaData metaData, String tableName) throws Exception {
        System.out.println("Table: " + tableName);
        ResultSet columns = metaData.getColumns(null, null, tableName, null);
        while (columns.next()) {
            String colName = columns.getString("COLUMN_NAME");
            String colType = columns.getString("TYPE_NAME");
            int colSize = columns.getInt("COLUMN_SIZE");
            System.out.println("  - " + colName + " (" + colType + ")");
        }
    }
}
