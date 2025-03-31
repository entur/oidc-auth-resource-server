# oidc-auth-jwt-test
This is a utility library for creating a mock-up authentication server for testing purposes.

Using Spring Boot? __This library is incorporated in [oidc-auth-spring-boot-starter-test](../frameworks/oidc-auth-spring-boot-starter-test)__. See [oidc-auth-spring-boot-starter](../frameworks/oidc-auth-spring-boot-starter) to get started.

## Example usage

```java
// create mock server
MockAuthenticationServer mockServer = new MockAuthenticationServer("partner");

// get the token factory
TenantTokenFactory factory = mockServer.getTenantTokenFactory();

// create new token
String internalToken = factory.generateInternalJwtToken(...);

// configure token signature validation using url
String certificateUrl = factory.getCertUrl("partner", "keycloak");

// and validate the token, i.e. using the oidc-auth-jwt project
```
