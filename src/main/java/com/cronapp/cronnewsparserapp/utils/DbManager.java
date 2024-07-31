package com.cronapp.cronnewsparserapp.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class DbManager {

    public static Connection getConnection(){
        try {
            List<String> props = PropertiesReader.read();
            return DriverManager.getConnection(props.get(0), props.get(1), props.get(2));
        } catch (SQLException e) {

            throw new RuntimeException(e);
        }
    }
}
