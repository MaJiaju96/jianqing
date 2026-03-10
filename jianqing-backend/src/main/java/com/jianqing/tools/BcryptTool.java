package com.jianqing.tools;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptTool {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String raw = "test123"; // 改成你的明文密码
        String hash = encoder.encode(raw);
        System.out.println("raw = " + raw);
        System.out.println("bcrypt = " + hash);
        System.out.println("match = " + encoder.matches(raw, hash));
    }
}
