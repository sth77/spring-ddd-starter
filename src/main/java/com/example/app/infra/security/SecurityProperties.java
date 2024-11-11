package com.example.app.infra.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    String frontendSuccess;
    List<User> users;

    @Data
    public static class User {
        String name;
        String password;
        List<String> roles;
    }
}
