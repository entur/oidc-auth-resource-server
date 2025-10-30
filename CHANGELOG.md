# [Release notes](https://github.com/entur/oidc-auth-client)

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