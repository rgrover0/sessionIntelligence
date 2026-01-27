package com.sessionintelligence.example;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class SampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }

    @GetMapping("/start")
    public String start(HttpServletRequest request) {
        request.getSession(true);
        return "session-started";
    }

    @GetMapping("/rate")
    public String rate(@RequestHeader(name = "X-Window-Name", required = false) String windowName) {
        return windowName == null ? "window-missing" : "window=" + windowName;
    }
}
