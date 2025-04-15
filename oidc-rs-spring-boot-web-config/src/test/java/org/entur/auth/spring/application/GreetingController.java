package org.entur.auth.spring.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class GreetingController {
    @GetMapping("/unprotected")
    public String getUnprotected() {
        log.info("Get unprotected method with GET");
        return "Hello from unprotected";
    }

    @PostMapping("/unprotected")
    public String postUnprotected() {
        log.info("Get unprotected method with GET");
        return "Hello from unprotected";
    }


    @PutMapping("/unprotected")
    public String putUnprotected() {
        log.info("Get unprotected method with GET");
        return "Hello from unprotected";
    }

    @DeleteMapping("/unprotected")
    public String deleteUnprotected() {
        log.info("Get unprotected method with GET");
        return "Hello from unprotected";
    }

    @GetMapping("/protected")
    public String getProtected() {
        log.info("Get protected method with GET");
        return "Hello from protected";
    }
}
