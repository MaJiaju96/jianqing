package com.jianqing.module.system.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

class SystemTransactionAnnotationTest {

    @Test
    void shouldMarkCriticalSystemWriteMethodsTransactional() {
        assertTransactional(SystemServiceImpl.class, "createUser", com.jianqing.module.system.dto.UserSaveRequest.class);
        assertTransactional(SystemServiceImpl.class, "updateUser", Long.class,
                com.jianqing.module.system.dto.UserSaveRequest.class);
        assertTransactional(SystemServiceImpl.class, "deleteUser", Long.class);
        assertTransactional(SystemServiceImpl.class, "assignUserRoles", Long.class, java.util.List.class);
        assertTransactional(SystemServiceImpl.class, "createRole", com.jianqing.module.system.dto.RoleSaveRequest.class);
        assertTransactional(SystemServiceImpl.class, "updateRole", Long.class,
                com.jianqing.module.system.dto.RoleSaveRequest.class);
        assertTransactional(SystemServiceImpl.class, "deleteRole", Long.class);
        assertTransactional(SystemServiceImpl.class, "assignRoleMenus", Long.class, java.util.List.class);
        assertTransactional(SystemServiceImpl.class, "createMenu", com.jianqing.module.system.dto.MenuSaveRequest.class);
        assertTransactional(SystemServiceImpl.class, "updateMenu", Long.class,
                com.jianqing.module.system.dto.MenuSaveRequest.class);
        assertTransactional(SystemServiceImpl.class, "deleteMenu", Long.class);
    }

    @Test
    void shouldMarkDomainWriteServicesTransactional() {
        assertTransactional(RoleServiceImpl.class, "createRole", com.jianqing.module.system.dto.RoleSaveRequest.class);
        assertTransactional(RoleServiceImpl.class, "updateRole", Long.class,
                com.jianqing.module.system.dto.RoleSaveRequest.class);
        assertTransactional(RoleServiceImpl.class, "deleteRole", Long.class);
        assertTransactional(RoleServiceImpl.class, "assignRoleMenus", Long.class, java.util.List.class);

        assertTransactional(MenuServiceImpl.class, "createMenu", com.jianqing.module.system.dto.MenuSaveRequest.class);
        assertTransactional(MenuServiceImpl.class, "updateMenu", Long.class,
                com.jianqing.module.system.dto.MenuSaveRequest.class);
        assertTransactional(MenuServiceImpl.class, "deleteMenu", Long.class);

        assertTransactional(DeptServiceImpl.class, "createDept", com.jianqing.module.system.dto.DeptSaveRequest.class);
        assertTransactional(DeptServiceImpl.class, "updateDept", Long.class,
                com.jianqing.module.system.dto.DeptSaveRequest.class);
        assertTransactional(DeptServiceImpl.class, "deleteDept", Long.class);
    }

    private void assertTransactional(Class<?> type, String methodName, Class<?>... parameterTypes) {
        try {
            Method method = type.getMethod(methodName, parameterTypes);
            Transactional transactional = method.getAnnotation(Transactional.class);
            Assertions.assertNotNull(transactional, type.getSimpleName() + "." + methodName + " should declare @Transactional");
        } catch (NoSuchMethodException exception) {
            throw new AssertionError("Method not found: " + type.getSimpleName() + "." + methodName, exception);
        }
    }
}
