package com.cronapp.cronnewsparserapp.scraping;

import com.cronapp.cronnewsparserapp.domains.NewsEntity;
import com.cronapp.cronnewsparserapp.utils.UrlHasher;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NewsScraper {

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private String BASE_URL = "https://oxu.az";
    public record PostRecord(String postUrl, String headerImageURL, String textContent, String htmlContent) {
    }

    public List<NewsEntity> fetchNewsData() {
        List<NewsEntity> newsEntityList = new ArrayList<>();
        try {
            //get homepage news posts container div
            Document doc = Jsoup.connect(BASE_URL).get();
            Elements newsItems = doc.select(".index-post-block"); // Adjust the selector as needed

            //iterate news posts one by one
            for (Element newsItem : newsItems) {
                //get publishing date time of news
                String dateTimeString = newsItem.attr("data-timestamp");
                LocalDateTime dateTime = parseDateTime(dateTimeString);

                //get post title
                String title = newsItem.select(".post-item-title span[itemprop=headline]").text();
                //get post url and concatenate it with base url to access full url
                String postUrl = BASE_URL + Objects.requireNonNull(newsItem.select(".post-item-title a").first()).attr("href");
                //get news category  sport, politics, world and etc.
                String category = newsItem.select(".post-item-footer-left a.post-item-category").text();

                //Calling method to fetch news body and news by one news -> news information is in another page
                PostRecord postRecord = fetchNewsContent(postUrl);
                if (postRecord != null) {
                    NewsEntity news = NewsEntity.builder()
                            .uuid(UrlHasher.hashUrl(postUrl))
                            .category(category)
                            .title(title)
                            .postUrl(postUrl)
                            .publicationTime(Timestamp.valueOf(dateTime))
                            .headerImageUrl(postRecord.headerImageURL)
                            .htmlContent(postRecord.htmlContent)
                            .textContent(postRecord.textContent)
                            .build();
                    newsEntityList.add(news);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newsEntityList;
    }

    private LocalDateTime parseDateTime(String dateTimeString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        return LocalDateTime.parse(dateTimeString, formatter);
    }

    public PostRecord fetchNewsContent(String postUrl) {
        try {
            // Fetch and parse the HTML document
            Document doc = Jsoup.connect(postUrl).get();
            // Extract header image URL (assuming a correct selector is provided)
            String imageUrl = extractImageUrl(doc);
            // Extract the main content of the post
            Element content = doc.selectFirst(".post-detail-content-inner"); // Adjust the selector as needed
            // Check if content element is present
            if (content == null) {
                System.err.println("Content not found for URL: " + postUrl);
                return null;
            }
            // Get text content
            String textContent = content.text();
            // Remove unwanted tags like <script> and <style>
            content.select("script, style").remove();
            String htmlContent = content.html();

            return new PostRecord(postUrl, imageUrl, textContent, htmlContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String extractImageUrl(Document doc) {
        Elements headerImageElement = doc.select(".post-detail-img img"); // Assuming an img tag inside the element
        if (headerImageElement.isEmpty()) return "";
        return headerImageElement.attr("src");
    }
}
