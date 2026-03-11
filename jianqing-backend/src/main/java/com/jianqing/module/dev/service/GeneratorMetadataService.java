package com.jianqing.module.dev.service;

import com.jianqing.module.dev.dto.GenColumnMeta;
import com.jianqing.module.dev.dto.GenTableSummary;

import java.util.List;

public interface GeneratorMetadataService {
    List<GenTableSummary> listTables();

    List<GenColumnMeta> listColumns(String tableName);
}
