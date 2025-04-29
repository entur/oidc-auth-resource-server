package org.entur.auth.spring.jwk;

import com.nimbusds.jose.RemoteKeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.DefaultJWKSetCache;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.Resource;
import com.nimbusds.jose.util.ResourceRetriever;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.jcip.annotations.ThreadSafe;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>OAuth2RemoteJWKSet is an implementation of the JWKSource interface that fetches
 * JSON Web Key (JWK) sets from a remote endpoint using OAuth2 authentication.
 * This class provides methods to retrieve and cache JWK sets from the specified
 * issuer URI and JWK set URL. It also supports rate limiting and throttling of
 * requests to the remote endpoint to prevent excessive load. </p><br>
 *
 * <p>The class is thread-safe, allowing multiple threads to access the same instance
 * concurrently without causing synchronization issues. It utilizes the SLF4J logging
 * framework for logging purposes, which can be customized through different logging
 * configurations.</p><br>
 *
 * <p>OAuth2RemoteJWKSet is designed to be used as part of OAuth2-based authentication
 * systems where JWT (JSON Web Token) verification requires access to the JWK set
 * to verify token signatures.</p><br>
 *
 * <p>Usage:
 * - Create an instance of OAuth2RemoteJWKSet by using the provided builder.
 * - Configure the desired cache lifespan, refresh time, and other options.
 * - Use the instance to fetch JWK sets from the remote endpoint when needed.</p><br>
 *
 * Example:
 * <pre>
 * {@code OAuth2RemoteJWKSet<C> jwkSet = OAuth2RemoteJWKSet.builder() }
 *     .issuerUri("https://example.com/auth/issuer")
 *     .jwkSetUrl(new URL("https://example.com/auth/keys"))
 *     .cacheLifespan(60) // Cache JWK sets for 60 minutes
 *     .retryOnFailure(true) // Retry JWK set retrieval on failure
 *     .build();
 *
 * // Retrieve JWK set
 * {@code JWKSet<C> set = jwkSet.get(new SecurityContext()); }
 * </pre>
 *
 * Note: This class uses the SLF4J logging framework, and the appropriate log
 * implementation must be provided in the classpath for logging to work correctly.
 */
@Slf4j
@ThreadSafe
public final class OAuth2RemoteJWKSet<C extends SecurityContext> implements JWKSource<C> {
    private static final String COULDN_T_FETCH_REMOTE_JWK_SET = "Couldn't fetch remote JWK set";

    /**
     * The default HTTP connect timeout for JWK set retrieval, in milliseconds. Set to 3000 milliseconds.
     */
    private static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 3000;

    /**
     * The default HTTP read timeout for JWK set retrieval, in milliseconds. Set to 3000 milliseconds.
     */
    private static final int DEFAULT_HTTP_READ_TIMEOUT = 3000;

    /**
     * The default HTTP entity size limit for JWK set retrieval, in bytes. Set to 50 KBytes.
     */
    private static final int DEFAULT_HTTP_SIZE_LIMIT = 50 * 1024;

    /**
     * The default throttle wait time in milliseconds, to avoid too many calls to the jwks endpoint.
     */
    public static final int DEFAULT_JWKS_THROTTLE_WAIT = 60000;

    /**
     * Maximum number waiting clients. When waiting queue is full exception is trowed and client is denied access.
     */
    public static final int DEFAULT_MAX_WAITING_CLIENTS = 20;

    @Getter
    private final String issuerUri;
    private final URL jwkSetUrl;
    private final ResourceRetriever resourceRetriever;
    private final DefaultJWKSetCache jwkSetCache;
    private final JWKExecutorService jwkExecutorService;

