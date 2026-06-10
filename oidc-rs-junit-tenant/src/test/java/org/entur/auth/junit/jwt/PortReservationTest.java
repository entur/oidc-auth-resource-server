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
