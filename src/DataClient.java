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
    private int purchasesCounter;
    private final int PURCHASESLIMIT = 1;
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
            Socket socket = new Socket(SHOST, SPORT);
            os = new ObjectOutputStream(socket.getOutputStream());
            os.flush();
            is = new ObjectInputStream(socket.getInputStream());
            Message message = (Message) is.readObject();
            if (message instanceof GreetingMessage) {
                sessionId = ((GreetingMessage) message).getSessionId();
                sendMessage(new GreetingMessage("Hi!"));
                System.out.println("Connection established with the server: " + ((GreetingMessage) message).getMessage());
                while (purchasesCounter < PURCHASESLIMIT) {

                    message = (Message) is.readObject();
                    switch (message) {
                        case Price price -> { double generatedPrice = generatePrice();
                            sendMessage(new Price(generatedPrice));
                            System.out.println("[ Server " + sessionId + " ] Price: " + price.getPrice());
                            System.out.println("[ Client " + sessionId + " ] Price: " + generatedPrice); }
                        case PurchaseRequests purchaseRequest -> {
                            if (PURCHASESLIMIT - purchasesCounter == 1) {
                                sendMessage(new PurchaseCompleted("Client has finished his purchases"));
                            }
                            else {
                                sendMessage(new StateMessage("Purchase completed!"));
                            }
                            purchasesCounter++;
                            System.out.println("[ Client " + sessionId + " ] Purchases: " + purchasesCounter); }
                        case null, default -> System.out.println("Unknown message type");
                    }
                }


                message = (Message) is.readObject();
                if (message instanceof GoodbyeMessage) {
                    is.close();
                    os.close();
                    socket.close();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
        new Thread(new DataClient()).start();
        new Thread(new DataClient()).start();
        new Thread(new DataClient()).start();
        new Thread(new DataClient()).start();
        new Thread(new DataClient()).start();


    }
}
