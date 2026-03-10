package com.jianqing.module.system.service.impl;

import com.jianqing.module.system.dto.UserSaveRequest;
import com.jianqing.module.system.entity.SysUser;
import com.jianqing.module.system.mapper.SysUserMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserWriteOperationHandlerTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SystemCacheEvictor systemCacheEvictor;

    @Test
    void shouldCreateUserAndEvictSystemUsersCache() {
        UserWriteOperationHandler handler = new UserWriteOperationHandler(sysUserMapper, passwordEncoder,
                systemCacheEvictor);
        UserSaveRequest request = buildRequest();
        when(passwordEncoder.encode("plain-pass")).thenReturn("encoded-pass");

        SysUser user = handler.createUser(request);

        ArgumentCaptor<SysUser> userCaptor = ArgumentCaptor.forClass(SysUser.class);
        verify(sysUserMapper).insert(userCaptor.capture());
        verify(systemCacheEvictor).evictSystemUsers();
        SysUser savedUser = userCaptor.getValue();
        Assertions.assertEquals("alice", savedUser.getUsername());
        Assertions.assertEquals("encoded-pass", savedUser.getPasswordHash());
        Assertions.assertEquals("Alice", savedUser.getNickname());
        Assertions.assertEquals("", savedUser.getRealName());
        Assertions.assertEquals(0, savedUser.getIsDeleted());
        Assertions.assertSame(savedUser, user);
    }

    @Test
    void shouldUpdateUserAndEvictRelatedCaches() {
        UserWriteOperationHandler handler = new UserWriteOperationHandler(sysUserMapper, passwordEncoder,
                systemCacheEvictor);
        UserSaveRequest request = buildRequest();
        SysUser user = new SysUser();
        user.setId(9L);
        user.setPasswordHash("old-pass");
        when(passwordEncoder.encode("plain-pass")).thenReturn("encoded-pass");

        handler.updateUser(user, request);

        verify(sysUserMapper).updateById(user);
        verify(systemCacheEvictor).evictSystemUsers();
        verify(systemCacheEvictor).evictUserAuth(9L);
        Assertions.assertEquals("alice", user.getUsername());
        Assertions.assertEquals("encoded-pass", user.getPasswordHash());
        Assertions.assertEquals(10L, user.getDeptId());
    }

    @Test
    void shouldDeleteUserAndClearUserRoles() {
        UserWriteOperationHandler handler = new UserWriteOperationHandler(sysUserMapper, passwordEncoder,
                systemCacheEvictor);
        SysUser user = new SysUser();
        user.setId(12L);
        user.setIsDeleted(0);

        handler.deleteUser(user);

        verify(sysUserMapper).updateById(user);
        verify(sysUserMapper).deleteUserRoleByUserId(12L);
        verify(systemCacheEvictor).evictSystemUsers();
        verify(systemCacheEvictor).evictUserAuth(12L);
        Assertions.assertEquals(1, user.getIsDeleted());
    }

    @Test
    void shouldRejectCreateWhenPasswordIsBlank() {
        UserWriteOperationHandler handler = new UserWriteOperationHandler(sysUserMapper, passwordEncoder,
                systemCacheEvictor);
        UserSaveRequest request = buildRequest();
        request.setPassword(" ");

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> handler.createUser(request));

        Assertions.assertEquals("新增用户时密码不能为空", exception.getMessage());
    }

    private UserSaveRequest buildRequest() {
        UserSaveRequest request = new UserSaveRequest();
        request.setUsername("alice");
        request.setPassword("plain-pass");
        request.setNickname("Alice");
        request.setDeptId(10L);
        request.setStatus(1);
        return request;
    }
}
