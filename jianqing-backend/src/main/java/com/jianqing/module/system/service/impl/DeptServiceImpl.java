package com.jianqing.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jianqing.module.system.dto.DeptSaveRequest;
import com.jianqing.module.system.dto.DeptTreeNode;
import com.jianqing.module.system.entity.SysDept;
import com.jianqing.module.system.mapper.SysDeptMapper;
import com.jianqing.module.system.service.DeptService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements DeptService {

    @Override
    public List<DeptTreeNode> listDeptTree() {
        return buildDeptTree(baseMapper.selectAllEnabledDepts());
    }

    @Override
    public DeptTreeNode createDept(DeptSaveRequest request) {
        validateParent(request.getParentId());
        SysDept dept = new SysDept();
        fillDept(dept, request);
        dept.setIsDeleted(0);
        baseMapper.insert(dept);
        return toDeptTreeNode(dept);
    }

    @Override
    public DeptTreeNode updateDept(Long id, DeptSaveRequest request) {
        SysDept dept = getDeptOrThrow(id);
        if (id.equals(request.getParentId())) {
            throw new IllegalArgumentException("部门不能将自己设置为父部门");
        }
        validateParent(request.getParentId());
        fillDept(dept, request);
        baseMapper.updateById(dept);
        return toDeptTreeNode(dept);
    }

    @Override
    public void deleteDept(Long id) {
        SysDept dept = getDeptOrThrow(id);
        Long childCount = baseMapper.countChildren(id);
        if (childCount != null && childCount > 0) {
            throw new IllegalArgumentException("请先删除子部门后再删除当前部门");
        }
        dept.setIsDeleted(1);
        baseMapper.updateById(dept);
    }

    private SysDept getDeptOrThrow(Long id) {
        SysDept dept = baseMapper.selectOne(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getId, id)
                .eq(SysDept::getIsDeleted, 0)
                .last("limit 1"));
        if (dept == null) {
            throw new IllegalArgumentException("部门不存在");
        }
        return dept;
    }

    private void validateParent(Long parentId) {
        if (parentId == null || parentId == 0L) {
            return;
        }
        SysDept parentDept = baseMapper.selectOne(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getId, parentId)
                .eq(SysDept::getIsDeleted, 0)
                .last("limit 1"));
        if (parentDept == null) {
            throw new IllegalArgumentException("父部门不存在");
        }
    }

    private void fillDept(SysDept dept, DeptSaveRequest request) {
        dept.setParentId(request.getParentId());
        dept.setDeptName(request.getDeptName());
        dept.setLeaderUserId(request.getLeaderUserId());
        dept.setPhone(safeText(request.getPhone()));
        dept.setEmail(safeText(request.getEmail()));
        dept.setSortNo(request.getSortNo());
        dept.setStatus(request.getStatus());
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private List<DeptTreeNode> buildDeptTree(List<SysDept> depts) {
        Map<Long, DeptTreeNode> nodeMap = new HashMap<>();
        for (SysDept dept : depts) {
            DeptTreeNode node = toDeptTreeNode(dept);
            nodeMap.put(node.getId(), node);
        }
        List<DeptTreeNode> roots = new ArrayList<>();
        for (DeptTreeNode node : nodeMap.values()) {
            Long parentId = node.getParentId();
            if (parentId == null || parentId == 0 || !nodeMap.containsKey(parentId)) {
                roots.add(node);
            } else {
                nodeMap.get(parentId).getChildren().add(node);
            }
        }
        sortTree(roots);
        return roots;
    }

    private void sortTree(List<DeptTreeNode> nodes) {
        nodes.sort(Comparator.comparing(DeptTreeNode::getSortNo, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(DeptTreeNode::getId, Comparator.nullsLast(Long::compareTo)));
        for (DeptTreeNode node : nodes) {
            sortTree(node.getChildren());
        }
    }

    private DeptTreeNode toDeptTreeNode(SysDept dept) {
        DeptTreeNode node = new DeptTreeNode();
        node.setId(dept.getId());
        node.setParentId(dept.getParentId());
        node.setDeptName(dept.getDeptName());
        node.setLeaderUserId(dept.getLeaderUserId());
        node.setPhone(dept.getPhone());
        node.setEmail(dept.getEmail());
        node.setSortNo(dept.getSortNo());
        node.setStatus(dept.getStatus());
        return node;
    }
}
