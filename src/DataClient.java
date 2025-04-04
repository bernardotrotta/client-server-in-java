import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

import messages.GreetingMessage;
import messages.Message;
import messages.Price;
import messages.PurchaseRequests;
import messages.StateMessage;

public class DataClient implements Runnable {
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private static final int SPORT = 4444;
    private static final String SHOST = "localhost";
    private int acquisti;

    public void generatePrices() {
        Random random = new Random();
        double price = Math.round(random.nextDouble(10, 75) * 100.0) / 100.0;
        System.out.println(price);
    }

    @Override
    public void run() {
        try {
            Socket client = new Socket(SHOST, SPORT);
            os = new ObjectOutputStream(client.getOutputStream());
            is = new ObjectInputStream(client.getInputStream());
            try {
                while (acquisti <= 10) {
                    Message message = (Message) is.readObject();
                    switch (message) {
                        case GreetingMessage greetingMessage -> {
                            os.writeObject(new GreetingMessage("Hi!"));
                            os.flush();
                            System.out.println("Client received: " + greetingMessage.getMessage());
                        }
                        case Price price -> {
                            os.writeObject(new Price(60.0));
                            os.flush();
                            System.out.println(price.getPrice());
                        }
                        case PurchaseRequests purchaseRequests -> {
                            os.writeObject(new StateMessage("Acquisto completato!"));
                            acquisti++;
                            // client.close();
                        }
                        case null, default -> System.out.println("Unknown message type received");
                    }
                }
                client.close();



            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
    }

    public static void main(String[] args) {
        new Thread(new DataClient()).start();
    }
}
