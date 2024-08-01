package com.cronapp.cronnewsparserapp.controller;

import com.cronapp.cronnewsparserapp.NewsApp;
import com.cronapp.cronnewsparserapp.domains.NewsEntity;
import com.cronapp.cronnewsparserapp.repos.Impls.NewsRepoImpl;
import com.cronapp.cronnewsparserapp.scraping.NewsScraper;
import com.cronapp.cronnewsparserapp.services.NewsService;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import javax.imageio.ImageIO;

public class NewsController implements Initializable {
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
    public VBox mainVBox;
    public ImageView loadingGif;
    @FXML
    public Hyperlink postUrlLink;

    private final NewsService newsService = new NewsService(NewsRepoImpl.getInstance());
    private final int size = 15;
    private int lastListSize = size;
    @FXML
    private DatePicker datePicker;

    @FXML
    protected void onDatePickerChange() {
        currentItem = 0;
        currentPage = 1;
        loadNewsForSelectedDate(currentPage);
        pageLbl.setText("Current Page: " + currentPage);
        newsListView.getSelectionModel().select(currentItem);
    }

    @FXML
    public void onLoadNextPageButtonClick() {
        if (lastListSize == size) currentPage++;
        loadNewsForSelectedDate(currentPage);
        pageLbl.setText("Current Page: " + currentPage);
        currentItem = 0;
        newsListView.getSelectionModel().select(currentItem);
        lastListSize = currentNewsList.size();
    }

    @FXML
    public void onLoadPreviousPageButtonClick() {
        if (currentPage > 1) currentPage--;
        loadNewsForSelectedDate(currentPage);
        pageLbl.setText("Current Page: " + currentPage);
        currentItem = 0;
        newsListView.getSelectionModel().select(currentItem);
    }

    @FXML
    public void onNextButtonClick() {
        if (currentItem < currentNewsList.size() - 1) currentItem++;
        newsListView.getSelectionModel().select(currentItem);
        setNews();
    }

    @FXML
    public void onPreviousButtonClick() {
        if (currentItem > 0) currentItem--;
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

    private void setNews() {
        downloadAndReplaceImage(currentNewsList.get(currentItem).getHeaderImageUrl());
        setImage();
        newsContentArea.setText(currentNewsList.get(currentItem).getTextContent());
        titleLbl.setText(currentNewsList.get(currentItem).getTitle());
        categoryLbl.setText(currentNewsList.get(currentItem).getCategory());
        publishedDateLbl.setText(getFormattedDateTime(currentNewsList.get(currentItem).getPublicationTime()));
        postUrlLink.setText(currentNewsList.get(currentItem).getPostUrl());
        postUrlLink.setOnAction(event -> {
            NewsApp.getInstance().getHostServices().showDocument(currentNewsList.get(currentItem).getPostUrl());
        });
    }

    private void setImage() {
        String imagePath = "C:\\Users\\kabdullayev\\Desktop\\apps\\CronNewsParserApp\\src\\main\\resources\\temp.jpg";
        Image image = new Image(imagePath, 300, 240, true, true);
        newsImageView.setImage(image);
        newsImageView.setFitWidth(300);
        newsImageView.setFitHeight(240);
        newsImageView.setPreserveRatio(false);
    }

    private void loadNewsForSelectedDate(int page) {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate != null) {
            currentNewsList = newsService.findAllPaginatedByDate(selectedDate, page, size);
            newsListView.getItems().clear();
            for (NewsEntity news : currentNewsList) {
                newsListView.getItems().add(getListedNews(news)); // Adjust if you want to display something else
            }
            if (currentNewsList.size() > 0) {
                downloadAndReplaceImage(currentNewsList.get(currentItem).getHeaderImageUrl());
                setNews();
            }
        }
    }

    public void forceToGetLatestNews() {
        datePicker.setValue(LocalDate.now());
        loadingGif.setImage(new Image("loading.gif"));
        mainVBox.setDisable(true);
        loadingGif.setVisible(true);
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                NewsScraper newsScraper = new NewsScraper();
                newsService.saveNewsList(newsScraper.fetchNewsData());
                currentPage = 1;
                currentItem = 0;
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                loadNewsForSelectedDate(currentPage);
                mainVBox.setDisable(false);
                loadingGif.setVisible(false);
            }

            @Override
            protected void failed() {
                super.failed();
                mainVBox.setDisable(false);
                loadingGif.setVisible(true);
            }
        };

        new Thread(task).start();
    }

    public void downloadAndReplaceImage(String imageUrl) {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;

        try {
            // Open a connection to the image URL
            URL url = new URL(imageUrl);
            URLConnection connection = url.openConnection();
            // Uncomment if needed for specific user-agent requirements
           // connection.setRequestProperty("User-Agent", "Chrome/126.0.6478.183");

            // Get input stream from the connection
            inputStream = connection.getInputStream();

            // Create output file (replace if exists)
            File outputFile = new File("C:\\Users\\kabdullayev\\Desktop\\apps\\CronNewsParserApp\\src\\main\\resources\\temp.webp");
            outputStream = new FileOutputStream(outputFile);

            // Buffer for data chunks
            byte[] buffer = new byte[4096];
            int bytesRead;

            // Write data to the file
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            File jpgFile = new File("C:\\Users\\kabdullayev\\Desktop\\apps\\CronNewsParserApp\\src\\main\\resources\\temp.jpg");
            if (!jpgFile.exists()) {
                jpgFile.createNewFile();
            }
            convertWebPToJPG(outputFile.getPath(), "C:\\Users\\kabdullayev\\Desktop\\apps\\CronNewsParserApp\\src\\main\\resources\\temp.jpg");
        } catch (IOException e) {
            System.err.println("Error downloading or saving the image: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close streams
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing streams: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void convertWebPToJPG(String webpPath, String jpgPath) {
        try {
            BufferedImage webpImage = ImageIO.read(new File(webpPath));
            ImageIO.write(webpImage, "jpg", new File(jpgPath));
        } catch (IOException e) {
            System.err.println("Error converting image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String getListedNews(NewsEntity news) {
        return String.format("%s\t|\t%s\t|\t(%s)", news.getTitle(), news.getCategory().toUpperCase(), getFormattedDateTime(news.getPublicationTime()));
    }

    public String getFormattedDateTime(Timestamp timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm");
        return timestamp.toLocalDateTime().format(formatter);
    }
}