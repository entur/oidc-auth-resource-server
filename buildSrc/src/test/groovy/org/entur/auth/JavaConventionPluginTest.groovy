package org.entur.auth

import org.gradle.testkit.runner.TaskOutcome

class JavaConventionPluginTest extends PluginTest {

    def setup() {
        buildFile << """
            plugins {
                id 'oidc-auth.java-conventions' 
                id 'com.github.ben-manes.versions' version '0.51.0' apply false
                id "com.github.spotbugs" version "6.1.3" apply false
            }
        """
    }

    def "fails on checkstyle error"() {
        given:
        new File(testProjectDir, 'src/main/java/com/example').mkdirs()
        new File(testProjectDir, 'src/main/java/com/example/Foo.java') << """
            package org.entur.auth;

            import java.util.*;

            class Foo {
                void bar() {
                }
            }
        """

        when:
        def result = runTaskWithFailure('build')

        then:
        result.task(":checkstyleMain").outcome == TaskOutcome.FAILED
        result.output.contains('Checkstyle rule violations were found.')
        result.output.contains('Checkstyle violations by severity: [error:1]')
    }

    def "fails on checkstyle warning"() {
        given:
        new File(testProjectDir, 'src/main/java/com/example').mkdirs()
        new File(testProjectDir, 'src/main/java/com/example/Foo.java') << """
            package org.entur.auth;

            class Foo {
                final static public String FOO = "BAR";

                void bar() {
                }
            }
        """

        when:
        def result = runTaskWithFailure('build')

        then:
        result.task(":checkstyleMain").outcome == TaskOutcome.FAILED
        result.output.contains('Checkstyle rule violations were found.')
        result.output.contains('Checkstyle violations by severity: [warning:1]')
    }

    def "fails on spotbugs error"() {
        given:
        new File(testProjectDir, 'src/main/java/com/example').mkdirs()
        new File(testProjectDir, 'src/main/java/com/example/Foo.java') << """
            package org.entur.auth;

            class Foo {
                void bar() {
                    String s = null;
                    s.hashCode();
                }
            }
        """

        when:
        def result = runTaskWithFailure('build')

        then:
        result.task(":spotbugsMain").outcome == TaskOutcome.FAILED
    }

    def "warns on deprecated API usage"() {
        given:
        new File(testProjectDir, 'src/main/java/com/example').mkdirs()
        new File(testProjectDir, 'src/main/java/com/example/Foo.java') << """
            package corg.entur.auth.build;

            public class Foo {
                @Deprecated
                public void deprecatedMethod() {}
            }
        """

        new File(testProjectDir, 'src/main/java/com/example/Bar.java') << """
            package org.entur.auth;

            public class Bar {
                public void bar() {
                    new Foo().deprecatedMethod();
                }
            }
        """

        when:
        def result = runTask('build')

        then:
        result.task(":build").outcome == TaskOutcome.SUCCESS
        result.output.contains('warning: [deprecation] deprecatedMethod()')
    }
}
