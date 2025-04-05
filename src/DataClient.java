import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

import messages.*;

public class DataClient implements Runnable {
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private static final int SPORT = 4444;
    private static final String SHOST = "localhost";
    private int acquisti;
    private int sessionId;

    public double generatePrice() {
        Random random = new Random();
        return Math.round(random.nextDouble(10, 100) * 100.0) / 100.0;
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
            Socket client = new Socket(SHOST, SPORT);
            os = new ObjectOutputStream(client.getOutputStream());
            is = new ObjectInputStream(client.getInputStream());
            try {
                while (acquisti < 3) {
                    Message message = (Message) is.readObject();
                    switch (message) {
                        case GreetingMessage greetingMessage -> {
                            sessionId = greetingMessage.getSessionId();
                            sendMessage(new GreetingMessage("Hi!"));
                            System.out.println("Connection established with the server: " + greetingMessage.getMessage());
                        }
                        case Price price -> {
                            double generatedPrice = generatePrice();
                            sendMessage(new Price(generatedPrice));
                            System.out.println("[ Server " + sessionId + " ] Price: " + price.getPrice());
                            System.out.println("[ Client " + sessionId + " ] Price: " + generatedPrice);
                        }
                        case PurchaseRequests purchaseRequests -> {
                            sendMessage(new StateMessage("Purchase completed!"));
                            acquisti++;
                            System.out.println("[ Client " + sessionId + " ] Purchases: " + acquisti);
                        }
                        case null, default -> System.out.println("Unknown message type received");
                    }

                }
                sendMessage(new StateMessage("Il client ha terminato gli acquisti"));
                sendMessage(new PurchaseCompleted());

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is != null) is.close();
                    if (os != null) os.close();
                    if (client != null && !client.isClosed()) client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Thread(new DataClient()).start();
        new Thread(new DataClient()).start();
        new Thread(new DataClient()).start();
        new Thread(new DataClient()).start();


    }
}
