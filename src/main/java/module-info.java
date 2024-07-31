module com.cronapp.cronnewsparserapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.naming;
    requires lombok;
    requires java.sql;
    requires org.jsoup;
    opens com.cronapp.cronnewsparserapp to javafx.fxml;
    exports com.cronapp.cronnewsparserapp;
    exports com.cronapp.cronnewsparserapp.controller;
    exports com.cronapp.cronnewsparserapp.services;
    exports com.cronapp.cronnewsparserapp.domains;
    exports com.cronapp.cronnewsparserapp.scraping;
    opens com.cronapp.cronnewsparserapp.controller to javafx.fxml;
}