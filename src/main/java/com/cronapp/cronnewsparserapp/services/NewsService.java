package com.cronapp.cronnewsparserapp.services;

import com.cronapp.cronnewsparserapp.domains.NewsEntity;
import com.cronapp.cronnewsparserapp.repos.NewsRepo;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class NewsService {
    private final NewsRepo newsRepository;

    public void saveNews(NewsEntity news) {
        newsRepository.save(news);
    }

    public void saveNewsList(List<NewsEntity> newsList) {
        for (NewsEntity news : newsList) {
            saveNews(news);
        }
    }

    public List<NewsEntity> getPaginatedNews(int page, int size) {
        return newsRepository.findAllPaginated(page, size);
    }

    public List<NewsEntity> findAllPaginatedByDate(LocalDate date, int page, int size) {
        return newsRepository.findAllPaginatedByDate(date, page, size);
    }

    public NewsEntity findById(String uuid) {
        return newsRepository.findById(uuid);
    }

}
