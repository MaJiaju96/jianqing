package com.jianqing.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DeptSaveRequest {

    @NotNull(message = "父部门不能为空")
    private Long parentId;
    @NotBlank(message = "部门名称不能为空")
    private String deptName;
    @NotNull(message = "负责人不能为空")
    private Long leaderUserId;
    private String phone;
    private String email;
    @NotNull(message = "排序不能为空")
    private Integer sortNo;
    @NotNull(message = "状态不能为空")
    private Integer status;

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public Long getLeaderUserId() {
        return leaderUserId;
    }

    public void setLeaderUserId(Long leaderUserId) {
        this.leaderUserId = leaderUserId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
