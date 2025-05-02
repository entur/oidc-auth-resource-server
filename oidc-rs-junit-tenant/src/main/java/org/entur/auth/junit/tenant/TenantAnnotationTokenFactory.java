package org.entur.auth.junit.tenant;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.lang.annotation.Annotation;
import java.util.Map;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.entur.auth.JwtTokenFactory;
import org.entur.auth.PortReservation;
import org.entur.auth.Provider;
import org.entur.auth.WireMockAuthenticationServer;

/**
 * Factory for generating JWT tokens based on tenant annotations used in tests.
 *
 * <p>This utility is intended to work with JUnit tests where annotations are used to define the
 * desired tenant type and properties for the test context.
 *
 * <p>Supports the following tenant annotations:
 *
 * <ul>
 *   <li>{@link org.entur.auth.junit.tenant.PartnerTenant}
 *   <li>{@link org.entur.auth.junit.tenant.InternalTenant}
 *   <li>{@link org.entur.auth.junit.tenant.TravellerTenant}
 *   <li>{@link org.entur.auth.junit.tenant.PersonTenant}
 * </ul>
 */
@Slf4j
public class TenantAnnotationTokenFactory {
    /** The name used for reserving the port for the mock authentication server. */
    public static final String MOCKAUTHSERVER_PORT_NAME = "MOCKAUTHSERVER_PORT";

    private final Provider provider;
    private final PortReservation portReservation;
    private JwtTokenFactory jwtTokenFactory;
    private WireMockAuthenticationServer server;

    /**
     * Constructs a new {@code TenantAnnotationTokenFactory}.
     *
     * @param provider the {@link org.entur.auth.Provider} to use when creating tokens
     * @param portReservation the {@link org.entur.auth.PortReservation} port to use when creating
     *     mock server
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

        if (this.server != null) {
            this.server.close();
        }

        log.info("Setup mock server on port {}", portReservation.getPort());
        this.jwtTokenFactory = new JwtTokenFactory(provider);
        this.server = server;
    }

    public void setServer(@NonNull WireMock wireMock, int port) {
        if (this.server != null && this.server.getMockServer() == wireMock) {
            return;
        }

        if (this.server != null) {
            this.server.close();
        }

        log.info("Setup mock server on port {}", portReservation.getPort());
        this.jwtTokenFactory = new JwtTokenFactory(provider);
        this.server = new WireMockAuthenticationServer(wireMock, port);
    }

    public void shutdown() {
        this.server.close();
        this.server = null;
        this.portReservation.start();
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
     * @param jwtTokenFactory the {@link org.entur.auth.JwtTokenFactory} to build the token with
     * @param provider the {@link org.entur.auth.Provider} to use when creating tokens
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
        if (tenant instanceof PartnerTenant) {
            checkTenantExists(server, jwtTokenFactory, provider, EnturProvider.TENANT_PARTNER);

            PartnerTenant annotation = (PartnerTenant) tenant;
            return "Bearer "
                    + jwtTokenFactory
                            .jwtTokenBuilder()
                            .provider(provider)
                            .domain(EnturProvider.TENANT_PARTNER)
                            .subject(annotation.subject())
                            .audience(annotation.audience())
                            .expiresInMinutes(annotation.expiresInMinutes())
                            .claims(
                                    Map.of(
                                            EnturProvider.CLAIM_AZP, annotation.clientId(),
                                            EnturProvider.CLAIM_ORGANISATION_ID, annotation.organisationId(),
                                            EnturProvider.CLAIM_EMAIL, annotation.email(),
                                            EnturProvider.CLAIM_EMAIL_VERIFIED, annotation.emailVerified(),
                                            EnturProvider.CLAIM_PREFERRED_USERNAME, annotation.username(),
                                            EnturProvider.CLAIM_PERMISSIONS, annotation.permissions()))
                            .create();
        } else if (tenant instanceof InternalTenant) {
            checkTenantExists(server, jwtTokenFactory, provider, EnturProvider.TENANT_INTERNAL);

            InternalTenant annotation = (InternalTenant) tenant;
            return "Bearer "
                    + jwtTokenFactory
                            .jwtTokenBuilder()
                            .provider(provider)
                            .domain(EnturProvider.TENANT_INTERNAL)
                            .subject(annotation.clientId())
                            .audience(annotation.audience())
                            .expiresInMinutes(annotation.expiresInMinutes())
                            .claims(
                                    Map.of(
                                            EnturProvider.CLAIM_AZP, annotation.clientId(),
                                            EnturProvider.CLAIM_ORGANISATION_ID, annotation.organisationId()))
                            .create();
        } else if (tenant instanceof TravellerTenant) {
            checkTenantExists(server, jwtTokenFactory, provider, EnturProvider.TENANT_TRAVELLER);

            TravellerTenant annotation = (TravellerTenant) tenant;
            return "Bearer "
                    + jwtTokenFactory
                            .jwtTokenBuilder()
                            .provider(provider)
                            .domain(EnturProvider.TENANT_TRAVELLER)
                            .audience(annotation.audience())
                            .expiresInMinutes(annotation.expiresInMinutes())
                            .claims(
                                    Map.of(
                                            EnturProvider.CLAIM_AZP, annotation.clientId(),
                                            EnturProvider.CLAIM_ORGANISATION_ID, annotation.organisationId(),
                                            EnturProvider.CLAIM_CUSTOMER_NUMBER, annotation.customerNumber()))
                            .create();
        } else if (tenant instanceof PersonTenant) {
            checkTenantExists(server, jwtTokenFactory, provider, EnturProvider.TENANT_PERSON);

            PersonTenant annotation = (PersonTenant) tenant;
            return "Bearer "
                    + jwtTokenFactory
                            .jwtTokenBuilder()
                            .provider(provider)
                            .domain(EnturProvider.TENANT_PERSON)
                            .audience(annotation.audience())
                            .expiresInMinutes(annotation.expiresInMinutes())
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
