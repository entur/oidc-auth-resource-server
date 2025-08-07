package org.entur.auth.junit.tenant;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.lang.annotation.Annotation;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.entur.auth.junit.jwt.EnturProvider;
import org.entur.auth.junit.jwt.JwtTokenFactory;
import org.entur.auth.junit.jwt.PortReservation;
import org.entur.auth.junit.jwt.Provider;

/**
 * Utility for creating bearer JWT tokens in JUnit tests based on tenant-specific annotations.
 *
 * <p>When a test method or class is annotated with one of the supported tenant annotations, this
 * factory will provision a local WireMock-backed authentication server, register the corresponding
 * tenant realm, and generate a signed JWT with claims derived from the annotation attributes.
 *
 * <h2>Thread Safety</h2>
 *
 * <p>Instances are thread-safe: concurrent calls to {@link #createToken(Annotation)} will
 * synchronize on the underlying provider to ensure only one mock server lifecycle at a time.
 *
 * <h2>Supported Tenant Annotations</h2>
 *
 * <ul>
 *   <li>{@link org.entur.auth.junit.tenant.TenantToken}
 *   <li>{@link org.entur.auth.junit.tenant.PartnerTenant}
 *   <li>{@link org.entur.auth.junit.tenant.InternalTenant}
 *   <li>{@link org.entur.auth.junit.tenant.TravellerTenant}
 *   <li>{@link org.entur.auth.junit.tenant.PersonTenant}
 * </ul>
 *
 * <p>For each annotation, corresponding default claims (e.g. clientId, organisationId, permissions)
 * are mapped into the token payload automatically.
 */
@Slf4j
public class TenantAnnotationTokenFactory implements AutoCloseable {
    private final Provider provider;
    private final PortReservation portReservation;
    private JwtTokenFactory jwtTokenFactory;
    private WireMockAuthenticationServer server;

    /**
     * Create a new factory that leverages the given provider and port reservation for setting up a
     * local mock OpenID Connect server.
     *
     * @param provider the JWT provider used to build and sign tokens
     * @param portReservation manager for reserving and releasing a TCP port for WireMock
     */
    public TenantAnnotationTokenFactory(
            @NonNull final Provider provider, @NonNull final PortReservation portReservation) {
        this.provider = provider;
        this.portReservation = portReservation;

        /* Ensure the WireMock server is running, reserving the port if needed. */
        portReservation.stop();
        setServer(new WireMockAuthenticationServer(portReservation.getPort()));
    }

    /**
     * Get the active WireMock authentication server, starting it if necessary.
     *
     * @return the running {@link WireMockAuthenticationServer}
     */
    public WireMockAuthenticationServer getServer() {
        return server;
    }

    /**
     * Assign an existing WireMock server instance to use for token issuance.
     *
     * <p>This will close any previously opened server bound to the reserved port, and reinitialize
     * the JWT token builder.
     *
     * @param server a preconfigured {@link WireMockAuthenticationServer}
     */
    public void setServer(@NonNull WireMockAuthenticationServer server) {
        synchronized (provider) {
            if (this.server != null && this.server == server) {
                return;
            }

            close();

            log.info("Setup mock server on port {}", portReservation.getPort());
            this.jwtTokenFactory = new JwtTokenFactory(provider);
            this.server = server;
        }
    }

    /**
     * Convenience overload: configure a WireMock instance and port directly.
     *
     * @param wireMock raw WireMock client instance
     * @param port TCP port where WireMock should listen
     */
    public void setServer(@NonNull WireMock wireMock, int port) {
        synchronized (provider) {
            if (this.server != null && this.server.getMockServer() == wireMock) {
                return;
            }

            close();

            log.info("Setup mock server on port {}", port);
            this.jwtTokenFactory = new JwtTokenFactory(provider);
            this.server = new WireMockAuthenticationServer(wireMock, port);
        }
    }

    /**
     * Shut down the current mock server (if any) and release the reserved port.
     *
     * <p>Subsequent calls to {@link #getServer()} or {@link #createToken(Annotation)} will start a
     * fresh server.
     */
    @Override
    public void close() {
        synchronized (provider) {
            if (this.server == null) {
                return;
            }

            this.server.close();
            if (this.server.getPort() == this.portReservation.getPort()) {
                this.portReservation.start();
            }
            this.server = null;
        }
    }

    /**
     * Generate a Bearer JWT token for the given tenant annotation.
     *
     * <p>If no server is active, one will be started automatically.
     *
     * @param tenant an annotation instance indicating which tenant realm and claims to use
     * @return the complete Authorization header value (including "Bearer ")
     * @throws IllegalArgumentException if the annotation type is not one of the supported tenants
     */
    public String createToken(final Annotation tenant) {
        synchronized (provider) {
            return createToken(server, jwtTokenFactory, provider, tenant);
        }
    }

