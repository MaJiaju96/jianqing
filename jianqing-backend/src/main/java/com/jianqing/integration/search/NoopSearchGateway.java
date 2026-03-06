package com.jianqing.integration.search;

import com.jianqing.integration.search.model.SearchPageResult;
import com.jianqing.integration.search.model.SearchQuery;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class NoopSearchGateway implements SearchGateway {

    @Override
    public void upsert(String index, String id, Object document) {
    }

    @Override
    public void delete(String index, String id) {
    }

    @Override
    public <T> SearchPageResult<T> search(String index, SearchQuery query, Class<T> clazz) {
        return new SearchPageResult<>(query.page(), query.size(), 0, Collections.emptyList());
    }
}
