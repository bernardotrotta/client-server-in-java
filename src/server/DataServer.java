package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DataServer {
    public static final int NTHREADS = 10;
    private static final int SPORT = 4444;
    private static final AtomicInteger sessionId = new AtomicInteger(1001);
    private static final Logger logger = Logger.getLogger(DataServer.class.getName());
    public static AtomicInteger clientsCompleted = new AtomicInteger(0);

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(NTHREADS);
        try (ServerSocket server = new ServerSocket(SPORT)) {
            System.out.println("Server listening on the port: " + SPORT);
            int i = 0;
            while (i < NTHREADS) {
                Socket clientSocket = server.accept();
                int currentSessionId = sessionId.getAndIncrement();
                executor.execute(new ClientHandler(clientSocket, currentSessionId));
                clientsCompleted.getAndIncrement();
                i++;
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error during server execution", e);
        }
    }
}