package org.entur.auth.spring.config.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;

public class DefaultAuthProviders implements AuthProviders {
    private static final Map<String, List<IssuerProperties>> issuers;

    static {
        issuers = new HashMap<>();

        List<IssuerProperties> devProviders = new ArrayList<>();
        devProviders.add(
                createProvider(
                        "https://internal.dev.entur.org/",
                        "https://internal.dev.entur.org/.well-known/jwks.json"));
        devProviders.add(
                createProvider(
                        "https://traveller.dev.entur.org/",
                        "https://traveller.dev.entur.org/.well-known/jwks.json"));
        devProviders.add(
                createProvider(
                        "https://partner.dev.entur.org/",
                        "https://partner.dev.entur.org/.well-known/jwks.json"));
        devProviders.add(
                createProvider(
                        "https://person.dev.entur.org/", "https://person.dev.entur.org/.well-known/jwks.json"));
        issuers.put("dev", devProviders);

        List<IssuerProperties> stageProviders = new ArrayList<>();
        stageProviders.add(
                createProvider(
                        "https://internal.staging.entur.org/",
                        "https://internal.staging.entur.org/.well-known/jwks.json"));
        stageProviders.add(
                createProvider(
                        "https://traveller.staging.entur.org/",
                        "https://traveller.staging.entur.org/.well-known/jwks.json"));
        stageProviders.add(
                createProvider(
                        "https://partner.staging.entur.org/",
                        "https://partner.staging.entur.org/.well-known/jwks.json"));
        stageProviders.add(
                createProvider(
                        "https://person.staging.entur.org/",
                        "https://person.staging.entur.org/.well-known/jwks.json"));
        issuers.put("stage", stageProviders);
        issuers.put("tst", stageProviders);

        List<IssuerProperties> prodProviders = new ArrayList<>();
        prodProviders.add(
                createProvider(
                        "https://internal.entur.org/", "https://internal.entur.org/.well-known/jwks.json"));
        prodProviders.add(
                createProvider(
                        "https://traveller.entur.org/", "https://traveller.entur.org/.well-known/jwks.json"));
        prodProviders.add(
                createProvider(
                        "https://partner.entur.org/", "https://partner.entur.org/.well-known/jwks.json"));
        prodProviders.add(
                createProvider(
                        "https://person.entur.org/", "https://person.entur.org/.well-known/jwks.json"));
        issuers.put("prod", prodProviders);
        issuers.put("prd", prodProviders);

        List<IssuerProperties> mockProviders = new ArrayList<>();
        mockProviders.add(
                createProvider(
                        "https://internal.mock.entur.io",
                        "http://localhost:${MOCKAUTHSERVER_PORT}/internal/.well-known/jwks.json"));
        mockProviders.add(
                createProvider(
                        "https://traveller.mock.entur.io",
                        "http://localhost:${MOCKAUTHSERVER_PORT}/traveller/.well-known/jwks.json"));
        mockProviders.add(
                createProvider(
                        "https://partner.mock.entur.io",
                        "http://localhost:${MOCKAUTHSERVER_PORT}/partner/.well-known/jwks.json"));
        mockProviders.add(
                createProvider(
                        "https://person.mock.entur.io",
                        "http://localhost:${MOCKAUTHSERVER_PORT}/person/.well-known/jwks.json"));
        issuers.put("mock", mockProviders);
    }

    public List<IssuerProperties> get(String environment, List<String> includeTenants) {
        if (environment == null || includeTenants == null || includeTenants.isEmpty()) {
            return Collections.emptyList();
        }

        return issuers.getOrDefault(environment, Collections.emptyList()).stream()
                .filter(provider -> filter(includeTenants, provider))
                .map(
                        issuerProperties ->
                                !issuerProperties.getCertificateUrl().contains("${MOCKAUTHSERVER_PORT}")
                                        ? issuerProperties
                                        : createProvider(
                                                issuerProperties.getIssuerUrl(),
                                                issuerProperties
                                                        .getCertificateUrl()
                                                        .replace(
                                                                "${MOCKAUTHSERVER_PORT}",
                                                                System.getProperty("MOCKAUTHSERVER_PORT"))))
                .toList();
    }

    private static IssuerProperties createProvider(String issuerUrl, String certificateUrl) {
        IssuerProperties provider = new IssuerProperties();
        provider.setIssuerUrl(issuerUrl);
        provider.setCertificateUrl(certificateUrl);
        return provider;
    }

    private static boolean filter(
            @NonNull List<String> includeTenants, @NonNull IssuerProperties provider) {
        return includeTenants.isEmpty()
                || includeTenants.stream()
                        .anyMatch(item -> provider.getIssuerUrl().contains(String.format("/%s", item)));
    }

    @NonNull
    public String getTenant(String authority) {
        final String result;
        if (authority.startsWith("partner.")) {
            result = "partner";
        } else if (authority.startsWith("internal.")) {
            result = "internal";
        } else if (authority.startsWith("traveller.")) {
            result = "traveller";
        } else if (authority.startsWith("person.")) {
            result = "person";
        } else {
            result = "unknown";
        }

        return result;
    }
}
