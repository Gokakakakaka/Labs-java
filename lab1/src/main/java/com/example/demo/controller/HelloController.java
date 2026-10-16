package com.example.demo.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public Integer hello(@RequestParam Integer name) {
        return name*name;
    }

    @GetMapping("/numbers")
    public List<Integer> numbers() {
        return List.of(1, 4, 8);
    }
}
