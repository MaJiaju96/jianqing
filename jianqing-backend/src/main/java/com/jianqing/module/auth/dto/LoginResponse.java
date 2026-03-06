package com.jianqing.module.auth.dto;

public record LoginResponse(String accessToken, String tokenType, long expireSeconds) {
}
