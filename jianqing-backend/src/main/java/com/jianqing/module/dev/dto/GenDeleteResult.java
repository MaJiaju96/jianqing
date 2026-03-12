package com.jianqing.module.dev.dto;

import java.util.ArrayList;
import java.util.List;

public class GenDeleteResult {

    private String markerId;
    private int deletedCount;
    private int missingCount;
    private List<String> deletedFiles = new ArrayList<>();
    private List<String> missingFiles = new ArrayList<>();

    public String getMarkerId() {
        return markerId;
    }

    public void setMarkerId(String markerId) {
        this.markerId = markerId;
    }

    public int getDeletedCount() {
        return deletedCount;
    }

    public void setDeletedCount(int deletedCount) {
        this.deletedCount = deletedCount;
    }

    public int getMissingCount() {
        return missingCount;
    }

    public void setMissingCount(int missingCount) {
        this.missingCount = missingCount;
    }

    public List<String> getDeletedFiles() {
        return deletedFiles;
    }

    public void setDeletedFiles(List<String> deletedFiles) {
        this.deletedFiles = deletedFiles;
    }

    public List<String> getMissingFiles() {
        return missingFiles;
    }

    public void setMissingFiles(List<String> missingFiles) {
        this.missingFiles = missingFiles;
    }
}
