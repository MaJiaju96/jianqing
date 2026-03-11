package com.jianqing.module.dev.dto;

public class GenPreviewFile {

    private String filePath;
    private String content;

    public GenPreviewFile() {
    }

    public GenPreviewFile(String filePath, String content) {
        this.filePath = filePath;
        this.content = content;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
