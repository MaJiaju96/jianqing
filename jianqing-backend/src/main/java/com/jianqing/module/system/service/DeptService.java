package com.jianqing.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jianqing.module.system.dto.DeptSaveRequest;
import com.jianqing.module.system.dto.DeptTreeNode;
import com.jianqing.module.system.entity.SysDept;

import java.util.List;

public interface DeptService extends IService<SysDept> {

    List<DeptTreeNode> listDeptTree();

    List<Long> listSelfAndDescendantDeptIds(Long deptId);

    DeptTreeNode createDept(DeptSaveRequest request);

    DeptTreeNode updateDept(Long id, DeptSaveRequest request);

    void deleteDept(Long id);
}
