package org.entur.auth.spring.config.authorization;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.http.HttpMethod;

@Data
public class AuthorizationMethodMatcherProperties {
    private AuthorizationHttpMethodMatcherProperties get =
            new AuthorizationHttpMethodMatcherProperties(HttpMethod.GET);
    private AuthorizationHttpMethodMatcherProperties head =
            new AuthorizationHttpMethodMatcherProperties(HttpMethod.HEAD);
    private AuthorizationHttpMethodMatcherProperties post =
            new AuthorizationHttpMethodMatcherProperties(HttpMethod.POST);
    private AuthorizationHttpMethodMatcherProperties put =
            new AuthorizationHttpMethodMatcherProperties(HttpMethod.PUT);
    private AuthorizationHttpMethodMatcherProperties patch =
            new AuthorizationHttpMethodMatcherProperties(HttpMethod.PATCH);
    private AuthorizationHttpMethodMatcherProperties delete =
            new AuthorizationHttpMethodMatcherProperties(HttpMethod.DELETE);
    private AuthorizationHttpMethodMatcherProperties options =
            new AuthorizationHttpMethodMatcherProperties(HttpMethod.OPTIONS);
    private AuthorizationHttpMethodMatcherProperties trace =
            new AuthorizationHttpMethodMatcherProperties(HttpMethod.TRACE);

    public List<AuthorizationHttpMethodMatcherProperties> getActiveMethods() {
        List<AuthorizationHttpMethodMatcherProperties> list = new ArrayList<>();
        get.appendToList(list);
        head.appendToList(list);
        post.appendToList(list);
        put.appendToList(list);
        patch.appendToList(list);
        delete.appendToList(list);
        options.appendToList(list);
        trace.appendToList(list);

        return list;
    }
}
