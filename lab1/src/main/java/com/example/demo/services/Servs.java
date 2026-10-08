package com.example.demo.service;

import com.example.demo.annotation.Rot13Llm;
import org.springframework.stereotype.Service;

@Rot13Llm
@Service
public class Servs {

    public String getMessage() {
        return "Example service";
    }
}
