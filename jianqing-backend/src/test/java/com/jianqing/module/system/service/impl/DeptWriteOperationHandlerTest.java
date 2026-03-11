package com.jianqing.module.system.service.impl;

import com.jianqing.module.system.dto.DeptSaveRequest;
import com.jianqing.module.system.dto.DeptTreeNode;
import com.jianqing.module.system.service.DeptService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeptWriteOperationHandlerTest {

    @Mock
    private DeptService deptService;

    @Test
    void shouldCreateDept() {
        DeptWriteOperationHandler handler = new DeptWriteOperationHandler(deptService);
        DeptSaveRequest request = new DeptSaveRequest();
        DeptTreeNode node = new DeptTreeNode();
        node.setId(1L);
        when(deptService.createDept(request)).thenReturn(node);

        DeptTreeNode actual = handler.createDept(request);

        verify(deptService).createDept(request);
        Assertions.assertSame(node, actual);
    }

    @Test
    void shouldUpdateDept() {
        DeptWriteOperationHandler handler = new DeptWriteOperationHandler(deptService);
        DeptSaveRequest request = new DeptSaveRequest();
        DeptTreeNode node = new DeptTreeNode();
        node.setId(2L);
        when(deptService.updateDept(2L, request)).thenReturn(node);

        DeptTreeNode actual = handler.updateDept(2L, request);

        verify(deptService).updateDept(2L, request);
        Assertions.assertSame(node, actual);
    }

    @Test
    void shouldDeleteDept() {
        DeptWriteOperationHandler handler = new DeptWriteOperationHandler(deptService);

        handler.deleteDept(3L);

        verify(deptService).deleteDept(3L);
    }
}
