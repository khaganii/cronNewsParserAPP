package com.cronapp.cronnewsparserapp.utils;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PropertiesReader {
    public static List<String> read() {
        Properties properties = new Properties();
        List<String> props = new ArrayList<>();
        try (InputStream inputStream = PropertiesReader.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (inputStream == null) {
                throw new RuntimeException("Could not find application.properties");
            }

            // Load properties file
            properties.load(inputStream);

            // Access properties
            props.add(properties.getProperty("url"));
            props.add(properties.getProperty("username"));
            props.add(properties.getProperty("password"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return props;
    }
}