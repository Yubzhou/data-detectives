package com.yubzhou.model.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotNewsItem {
    private Long newsId;
    private Double hotness;
}