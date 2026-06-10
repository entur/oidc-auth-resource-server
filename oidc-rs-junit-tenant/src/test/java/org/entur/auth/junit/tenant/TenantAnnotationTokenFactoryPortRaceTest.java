package org.entur.auth.junit.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.ServerSocket;
import org.entur.auth.junit.jwt.EnturProvider;
import org.entur.auth.junit.jwt.PortReservation;
import org.junit.jupiter.api.Test;

class TenantAnnotationTokenFactoryPortRaceTest {

    /**
     * Simulates a parallel test fork stealing the reserved port in the window between the reservation
     * socket being released and WireMock binding it. The factory must recover by reserving a fresh
     * port instead of failing the whole run.
     */
    @Test
    void recoversWhenReservedPortIsStolenBeforeWireMockBinds() throws IOException {
        String propertyName = "TenantAnnotationTokenFactoryPortRaceTest.port";
        PortReservation reservation = new PortReservation(propertyName);
        try {
            reservation.start();
            int stolenPort = reservation.getPort();

            reservation.stop();
            // bind the wildcard address, like a WireMock server in a competing fork would
            try (ServerSocket thief = new ServerSocket(stolenPort)) {
                try (TenantAnnotationTokenFactory factory =
                             new TenantAnnotationTokenFactory(new EnturProvider(), reservation)) {
                    assertNotNull(factory.getServer());
                    assertNotEquals(stolenPort, factory.getServer().getPort());
                    assertEquals(reservation.getPort(), factory.getServer().getPort());
                    assertEquals(
                            Integer.toString(factory.getServer().getPort()), System.getProperty(propertyName));
                }
            }
        } finally {
            reservation.stop();
            System.clearProperty(propertyName);
        }
    }
}
