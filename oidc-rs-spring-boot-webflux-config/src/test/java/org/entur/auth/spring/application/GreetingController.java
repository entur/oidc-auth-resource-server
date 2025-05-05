package org.entur.auth.spring.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequiredArgsConstructor
public class GreetingController {
    @GetMapping("/unprotected")
    public Mono<String> getUnprotected() {
        log.info("Get unprotected method with GET");
        return Mono.just("Hello from unprotected");
    }

    @PostMapping("/unprotected")
    public Mono<String> postUnprotected() {
        log.info("Get unprotected method with GET");
        return Mono.just("Hello from unprotected");
    }

    @PutMapping("/unprotected")
    public Mono<String> putUnprotected() {
        log.info("Get unprotected method with GET");
        return Mono.just("Hello from unprotected");
    }

    @DeleteMapping("/unprotected")
    public Mono<String> deleteUnprotected() {
        log.info("Get unprotected method with GET");
        return Mono.just("Hello from unprotected");
    }

    @GetMapping("/protected")
    public Mono<String> getProtected() {
        log.info("Get protected method with GET");
        return Mono.just("Hello from protected");
    }
}
