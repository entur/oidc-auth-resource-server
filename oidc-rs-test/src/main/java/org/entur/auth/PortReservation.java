package org.entur.auth;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import javax.net.ServerSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utility for reserving a port. */
public class PortReservation {

    private static final Logger log = LoggerFactory.getLogger(PortReservation.class);

    private static final int PORT_RANGE_MAX = 65535;
    private static final int PORT_RANGE_START = 10000;
    private static final int PORT_RANGE_END = PORT_RANGE_MAX;

    private final int portRangeStart;
    private final int portRangeEnd;

    public PortReservation(String portNames) {
        this(PORT_RANGE_START, PORT_RANGE_END, portNames);
    }

    public PortReservation(int portRangeStart, int portRangeEnd, String portName) {
        this.portRangeStart = portRangeStart;
        this.portRangeEnd = portRangeEnd;

        this.propertyName = portName;
    }

    private final String propertyName;
    private int port = -1;
    private ServerSocket serverSocket;

    private void reserved(int port, ServerSocket serverSocket) {
        this.port = port;
        this.serverSocket = serverSocket;

        System.setProperty(propertyName, Integer.toString(port));
    }

    public boolean start() {
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
        // check if the property already exists, if so it must be free
        String property = System.getProperty(propertyName);
        if (property != null) {
            if (reserve(Integer.parseInt(property))) {
                log.warn("Reserved previously configured port " + property);
                return true;
            } else {
                throw new IllegalArgumentException("Preconfigured port " + property + " is not free");
            }
        }
        // systematically try ports in range
        // starting at 'random' offset
        int portRange = portRangeEnd - portRangeStart + 1;

        int offset =
                (propertyName.hashCode() + (int) System.currentTimeMillis())
                        % portRange; // more or less random per port name

        for (int i = 0; i < portRange; i++) {
            int candidatePort = portRangeStart + (offset + portRange) % portRange;
            if (reserve(candidatePort)) {
                log.warn("Reserved newly configured port " + candidatePort);
                return true;
            }
        }
        throw new IllegalArgumentException("Unable to reserve free port");
    }

    private boolean reserve(int candidatePort) {
        try {
            ServerSocket result = capturePort(candidatePort);
            if (result != null) {
                reserved(candidatePort, result);

                return true;
            }
        } catch (Exception e) {
            // continue
        }
        return false;
    }

    public int getPort() {
        return port;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void stop() {
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

    private static ServerSocket capturePort(int port) {
        try {
            return ServerSocketFactory.getDefault()
                    .createServerSocket(port, 1, InetAddress.getByName("localhost"));
        } catch (Exception ex) {
            return null;
        }
    }
}
