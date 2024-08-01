package com.cronapp.cronnewsparserapp;

import com.cronapp.cronnewsparserapp.repos.Impls.NewsRepoImpl;
import com.cronapp.cronnewsparserapp.scraping.NewsScraper;
import com.cronapp.cronnewsparserapp.services.NewsService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class NewsApp extends Application {
    private static NewsApp instance;

    public static NewsApp getInstance() {
        return instance;
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    NewsService newsService = new NewsService(NewsRepoImpl.getInstance());

    @Override
    public void start(Stage stage) {
        instance = this;
        // Main window shown on desktop
        Platform.runLater(() -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(NewsApp.class.getResource("main_view.fxml"));
                VBox root = fxmlLoader.load();
                Scene scene = new Scene(root, 1076, 800);

                // Add the loading image view to the scene's root layout
                stage.setTitle("News Application");
                stage.getIcons().add(new Image("/icon.png"));
                stage.setScene(scene);
                stage.setResizable(false);
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Call scheduling method synchronously
        scheduling();

        // Schedule a task to fetch the latest news every minute
        scheduler.scheduleAtFixedRate(this::scheduling, 0, 5, TimeUnit.MINUTES);
    }

    // Create Timer task to fetch news from website in a given time range
    private void scheduling() {
            NewsScraper newsScraper = new NewsScraper();
            newsService.saveNewsList(newsScraper.fetchNewsData());
    }


    public static void main(String[] args) {
        launch();
    }
}