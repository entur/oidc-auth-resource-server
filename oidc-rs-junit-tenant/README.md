# oidc-auth-jwt-junit
Junit 5 extension for working with `JWT` tokens and `Tenants`.

 * annotation for injection of `Bearer` tokens into test methods
 * background Wiremock instance for serving sertificates for validation of tokens

Using Spring Boot? __This library is incorporated in [oidc-auth-spring-boot-starter-test](../frameworks/oidc-auth-spring-boot-starter-test)__. See [oidc-auth-spring-boot-starter](../frameworks/oidc-auth-spring-boot-starter) to get started.

## Usage
For unit tests, add the `@ExtendWith` with `TenantJsonWebToken` at the class level, and one (or more) annotations on a String method parameters

  * `@PartnerTenant`
  * `@InternalTenant`
  * `@TravellerTenant`

like this:

```java
@ExtendWith(TenantJsonWebToken.class)
public class TenantJsonWebTokenTest {

    @Test
    void test1(@PartnerTenant(organisationId=917422575L) String token) {
        given()
                .header("Authorization", token)
                .log().all()
                .when()
                .get("/myResource")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .body(is(CoreMatchers.equalTo("Authorized as partner")));
    }
}
```
where the controller at `/myResource` would map the token to a `Tenant` of type `Partner`, i.e. for a Spring REST controller method

```
@GetMapping("/myResource")
@PreAuthorize("hasAnyAuthority('partner')")
public Greeting myResource(Tenant tenant) {
    log.info("Get my partner resource");
    
    Partner partner = (Partner)tenant; // with organisation id 917422575
}
```

## Configuration
The System Property `MOCKAUTHSERVER_PORT` is used to communicate the mock server port number. Your should pick that value up when mapping issuer and certificat URL, i.e. for Keycloak and a `partner`:

```
entur:
  auth:
    issuers:
      - issuerUrl: "https://mock/auth/realms/partner"
        certificateUrl: "http://localhost:${MOCKAUTHSERVER_PORT}/auth/realms/partner/protocol/openid-connect/certs"
        authProvider: "KEYCLOAK"
        certificateReloadPeriodInMinutes: 5
        fallbackCertificateReloadPeriodInMinutes=50
        
```

or Auth0

```
entur:
  auth:
    issuers:
      - issuerUrl: "https://partner.mock.entur.io"
        certificateUrl: "http://localhost:${MOCKAUTHSERVER_PORT}/partner/.well-known/jwks.json"
        authProvider: "AUTH0"
        certificateReloadPeriodInMinutes: 5
        fallbackCertificateReloadPeriodInMinutes=50
```

notice that the issuer and certificate URLs are not the same.

### Spring configuration
See [oidc-auth-spring-boot-starter](frameworks/oidc-auth-spring-boot-starter) and also note that the [oidc-auth-spring-boot-starter-test](frameworks/oidc-auth-spring-boot-starter-test) very much simplifies setup of the above configuration for testing.

### Reuse Wiremock instance
For additional mocking responses, the Wiremock instance can be reused. Note that all 
mocking is reset between each method.

Add a `WireMockServer` parameter to the test method signature and setup mocking:

```
@Test
public void testMyMocking(@TravellerTenant String authorization, WireMockServer mockServer) throws Exception {
    // setup mock response for calls to
    // http://localhost: + mockServer.port() + /test
    
    mockServer.stubFor(get(urlEqualTo("/test"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/plain")
                    .withBody("test response")));
}
```

