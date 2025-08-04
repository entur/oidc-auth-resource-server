package org.entur.auth.junit.jwt;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import lombok.NonNull;

/**
 * Factory for generating signed JSON Web Tokens (JWTs) and exposing public keys (JWKS) for multiple
 * tenants and authentication providers.
 *
 * <p>Maintains an in-memory cache of RSA key pairs per provider and tenant domain. Provides methods
 * to add tenants, check tenant existence, generate tokens, and expose the public keys as JSON Web
 * Key Sets (JWKS).
 */
public class JwtTokenFactory {
    /** Nested map of key pairs indexed first by provider name, then by tenant domain. */
    private final Map<String, Map<String, KeyPair>> keyPairsByTenantByProvider = new HashMap<>();

    /** Lookup map from provider name to its {@link Provider} instance. */
    private final Map<String, Provider> providerMap = new HashMap<>();

    /** RSA key-pair generator (2048-bit). */
    private final KeyPairGenerator keyGen;

    /**
     * Constructs a new {@code JwtTokenFactory} with no tenants. Initializes the internal RSA {@link
     * KeyPairGenerator}.
     *
     * @throws AlgorithmDoNotExistsException if the RSA algorithm is unavailable
     */
    public JwtTokenFactory() {
        keyGen = setupKeyPairGenerator();
    }

    /**
     * Constructs a new {@code JwtTokenFactory}, registers the given provider, and generates key pairs
     * for the specified tenants.
     *
     * @param provider the authentication provider to register
     * @param tenants one or more tenant domains for which to generate key pairs
     * @throws AlgorithmDoNotExistsException if the RSA algorithm is unavailable
     */
    public JwtTokenFactory(Provider provider, String... tenants) {
        this();
        addTenants(provider, tenants);
    }

    /**
     * Constructs a new {@code JwtTokenFactory}, registers the given provider, and generates key pairs
     * for the specified tenants.
     *
     * @param provider the authentication provider to register
     * @param tenants a collection of tenant domains for which to generate key pairs
     * @throws AlgorithmDoNotExistsException if the RSA algorithm is unavailable
     */
    public JwtTokenFactory(Provider provider, Collection<String> tenants) {
        this();
        tenants.forEach(tenant -> addTenants(provider, tenant));
    }

    /**
     * Registers the given provider (if not already present) and generates RSA key pairs for each of
     * the provided tenant domains.
     *
     * @param provider the authentication provider to register
     * @param tenants one or more tenant domains to initialize with RSA key pairs
     */
    public void addTenants(Provider provider, String... tenants) {
        synchronized (keyPairsByTenantByProvider) {
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
    }

    /**
     * Returns whether a key pair has been generated for the given provider and tenant domain.
     *
     * @param provider the authentication provider
     * @param tenant the tenant domain to check
     * @return {@code true} if a key pair exists for {@code tenant} under {@code provider}; {@code
     *     false} otherwise
     */
    public boolean containsTenant(Provider provider, String tenant) {
        var keyPairsMap = keyPairsByTenantByProvider.get(provider.getName());
        if (keyPairsMap == null) {
            return false;
        }
        return keyPairsMap.containsKey(tenant);
    }

    /**
     * Generates and signs a JWT for the specified provider and tenant.
     *
     * @param provider the authentication provider issuing the token
     * @param domain the tenant domain for which the token is issued
     * @param subject (optional) the JWT subject (e.g., user ID); may be {@code null}
     * @param audience (optional) array of audience values; may be {@code null}
     * @param claims (optional) additional custom claims to include; may be {@code null}
     * @param expiresAt the expiration time of the token (must be in the future)
     * @return a signed JWT compact‐serialization string
     * @throws IllegalArgumentException if no key pair exists for the given {@code provider}/{@code
     *     domain}
     */
    @lombok.Builder(builderMethodName = "jwtTokenBuilder", buildMethodName = "create")
    public String generateJwtToken(
            @NonNull Provider provider,
            @NonNull String domain,
            String subject,
            String[] audience,
            Map<String, Object> claims,
            Instant expiresAt) {
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
                        .expiration(Date.from(expiresAt));

        if (subject != null) {
            jwtBuilder.subject(subject);
        }

        if (audience != null) {
            var audienceBuilder = jwtBuilder.audience();
            Arrays.stream(audience).forEach(audienceBuilder::add);
        }

        if (claims != null) {
            claims.entrySet().stream()
                    .filter(stringObjectEntry -> stringObjectEntry.getValue() != null)
                    .forEach(
                            stringObjectEntry ->
                                    jwtBuilder.claim(stringObjectEntry.getKey(), stringObjectEntry.getValue()));
        }

        return jwtBuilder.signWith(keyPair.getPrivate()).compact();
    }

    /**
     * Builds a map of certificate endpoint paths to their JSON Web Key Set (JWKS) responses, one per
     * tenant and provider.
     *
     * @return a map where each key is the provider’s certificate URI for a tenant, and each value is
     *     the corresponding JWKS JSON string
     */
    public Map<String, String> createCertificates() {

        Map<String, String> certs = new HashMap<>();
        synchronized (keyPairsByTenantByProvider) {
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
        }

        return certs;
    }

    /**
     * Creates and configures a 2048-bit RSA {@link KeyPairGenerator}.
     *
     * @return a ready-to-use {@code KeyPairGenerator} for RSA 2048
     * @throws AlgorithmDoNotExistsException if the RSA algorithm is not supported
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
