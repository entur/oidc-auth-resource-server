package org.entur.auth.spring.application;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EchoController {
    @GetMapping("/echo")
    public String echo(@RequestParam(name="message", required = false) String message) {
        return "Message: " + message;
    }
}
