package com.fox.task_management_api.controller;


import com.fox.task_management_api.dto.LoginRequest;
import com.fox.task_management_api.model.User;
import com.fox.task_management_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class AuthController {
    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
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
            return "Login successful";
        return "Invalid username or password";
    }

}
