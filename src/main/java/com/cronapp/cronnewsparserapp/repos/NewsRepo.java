package com.cronapp.cronnewsparserapp.repos;

import com.cronapp.cronnewsparserapp.domains.NewsEntity;

import java.time.LocalDate;
import java.util.List;

public interface NewsRepo {
    void save(NewsEntity news);

    List<NewsEntity> findAllPaginated(int page, int size);

    NewsEntity findById(String uuid);

    List<NewsEntity> findAllPaginatedByDate(LocalDate date, int page, int size);

}
