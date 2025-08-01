package org.entur.auth.junit.tenant;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.entur.auth.junit.jwt.EnturProvider;
import org.entur.auth.junit.jwt.PortReservation;
import org.entur.auth.junit.jwt.Provider;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * TenantJsonWebToken is a JUnit extension that implements both {@link
 * org.junit.jupiter.api.extension.ParameterResolver} and {@link
 * org.junit.jupiter.api.extension.BeforeEachCallback} to support the injection and generation of
 * tenant-specific JSON Web Tokens (JWT) during testing.
 *
 * <p>This extension sets up a WireMock authentication server, reserves a port, and initializes the
 * JWT token factories needed to create tokens for different tenant types. It supports injection for
 * parameters that are annotated with tenant-specific annotations (such as {@code PartnerTenant},
 * {@code InternalTenant}, {@code TravellerTenant}, and {@code PersonTenant}), as well as for
 * parameters of type {@link org.entur.auth.junit.tenant.TenantAnnotationTokenFactory} or {@link
 * com.github.tomakehurst.wiremock.WireMockServer}.
 *
 * <p>The supported tenant types are defined by the {@code SUPPORTED_TENANTS} array and correspond
 * to constants provided by the {@link org.entur.auth.junit.jwt.EnturProvider}.
 */
@Slf4j
public class TenantJsonWebToken implements ParameterResolver, BeforeAllCallback {
    /** The name used for reserving the port for the mock authentication server. */
    public static final String MOCKAUTHSERVER_PORT_NAME = "MOCKAUTHSERVER_PORT";

    private static final Provider provider = new EnturProvider();

    /**
     * Map of Tenant identifiers supported by the JWT token factory -> tenant-specific annotation
     * types that this extension supports.
     */
    private static final List<Class<? extends Annotation>> TENANT_LIST =
            List.of(
                    PartnerTenant.class,
                    InternalTenant.class,
                    TravellerTenant.class,
                    PersonTenant.class,
                    TenantToken.class);

    private static TenantAnnotationTokenFactory tokenFactory;
    private static PortReservation portReservation;

    /**
     * This before all callback initializes the WireMock authentication server, the JWT token factory,
     * and the certificate mappings.
     */
    @Override
    public void beforeAll(ExtensionContext context) {
        setupTokenFactory();
    }

    /**
     * Determines if the given parameter is supported by this extension.
     *
     * <p>A parameter is considered supported if it is annotated with one of the tenant annotations,
     * or if its type is either {@link org.entur.auth.junit.tenant.TenantAnnotationTokenFactory} or
     * {@link com.github.tomakehurst.wiremock.WireMockServer}.
     *
     * @param parameterContext the context for the parameter to be resolved
     * @param extensionContext the current extension context; never {@code null}
     * @return {@code true} if the parameter is supported; {@code false} otherwise
     * @throws org.junit.jupiter.api.extension.ParameterResolutionException if parameter resolution
     *     fails
     */
    @Override
    public boolean supportsParameter(
            ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {

        if (parameterContext.getParameter().getType() == String.class) {
            for (Class<? extends Annotation> c : TENANT_LIST) {
                if (parameterContext.findAnnotation(c).isPresent()) {
                    return true;
                }
            }
            return false;
        } else if (parameterContext.getParameter().getType() == WireMockAuthenticationServer.class) {
            return true;
        } else if (parameterContext.getParameter().getType() == TenantAnnotationTokenFactory.class) {
            return true;
        } else return parameterContext.getParameter().getType() == WireMock.class;
    }

    /**
     * Resolves the parameter for injection.
     *
     * <p>For parameters annotated with a tenant-specific annotation, this method returns a JSON Web
     * Token generated by the {@link org.entur.auth.junit.tenant.TenantAnnotationTokenFactory}. For
     * parameters of type {@link org.entur.auth.junit.tenant.TenantAnnotationTokenFactory}, it returns
     * the token factory instance.
     *
     * @param parameterContext the context for the parameter to be resolved
     * @param extensionContext the current extension context; never {@code null}
     * @return the resolved parameter value
     * @throws org.junit.jupiter.api.extension.ParameterResolutionException if the parameter cannot be
     *     resolved
     */
    @Override
    public Object resolveParameter(
            ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {

        if (parameterContext.getParameter().getType() == String.class) {
            for (var entry : TENANT_LIST) {
                Optional<? extends Annotation> annotation = parameterContext.findAnnotation(entry);
                if (annotation.isPresent()) {
                    return tokenFactory.createToken(annotation.get());
                }
            }
        } else if (parameterContext.getParameter().getType() == TenantAnnotationTokenFactory.class) {
            return tokenFactory;
        } else if (parameterContext.getParameter().getType() == WireMockAuthenticationServer.class) {
            return tokenFactory.getServer();
        } else if (parameterContext.getParameter().getType() == WireMock.class) {
            return tokenFactory.getServer().getMockServer();
        }

        throw new CanNotResolveParameterException();
    }

    public static void setupTokenFactory() {
        // this code should run before the spring context starts, but in case the class is not loaded,
        // the spring context will run first.
        // In which case the MOCKAUTHSERVER_PORT port must be a free port.
        if (portReservation == null) {
            portReservation = new PortReservation(MOCKAUTHSERVER_PORT_NAME);
        }

        // Initiate portReservation
        if (portReservation.getPort() < 0) {
            portReservation.start();
        }

        // Run with a fixed set of certificates so that spring context reuse works:
        // Context A loads the certificates, context B (i.e. not-dirty A context) uses those
        // certificates to validate tokens.
        if (tokenFactory == null) {
            tokenFactory = new TenantAnnotationTokenFactory(provider, portReservation);
        }
    }
}