    /**
     * Constructor for OAuth2RemoteJWKSet class.
     *
     * @param issuerUri         The URI of the issuer for OAuth2.
     * @param jwkSetUrl         The URL to fetch the JWK set from.
     * @param cacheLifespan     The lifespan of the JWK set cache in minutes.
     * @param refreshTime       The refresh time for the JWK set cache in minutes.
     * @param retryOnFailure    Whether to retry JWK set retrieval on failure.
     * @param connectTimeout    The HTTP connect timeout in seconds.
     * @param readTimeout       The HTTP read timeout in seconds.
     * @param maxWaitingClients Maximum number of waiting clients in the executor service.
     * @param jwksThrottleWait  Throttle wait time in seconds to avoid too many calls to the jwks endpoint.
     */
    @Builder
    public OAuth2RemoteJWKSet(@NonNull String issuerUri, @NonNull URL jwkSetUrl, int cacheLifespan, int refreshTime, boolean retryOnFailure, int connectTimeout, int readTimeout, int maxWaitingClients, int jwksThrottleWait) {
        this.issuerUri = issuerUri;
        this.jwkSetUrl = jwkSetUrl;
        this.resourceRetriever = new DefaultResourceRetriever(connectTimeout <= 0 ? DEFAULT_HTTP_CONNECT_TIMEOUT : connectTimeout * 1000, readTimeout <= 0 ? DEFAULT_HTTP_READ_TIMEOUT : readTimeout * 1000, DEFAULT_HTTP_SIZE_LIMIT);
        this.jwkSetCache = new DefaultJWKSetCache(cacheLifespan <= 0 ? -1 : cacheLifespan, refreshTime <= 0 ? -1 : refreshTime, TimeUnit.MINUTES);
        this.jwkExecutorService = JWKExecutorService.builder()
                .oAuth2RemoteJWKSet(this)
                .retryOnFailure(retryOnFailure)
                .maxWaitingClients(maxWaitingClients <= 0 ? DEFAULT_MAX_WAITING_CLIENTS : maxWaitingClients)
                .jwksThrottleWait(jwksThrottleWait <= 0 ? DEFAULT_JWKS_THROTTLE_WAIT : jwksThrottleWait * 1000)
                .build();
    }

    /**
     * Retrieves a list of JWKs matching the specified selector. The cache will we used as follows:
     * <ol>
     *     <li>Cache is up to date and key is in cache:  A list of JWK is selected and returned from the local cache.</li>
     *     <li>Key is not in cache:  First start a refresh tread if not already running. Wait for refresh finished, then a list of JWK is selected and returned from the updated cache.</li>
     *     <li>Cache requires refresh and key is in cache:  First start a refresh tread if not already running. Then a list of JWK is selected and returned from the local cache.</li>
     *     <li>Cache is expired: First start a refresh tread if not already running. Wait for refresh finished, then a list of JWK is selected and returned from the updated cache.</li>
     * </ol>
     *
     * If the cache not can be refreshed and the lifetime for the cache has been expired will a LivenessState.BROKEN be posted.
     *
     * @param jwkSelector A JWK selector. Must not be {@code null}.
     * @param context     Optional context, and currently not in use.
     *
     * @return The matching JWKs, empty list if no matches were found.
     *
     * @throws RemoteKeySourceException If key sourcing failed. Can only occur for new keys or when the cache is expired.
     */
    @Override
    public List<JWK> get(final JWKSelector jwkSelector, final C context)
            throws RemoteKeySourceException {

        // Handle cache lifetime expired
        if(this.jwkSetCache.isExpired()) {
            cacheIsExpiredAndCacheIsNotValid();
        }

        JWKSet jwkSet = this.jwkSetCache.get();
        if(jwkSet == null) {
            // First time use and cache is not initialized
            jwkSet = updateJwkSetAndGetUpdatedJWKSetFromCache();
        } else if(this.jwkSetCache.requiresRefresh()) {
            jwkExecutorService.submitUpdateJWKSet();
        }

        // Run the selector on the JWK set
        List<JWK> matches = jwkSelector.select(jwkSet);
        if (! matches.isEmpty()) {
            return matches; // Success
        }

        // Refresh the JWK set if the sought key ID is not in the cached JWK set
        // Looking for JWK with specific ID?
        String soughtKeyID = getFirstSpecifiedKeyID(jwkSelector.getMatcher());
        if (soughtKeyID == null) {
            return Collections.emptyList(); // No key ID specified, return no matches
        }

        if (jwkSet.getKeyByKeyId(soughtKeyID) != null) {
            // The key ID exists in the cached JWK set, matching
            // failed for some other reason, return no matches
            return Collections.emptyList();
        }

        jwkSet = updateJwkSetAndGetUpdatedJWKSetFromCache();

        // Repeat select, return final result (success or no matches)
        return jwkSelector.select(jwkSet);
    }

