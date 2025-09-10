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
 * JUnit extension for generating and injecting tenant-specific JSON Web Tokens (JWTs) into test
 * methods. This extension implements both {@link ParameterResolver} and {@link BeforeAllCallback}
 * to ensure a WireMock-based authentication server is started and a {@link
 * TenantAnnotationTokenFactory} is initialized once per test run.
 *
 * <p>Supported injection targets:
 *
 * <ul>
 *   <li>Parameters of type {@code String} annotated with one of the tenant annotations:
 *       {@code @PartnerTenant}, {@code @InternalTenant}, {@code @TravellerTenant},
 *       {@code @PersonTenant}, or {@code @TenantToken}. These will receive a JWT string for the
 *       corresponding tenant.
 *   <li>Parameters of type {@link TenantAnnotationTokenFactory}: injects the underlying token
 *       factory.
 *   <li>Parameters of type {@link WireMockAuthenticationServer}: injects the running WireMock
 *       authentication server.
 *   <li>Parameters of type {@link WireMock}: injects the WireMock client for stubbing and
 *       verification.
 * </ul>
 *
 * <p>Internally, the extension reserves a network port (named {@link #MOCKAUTHSERVER_PORT_NAME}),
 * starts the WireMock server on that port, and reuses a fixed set of signing certificates to ensure
 * Spring application context reuse across tests.
 *
 * @since 1.0
 */
@Slf4j
public class TenantJsonWebToken implements ParameterResolver, BeforeAllCallback {

    /** Key name used to reserve and retrieve the port for the mock authentication server. */
    public static final String MOCKAUTHSERVER_PORT_NAME = "MOCKAUTHSERVER_PORT";

    /** JWT provider implementation used to create tokens for supported tenants. */
    private static final Provider provider = new EnturProvider();

    /** List of tenant annotation types that this extension supports for parameter injection. */
    private static final List<Class<? extends Annotation>> TENANT_LIST =
            List.of(
                    PartnerTenant.class,
                    InternalTenant.class,
                    TravellerTenant.class,
                    PersonTenant.class,
                    TenantToken.class);

    /** Singleton token factory responsible for issuing tenant-specific JWTs. */
    private static TenantAnnotationTokenFactory tokenFactory;

    /** Manages reservation of the network port for the WireMock authentication server. */
    private static PortReservation portReservation;

    /** Construct a new TenantJsonWebToken and initializes the reserved port */
    public TenantJsonWebToken() {
        setupPortReservation();
    }

    /**
     * Invoked once before all tests in the current execution. Ensures the WireMock server and token
     * factory are initialized and ready for use.
     *
     * @param context the current {@link ExtensionContext}, never {@code null}
     */
    @Override
    public void beforeAll(ExtensionContext context) {
        setupTenantAnnotation();
    }

    /**
     * Determines whether this extension can supply a value for the given test method parameter.
     * Supported types are:
     *
     * <ul>
     *   <li>{@code String} parameters annotated with a supported tenant annotation
     *   <li>{@link TenantAnnotationTokenFactory}
     *   <li>{@link WireMockAuthenticationServer}
     *   <li>{@link WireMock}
     * </ul>
     *
     * @param parameterContext the context for the parameter to be resolved
     * @param extensionContext the current {@link ExtensionContext}, never {@code null}
     * @return {@code true} if this extension supports resolving the parameter; {@code false}
     *     otherwise
     * @throws ParameterResolutionException if parameter inspection fails
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
     * Resolves and injects the parameter value for supported test method parameters.
     *
     * <ul>
     *   <li>For {@code String} parameters annotated with a tenant annotation, returns a JWT for that
     *       tenant.
     *   <li>For {@link TenantAnnotationTokenFactory}, returns the configured token factory.
     *   <li>For {@link WireMockAuthenticationServer}, returns the running server instance.
     *   <li>For {@link WireMock}, returns the WireMock client for request stubbing.
     * </ul>
     *
     * @param parameterContext the context for the parameter to be resolved
     * @param extensionContext the current {@link ExtensionContext}, never {@code null}
     * @return the object to inject into the test method parameter
     * @throws ParameterResolutionException if the parameter is not supported or resolution fails
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

    /**
     * Initializes the {@link TenantAnnotationTokenFactory} and starts the WireMock server on a
     * reserved port if not already started. Ensures the same signing certificates are reused across
     * test contexts to support Spring context reuse.
     */
    public static void setupTokenFactory() {
        setupPortReservation();
        setupTenantAnnotation();
    }

    /** Initializes and starts the TenantAnnotationTokenFactory if not already started. */
    private static void setupTenantAnnotation() {
        synchronized (provider) {
            // Create the token factory once, linking it to the provider and reserved port
            if (tokenFactory == null) {
                tokenFactory = new TenantAnnotationTokenFactory(provider, portReservation);
            }
        }
    }

    /** Initializes and starts the reserved port if not already started. */
    private static void setupPortReservation() {
        synchronized (provider) {
            if (portReservation == null) {
                portReservation = new PortReservation(MOCKAUTHSERVER_PORT_NAME);
            }

            // Reserve and start the WireMock server port if needed
            if (portReservation.getPort() < 0) {
                portReservation.start();
            }
        }
    }
}
