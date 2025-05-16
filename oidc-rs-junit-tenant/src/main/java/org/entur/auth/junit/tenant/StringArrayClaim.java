package org.entur.auth.junit.tenant;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface StringArrayClaim {
    String name();

    String[] value();
}
