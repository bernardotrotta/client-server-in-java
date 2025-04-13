import messages.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

public class DataClient implements Runnable {
    private static final int SPORT = 4444;
    private static final String SHOST = "localhost";
    private final int PURCHASESLIMIT = 10;
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private int purchasesCounter;
    private int sessionId;

    public static void main(String[] args) {
        new Thread(new DataClient()).start();
        new Thread(new DataClient()).start();
        new Thread(new DataClient()).start();
    }

    public double generatePrice() {
        Random random = new Random();
        return Math.round(random.nextDouble(10, 100) * 100.0) / 100.0;
    }

    public void sendMessage(Message<?> message) {
        try {
            os.writeObject(message);
            os.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handlePriceMessage(Price price) {
        double generatedPrice = generatePrice();
        sendMessage(new Price(generatedPrice));
        printPriceLog(price.get(), generatedPrice);
    }

    private void handlePurchaseRequest() {
        purchasesCounter++;
        if (PURCHASESLIMIT == purchasesCounter) {
            sendMessage(new PurchaseCompleted(String.format("[Session %d] Client has finished his purchases!", sessionId)));
        } else {
            sendMessage(new StateMessage("Purchase completed!"));
        }
    }

    private void handleMessage(Message<?> message) {
        switch (message) {
            case Price price -> handlePriceMessage(price);
            case PurchaseRequests _ -> handlePurchaseRequest();
            default -> System.out.println("Unknown message type");
        }
    }

    private void openConnection(Socket socket) throws IOException {
        os = new ObjectOutputStream(socket.getOutputStream());
        os.flush();
        is = new ObjectInputStream(socket.getInputStream());
    }

    private void closeConnection(Socket socket) throws IOException {
        is.close();
        os.close();
        socket.close();
    }

    private void greetServer(Message<?> message) {
        sessionId = ((GreetingMessage) message).getSessionId();
        sendMessage(new GreetingMessage("Hi!", sessionId));
        System.out.println("Connection established with the server: " + message.get());
    }

    private void printPriceLog(double serverPrice, double clientPrice) {
        System.out.printf("[Session %d] Purchases: %d | Server Price: $%.2f | Client Price: $%.2f%n", sessionId, purchasesCounter, serverPrice, clientPrice);
    }

    private Message<?> readMessage() throws IOException, ClassNotFoundException {
        return (Message<?>) is.readObject();
    }

    private boolean isValidGreeting(Message<?> message) {
        if (message instanceof GreetingMessage) {
            greetServer(message);
            System.out.printf("[Session %d] Connection established with the server%n", sessionId);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(SHOST, SPORT);
            openConnection(socket);
            Message<?> message = readMessage();
            if (isValidGreeting(message)) {
                while (purchasesCounter < PURCHASESLIMIT) {
                    message = readMessage();
                    handleMessage(message);
                }
                message = readMessage();
                if (message instanceof GoodbyeMessage) {
                    closeConnection(socket);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}