package org.entur.auth.spring.common.server;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ApiProperties {
    private String issuerUrl;
    private List<String> audiences = new ArrayList<>();
}
