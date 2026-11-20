package com.example.demo;

import org.springframework.boot.SpringApplication;
import com.example.demo.db.Db;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);

        try {
          Db.connect(); 
        } catch (java.sql.SQLException e) {
          System.err.println("Err: " + e.getMessage());
        }
    }
}
