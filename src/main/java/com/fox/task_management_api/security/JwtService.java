package com.fox.task_management_api.security;


import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;
}