    /**
     * Internal dispatch logic for creating a JWT based on specific tenant annotation types.
     *
     * @param server the mock authentication server for stub mappings
     * @param jwtTokenFactory factory to construct JWT tokens
     * @param provider the signing provider for tokens
     * @param tenant the annotation instance; must be one of the supported types
     * @return a signed JWT string prefixed with "Bearer "
     * @throws IllegalArgumentException when an unknown annotation is passed
     */
    private static String createToken(
            final WireMockAuthenticationServer server,
            final JwtTokenFactory jwtTokenFactory,
            final Provider provider,
            final Annotation tenant) {
        if (tenant instanceof TenantToken annotation) {
            checkTenantExists(server, jwtTokenFactory, provider, annotation.tenant());

            Map<String, Object> claims = new HashMap<>();
            claims.putAll(
                    Arrays.stream(annotation.stringClaims())
                            .collect(Collectors.toMap(StringClaim::name, StringClaim::value)));

            claims.putAll(
                    Arrays.stream(annotation.stringArrayClaims())
                            .collect(Collectors.toMap(StringArrayClaim::name, StringArrayClaim::value)));

            claims.putAll(
                    Arrays.stream(annotation.longClaims())
                            .collect(Collectors.toMap(LongClaim::name, LongClaim::value)));

            claims.putAll(
                    Arrays.stream(annotation.longArrayClaims())
                            .collect(Collectors.toMap(LongArrayClaim::name, LongArrayClaim::value)));

            claims.putAll(
                    Arrays.stream(annotation.booleanClaims())
                            .collect(Collectors.toMap(BooleanClaim::name, BooleanClaim::value)));

            return "Bearer "
                    + jwtTokenFactory
                            .jwtTokenBuilder()
                            .provider(provider)
                            .domain(annotation.tenant())
                            .subject(annotation.subject())
                            .audience(annotation.audience())
                            .expiresAt(Instant.now().plusNanos(annotation.expiresIn()))
                            .claims(claims)
                            .create();
        } else if (tenant instanceof PartnerTenant annotation) {
            checkTenantExists(server, jwtTokenFactory, provider, EnturProvider.TENANT_PARTNER);

            return "Bearer "
                    + jwtTokenFactory
                            .jwtTokenBuilder()
                            .provider(provider)
                            .domain(EnturProvider.TENANT_PARTNER)
                            .subject(annotation.subject())
                            .audience(annotation.audience() == null ? null : new String[] {annotation.audience()})
                            .expiresAt(ZonedDateTime.now().plusMinutes(annotation.expiresInMinutes()).toInstant())
                            .claims(
                                    Map.of(
                                            EnturProvider.CLAIM_AZP, annotation.clientId(),
                                            EnturProvider.CLAIM_ORGANISATION_ID, annotation.organisationId(),
                                            EnturProvider.CLAIM_EMAIL, annotation.email(),
                                            EnturProvider.CLAIM_EMAIL_VERIFIED, annotation.emailVerified(),
                                            EnturProvider.CLAIM_PREFERRED_USERNAME, annotation.username(),
                                            EnturProvider.CLAIM_PERMISSIONS, annotation.permissions()))
                            .create();
        } else if (tenant instanceof InternalTenant annotation) {
            checkTenantExists(server, jwtTokenFactory, provider, EnturProvider.TENANT_INTERNAL);

            return "Bearer "
                    + jwtTokenFactory
                            .jwtTokenBuilder()
                            .provider(provider)
                            .domain(EnturProvider.TENANT_INTERNAL)
                            .subject(annotation.clientId())
                            .audience(annotation.audience() == null ? null : new String[] {annotation.audience()})
                            .expiresAt(ZonedDateTime.now().plusMinutes(annotation.expiresInMinutes()).toInstant())
                            .claims(
                                    Map.of(
                                            EnturProvider.CLAIM_AZP, annotation.clientId(),
                                            EnturProvider.CLAIM_ORGANISATION_ID, annotation.organisationId()))
                            .create();
        } else if (tenant instanceof TravellerTenant annotation) {
            checkTenantExists(server, jwtTokenFactory, provider, EnturProvider.TENANT_TRAVELLER);

            return "Bearer "
                    + jwtTokenFactory
                            .jwtTokenBuilder()
                            .provider(provider)
                            .domain(EnturProvider.TENANT_TRAVELLER)
                            .audience(annotation.audience() == null ? null : new String[] {annotation.audience()})
                            .expiresAt(ZonedDateTime.now().plusMinutes(annotation.expiresInMinutes()).toInstant())
                            .claims(
                                    Map.of(
                                            EnturProvider.CLAIM_AZP, annotation.clientId(),
                                            EnturProvider.CLAIM_ORGANISATION_ID, annotation.organisationId(),
                                            EnturProvider.CLAIM_CUSTOMER_NUMBER, annotation.customerNumber()))
                            .create();
        } else if (tenant instanceof PersonTenant annotation) {
            checkTenantExists(server, jwtTokenFactory, provider, EnturProvider.TENANT_PERSON);

            return "Bearer "
                    + jwtTokenFactory
                            .jwtTokenBuilder()
                            .provider(provider)
                            .domain(EnturProvider.TENANT_PERSON)
                            .audience(annotation.audience() == null ? null : new String[] {annotation.audience()})
                            .expiresAt(ZonedDateTime.now().plusMinutes(annotation.expiresInMinutes()).toInstant())
                            .claims(
                                    Map.of(
                                            EnturProvider.CLAIM_AZP,
                                            annotation.clientId(),
                                            EnturProvider.CLAIM_ORGANISATION_ID,
                                            annotation.organisationId(),
                                            EnturProvider.CLAIM_SOCIAL_SECURITY_NUMBER,
                                            annotation.socialSecurityNumber()))
                            .create();
        }
        throw new IllegalArgumentException("Unknown tenant " + tenant);
    }

    /**
     * If the tenant realm is not yet registered with the provider, add it and push new certificate
     * mappings to the server.
     */
    private static void checkTenantExists(
            final WireMockAuthenticationServer server,
            final JwtTokenFactory jwtTokenFactory,
            final Provider provider,
            final String tenant) {
        if (!jwtTokenFactory.containsTenant(provider, tenant)) {
            jwtTokenFactory.addTenants(provider, tenant);
            jwtTokenFactory.createCertificates().forEach(server::setJsonStubMappings);
        }
    }
}
