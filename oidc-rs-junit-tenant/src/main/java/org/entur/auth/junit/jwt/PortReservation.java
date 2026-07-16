package org.entur.auth.junit.jwt;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.SecureRandom;
import javax.net.ServerSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utility for reserving a port. */
public class PortReservation {

    private static final Logger log = LoggerFactory.getLogger(PortReservation.class);

    private static final int PORT_RANGE_MAX = 65535;
    private static final int PORT_RANGE_START = 10000;
    private static final int PORT_RANGE_END = PORT_RANGE_MAX;

    // seeded independently per JVM, so parallel test forks do not probe the same port sequence
    private static final SecureRandom RANDOM = new SecureRandom();

    private final int portRangeStart;
    private final int portRangeEnd;

    private final String propertyName;
    private volatile int port = -1;
    private ServerSocket serverSocket;
    // true when this instance picked the port itself; false when it was pinned via the property
    private boolean portChosenByScan = false;

    public PortReservation(String portNames) {
        this(PORT_RANGE_START, PORT_RANGE_END, portNames);
    }

    public PortReservation(int portRangeStart, int portRangeEnd, String portName) {
        this.portRangeStart = portRangeStart;
        this.portRangeEnd = portRangeEnd;

        this.propertyName = portName;
    }

    public int getPort() {
        return port;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public synchronized boolean start() {
        if (portRangeStart <= 0) {
            throw new IllegalArgumentException("Port range start must be greater than 0.");
        }
        if (portRangeEnd < portRangeStart) {
            throw new IllegalArgumentException("Port range end must not be lower than port range end.");
        }
        if (portRangeEnd > PORT_RANGE_MAX) {
            throw new IllegalArgumentException(
                    "Port range end must not be larger than " + PORT_RANGE_MAX + ".");
        }
        if (serverSocket != null) {
            return true; // already holding a reservation
        }
        // check if the property already exists, if so it must be free
        String property = System.getProperty(propertyName);
        if (property != null) {
            int preconfiguredPort = Integer.parseInt(property);
            // distinguish a port this instance picked earlier from one pinned externally
            boolean ownPort = portChosenByScan && preconfiguredPort == this.port;
            if (reserve(preconfiguredPort, true)) {
                portChosenByScan = ownPort;
                log.warn("Reserved previously configured port " + property);
                return true;
            }
            if (!ownPort) {
                throw new IllegalArgumentException("Preconfigured port " + property + " is not free");
            }
            // the port this reservation picked earlier has been taken by another process;
            // abandon it and scan for a new one
            System.clearProperty(propertyName);
            this.port = -1;
        }
        scan();
        return true;
    }

    /**
     * Abandon the current reservation and reserve a fresh port from the range. Intended for recovery
     * when another process binds the port between {@link #stop()} and the consumer's own bind
     * attempt.
     *
     * @throws IllegalStateException if the port was preconfigured externally and therefore cannot be
     *     changed
     */
    public synchronized void rescan() {
        if (!portChosenByScan && System.getProperty(propertyName) != null) {
            throw new IllegalStateException(
                    "Preconfigured port " + System.getProperty(propertyName) + " cannot be rescanned");
        }
        stop();
        System.clearProperty(propertyName);
        this.port = -1;
        scan();
    }

    private void scan() {
        // systematically try ports in range, starting at a random offset
        int portRange = portRangeEnd - portRangeStart + 1;
        int offset = RANDOM.nextInt(portRange);

        for (int i = 0; i < portRange; i++) {
            int candidatePort = portRangeStart + (offset + i) % portRange;
            if (reserve(candidatePort, false)) {
                portChosenByScan = true;
                log.warn("Reserved newly configured port " + candidatePort);
                return;
            }
        }
        throw new IllegalArgumentException("Unable to reserve free port");
    }

    public synchronized void stop() {
        ServerSocket serverSocket = this.serverSocket;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // ignore
            }

            this.serverSocket = null;
        }
    }

    private boolean reserve(int candidatePort, boolean retry) {
        // While scanning, an occupied port is skipped immediately so the next candidate can be
        // tried; only a specific (preconfigured or previously reserved) port is worth waiting for.
        int attempts = retry ? 10 : 1;
        for (int i = 0; i < attempts; i++) {

            if (i > 0) {
                log.debug("Waiting 1 second before try reserve port {}.", candidatePort);
                try {
                    // Object.wait releases the monitor while sleeping, so other threads are not
                    // blocked for the full retry duration; a stray notify only shortens the wait
                    wait(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            ServerSocket result = capturePort(candidatePort);
            if (result != null) {
                reserved(candidatePort, result);
                return true;
            }
        }
        return false;
    }

    private void reserved(int port, ServerSocket serverSocket) {
        stop(); // release any socket a concurrent caller reserved while this thread waited
        this.port = port;
        this.serverSocket = serverSocket;

        System.setProperty(propertyName, Integer.toString(port));
    }

    private static ServerSocket capturePort(int port) {
        try {
            return ServerSocketFactory.getDefault()
                    .createServerSocket(port, 1, InetAddress.getByName("localhost"));
        } catch (Exception ex) {
            return null;
        }
    }
}
