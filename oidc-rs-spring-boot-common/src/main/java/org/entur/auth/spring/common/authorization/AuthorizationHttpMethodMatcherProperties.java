package org.entur.auth.spring.common.authorization;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;

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
