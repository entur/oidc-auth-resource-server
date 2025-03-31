package org.entur.auth.junit.tenant;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.entur.auth.PortReservation;
import org.entur.auth.Provider;
import org.entur.auth.JwtTokenFactory;
import org.entur.auth.WireMockAuthenticationServer;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.annotation.Annotation;
import java.util.Optional;

/**
 * TenantJsonWebToken is a JUnit extension that implements both {@link ParameterResolver} and
 * {@link BeforeEachCallback} to support the injection and generation of tenant-specific JSON Web Tokens (JWT)
 * during testing.
 * <p>
 * This extension sets up a WireMock authentication server, reserves a port, and initializes the JWT token
 * factories needed to create tokens for different tenant types. It supports injection for parameters that are
 * annotated with tenant-specific annotations (such as {@code PartnerTenant}, {@code InternalTenant},
 * {@code TravellerTenant}, and {@code PersonTenant}), as well as for parameters of type
 * {@link TenantAnnotationTokenFactory} or {@link WireMockServer}.
 * <p>
 * The supported tenant types are defined by the {@code SUPPORTED_TENANTS} array and correspond to constants
 * provided by the {@link EnturProvider}.
 */
public class TenantJsonWebToken implements ParameterResolver, BeforeEachCallback {
	/**
	 * The name used for reserving the port for the mock authentication server.
	 */
	public static final String MOCKAUTHSERVER_PORT_NAME = "MOCKAUTHSERVER_PORT";

	/**
	 * Tenant identifiers supported by the JWT token factory.
	 */
	private static final String[] SUPPORTED_TENANTS = {EnturProvider.TENANT_PARTNER, EnturProvider.TENANT_INTERNAL, EnturProvider.TENANT_TRAVELLER, EnturProvider.TENANT_PERSON};

	/**
	 * An array of tenant-specific annotation types that this extension supports.
	 */
	@SuppressWarnings("unchecked")
	private static final Class<? extends Annotation>[] ANNOTATIONS = new Class[]{
			PartnerTenant.class,
			InternalTenant.class,
			TravellerTenant.class,
			PersonTenant.class
	};

	private static Provider provider = new EnturProvider();
	private static JwtTokenFactory jwtTokenFactory;
	private static TenantAnnotationTokenFactory tokenFactory;
	private static PortReservation portReservation;
	private static WireMockAuthenticationServer server;

	/**
	 * Constructs a new TenantJsonWebToken extension.
	 * <p>
	 * This constructor initializes the WireMock authentication server, the JWT token factory,
	 * and the certificate mappings.
	 */
	public TenantJsonWebToken() {
		setupWireMock();
		setupTokenFactory();
		setupCertificates();
	}

	/**
	 * Sets up the WireMock authentication server.
	 * <p>
	 * This method reserves a port (if not already reserved) and creates a {@link WireMockAuthenticationServer}
	 * instance on that port. It ensures that the port reservation is started if not already active.
	 */
	private static void setupWireMock() {
		// this code should run before the spring context starts, but in case the class is not loaded,
		// the spring context will run first. In which case the MOCKAUTHSERVER_PORT port must be a free port
		if (portReservation == null) {
			portReservation = new PortReservation(MOCKAUTHSERVER_PORT_NAME);
		}

		// Reserve port
		if(portReservation.getPort() < 0) {
			portReservation.start();
		}

		// Create WireMockAuthenticationServer
		if (server == null) {
			server = new WireMockAuthenticationServer(portReservation.getPort());
		}
	}


