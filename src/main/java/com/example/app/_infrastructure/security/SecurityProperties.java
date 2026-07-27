package com.example.app._infrastructure.security;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    String frontendSuccess = "";
    List<User> users = List.of();

    @Data
    public static class User {
        String name = "";
        String password = "";
        List<String> roles = List.of();
    }
}
