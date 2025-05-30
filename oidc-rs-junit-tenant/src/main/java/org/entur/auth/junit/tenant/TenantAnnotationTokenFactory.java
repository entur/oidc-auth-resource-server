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
 * Factory for generating JWT tokens based on tenant annotations used in tests.
 *
 * <p>This utility is intended to work with JUnit tests where annotations are used to define the
 * desired tenant type and properties for the test context.
 *
 * <p>Supports the following tenant annotations:
 *
 * <ul>
 *   <li>{@link org.entur.auth.junit.tenant.TenantToken}
 *   <li>{@link org.entur.auth.junit.tenant.PartnerTenant}
 *   <li>{@link org.entur.auth.junit.tenant.InternalTenant}
 *   <li>{@link org.entur.auth.junit.tenant.TravellerTenant}
 *   <li>{@link org.entur.auth.junit.tenant.PersonTenant}
 * </ul>
 */
@Slf4j
public class TenantAnnotationTokenFactory implements AutoCloseable {
    private final Provider provider;
    private final PortReservation portReservation;
    private JwtTokenFactory jwtTokenFactory;
    private WireMockAuthenticationServer server;

    /**
     * Constructs a new {@code TenantAnnotationTokenFactory}.
     *
     * @param provider the {@link org.entur.auth.junit.jwt.Provider} to use when creating tokens
     * @param portReservation the {@link org.entur.auth.junit.jwt.PortReservation} port to use when
     *     creating mock server
     */
    public TenantAnnotationTokenFactory(
            @NonNull final Provider provider, @NonNull final PortReservation portReservation) {
        this.provider = provider;
        this.portReservation = portReservation;
    }

    public WireMockAuthenticationServer getServer() {
        checkServer();
        return server;
    }

    public void setServer(@NonNull WireMockAuthenticationServer server) {
        if (this.server != null && this.server == server) {
            return;
        }

        close();

        log.info("Setup mock server on port {}", portReservation.getPort());
        this.jwtTokenFactory = new JwtTokenFactory(provider);
        this.server = server;
    }

    public void setServer(@NonNull WireMock wireMock, int port) {
        if (this.server != null && this.server.getMockServer() == wireMock) {
            return;
        }

        close();

        log.info("Setup mock server on port {}", port);
        this.jwtTokenFactory = new JwtTokenFactory(provider);
        this.server = new WireMockAuthenticationServer(wireMock, port);
    }

    public void close() {
        if (this.server == null) {
            return;
        }

        this.server.close();
        if (this.server.getPort() == this.portReservation.getPort()) {
            this.portReservation.start();
        }
        this.server = null;
    }

    /**
     * Creates a bearer JWT token from the given tenant annotation.
     *
     * @param tenant the tenant annotation instance, must be one of the supported tenant types: {@link
     *     org.entur.auth.junit.tenant.PartnerTenant}, {@link
     *     org.entur.auth.junit.tenant.InternalTenant}, {@link
     *     org.entur.auth.junit.tenant.TravellerTenant}, or {@link
     *     org.entur.auth.junit.tenant.PersonTenant}
     * @return a bearer token string (with prefix {@code "Bearer "})
     * @throws IllegalArgumentException if the annotation is of an unknown tenant type
     */
    public String createToken(final Annotation tenant) {
        checkServer();

        return createToken(server, jwtTokenFactory, provider, tenant);
    }

    private void checkServer() {
        if (this.server == null) {
            portReservation.stop();
            setServer(new WireMockAuthenticationServer(portReservation.getPort()));
        }
    }

    /**
     * Creates a bearer JWT token from the given tenant annotation.
     *
     * @param jwtTokenFactory the {@link org.entur.auth.junit.jwt.JwtTokenFactory} to build the token
     *     with
     * @param provider the {@link org.entur.auth.junit.jwt.Provider} to use when creating tokens
     * @param tenant the tenant annotation instance, must be one of the supported tenant types: {@link
     *     org.entur.auth.junit.tenant.PartnerTenant}, {@link
     *     org.entur.auth.junit.tenant.InternalTenant}, {@link
     *     org.entur.auth.junit.tenant.TravellerTenant}, or {@link
     *     org.entur.auth.junit.tenant.PersonTenant}
     * @return a bearer token string (with prefix {@code "Bearer "})
     * @throws IllegalArgumentException if the annotation is of an unknown tenant type
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
