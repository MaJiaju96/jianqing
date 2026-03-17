package com.jianqing.module.system.service.impl;

import com.jianqing.module.system.entity.SysDept;
import com.jianqing.module.system.mapper.SysDeptMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeptServiceImplTest {

    @Mock
    private SysDeptMapper sysDeptMapper;

    @InjectMocks
    private DeptServiceImpl deptService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(deptService, "baseMapper", sysDeptMapper);
    }

    @Test
    void shouldListSelfAndDescendantDeptIdsWithinCurrentSubtree() {
        when(sysDeptMapper.selectAllEnabledDepts()).thenReturn(List.of(
                buildDept(1L, 0L),
                buildDept(2L, 1L),
                buildDept(3L, 2L),
                buildDept(4L, 1L),
                buildDept(9L, 0L),
                buildDept(10L, 9L)
        ));

        List<Long> deptIds = deptService.listSelfAndDescendantDeptIds(2L);

        Assertions.assertEquals(List.of(2L, 3L), deptIds);
    }

    @Test
    void shouldReturnEmptyWhenDeptIsOutsideEnabledDeptList() {
        when(sysDeptMapper.selectAllEnabledDepts()).thenReturn(List.of(
                buildDept(1L, 0L),
                buildDept(2L, 1L)
        ));

        List<Long> deptIds = deptService.listSelfAndDescendantDeptIds(99L);

        Assertions.assertTrue(deptIds.isEmpty());
    }

    private SysDept buildDept(Long id, Long parentId) {
        SysDept dept = new SysDept();
        dept.setId(id);
        dept.setParentId(parentId);
        dept.setDeptName("dept-" + id);
        dept.setStatus(1);
        dept.setIsDeleted(0);
        return dept;
    }
}
