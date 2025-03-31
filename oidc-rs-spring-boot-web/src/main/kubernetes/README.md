# kubernetes config maps
Create using command

> kubectl create configmap oidc-auth-config --from-file ./frameworks/oidc-auth-spring-boot-starter/src/main/kubernetes/XXXXX/providers.properties

Update using command

> kubectl create configmap oidc-auth-config --from-file ./frameworks/oidc-auth-spring-boot-starter/src/main/kubernetes/XXXXX/providers.properties --dry-run -o yaml | kubectl replace -f -

where XXXXX is your environment. View the result:

> kubectl get cm oidc-auth-config -o yaml

View existing configuration for an application:

> kubectl get deployment YYYYY -o yaml

where YYYYY is an application like 'international-sales'.

Alternatively (not recommended) try

> kc edit cm oidc-auth-config -o yaml

