package com.example.demo.controller;

import com.example.demo.annotation.Rot13Llm;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Rot13Llm
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hallo, lAdss!";
    }

    @GetMapping("/numbers")
    public List<Integer> numbers() {
        return List.of(1, 4, 8);
    }
}
