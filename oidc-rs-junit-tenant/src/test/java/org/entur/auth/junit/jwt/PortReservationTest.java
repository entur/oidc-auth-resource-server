package org.entur.auth.junit.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import org.junit.jupiter.api.Test;

class PortReservationTest {

    @Test
    void scanSkipsOccupiedPortsAndAdvancesToNextCandidate() throws IOException {
        String propertyName = "PortReservationTest.scan";
        int base = findConsecutiveFreePorts(3);
        try (ServerSocket occupiedFirst = bind(base);
                ServerSocket occupiedSecond = bind(base + 1)) {
            PortReservation reservation = new PortReservation(base, base + 2, propertyName);
            try {
                assertTrue(reservation.start());
                assertEquals(base + 2, reservation.getPort());
                assertEquals(Integer.toString(base + 2), System.getProperty(propertyName));
            } finally {
                reservation.stop();
                System.clearProperty(propertyName);
            }
        }
    }

    @Test
    void rescanReservesNewPortWhenPreviousPortIsStolen() throws IOException {
        String propertyName = "PortReservationTest.rescan";
        PortReservation reservation = new PortReservation(propertyName);
        try {
            reservation.start();
            int stolenPort = reservation.getPort();

            reservation.stop();
            try (ServerSocket thief = bind(stolenPort)) {
                reservation.rescan();

                assertTrue(reservation.getPort() > 0);
                assertNotEquals(stolenPort, reservation.getPort());
                assertEquals(Integer.toString(reservation.getPort()), System.getProperty(propertyName));
            }
        } finally {
            reservation.stop();
            System.clearProperty(propertyName);
        }
    }

    /**
     * Simulates another process stealing the reserved port in the window after {@link
     * PortReservation#stop()}. The stale port stays pinned until recovery is requested, but
     * re-running {@link PortReservation#start()} (as {@code TenantAnnotationTokenFactory#close()}
     * does) must abandon the stolen port and reserve a fresh one instead of failing.
     *
     * <p>Reproduces the upstream oidc-lib bug where start() instead throws {@code
     * IllegalArgumentException: Preconfigured port ... is not free}, pinning the dead port forever.
     *
     * <p>Slow by design: start() retries the previously reserved port for ~10 seconds before giving
     * it up, in case the thief is short-lived.
     */
    @Test
    void restartReservesNewPortWhenPreviousPortIsStolen() throws IOException {
        String propertyName = "PortReservationTest.restart";
        PortReservation reservation = new PortReservation(propertyName);
        try {
            reservation.start();
            int stolenPort = reservation.getPort();

            reservation.stop();
            try (ServerSocket thief = bind(stolenPort)) {
                // the stale reservation still reports the stolen port...
                assertEquals(stolenPort, reservation.getPort());

                // ...but restarting recovers by abandoning it and scanning for a new one
                assertTrue(reservation.start());
                assertTrue(reservation.getPort() > 0);
                assertNotEquals(stolenPort, reservation.getPort());
                assertEquals(Integer.toString(reservation.getPort()), System.getProperty(propertyName));
            }
        } finally {
            reservation.stop();
            System.clearProperty(propertyName);
        }
    }

    @Test
    void externallyPreconfiguredPortIsReusedAndCannotBeRescanned() throws IOException {
        String propertyName = "PortReservationTest.preconfigured";
        int freePort;
        try (ServerSocket probe = bind(0)) {
            freePort = probe.getLocalPort();
        }
        System.setProperty(propertyName, Integer.toString(freePort));
        PortReservation reservation = new PortReservation(propertyName);
        try {
            assertTrue(reservation.start());
            assertEquals(freePort, reservation.getPort());
            assertThrows(IllegalStateException.class, reservation::rescan);
        } finally {
            reservation.stop();
            System.clearProperty(propertyName);
        }
    }

    private static ServerSocket bind(int port) throws IOException {
        return new ServerSocket(port, 1, InetAddress.getByName("localhost"));
    }

    private static int findConsecutiveFreePorts(int count) throws IOException {
        for (int base = 24000; base < 64000; base += count) {
            ServerSocket[] sockets = new ServerSocket[count];
            try {
                for (int i = 0; i < count; i++) {
                    sockets[i] = bind(base + i);
                }
                return base;
            } catch (IOException e) {
                // try the next block of ports
            } finally {
                for (ServerSocket socket : sockets) {
                    if (socket != null) {
                        socket.close();
                    }
                }
            }
        }
        throw new IOException("Unable to find " + count + " consecutive free ports");
    }
}
