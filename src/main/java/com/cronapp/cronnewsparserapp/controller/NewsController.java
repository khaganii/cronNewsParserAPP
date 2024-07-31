package com.cronapp.cronnewsparserapp.controller;

import com.cronapp.cronnewsparserapp.domains.NewsEntity;
import com.cronapp.cronnewsparserapp.repos.Impls.NewsRepoImpl;
import com.cronapp.cronnewsparserapp.scraping.NewsScraper;
import com.cronapp.cronnewsparserapp.services.NewsService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;


public class NewsController  implements Initializable {
    private int currentPage;
    private int currentItem;

    private List<NewsEntity> currentNewsList;

    public ListView<String> newsListView;

    public ImageView newsImageView;
    public TextArea newsContentArea;
    public Label pageLbl;
    public Label titleLbl;
    public Label publishedDateLbl;
    public Label categoryLbl;

    private final NewsService newsService = new NewsService(NewsRepoImpl.getInstance());
    private final int size = 15;
    @FXML
    private DatePicker datePicker;

    @FXML
    protected void onDatePickerChange() {
        currentItem = 0;
        currentPage =1;
        loadNewsForSelectedDate(currentPage);
        pageLbl.setText("Current Page: " + currentPage);
        newsListView.getSelectionModel().select(currentItem);
    }

    @FXML
    public void onLoadNextPageButtonClick(){
        currentPage++;
        loadNewsForSelectedDate(currentPage);
        pageLbl.setText("Current Page: " + currentPage);
        currentItem = 0;
        newsListView.getSelectionModel().select(currentItem);
    }

    @FXML
    public void onNextButtonClick(){
        if(currentItem < currentNewsList.size()-1) currentItem++;
        newsListView.getSelectionModel().select(currentItem);
        setNews();
    }

    @FXML
    public void onPreviousButtonClick(){
        if(currentItem > 0) currentItem--;
        newsListView.getSelectionModel().select(currentItem);
        setNews();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        datePicker.setValue(LocalDate.now());
        datePicker.getEditor().setDisable(true);
        newsContentArea.setWrapText(true);
        newsContentArea.setStyle("-fx-font-size: 12pt;");
        currentPage = 1;
        currentItem = 0;
        loadNewsForSelectedDate(currentPage);
        pageLbl.setText("Current Page: " + currentPage);
        newsListView.getSelectionModel().select(currentItem);

        newsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { //on double click
                currentItem = newsListView.getSelectionModel().getSelectedIndex();
                setNews();
            }
        });
    }

    private void setNews(){
        //                downloadAndReplaceImage(newsList.get(0).getHeaderImageUrl(), "/flower.jpg");
        newsContentArea.setText(currentNewsList.get(currentItem).getTextContent());
        titleLbl.setText(currentNewsList.get(currentItem).getTitle());
        categoryLbl.setText(currentNewsList.get(currentItem).getCategory());
        publishedDateLbl.setText(getFormattedDateTime(currentNewsList.get(currentItem).getPublicationTime()));

    }

    private void loadNewsForSelectedDate(int page) {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate != null) {
            currentNewsList = newsService.findAllPaginatedByDate(selectedDate, page, size);
            newsListView.getItems().clear();
            for (NewsEntity news : currentNewsList) {
                newsListView.getItems().add(getListedNews(news)); // Adjust if you want to display something else
            }
            if(currentNewsList.size() > 0){
                try {
                    loadImage(currentNewsList.get(0).getHeaderImageUrl());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                setNews();
            }
        }
    }

    public void forceToGetLatestNews(){
        NewsScraper newsScraper = new NewsScraper();
        newsService.saveNewsList(newsScraper.fetchNewsData());
        currentPage=1;
        loadNewsForSelectedDate(currentPage);
    }

    public void loadImage(String imageUrl) throws IOException {
        Image image = null;
        try {
            URL url = new URL(imageUrl);
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("User-Agent", "Chrome/126.0.6478.183");
            conn.connect();
            try (InputStream urlStream = conn.getInputStream()) {
                image = new Image(urlStream, 400, 240, true, true);
                newsImageView.setImage(image);
            }
        } catch (IOException e) {
            System.out.println("Something went wrong, sorry:" + e.toString());
            e.printStackTrace();
        }
    }

//    public void downloadAndReplaceImage(String imageUrl, String outputFilePath) {
//        InputStream inputStream = null;
//        FileOutputStream outputStream = null;
//
//        try {
//            // Open a connection to the image URL
//            URL url = new URL(imageUrl);
//            URLConnection connection = url.openConnection();
//            connection.setRequestProperty("User-Agent", "Chrome/126.0.6478.183");
//
//            // Get input stream from the connection
//            inputStream = connection.getInputStream();
//
//            // Create output file (replace if exists)
//            File outputFile = new File(outputFilePath);
//            outputStream = new FileOutputStream(outputFile);
//
//            // Buffer for data chunks
//            byte[] buffer = new byte[4096];
//            int bytesRead;
//
//            // Write data to the file
//            while ((bytesRead = inputStream.read(buffer)) != -1) {
//                outputStream.write(buffer, 0, bytesRead);
//            }
//
//            System.out.println("Image downloaded and replaced successfully!");
//
//        } catch (IOException e) {
//            System.err.println("Error downloading or saving the image: " + e.getMessage());
//            e.printStackTrace();
//        } finally {
//            // Close streams
//            try {
//                if (inputStream != null) {
//                    inputStream.close();
//                }
//                if (outputStream != null) {
//                    outputStream.close();
//                }
//            } catch (IOException e) {
//                System.err.println("Error closing streams: " + e.getMessage());
//                e.printStackTrace();
//            }
//        }
//        newsImageView.setImage(new Image("/flower.jpg",400, 240, true, true));
//    }

    public String getListedNews(NewsEntity news) {
        return String.format("%s\t|\t%s\t|\t(%s)", news.getTitle(), news.getCategory().toUpperCase(), getFormattedDateTime(news.getPublicationTime()));
    }

    public String getFormattedDateTime(Timestamp timestamp){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm");
        return timestamp.toLocalDateTime().format(formatter);
    }
}