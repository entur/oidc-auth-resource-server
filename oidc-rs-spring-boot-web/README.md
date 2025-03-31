# Oidc Auth Resource Server for Spring Booot 3 Servlet 
This artifact oauth2-rs-spring-boot-3-web will help you set up Spring OAuth 2.0 Resource Server JWT with Multitenancy support. 
This Spring Security configuration will help protecting REST endpoints by using JWT Bearer tokens for authentication.

## Table of Contents
[TOC]

## Requirements

| Requirement    | Functionality               | Comment                                               |
|----------------|-----------------------------|-------------------------------------------------------|
| Java 17+       | All                         | Java 17 is the minimum requirement for Spring Boot 3. |
| Spring Boot 3  | Spring Security integration |                                                       |


## Quickstart
In this getting started guide we will describe how to set up a Spring Boot 3 application with authentication delivered by oidc-auth.

Links to other frameworks and services that are mentioned/relevant for this guide:

* [Spring Boot 3](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#getting-started)
* [Spring Security OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
* [The DevOps Handbook](https://enturas.atlassian.net/wiki/spaces/ESP/pages/912392213/The+DevOps+Handbook)
* [Application Security Requirements](https://enturas.atlassian.net/wiki/spaces/ESP/pages/3293511836/Application+Security+Requirements)
* [Technical Platform Documentation](https://enturas.atlassian.net/wiki/spaces/ESP/pages/3649273997/Technical+Platform+Documentation)

### Setup Spring Boot Application
Oidc Auth Resource Server for Spring Booot 3 Servlet has built-in support for Spring Boot autoconfiguration and will give you an easy way to configure Permission Client via application.yaml.

This getting started will also build on procedures from The DevOps Handbook and Technical Platform Documentation.

Verify your Spring Boot Application setup:

* Check you have set up Spring Boot 3 application without taken any action to disable autoconfiguration.
* Configured to load dependencies from JFrog. See [JFrog settings example](#JFrog-settings-example)
* Application and junit tests can be run locally.

### Add Oidc Auth Resource Server dependencies
To use Oidc Auth Resource Server for Spring Booot 3 Servlet it is necessary to add the following dependencies to your build.gradle file:
```groovy
ext {
     oidcAuthVersion = "<Version>"
}

dependencies {
     implementation("org.entur.auth:oauth2-rs-spring-boot-3-web:${oidcAuthVersion}")
     testImplementation("org.entur.auth:oauth2-rs-spring-boot-3-web-test:${oidcAuthVersion}")   // Optional to support junit testing
}
```

### Setup application.yaml
```yaml
entur:
  # Setup environment and tenants using predefines configuration. 
  # Alternative see documentation below for provide your own configuration. 
  auth:
    tenants:
      environment: dev | tst | prd     # Example: ${jwt_environment:dev}
      include: internal,traveller,partner,person
      
  # Optional setup of cors for your application. See own chapter for more settings.
    cors:
      mode: api | webapp | default
      hosts:
           - list_of_hosts
           - https://myapp.entur.org
           - https://*.example1.com # domains ending with example1.com
           - https://*.example2.com:[8080,8081] # domains ending with example2.com on port 8080 or port 8081
           - https://*.example3.com:[*] # domains ending with example3.com on any port, including the default port             
      
# Optional setup of management health with readiness to let Kubernetes take actions based on jwks cache status.      
management:
     endpoints.web.exposure.include: health, readiness  # Add others if needed
     endpoint:
          health:
               probes.enabled: true
               show-details: always   # Optional
               group.readiness.include: readinessState, jwksState


```

### Setup application-test.yaml
```yaml
# Test related options for using @ExtendWith({SpringExtension.class, TenantJsonWebToken.class})
entur:
  auth:
    lazy-load: true                 # Use true to delay load of JWKS until wiremock is started
    test.load-issuers: true         # If true your test will load JWKS from issuers settings, default false
    test.load-environments: false   # If true your test will load JWKS from environments settings, default false
    test.load-external: false       # If true your test will load JWKS from external settings, default false

    issuers:                        # Mock JWKS domains to be provided from local wiremock.
         - issuerUrl: https://partner.mock.entur.io
           certificateUrl: http://localhost:${MOCKAUTHSERVER_PORT}/partner/.well-known/jwks.json
         - issuerUrl: https://internal.mock.entur.io
           certificateUrl: http://localhost:${MOCKAUTHSERVER_PORT}/internal/.well-known/jwks.json
```

## Migrate from oidc-auth-spring-boot-starter
### Remove from build.gradle
```
implementation("org.entur.auth:oidc-auth-spring-boot-starter:${oidcAuthVersion}")
testImplementation("org.entur.auth:oidc-auth-spring-boot-starter-test:${oidcAuthVersion}")
testImplementation("org.entur.auth:oidc-auth-jwt-junit:${oidcAuthVersion}")
```
### Add to build.gradle
```
implementation("org.entur.auth:oauth2-rs-spring-boot-3-web:${oidcAuthVersion}")
testImplementation("org.entur.auth:oauth2-rs-spring-boot-3-web-test:${oidcAuthVersion}")
```
### Rewrite Java code
Search in your code for
```
var authentication = SecurityContextHolder.getContext().getAuthentication();
```
returned principal object in authentication have changed from Entur Tenant to Spring Jwt.
Conversion can be done with helper class JwtConverter.

### Application.yaml
If you have confgured use test certificates in your yaml files like this:
```
entur:
  auth:
     issuers:
        - issuerUrl: https://partner.mock.entur.io
          certificateUrl: http://localhost:${MOCKAUTHSERVER_PORT}/partner/.well-known/jwks.json
          authProvider: AUTH0
          certificateReloadPeriodInMinutes: 7
          fallbackCertificateReloadPeriodInMinutes: 70
```
Then you should add to your test yaml file: 
```
entur:
  auth:
     lazy-load: true
     test.load-issuers: true
```

### JUnit extention TenantJsonWebToken
If you want to use @InternalTenant in combination with Permission Client, 
notice that clientId value will also be used as subject in access token. 

### Web Mvc Configurer
This starter will set register en argument resolver to support Tenant, Partner, Internal and Traveller objects. This is implemented like this:

```java
public static class JWTWebMvcConfig implements WebMvcConfigurer {
     @Override
     public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
          resolvers.add(new TenantArgumentResolver());
     }
}
```

## Configuring your application

This starter loads configuration properties via the Spring context (from appliation.yaml and other sources).

### Tenant configuration
The possible Auth0 tentants are 

 * partner
 * internal
 * traveller
 * person

#### Tenant configuration via environment setting
Default tenant configuration can be used by setting
``` yml
entur.auth.tenants.environment = dev | tst | stage | prod | prd
entur.auth.tenants.include=internal,traveller,partner,person
``` 

#### Tenant configuration via Kubernets Config Map
For applications running in Kubernetes, using external configuration in form of a `Config Map` might be a good alternative. Configure the following properties via your application.yaml, command-line system property or environment variables:

  *  `entur.auth.external.resource` - path to properties file, e.g. `file:/etc/entur/oidc-auth/providers.properties` (YAML is not supported)
  *  `entur.auth.external.tenants` - list of legal tenants.
  *  `entur.auth.external.providers` - list of legal providers (AUTH0).
  
Note that the properties file is assumed to contain all possible tenants and providers,
so __make sure the `entur.auth.external.tenants` value is correct__. 

Platform team can help creating/updating the `Config Map`, most likely a shared map already exists per environment. Check for the config map using the command

> kubectl get cm oidc-auth-config -o yaml

The result should contain a file `providers.properties` with the full set of possible tenant/authentication providers. Add the following to your Kubernetes configuration

```
spec:
  containers:
  - ..
    volumeMounts:
        - name: oidc-auth-volume
          mountPath: /etc/entur/oidc-auth
  volumes:
    - name: oidc-auth-volume
      configMap:
        name: oidc-auth-config
```

to mount the file `/etc/entur/oidc-auth/providers.properties`. See [full example](src/main/kubernetes/kubernetes-example.yaml).

If the deployment has multiple containers, __double check that the above is mounted on the correct container__. Remember to refresh the deployment-configuration completely. 

View the result using

> kubectl get deployment my-app -o yaml

## Auth0 API configuration 
APIs (also know as audience) can be specified (per environment) using the property

  *  `entur.auth.apis` - a list of the following values
     * `issuerUrl` - issuer identifier, like `https://partner.staging.entur.org/` (see [property files](src/main/kubernetes) in this project).
     * `audiences` - APIs to be supported, like `https://personalbillett.entur.org`

Note that tokens from other APIs will be rejected, so be careful when locking down your service to a specific API.

## Authorization
The base requirement is that all requests must be so-called _fully authenticated_ - in other words having a valid JWT token (from any of the configured tenants). 

Exceptions must be explicitly configured, except for the following list:

 * all `actuator` endpoints
 * OpenAPI-definition at `/v2/api-docs` or `/v3/api-docs`

### Authorization filter configuration
The authorization filter runs before the request/response-filter, and enforces the use of _fully authenticated_.

Open endpoints (i.e. permitted for all, open to the world) are configured using matcher:

```
entur:
  auth:
    authorization:
      permit-all:
        matcher:
          patterns:
           - /unprotected/*
           - /some/path/{myVariable}
          method:
            get:
              patterns:
               - /some/path/{myVariable}
```
Note that Spring Boot 3 uses ant matcher by default and MVC matcher can be used if HandlerMappingIntrospector is registrated. 
Ant-matcher amd mvc-matcher syntax is deprecated. 

## Adding fine-grained authorization checks in your Controller using annotations

When using the security library, you can secure endpoints by adding the `@PreAuthorize` annotation. This annotation can receive a list of authorities. See the following code example to see a basic implementation. The auth library also makes it possible to add the `Tenant` object to the argument list of your endpoint. This object is the `Tenant` that has been generated from the authorization header.

```java
@RestController
public class TestController {

    @GetMapping(value = "/realm",  produces = arrayOf(MediaType.TEXT_PLAIN_VALUE))
    @PreAuthorize("hasAnyAuthority('traveller', 'internal', 'partner')")
    public String authenticatedEndpoint(Tenant tenant){
        return tenant.getAuthority();
    }
}
```

If you for some reason want to support unauthenticated requests in the same method which injects `Tenant`, add @Nullable to the `Tenant` argument. 

`Partner`, `Internal` and `Traveller` subtypes can also be directly injected (always combine this with `@PreAuthorize`):

```java
@RestController
public class MyController {

    @GetMapping(value = "/partnerResource",  produces = arrayOf(MediaType.TEXT_PLAIN_VALUE))
    @PreAuthorize("hasAnyAuthority('partner')")
    public String authenticatedEndpoint(Partner partner){
        // ... partner specific stuff
    }
}
```

## Configure network connection timeouts
Add 

```
entur:
  auth:
    readTimeoutInSeconds: 3
    connectTimeoutInSeconds: 3
```

to configure connect- and read timeouts for getting token validation certificates from the authentication provider. This is usually not necessary, so use only if you're seeing connection timeouts.

## Cross-Origin Resource Sharing (CORS)

```
entur:
  auth:
    cors: 
      enabled: true    # optional and default is true
      mode: api | webapp | default
      hosts:
        - https://myapp.entur.org
        - https://myotherpetstore.swagger.io
        - https://*.example1.com # domains ending with example1.com
        - https://*.example2.com:[8080,8081] # domains ending with example2.com on port 8080 or port 8081
        - https://*.example3.com:[*] # domains ending with example3.com on any port, including the default port             
        
```

where `xyz` is from the following list

* api - CORS requests from the API development portal & petstore plus the hosts specified in the `hosts` property list are accepted.  
* webapp - CORS requests from the hosts specified in the `hosts` property list are accepted. 
* default - CORS is set to '*' and `hosts` property list is ignored.

If no mode is set, no configuration is added by this starter. This allows for adding your own custom implementation.

```
@Bean("corsConfigurationSource")
public CorsConfigurationSource myCorsConfigurationSource() {
	// ...
} 
```

Note that the bean name must be as above in order for Spring to pick up the bean. 

Applications using the `api` mode __please use the Apigee SharedFlow with name `API-CorsSupport`__ (which can be added to `PreFlow` in the API-proxy) to filter out CORS requests that will certainly fail before they are propagated to the backend.


See also [this Confluence page](https://enturas.atlassian.net/wiki/spaces/ESP/pages/721518706/Retningslinjer+for+bruk+av+HTTP-headere+i+REST-tjenester) for more on use of HTTP headers and CORS.


#### CORS and API gateway
In general, the API gateway should respond with HTTP 403 to requests with unknown origins. All other requests, including OPTIONS calls, can be sent backwards to the Spring application.

## Health Indicator
When running application in Kubernetes you may enable Health Indicator and include jwks cache status. 
Edit you application.yaml file and add:

```yml
management:
  endpoints.web.exposure.include: health  # Add others if needed
  endpoint:
    health:
      probes.enabled: true
      show-details: always   # Optinal
      group.readiness.include: readinessState, jwksState
```

Notice if ```lazy-load: true``` is used will the health endpoint return 503 until all jwks is loaded. 
A solution can be to include following settings in your test:
```java
@TestPropertySource(properties = {"management.health.jwks.enabled=false", "management.endpoint.health.group.readiness.include=readinessState"})
@TestPropertySource(properties = {"management.endpoint.health.group.readiness.include=readinessState"})
```

## Context logging
For copying interesting JWT fields through to the MDC logging context, configure mappings:

```yaml
entur:
  auth:
    mdc:
      enabled: true   # Set to false if all mdc (default or custom) logging shall be disabled. Default is true.
      mappings:       # List of custom mdc mapping
      - from: iss     # from claim
        to: issuer    # to mdc key
```

## Testing
It is __higly recommended that you have some tests that validates that the security configuration is working as expected__. 

We have created a [oauth2-rs-spring-boot-3-web-test](../oauth2-rs-spring-boot-3-web-test) project that makes unit testing really simple. 
Examples of unit tests can be found in the [here](https://bitbucket.org/enturas/oidc-auth/src/master/frameworks/oauth2-rs-spring-boot-3-web-test/src/test/java/org/entur/auth/spring/) and [oidc-auth-spring-boot-starter-example](../../examples/oidc-auth-spring-boot-starter-example) for a working example.

### Custom audience example
Custom audience test is a nice example to look at for creating your own tests. Source code can be found [here](https://bitbucket.org/enturas/oidc-auth/src/master/frameworks/oauth2-rs-spring-boot-3-web-test/src/test/java/org/entur/auth/spring/CustomAudienceApiTest.java).
Below is som comments to the source code;

#### application-customAudience.yaml
We notice the test uses @ActiveProfiles("customAudience") with purpose to include the following:
```yaml
entur:
  auth:
    lazy-load: true               # Use true to delay load of JWKS until wiremock is started
    test.load-issuers: true       # If true your test will load JWKS from issuers settings, default false
    apis:                         # Audience to be used in our tests.
      - issuerUrl: https://internal.mock.entur.io
        audiences:
          - https://my.api
    issuers:                      # Mock JWKS domains to be provided from local wiremock.
      - issuerUrl: https://partner.mock.entur.io
        certificateUrl: http://localhost:${MOCKAUTHSERVER_PORT}/partner/.well-known/jwks.json
      - issuerUrl: https://internal.mock.entur.io
        certificateUrl: http://localhost:${MOCKAUTHSERVER_PORT}/internal/.well-known/jwks.json
```

#### CustomAudienceApiTest.java
Test includes`@ExtendWith(TenantJsonWebToken.class)`to support use of @InternalTenant and @PartnerTenant for generating access token to be used in our tests.
Access token is included in http headers when calling endpoints for integration testing.

### When Using MockMvc
When using Oidc Auth Resource Server for Spring Booot 3 Servlet together with [MockMvc](https://docs.spring.io/spring-framework/reference/testing/spring-mvc-test-framework.html)
remember to import autoconfiguration manually in your tests with:
```java
@Import(org.entur.auth.spring.config.EnturResourceServerAutoConfiguration.class)
@Import(org.entur.auth.spring.config.TestResourceServerAutoConfiguration.class)
@Import(org.entur.auth.spring.config.JwksHealthIndicator.class)  // Optional: to start JwksHealthIndicator
```

## Getting hold of the Json Web Token
In special cases, getting hold of the `Authorization` token can be necessary. The preferred approach is getting the __validated token__ via the current `Authentication`:

```
EnturAuthenticationToken authentication = (EnturAuthenticationToken)SecurityContextHolder.getContext().getAuthentication();

// get token value (including Bearer)
String token = authentication.getCredentials(); //  'Bearer XYZ'

// get parsed token value
DecodedJWT jwt = authentication.parseCredentials();
```

### Running the application on the developer machine
When running the app on a local developer machine (i.e. as in `dev`), the app file mounted via Kubernetes Config Map must be copied to the developer machine somehow. Copy the Add the following to your gradle build:

```groovy
bootRun {
    systemProperty 'spring.profiles.active', 'local'
    systemProperty 'entur.auth.external.resource', './local.providers.properties'
}

task oidcLocalProviders(type: Exec) {
    ext {
        kubernetesContext = 'gke_entur-1287_europe-west1-d_entur'
        kubernetesNamespace = 'dev'
    }

    outputs.file file("build/resources/main/local.providers.properties")
    commandLine 'bash', '-c', "kubectl --context ${kubernetesContext} --namespace ${kubernetesNamespace} get cm oidc-auth-config -o yaml | grep entur.auth > build/resources/main/local.providers.properties"
}
tasks.bootRun.dependsOn(oidcLocalProviders)
```

Alternatively copy the correct [property file](src/main/kubernetes) to the same local path as configured in `entur.auth.external.resource`.

### Configuration of provider using local properties (not recommended for apps running in GCP)

__Alternative 1__: copy the provider [property files](src/main/kubernetes) into the application resources and set the `entur.auth.external.resource` to the corresponding classpath resource (which depends on the environment).

__Alternative 2__: Add properties to your local `application.yml` file. The configuration properties is a list of issuer objects. See the following example for all configurable properties

```yaml
entur:
  auth:
    issuers:
      - issuerUrl: "http://auth0-url-issuer/partner"
        certificateUrl: "http://auth0-url-cert"
        authProvider: "AUTH0"
        certificateReloadPeriodInMinutes: 7
        fallbackCertificateReloadPeriodInMinutes: 70
```

The __fallback__ is only used on network/parse issues, implying transient network or service disruptions.

# Troubleshooting

## Cannot resolve `MOCKAUTHSERVER_PORT` property
Make sure the Spring context is not loaded before our test extension (because the extension sets that property). Remove 

```
TestInstance(Lifecycle.PER_CLASS)
```

if present. 
