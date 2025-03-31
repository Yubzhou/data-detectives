package com.yubzhou.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yubzhou.mapper.NewsMapper;
import com.yubzhou.model.po.News;
import com.yubzhou.service.NewsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News> implements NewsService {
}
