package org.entur.auth;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import lombok.NonNull;

/** Factory for generating JWT tokens and managing cryptographic keys per tenant and provider. */
public class JwtTokenFactory {
    /** Stores key pairs for each tenant and provider. */
    private final Map<String, Map<String, KeyPair>> keyPairsByTenantByProvider =
            new ConcurrentHashMap<>();

    /** Maps provider names to their corresponding {@link Provider} instances. */
    private final Map<String, Provider> providerMap = new ConcurrentHashMap<>();

    private final KeyPairGenerator keyGen;

    /** Constructs a {@code TenantTokenFactory} with a default provider. */
    public JwtTokenFactory() {
        keyGen = setupKeyPairGenerator();
    }

    /**
     * Constructs a {@code TenantTokenFactory} with the given providers.
     *
     * @param provider to add to TenantTokenFactory
     * @param tenants to register for provider
     */
    public JwtTokenFactory(Provider provider, String... tenants) {
        this();
        addTenants(provider, tenants);
    }

    /**
     * Constructs a {@code TenantTokenFactory} with a collection of providers. Initializes
     * cryptographic key pairs for each provider and tenant.
     *
     * @param provider to add to TenantTokenFactory
     * @param tenants to register for provider
     */
    public JwtTokenFactory(Provider provider, Collection<String> tenants) {
        this();
        tenants.forEach(tenant -> addTenants(provider, tenant));
    }

    public void addTenants(Provider provider, String... tenants) {
        var keyPairsMap =
                keyPairsByTenantByProvider.computeIfAbsent(
                        provider.getName(),
                        providerName -> {
                            providerMap.put(provider.getName(), provider);
                            return new HashMap<>();
                        });
        Arrays.stream(tenants)
                .forEach(tenant -> keyPairsMap.computeIfAbsent(tenant, key -> keyGen.generateKeyPair()));
    }

    /**
     * Check if {@code TenantTokenFactory} contains a tenant.
     *
     * @param provider TenantTokenFactory to use
     * @param tenant to check for
     */
    public boolean containsTenant(Provider provider, String tenant) {
        var keyPairsMap = keyPairsByTenantByProvider.get(provider.getName());
        if (keyPairsMap == null) {
            return false;
        }
        return keyPairsMap.containsKey(tenant);
    }

    /**
     * Generates a JWT token for the specified provider and tenant.
     *
     * @param provider the authentication provider
     * @param domain the tenant domain
     * @param subject the subject (user identifier)
     * @param audience the audience of the token
     * @param claims additional claims to include in the token
     * @param expiresInMinutes the expiration time in minutes
     * @return the generated JWT token as a string
     * @throws IllegalArgumentException if the tenant is unknown for the given provider
     */
    @lombok.Builder(builderMethodName = "jwtTokenBuilder", buildMethodName = "create")
    public String generateJwtToken(
            @NonNull Provider provider,
            @NonNull String domain,
            String subject,
            String audience,
            Map<String, Object> claims,
            int expiresInMinutes) {
        Date expiresAt = Date.from(ZonedDateTime.now().plusMinutes(expiresInMinutes).toInstant());
        Map<String, KeyPair> providerMap = keyPairsByTenantByProvider.get(provider.getName());
        if (providerMap == null) {
            throw new IllegalArgumentException(
                    "Unknown domain " + domain + " for provider " + provider.getName());
        }

        KeyPair keyPair = providerMap.get(domain);

        JwtBuilder jwtBuilder =
                Jwts.builder()
                        .header()
                        .keyId(Provider.KEY_ID_DEFAULT)
                        .add("typ", "JWT")
                        .and()
                        .issuer(provider.getIssuerUrl(domain))
                        .expiration(expiresAt);

        if (subject != null) {
            jwtBuilder.subject(subject);
        }

        if (audience != null) {
            jwtBuilder.audience().add(audience);
        }

        if (claims != null) {
            claims.entrySet().stream()
                    .filter(stringObjectEntry -> stringObjectEntry.getValue() != null)
                    .forEach(
                            stringObjectEntry ->
                                    jwtBuilder.claim(
                                            provider.mapClaimName(stringObjectEntry.getKey()),
                                            stringObjectEntry.getValue()));
        }

        return jwtBuilder.signWith(keyPair.getPrivate()).compact();
    }

    /**
     * Creates a mapping of certificate paths to their corresponding JSON Web Key Set (JWKS)
     * responses.
     *
     * @return a map where keys are certificate paths and values are JWKS responses in JSON format
     */
    public Map<String, String> createCertificates() {

        Map<String, String> certs = new HashMap<>();

        for (Entry<String, Map<String, KeyPair>> keyPairsByTenantByProviderEntry :
                keyPairsByTenantByProvider.entrySet()) {
            for (Entry<String, KeyPair> entry : keyPairsByTenantByProviderEntry.getValue().entrySet()) {
                RSAPublicKey pk = (RSAPublicKey) entry.getValue().getPublic();
                String n = Base64.getUrlEncoder().encodeToString(pk.getModulus().toByteArray());
                String e = Base64.getUrlEncoder().encodeToString(pk.getPublicExponent().toByteArray());
                String certEndpoint =
                        providerMap.get(keyPairsByTenantByProviderEntry.getKey()).getCertPath(entry.getKey());

                String response =
                        String.format(
                                "{\"keys\":[{\"kid\":\"%s\",\"kty\":\"RSA\",\"alg\":\"RS256\",\"use\":\"sig\",\"n\":\"%s\",\"e\":\"%s\"}]}",
                                Provider.KEY_ID_DEFAULT, n, e);

                certs.put(certEndpoint, response);
            }
        }

        return certs;
    }

    /**
     * Sets up and returns a new RSA {@link KeyPairGenerator} with a key size of 2048 bits.
     *
     * @return the initialized {@code KeyPairGenerator}
     * @throws AlgorithmDoNotExistsException if RSA algorithm is not available
     */
    private static KeyPairGenerator setupKeyPairGenerator() {
        KeyPairGenerator keyGen;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);

            return keyGen;
        } catch (NoSuchAlgorithmException e) {
            throw new AlgorithmDoNotExistsException(e);
        }
    }
}