	/**
	 * Initializes the JWT token factory and tenant annotation token factory.
	 * <p>
	 * This method creates the {@link JwtTokenFactory} using the provided {@link Provider} and the list
	 * of supported tenants. It also registers a default tenant token for the partner tenant and initializes
	 * the {@link TenantAnnotationTokenFactory}.
	 */
	private static void setupTokenFactory() {
		// Create Entur provider
		if (provider == null) {
			provider = new EnturProvider();
		}

		// Create TenantTokenFactory
		if (jwtTokenFactory == null) {
			jwtTokenFactory = new JwtTokenFactory(provider, SUPPORTED_TENANTS);
			jwtTokenFactory.addTenants(provider, EnturProvider.TENANT_PARTNER);
		}

		// Run with a fixed set of certificates so that spring context reuse works:
		// Context A loads the certificates, context B (i.e. not-dirty A context) uses those certificates to validate tokens.
		if (tokenFactory == null) {
			tokenFactory = new TenantAnnotationTokenFactory(provider, jwtTokenFactory);
		}
	}

	/**
	 * Sets up certificates for token validation.
	 * <p>
	 * This method uses the {@link JwtTokenFactory} to create certificates and registers each certificate's JSON stub
	 * mappings on the WireMock server.
	 */
	private static void setupCertificates() {
		jwtTokenFactory.createCertificates()
				.forEach(( certEndpoint, response) -> server.setJsonStubMappings(certEndpoint, response));
	}

	/**
	 * Determines if the given parameter is supported by this extension.
	 * <p>
	 * A parameter is considered supported if it is annotated with one of the tenant annotations, or if its type
	 * is either {@link TenantAnnotationTokenFactory} or {@link WireMockServer}.
	 *
	 * @param parameterContext the context for the parameter to be resolved
	 * @param extensionContext the current extension context; never {@code null}
	 * @return {@code true} if the parameter is supported; {@code false} otherwise
	 * @throws ParameterResolutionException if parameter resolution fails
	 */
	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
		for (Class<? extends Annotation> c : ANNOTATIONS) {
			if (parameterContext.findAnnotation(c).isPresent()) {
				return true;
			}
		}
		if (parameterContext.getParameter().getType() == TenantAnnotationTokenFactory.class) {
			return true;
		}

		return parameterContext.getParameter().getType() == WireMockServer.class;
	}

	/**
	 * Resolves the parameter for injection.
	 * <p>
	 * For parameters annotated with a tenant-specific annotation, this method returns a JSON Web Token generated
	 * by the {@link TenantAnnotationTokenFactory}. For parameters of type {@link TenantAnnotationTokenFactory}, it returns
	 * the token factory instance. For parameters of type {@link WireMockServer}, it returns the underlying WireMock server.
	 *
	 * @param parameterContext the context for the parameter to be resolved
	 * @param extensionContext the current extension context; never {@code null}
	 * @return the resolved parameter value
	 * @throws ParameterResolutionException if the parameter cannot be resolved
	 */
	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
		for (Class<? extends Annotation> c : ANNOTATIONS) {
			Optional<? extends Annotation> annotation = parameterContext.findAnnotation(c);
			if (annotation.isPresent()) {
				return tokenFactory.createToken(annotation.get());
			}
		}
		if (parameterContext.getParameter().getType() == TenantAnnotationTokenFactory.class) {
			return tokenFactory;
		}
		if (parameterContext.getParameter().getType() == WireMockServer.class) {
			return server.getMockServer();
		}
		throw new CanNotResolveParameterException();
	}

	/**
	 * Callback that is invoked before each test execution.
	 * <p>
	 * This method ensures that the WireMock server is running before each test.
	 *
	 * @param context the current extension context; never {@code null}
	 */
	@Override
	public void beforeEach(ExtensionContext context) {
		startIfNotRunning();
	}

	/**
	 * Starts the WireMock server if it is not already running.
	 * <p>
	 * If the server is not running, this method stops the port reservation, starts the server,
	 * and yields until the server is confirmed to be running.
	 */
	protected void startIfNotRunning() {
		if (!server.isRunning()) {
			portReservation.stop();
			server.start();
			while (!server.isRunning()) {
				Thread.yield();
			}
		}
	}

	/**
	 * Creates a JSON Web Token based on the provided tenant annotation.
	 *
	 * @param tenant the tenant annotation used to determine the token details
	 * @return the generated JSON Web Token as a {@link String}
	 */
	public String createToken(Annotation tenant) {
		return tokenFactory.createToken(tenant);
	}
}