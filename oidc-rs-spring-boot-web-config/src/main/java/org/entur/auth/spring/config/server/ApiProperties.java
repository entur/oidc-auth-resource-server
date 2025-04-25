package org.entur.auth.spring.config.server;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ApiProperties {
    private String issuerUrl;
    private List<String> audiences = new ArrayList<>();
}
