package com.cronapp.cronnewsparserapp.repos.Impls;

import com.cronapp.cronnewsparserapp.domains.NewsEntity;
import com.cronapp.cronnewsparserapp.repos.NewsRepo;
import com.cronapp.cronnewsparserapp.utils.CheckTableExist;
import com.cronapp.cronnewsparserapp.utils.DbManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class NewsRepoImpl implements NewsRepo {

    private static NewsRepoImpl instance;
    // Private constructor to prevent instantiation
    private NewsRepoImpl() {

    }
    // Synchronized method to control simultaneous access
    public static synchronized NewsRepoImpl getInstance() {
        if (instance == null) {
            instance = new NewsRepoImpl();
        }
        return instance;
    }

    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS news (" +
                    "    uuid VARCHAR(255) NOT NULL PRIMARY KEY," +
                    "    category VARCHAR(255) NOT NULL," +
                    "    title VARCHAR(255) NOT NULL," +
                    "    post_url VARCHAR(1024) NOT NULL," + // assuming URL might be longer
                    "    header_image_url VARCHAR(1024)," +  // assuming URL might be longer
                    "    text_content TEXT," +
                    "    html_content TEXT," +
                    "    publication_time DATETIME NOT NULL" +
                    ");";

    private void checkTableExist(){
        if(!CheckTableExist.check()){
            try (Connection connection = DbManager.getConnection();
                 Statement statement = connection.createStatement()) {

                // Execute the SQL statement to create the table
                statement.executeUpdate(CREATE_TABLE_SQL);
                System.out.println("tried to create a table");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void save(NewsEntity news) {
        checkTableExist();

        final String SELECT_EXISTING_SQL = "SELECT COUNT(*) FROM news WHERE uuid = ?";
        final String INSERT_NEWS_SQL = "INSERT INTO news (uuid, category, title, post_url, header_image_url, text_content, html_content, publication_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection connection = DbManager.getConnection()) {
            // Check if the record already exists
            try (PreparedStatement selectStmt = connection.prepareStatement(SELECT_EXISTING_SQL)) {
                selectStmt.setString(1, news.getUuid());
                ResultSet rs = selectStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.printf("News with UUID: {%s} already exists. Skipping insertion.%n", news.getUuid());
                    return; // Skip the insert if the record already exists
                }
            }

            // Insert the new record
            try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_NEWS_SQL)) {
                preparedStatement.setString(1, news.getUuid());
                preparedStatement.setString(2, news.getCategory());
                preparedStatement.setString(3, news.getTitle());
                preparedStatement.setString(4, news.getPostUrl());
                preparedStatement.setString(5, news.getHeaderImageUrl());
                preparedStatement.setString(6, news.getTextContent());
                preparedStatement.setString(7, news.getHtmlContent());
                preparedStatement.setTimestamp(8, news.getPublicationTime());

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.printf("News inserted successfully with UUID: {%s}%n", news.getUuid());
                }
            }
        } catch (SQLException e) {
            System.out.printf("Failed to save news with UUID: {%s}%n", news.getUuid());
        }
    }

    @Override
    public List<NewsEntity> findAllPaginated(int page, int size) {
        List<NewsEntity> newsList = new ArrayList<>();
        int offset = (page - 1) * size;
        String sql = "SELECT uuid, category, title, post_url, header_image_url, text_content, html_content, publication_time " +
                "FROM news " +
                "ORDER BY publication_time DESC " +
                "LIMIT ? OFFSET ?";

        try (Connection connection = DbManager.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, size);
            ps.setInt(2, offset);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                NewsEntity news = NewsEntity.builder()
                        .uuid(rs.getString("uuid"))
                        .category(rs.getString("category"))
                        .title(rs.getString("title"))
                        .postUrl(rs.getString("post_url"))
                        .headerImageUrl(rs.getString("header_image_url"))
                        .textContent(rs.getString("text_content"))
                        .htmlContent(rs.getString("html_content"))
                        .publicationTime(rs.getTimestamp("publication_time"))
                        .build();
                newsList.add(news);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newsList;
    }

    @Override
    public NewsEntity findById(String uuid) {
        String sql = "SELECT uuid, category, title, post_url, header_image_url, text_content, html_content, publication_time " +
                "FROM news WHERE uuid = ?";
        NewsEntity newsEntity = null;

        try (Connection connection = DbManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                newsEntity = NewsEntity.builder()
                        .uuid(rs.getString("uuid"))
                        .category(rs.getString("category"))
                        .title(rs.getString("title"))
                        .postUrl(rs.getString("post_url"))
                        .headerImageUrl(rs.getString("header_image_url"))
                        .textContent(rs.getString("text_content"))
                        .htmlContent(rs.getString("html_content"))
                        .publicationTime(rs.getTimestamp("publication_time"))
                        .build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newsEntity;
    }


    @Override
    public List<NewsEntity> findAllPaginatedByDate(LocalDate date, int page, int size) {
        List<NewsEntity> newsList = new ArrayList<>();
        int offset = (page - 1) * size;

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        String sql = "SELECT uuid, category, title, post_url, header_image_url, text_content, html_content, publication_time " +
                "FROM news " +
                "WHERE publication_time >= ? AND publication_time < ? " +
                "ORDER BY publication_time DESC " +
                "LIMIT ? OFFSET ?";

        try (Connection connection = DbManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(startOfDay));
            ps.setTimestamp(2, Timestamp.valueOf(endOfDay));
            ps.setInt(3, size);
            ps.setInt(4, offset);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                NewsEntity news = NewsEntity.builder()
                        .uuid(rs.getString("uuid"))
                        .category(rs.getString("category"))
                        .title(rs.getString("title"))
                        .postUrl(rs.getString("post_url"))
                        .headerImageUrl(rs.getString("header_image_url"))
                        .textContent(rs.getString("text_content"))
                        .htmlContent(rs.getString("html_content"))
                        .publicationTime(rs.getTimestamp("publication_time"))
                        .build();
                newsList.add(news);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newsList;
    }

}
