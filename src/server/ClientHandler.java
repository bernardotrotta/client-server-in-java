package server;

import messages.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final int sessionId;
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private boolean isRunning;
    private int clientPurchases = 0;

    public ClientHandler(Socket clientSocket, int sessionId) {
        this.clientSocket = clientSocket;
        this.sessionId = sessionId;
    }

    private double generatePrice() throws InterruptedException {
        Thread.sleep(1000);
        Random random = new Random();
        return Math.round(random.nextDouble(10, 100) * 100.0) / 100.0;
    }

    private void sendMessage(Message<?> message) throws IOException {
        if (!clientSocket.isClosed()) {
            os.writeObject(message);
            os.flush();
        } else {
            is.close();
            os.close();
        }
    }

    private void handlePriceMessage(double serverPrice, Price clientPrice) throws IOException {
        printPriceLog(serverPrice, clientPrice.get());
        if (serverPrice <= clientPrice.get()) {
            clientPurchases++;
            sendMessage(new PurchaseRequests());
            System.out.printf("[Session %d] Sent purchase request to client%n", sessionId);
        } else {
            sendMessage(new Price(serverPrice));
        }
    }

    private void handleStateMessage(StateMessage stateMessage) throws InterruptedException, IOException {
        System.out.printf("[Session %d] %s%n", sessionId, stateMessage.get());
        double serverPrice = generatePrice();
        sendMessage(new Price(serverPrice));
    }

    private void handlePurchaseCompletedMessage(PurchaseCompleted purchaseCompleted) throws IOException {
        sendMessage(new GoodbyeMessage());
        System.out.println(purchaseCompleted.get());
        isRunning = false;
    }

    private void closeConnection() throws IOException {
        is.close();
        os.close();
        clientSocket.close();
        DataServer.clientsCompleted.getAndDecrement();
        if (DataServer.clientsCompleted.get() == 0) {
            System.exit(0);
        }
    }

    private void openConnection() throws IOException {
        os = new ObjectOutputStream(clientSocket.getOutputStream());
        os.flush();
        is = new ObjectInputStream(clientSocket.getInputStream());
        isRunning = true;
    }

    private void handleMessages(Message<?> message, Double serverPrice) throws IOException, InterruptedException {
        switch (message) {
            case Price clientPrice -> handlePriceMessage(serverPrice, clientPrice);
            case StateMessage stateMessage -> handleStateMessage(stateMessage);
            case PurchaseCompleted purchaseCompleted -> handlePurchaseCompletedMessage(purchaseCompleted);
            default -> System.out.println("Unknown message type");
        }
    }

    private void greetClient() throws IOException {
        GreetingMessage greetingMessage = new GreetingMessage("Hello!", sessionId);
        sendMessage(greetingMessage);
    }

    private Message<?> readMessage() throws IOException, ClassNotFoundException {
        return (Message<?>) is.readObject();
    }

    private boolean isValidGreeting(Message<?> message) {
        if (message instanceof GreetingMessage greetingMessage) {
            System.out.println("Connection established with client[" + sessionId + "]: " + greetingMessage.get());
            return true;
        } else {
            return false;
        }

    }

    private void printPriceLog(double serverPrice, double clientPrice) {
        System.out.printf("[Session %d] Purchases: %d | Server Price: $%.2f | Client Price: $%.2f%n", sessionId, clientPurchases, serverPrice, clientPrice);
    }


    @Override
    public void run() {
        try {
            openConnection();
            greetClient();
            Message<?> message = readMessage();
            if (isValidGreeting(message)) {
                double serverPrice = generatePrice();
                sendMessage(new Price(serverPrice));
                printPriceLog(serverPrice, 0);
                while (isRunning) {
                    message = readMessage();
                    serverPrice = generatePrice();
                    handleMessages(message, serverPrice);
                }
                closeConnection();
            }
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
