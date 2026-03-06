package com.jianqing.integration.search;

import com.jianqing.integration.search.model.SearchPageResult;
import com.jianqing.integration.search.model.SearchQuery;

public interface SearchGateway {
    void upsert(String index, String id, Object document);

    void delete(String index, String id);

    <T> SearchPageResult<T> search(String index, SearchQuery query, Class<T> clazz);
}
