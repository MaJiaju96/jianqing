package com.jianqing.module.dev.service;

import com.jianqing.module.dev.dto.GenPreviewFile;
import com.jianqing.module.dev.dto.GenPreviewRequest;

import java.util.List;

public interface GeneratorPreviewService {

    List<GenPreviewFile> preview(GenPreviewRequest request);

    byte[] downloadZip(GenPreviewRequest request);

    List<GenPreviewFile> generateToProject(GenPreviewRequest request, String projectRoot);
}
