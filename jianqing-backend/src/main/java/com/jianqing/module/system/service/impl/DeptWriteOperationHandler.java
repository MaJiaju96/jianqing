package com.jianqing.module.system.service.impl;

import com.jianqing.module.system.dto.DeptSaveRequest;
import com.jianqing.module.system.dto.DeptTreeNode;
import com.jianqing.module.system.service.DeptService;
import org.springframework.stereotype.Component;

@Component
public class DeptWriteOperationHandler {

    private final DeptService deptService;

    public DeptWriteOperationHandler(DeptService deptService) {
        this.deptService = deptService;
    }

    public DeptTreeNode createDept(DeptSaveRequest request) {
        return deptService.createDept(request);
    }

    public DeptTreeNode updateDept(Long id, DeptSaveRequest request) {
        return deptService.updateDept(id, request);
    }

    public void deleteDept(Long id) {
        deptService.deleteDept(id);
    }
}
