package com.cronapp.cronnewsparserapp.domains;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class NewsEntity {
    private String uuid;
    private String category;
    private String title;
    private String postUrl;
    private String headerImageUrl;
    private String textContent;
    private String htmlContent;
    private Timestamp publicationTime;

}