    private JWKSet updateJwkSetAndGetUpdatedJWKSetFromCache() throws RemoteKeySourceException {
        jwkExecutorService.submitUpdateJWKSet();
        jwkExecutorService.waitForUpdateJWKSetSubmit();
        return this.jwkSetCache.get();
    }

    private void cacheIsExpiredAndCacheIsNotValid() throws RemoteKeySourceException {
        try {
            jwkExecutorService.submitUpdateJWKSet();
            jwkExecutorService.waitForUpdateJWKSetSubmit();
        } catch(Exception e) {
            log.warn(COULDN_T_FETCH_REMOTE_JWK_SET + ": " + e.getMessage(), e);
            throw new RemoteKeySourceException(COULDN_T_FETCH_REMOTE_JWK_SET + ": " + e.getMessage(), e);
        }
    }

    /**
     * Initiates the process to update the JWK (JSON Web Key) set from a remote URL.
     * This method submits a task to the JWK executor service to fetch and update
     * the JWK set asynchronously. It then waits for the update task to complete.
     *
     * @throws RemoteKeySourceException If there's an issue while updating the JWK set
     *                                 from the remote source.
     */
    public void updateJWKSetFromURL() throws RemoteKeySourceException {
        jwkExecutorService.submitUpdateJWKSet();
        jwkExecutorService.waitForUpdateJWKSetSubmit();
    }

    /**
     * Initiates an orderly shutdown of the JWK (JSON Web Key) executor service.
     * After this method is called, the executor service will no longer accept
     * new tasks, and it will attempt to gracefully shut down the existing tasks.
     * Any tasks that have not started will not be executed.
     */
    public void shutdown() {
        jwkExecutorService.shutdown();
    }

    private static String getFirstSpecifiedKeyID(final JWKMatcher jwkMatcher) {

        Set<String> keyIDs = jwkMatcher.getKeyIDs();

        if (keyIDs == null || keyIDs.isEmpty()) {
            return null;
        }

        for (String id: keyIDs) {
            if (id != null) {
                return id;
            }
        }
        return null; // No kid in matcher
    }

    /**
     * Check the readiness status for JWK (JSON Web Key) set.
     * @return {@code true} if the JWK set is ready, otherwise {@code false}.
     */
    public boolean getReadiness() {
        boolean readiness = this.jwkSetCache.get() != null;
        if(!readiness) {
            this.jwkExecutorService.submitUpdateJWKSet();
        }

        return readiness;
    }

    private static class JWKExecutorService {
        private final ExecutorService executorService = Executors.newSingleThreadExecutor();
        private final Object syncSubmit = new Object();

        private final OAuth2RemoteJWKSet<?> oAuth2RemoteJWKSet;
        private final boolean retryOnFailure;
        private final int maxWaitingClients;
        private final int jwksThrottleWait;

        private FutureRecord updateFutureHelper = null;
        private Future<Boolean> waitFuture = null;

        @Builder
        protected JWKExecutorService(OAuth2RemoteJWKSet<?> oAuth2RemoteJWKSet, boolean retryOnFailure, int maxWaitingClients, int jwksThrottleWait) {
            this.oAuth2RemoteJWKSet = oAuth2RemoteJWKSet;
            this.retryOnFailure = retryOnFailure;
            this.maxWaitingClients = maxWaitingClients;
            this.jwksThrottleWait = jwksThrottleWait;
        }

        public void shutdown() {
            executorService.shutdown();
        }

        public void submitUpdateJWKSet() {
            synchronized (syncSubmit) {
                if(waitFuture != null && !(waitFuture.isCancelled() || waitFuture.isDone())) {
                    log.debug("Update of JWKSet is not performed since we are in throttle period and cache is treated as up to date.");
                    return;
                }

                if(updateFutureHelper == null || updateFutureHelper.future.isDone() || updateFutureHelper.future.isCancelled()) {
                    log.debug("Update of JWKSet is added to execution service");
                    updateFutureHelper = new FutureRecord(executorService.submit(getCallableUpdateJWKSet(oAuth2RemoteJWKSet)));
                    waitFuture = executorService.submit(getCallableWaitAfterRefresh()); // Flood protection. Let executorService wait after update JWK set
                }
            }
        }

