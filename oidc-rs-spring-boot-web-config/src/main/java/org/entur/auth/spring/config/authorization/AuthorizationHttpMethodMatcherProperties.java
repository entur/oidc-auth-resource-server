package org.entur.auth.spring.config.authorization;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class AuthorizationHttpMethodMatcherProperties {
    private final HttpMethod verb;
    private List<String> patterns = new ArrayList<>();

    public String[] getPatternsAsArray() {
        return patterns.toArray(new String[0]);
    }

    public void appendToList(List<AuthorizationHttpMethodMatcherProperties> list) {
        list.add(this);
    }
}
