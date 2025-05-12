# oidc-auth-resource-server
**oidc-auth-resource-server** is a Java / Spring Boot library that makes it trivial to protect HTTP endpoints with OAuth 2.0 / OpenID Connect (OIDC) access tokens. It handles JWT validation, JWK-set caching, opaque-token introspection, and multi-tenant setups for both **Spring MVC** and **WebFlux** applications.

## Modules
| Gradle sub-project                   | Purpose                                                                 |
|--------------------------------------|-------------------------------------------------------------------------|
| `oidc-rs-spring-boot-web`            | Auto-configuration for Spring MVC (Servlet stack)                       |
| `oidc-rs-spring-boot-web-config`     | Support application.yaml configuration for Spring MVC (Servlet stack)   |
| `oidc-rs-spring-boot-webflux`        | Auto-configuration for Spring WebFlux (reactive stack)                  |
| `oidc-rs-spring-boot-webflux-config` | Support application.yaml configuration for WebFlux (reactive stack)     |
| `oidc-rs-junit-tenant`               | JUnit 5 helpers for spinning up an in-memory IdP and generating tokens  |
| `oidc-rs-spring-boot-web-test`       | Bundeled `oidc-rs-junit-tenant` and `oidc-rs-spring-boot-web-config`    |
| `oidc-rs-spring-boot-webflux-test`   | Bundeled `oidc-rs-junit-tenant` and `oidc-rs-spring-boot-webflux-config`|
| `oidc-rs-spring-boot-common`         | Core validation & utility classes                                       |
| `oidc-rs-test`                       | Test utilities shared across modules                                    |

## Features
* Plug-and-play **JWT** verification
* Automatic **JWK-set download and refresh** with configurable cache TTL
* Audience, issuer, scope/authority and claim validation
* **Multi-tenant** support – secure the same app with several issuers in parallel
* Ready-made `SecurityFilterChain` beans for both Spring MVC & WebFlux
* JUnit extension for fast, isolated tests (no real IdP required)

## Installation
Add the dependency that matches your runtime stack:

```groovy
// Spring MVC / Servlet
implementation("org.entur.auth.resource-server:oidc-rs-spring-boot-web-config:${oidcAuthServerVersion}")
testImplementation("org.entur.auth.resource-server:oidc-rs-spring-boot-web-test:${oidcAuthServerVersion}")
```
```groovy
// OR: Spring WebFlux
implementation("org.entur.auth.resource-server:oidc-rs-spring-boot-webflux-config:${oidcAuthServerVersion}")
testImplementation("org.entur.auth.resource-server:oidc-rs-spring-boot-webflux-test:${oidcAuthServerVersion}")
```

If you only need manual low-level configuration without use of application.yaml, 
depend on **`oidc-rs-spring-boot-web`** or **`oidc-rs-spring-boot-webflux`** directly.

## Configuration with Application.yaml

### Using predefined tenants
Using predefined environment and tenants from application.yaml:

```yaml
entur:
  auth:
    tenants:
      environment: dev  # Predefined values for Entur: dev, tst, prd, stage, prod, mock
      include: partner  # Predefined values for Entur: partner, internal, traveller, person
```

Entur's predefined environment definitions can be replaced by defining own Spring bean:
```java
@Bean
public AuthProviders authProviders() {
    return new MyAuthProviders();
}
```

> [!NOTE]  
> AuthProviders will also provide a granted authority based on tenant to support example: ```@PreAuthorize("hasAnyAuthority('internal')")```

> [!TIP]
> Default Spring be used by turning off authentication manager configuration with: ```entur.auth.enabled: false```

### Using issuers in application.yml
Additional to *predefined tenants* can issuers be defined in application.yaml:

```yaml
entur:
  auth:
    issuers:
      - 
        issuerUrl: https://internal.mock.entur.io
        certificateUrl: http://localhost:${MOCKAUTHSERVER_PORT}/internal/.well-known/jwks.json
      -  
        issuerUrl: https://partner.mock.entur.io
        certificateUrl: http://localhost:${MOCKAUTHSERVER_PORT}/partner/.well-known/jwks.json
```

### Configure authorization filter
By default, will the authorization filter enforces the use of fully authenticated.

Endpoints can be opened to the world from application.yaml. Example:
```yaml
entur:
  auth:
    authorization:
      permit-all:
        matcher:
          patterns:  # All methods will be allowed
            - /unprotected
            - /unprotected/**
          method:   # Defined methods will be allowed
            get:
              patterns:
                - /unprotected
                - /unprotected/**
```

> [!TIP]
> Default Spring be used by turning off authorize configuration with:```entur.auth.authorization.enabled: false```

### Configure Cross-Origin Resource Sharing (CORS)
Spring default CORS configuration can be overrides from application.yaml:

```yaml
entur:
  auth:
    cors:
      mode: api | webapp | default
      
      hosts: # Optional manually configuration
        - https://myapp.entur.org
        - https://myotherpetstore.swagger.io
        - https://*.example1.com # domains ending with example1.com
        - https://*.example2.com:[8080,8081] # domains ending with example2.com on port 8080 or port 8081
        - https://*.example3.com:[*] # domains ending with example3.com on any port, including the default port  
```

#### Predefined modes:

- api: CORS requests from the API development portal & petstore plus the hosts specified in the hosts property list are accepted.
- webapp: CORS requests from the hosts specified in the hosts property list are accepted.
- default: CORS is set to '*' and hosts property list is ignored.

