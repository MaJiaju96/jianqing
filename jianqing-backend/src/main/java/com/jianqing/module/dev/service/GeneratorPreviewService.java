package com.jianqing.module.dev.service;

import com.jianqing.module.dev.dto.GenDeleteResult;
import com.jianqing.module.dev.dto.GenPreviewFile;
import com.jianqing.module.dev.dto.GenPreviewRequest;
import com.jianqing.module.dev.dto.GenWriteRecordItem;
import com.jianqing.module.dev.dto.GenWriteResult;

import java.util.List;
import java.time.LocalDateTime;

public interface GeneratorPreviewService {

    List<GenPreviewFile> preview(GenPreviewRequest request);

    byte[] downloadZip(GenPreviewRequest request);

    List<String> listConflictFiles(GenPreviewRequest request, String projectRoot);

    GenWriteResult generateToProject(GenPreviewRequest request, String projectRoot, boolean overwrite);

    List<GenWriteRecordItem> listWriteRecords(int limit, String tableName, LocalDateTime startAt, LocalDateTime endAt);

    GenDeleteResult deleteByMarker(String markerId, String projectRoot);
}
