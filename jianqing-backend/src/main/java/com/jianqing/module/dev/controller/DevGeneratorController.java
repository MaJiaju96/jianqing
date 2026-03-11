package com.jianqing.module.dev.controller;

import com.jianqing.common.api.ApiResponse;
import com.jianqing.module.dev.dto.GenColumnMeta;
import com.jianqing.module.dev.dto.GenPreviewFile;
import com.jianqing.module.dev.dto.GenPreviewRequest;
import com.jianqing.module.dev.dto.GenTableSummary;
import com.jianqing.module.dev.service.GeneratorMetadataService;
import com.jianqing.module.dev.service.GeneratorPreviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dev/gen")
public class DevGeneratorController {

    private final GeneratorMetadataService generatorMetadataService;
    private final GeneratorPreviewService generatorPreviewService;

    public DevGeneratorController(GeneratorMetadataService generatorMetadataService,
                                  GeneratorPreviewService generatorPreviewService) {
        this.generatorMetadataService = generatorMetadataService;
        this.generatorPreviewService = generatorPreviewService;
    }

    @GetMapping("/tables")
    public ApiResponse<List<GenTableSummary>> tables() {
        return ApiResponse.success(generatorMetadataService.listTables());
    }

    @GetMapping("/tables/{tableName}/columns")
    public ApiResponse<List<GenColumnMeta>> columns(@PathVariable String tableName) {
        return ApiResponse.success(generatorMetadataService.listColumns(tableName));
    }

    @PostMapping("/preview")
    public ApiResponse<List<GenPreviewFile>> preview(@Valid @RequestBody GenPreviewRequest request) {
        return ApiResponse.success(generatorPreviewService.preview(request));
    }

    @PostMapping("/download")
    public ResponseEntity<byte[]> download(@Valid @RequestBody GenPreviewRequest request) {
        byte[] body = generatorPreviewService.downloadZip(request);
        String fileName = request.getBusinessName() + "-generator-preview.zip";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(body);
    }

    @PostMapping("/write")
    public ApiResponse<List<GenPreviewFile>> write(@Valid @RequestBody GenPreviewRequest request,
                                                    @RequestParam(required = false, defaultValue = "") String projectRoot) {
        List<GenPreviewFile> files = generatorPreviewService.generateToProject(request, projectRoot);
        return ApiResponse.success(files);
    }
}
