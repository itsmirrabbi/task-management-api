package com.fox.task_management_api.controller;


import com.fox.task_management_api.dto.LoginRequest;
import com.fox.task_management_api.model.User;
import com.fox.task_management_api.repository.UserRepository;
import com.fox.task_management_api.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class AuthController {
    private final JwtService jwtService;

    private final UserRepository userRepository;

    public AuthController(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public User register(@RequestBody User user)
    {
        return userRepository.save(user);
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest  request)
    {

        Optional<User>user = userRepository.findByUsername(request.getUsername());
        if(user.isPresent() && user.get().getPassword().equals(request.getPassword()))
            return jwtService.generateToken(user.get().getUsername());
        return "Invalid username or password";
    }

}
