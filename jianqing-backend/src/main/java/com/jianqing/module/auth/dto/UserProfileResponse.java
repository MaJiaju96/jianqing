package com.jianqing.module.auth.dto;

import java.util.List;

public record UserProfileResponse(Long userId,
                                  String username,
                                  String nickname,
                                  List<String> roles,
                                  List<String> permissions) {
}
