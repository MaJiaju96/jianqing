package com.jianqing.integration.search.model;

import java.util.Map;

public record SearchQuery(int page, int size, String keyword, Map<String, Object> filters) {
}
