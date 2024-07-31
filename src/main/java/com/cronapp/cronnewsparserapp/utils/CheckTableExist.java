package com.cronapp.cronnewsparserapp.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class CheckTableExist {
    public static boolean check() {
        String tableName = "news"; // The table name you want to check
        boolean exist = false;
        try (Connection connection = DbManager.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            // Check if the table exists
            try (var resultSet = metaData.getTables(null, null, tableName, null)) {
                if (resultSet.next()) 
                    exist = true;
                else 
                    exist = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exist;
    }
}
