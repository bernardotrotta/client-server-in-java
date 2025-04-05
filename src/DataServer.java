import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import messages.*;

public class DataServer {
    private static final int SPORT = 4444;
    private static int sessionId;

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(SPORT)) {
            System.out.println("Server in ascolto sulla porta: " + SPORT);
            while (true) {
                Socket clientSocket = server.accept();
                new Thread(new ClientHandler(clientSocket, sessionId)).start();
                sessionId++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


class ClientHandler implements Runnable {
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private final Socket clientSocket;
    private final int sessionId;

    public ClientHandler(Socket clientSocket, int sessionId) {
        this.clientSocket = clientSocket;
        this.sessionId = sessionId;
    }

    public double generatePrice() {
        Random random = new Random();
        return Math.round(random.nextDouble(10, 100) * 100.0) / 100.0;
    }

    public void checkPrice(double serverPrice, double clientPrice) {
        if (serverPrice <= clientPrice) {
            sendMessage(new PurchaseRequests());
        }
    }

    public void sendMessage(Message message) {
        try {
            os.writeObject(message);
            os.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            os = new ObjectOutputStream(clientSocket.getOutputStream());
            is = new ObjectInputStream(clientSocket.getInputStream());
            GreetingMessage greetingMessage = new GreetingMessage("Hello");
            greetingMessage.setSessionId(sessionId);
            sendMessage(greetingMessage);
            Message message = (Message) is.readObject();
            if (message instanceof GreetingMessage) {
                System.out.println("Connection established with client["+ sessionId +"]: " + ((GreetingMessage) message).getMessage());
                boolean running = true;
                double generatedPrice = generatePrice();
                sendMessage(new Price(generatedPrice));
                while (running) {
                    message = (Message) is.readObject();
                    switch (message) {
                        case Price price -> checkPrice(generatedPrice, price.getPrice());
                        case StateMessage stateMessage -> System.out.println("Server received: " + stateMessage.getMessage());
                        case PurchaseCompleted purchaseCompleted -> {
                            running = false;
                        }
                        case null, default -> System.out.println("Unknown message type received");
                    }
                    generatedPrice = generatePrice();
                    sendMessage(new Price(generatedPrice));
                }
            }
        } catch (Exception e) {
            System.out.println("Errore");
            e.printStackTrace();

        } finally {
            try {
                if (is != null) is.close();
                if (os != null) os.close();
                if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
            } catch (IOException e) {
                System.out.println("Errore");
                e.printStackTrace();
            }
        }
    }

}