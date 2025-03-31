package org.entur.auth.junit.tenant;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.entur.auth.Provider;
import org.entur.auth.JwtTokenFactory;

/**
 * Factory for generating JWT tokens based on tenant annotations used in tests.
 * <p>
 * This utility is intended to work with JUnit tests where annotations are used to define
 * the desired tenant type and properties for the test context.
 * </p>
 * <p>
 * Supports the following tenant annotations:
 * <ul>
 *     <li>{@link PartnerTenant}</li>
 *     <li>{@link InternalTenant}</li>
 *     <li>{@link TravellerTenant}</li>
 *     <li>{@link PersonTenant}</li>
 * </ul>
 * </p>
 */
class TenantAnnotationTokenFactory {
    private final Provider provider;
    private final JwtTokenFactory jwtTokenFactory;

    /**
     * Constructs a new {@code TenantAnnotationTokenFactory}.
     *
     * @param provider         the {@link Provider} to use when creating tokens
     * @param jwtTokenFactory  the {@link JwtTokenFactory} to build the token with
     */
    public TenantAnnotationTokenFactory(final Provider provider, final JwtTokenFactory jwtTokenFactory) {
        this.provider = provider;
        this.jwtTokenFactory = jwtTokenFactory;
    }

    /**
     * Creates a bearer JWT token from the given tenant annotation.
     *
     * @param tenant the tenant annotation instance, must be one of the supported tenant types:
     *               {@link PartnerTenant}, {@link InternalTenant}, {@link TravellerTenant}, or {@link PersonTenant}
     * @return a bearer token string (with prefix {@code "Bearer "})
     * @throws IllegalArgumentException if the annotation is of an unknown tenant type
     */
    public String createToken(Annotation tenant) {
        if (tenant instanceof PartnerTenant) {
            PartnerTenant annotation = (PartnerTenant) tenant;
            return "Bearer " + jwtTokenFactory.jwtTokenBuilder()
                    .provider(provider)
                    .domain(EnturProvider.TENANT_PARTNER)
                    .subject(annotation.subject())
                    .audience(annotation.audience())
                    .expiresInMinutes(annotation.expiresInMinutes())
                    .claims(Map.of(
                            EnturProvider.CLAIM_AZP, annotation.clientId(),
                            EnturProvider.CLAIM_ORGANISATION_ID, annotation.organisationId(),
                            EnturProvider.CLAIM_EMAIL, annotation.email(),
                            EnturProvider.CLAIM_EMAIL_VERIFIED, annotation.emailVerified(),
                            EnturProvider.CLAIM_PREFERRED_USERNAME, annotation.username(),
                            EnturProvider.CLAIM_PERMISSIONS, annotation.permissions()
                            ))
                    .create();
        } else if (tenant instanceof InternalTenant) {
            InternalTenant annotation = (InternalTenant) tenant;
            return "Bearer " + jwtTokenFactory.jwtTokenBuilder()
                    .provider(provider)
                    .domain(EnturProvider.TENANT_INTERNAL)
                    .subject(annotation.clientId())
                    .audience(annotation.audience())
                    .expiresInMinutes(annotation.expiresInMinutes())
                    .claims(Map.of(
                            EnturProvider.CLAIM_AZP, annotation.clientId(),
                            EnturProvider.CLAIM_ORGANISATION_ID, annotation.organisationId()
                            ))
                    .create();
        } else if (tenant instanceof TravellerTenant) {
            TravellerTenant annotation = (TravellerTenant) tenant;
            return "Bearer " + jwtTokenFactory.jwtTokenBuilder()
                    .provider(provider)
                    .domain(EnturProvider.TENANT_TRAVELLER)
                    .audience(annotation.audience())
                    .expiresInMinutes(annotation.expiresInMinutes())
                    .claims(Map.of(
                            EnturProvider.CLAIM_AZP, annotation.clientId(),
                            EnturProvider.CLAIM_ORGANISATION_ID, annotation.organisationId(),
                            EnturProvider.CLAIM_CUSTOMER_NUMBER, annotation.customerNumber()
                            ))
                    .create();
        } else if (tenant instanceof PersonTenant) {
            PersonTenant annotation = (PersonTenant) tenant;
            return "Bearer " + jwtTokenFactory.jwtTokenBuilder()
                    .provider(provider)
                    .domain(EnturProvider.TENANT_PERSON)
                    .audience(annotation.audience())
                    .expiresInMinutes(annotation.expiresInMinutes())
                    .claims(Map.of(
                            EnturProvider.CLAIM_AZP, annotation.clientId(),
                            EnturProvider.CLAIM_ORGANISATION_ID, annotation.organisationId(),
                            EnturProvider.CLAIM_SOCIAL_SECURITY_NUMBER, annotation.socialSecurityNumber()
                    ))
                    .create();
        }
        throw new IllegalArgumentException("Unknown tenant " + tenant);
    }
}
