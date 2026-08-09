package com.smartlibrary.service;

import com.smartlibrary.entity.Book;

import java.util.Map;

public interface BookCoverService {
    String resolveCoverUrl(Book book);
    Map<String, Object> auditAndResolveAllCovers();
}
