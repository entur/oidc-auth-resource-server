# [Release notes](https://github.com/entur/oidc-auth-resource-server)

## oidc-auth-resource-server v3.0.0
* Migrate project to Spring Boot 4.

## oidc-auth-resource-server v2.2.0
* Change the behaviour of JWKS health indicator.

## oidc-auth-resource-server v2.1.0
* Increase default outage tolerance duration.

## oidc-auth-resource-server v2.0.0
* Changed actuator permit from actuator/** too individual paths.

## oidc-auth-resource-server v1.1.4
* Add support for parameter annotation in JUnit test classes.

## oidc-auth-resource-server v1.1.3
* Moved port reservation from beforeAll into aconstuctor of TenantJsonWebToken.

## oidc-auth-resource-server v1.1.2
* Make ConfigJwksHealthIndicatorAutoConfiguration optional whn using @WebMvcTest.

## oidc-auth-resource-server v1.1.1
* Add use of synchronized to oidc-rs-junit-tenant.

## oidc-auth-resource-server v1.1.0
* Make retryOnFailure defalut true and outageTolerant default 300
* Make method TenantJsonWebToken.setupTokenFactory public

## oidc-auth-resource-server v1.0.1
* Fix TenantAnnotationTokenFactory don't handle PortReservation correct

## oidc-auth-resource-server v1.0.0
 * oidc-auth-resource-server are split out as separate project from oidc-auth