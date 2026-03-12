package com.jianqing.module.dev.dto;

import java.util.ArrayList;
import java.util.List;

public class GenWriteResult {

    private String markerId;
    private List<GenPreviewFile> files = new ArrayList<>();

    public String getMarkerId() {
        return markerId;
    }

    public void setMarkerId(String markerId) {
        this.markerId = markerId;
    }

    public List<GenPreviewFile> getFiles() {
        return files;
    }

    public void setFiles(List<GenPreviewFile> files) {
        this.files = files;
    }
}
