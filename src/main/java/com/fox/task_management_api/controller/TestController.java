package com.fox.task_management_api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/tasks")
    public String tasks() {
        return "You are authenticated!";
    }
}