        public void waitForUpdateJWKSetSubmit() throws RemoteKeySourceException {
            log.debug("Start waitForUpdateJWKSetSubmit()");
            if(updateFutureHelper == null) {
                log.error("Internal error. UpdateJWKSetFuture shall not be null.");
                throw new RemoteKeySourceException("Internal error. UpdateJWKSetFuture shall not be null.", null);
            }
            try {
                FutureRecord tempFutureHelper = updateFutureHelper;
                Future<Boolean> tempWaitFuture = waitFuture;
                if(!tempFutureHelper.future.isDone() && !tempFutureHelper.future.isCancelled()) {
                    int waitingClientsCount = tempFutureHelper.waitingClientsCount.get();
                    if(waitingClientsCount >= maxWaitingClients) {
                        log.debug("Maximum waiting clients is exceeded, return with exception");
                        throw new RemoteKeySourceException("Maximum waiting clients is exceeded", null);
                    }

                    waitingClientsCount = tempFutureHelper.waitingClientsCount.incrementAndGet();
                    log.debug("Number of waiting clients: {}", waitingClientsCount);
                    if(tempFutureHelper.future.get() == null) {
                        if (!retryOnFailure) {
                            log.warn(COULDN_T_FETCH_REMOTE_JWK_SET);
                            throw new RemoteKeySourceException(COULDN_T_FETCH_REMOTE_JWK_SET, null);
                        }

                        log.debug("Couldn't fetch remote JWK set, but we try one more time");

                        // wait on throttle
                        tempWaitFuture.get();

                        submitUpdateJWKSet();
                        tempFutureHelper = updateFutureHelper;
                        if (tempFutureHelper.future.get() == null) {
                            log.warn(COULDN_T_FETCH_REMOTE_JWK_SET);
                            throw new RemoteKeySourceException(COULDN_T_FETCH_REMOTE_JWK_SET, null);
                        }
                    }
                }
            } catch (InterruptedException e) {
                log.error(COULDN_T_FETCH_REMOTE_JWK_SET + ": " + e.getMessage(), e);
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                log.error(COULDN_T_FETCH_REMOTE_JWK_SET + ": " + e.getMessage(), e);
                throw new RemoteKeySourceException(COULDN_T_FETCH_REMOTE_JWK_SET + ": " + e.getMessage(), e);
            }
        }

        private Callable<JWKSet> getCallableUpdateJWKSet(OAuth2RemoteJWKSet<?> oAuth2RemoteJWKSet) {
            return () -> {
                log.debug("Starting fetch remote JWK set");
                final JWKSet jwkSet;
                try{
                    jwkSet = getJWKSet(oAuth2RemoteJWKSet.jwkSetUrl, oAuth2RemoteJWKSet.resourceRetriever);
                    oAuth2RemoteJWKSet.jwkSetCache.put(jwkSet);
                } catch(RemoteKeySourceException e) {
                    log.info(e.getMessage(), e);
                    return null;
                }
                log.debug("Finished fetch remote JWK set");
                return jwkSet;
            };
        }

        private JWKSet getJWKSet(URL url, ResourceRetriever resourceRetriever)
                throws RemoteKeySourceException {
            Resource res;
            try {
                res = resourceRetriever.retrieveResource(url);
            } catch (IOException e) {
                throw new RemoteKeySourceException("Couldn't retrieve remote JWK set: " + e.getMessage(), e);
            }

            JWKSet jwkSet;
            try {
                jwkSet = JWKSet.parse(res.getContent());
            } catch (java.text.ParseException e) {
                throw new RemoteKeySourceException("Couldn't parse remote JWK set: " + e.getMessage(), e);
            }

            return jwkSet;
        }

        private Callable<Boolean> getCallableWaitAfterRefresh() {

            return () -> {
                log.debug("Starting throttle wait");
                Thread.sleep(jwksThrottleWait);
                log.debug("Finished throttle wait");
                return true;
            };
        }

        @AllArgsConstructor
        private static class FutureRecord {
            public final AtomicInteger waitingClientsCount = new AtomicInteger(0);
            public final Future<JWKSet> future;
        }
    }
}
