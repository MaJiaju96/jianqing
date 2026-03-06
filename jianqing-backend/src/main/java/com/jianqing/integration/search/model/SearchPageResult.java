package com.jianqing.integration.search.model;

import java.util.List;

public record SearchPageResult<T>(long page, long size, long total, List<T> records) {
}
