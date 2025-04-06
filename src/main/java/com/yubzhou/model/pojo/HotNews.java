package com.yubzhou.model.pojo;

import com.yubzhou.model.po.News;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotNews {
    private Long newsId;
    private Integer rank;
    private Double hotness;
    private News news;

    public HotNews(Long newsId, Double hotness) {
        this.newsId = newsId;
        this.hotness = hotness;
    }
}