package org.entur.auth.spring.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class GreetingController {
    @GetMapping("/protected")
    public String getProtected() {
        log.info("Get protected method with GET");
        return "Hello from protected";
    }
}