> [!TIP]
> Default Spring be used by turning off CORS configuration with:```entur.auth.cors.enabled: false```


### Configure health indicator

When running application in Kubernetes you may enable Health Indicator and include jwks cache status from application.yaml:

```yaml
management:
  endpoints.web.exposure.include: health  # Add others if needed
  endpoint:
    health:
      probes.enabled: true
      group.readiness.include: readinessState, jwksState
```

> [!TIP]
> Confgured health indicator can be turned off with:```management.health.jwks.enabled: false```

### Configure Context logging
For copying interesting JWT fields through to the MDC logging context. Default configure mappings is shown below:

```yaml
entur:
  auth:
    mdc:
      mappings:       # List of custom mdc mapping
        -
          from: azp       # azp from claim
          to: clientId    # to mdc key
        -
          from: https://entur.io/organisationID
          to: organisationId
```

> [!TIP]
> Default Spring be used by turning off MDC configuration with:```entur.auth.mdc.enabled: false``` 

### Advanced cache tuning

```yaml
entur:
  auth:
    lazy-load: true | false  # When using RSA certificates can JWKS load be delayed. Default = false.
    retry-on-failure: true | false # When true will failure on loading JWKS be retried. Default = false.
    connect-timeout: <seconds> # Numbus Resource Retriever - connectTimeout. Default = 5 seconds.
    read-timeout: <seconds> # Numbus Resource Retriever - readTimeout. Default = 5 seconds.
    jwks-throttle-wait: <seconds> # Numbus JWKSourceBuilder - rateLimited. Default = 30 seconds.
    refresh-ahead-time: <seconds> # Numbus RefreshAheadCache - refreshAheadTime. Default = 30 seconds.
    cache-refresh-timeout: <seconds> # Numbus Cache - cacheRefreshTimeout. Default = 15 seconds.
    cache-lifespan: <seconds> # Numbus Cache - cacheLifespan. Default = 300 seconds.
```

On issuer confguration can this values be overwritten:

```yaml
entur:
  auth:
    issuers:
      - issuerUrl: https://my.site.io
        certificateUrl: https://my.site.io/.well-known/jwks.json
        retry-on-failure: true | false # When true will failure on loading JWKS be retried.
        jwks-throttle-wait: <seconds> # Numbus JWKSourceBuilder - rateLimited. 
        refresh-ahead-time: <seconds> # Numbus RefreshAheadCache - refreshAheadTime. 
        cache-refresh-timeout: <seconds> # Numbus Cache - cacheRefreshTimeout. 
        cache-lifespan: <seconds> # Numbus Cache - cacheLifespan. 
```



## Testing
Local testing with jwt generation is supported with JUnit integration.

First configure Spring Security to use a local web server for JWKS:
```yaml
entur:
  auth:
    tenants:
      environment: mock
      include: partner, internal
```
Or by:
```yaml
entur:
  auth:
    test.load-issuers: true     # Needed since oidc-rs-spring-boot-web-test deletes issuers by default 
    # load-environments: true   # Setting to tell oidc-rs-spring-boot-web-test not clear tenant environment 
    issuers:
      - issuerUrl: https://internal.mock.entur.io
        certificateUrl: http://localhost:${MOCKAUTHSERVER_PORT}/internal/.well-known/jwks.json
      - issuerUrl: https://partner.mock.entur.io
        certificateUrl: http://localhost:${MOCKAUTHSERVER_PORT}/partner/.well-known/jwks.json
```

Example of JUnit test:

```java
@ExtendWith({SpringExtension.class, TenantJsonWebToken.class})
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AuthorizeTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testProtectedWithPartner(@PartnerTenant(clientId = "clientId", subject = "subject") String authorization) throws Exception {
        var requestHeaders = new HttpHeaders();
        requestHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        requestHeaders.add("Authorization", authorization);

        mockMvc.perform(get("/protected").headers(requestHeaders)).andExpect(status().isOk());
    }
}
```

## Customising the SecurityFilterChain
If additional customising of SecurityFilterChain is needed, it can be provided by implementing own Spring Bean's.

Example:  

```java
@Configuration
public class ResourceServerDefaultAutoConfiguration {
    @Bean
    public UserDetailsService userDetailsService() {
        return new MyDetailsService(); 
    }

    @Bean
    public ConfigureAuthorizeRequests configureAuthorizeRequests() {
        return new MyConfigureAuthorizeRequests();
    }

    @Bean
    public ConfigureSessionManagement configureSessionManagement() {
        return new MyConfigureSessionManagement();
    }

    @Bean
    public ConfigureCsrf configureCsrf() {
        return new MyConfigureCsrf();
    }

    @Bean
    public ConfigureCors configureCors() {
        return new MyConfigureCors();
    }

    @Bean
    public ConfigureMdcRequestFilter configureMdcRequestFilter() {
        return new MyMdcRequestFilter();
    }

    @Bean
    public ConfigureAuth2ResourceServer configureAuth2ResourceServer() {
        return new MyConfigureAuth2ResourceServer();
    }
}
```

## Development
Clone the repository:
```bash
git clone https://github.com/entur/oidc-auth-resource-server.git
cd oidc-auth-client
```

Build and run JUnit tests:
```bash
./gradlew build
```

## Contributing
Pull requests are welcome! See [CONTRIBUTING](CONTRIBUTING.md) for details.

## License
Licensed under the **EUPL-1.2** – see [LICENSE](LICENSE.txt) for the full text.
