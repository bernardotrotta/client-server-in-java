import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.Thread.State;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import messages.Message;
import messages.GreetingMessage;
import messages.Price;
import messages.PurchaseRequests;
import messages.StateMessage;

public class DataServer {
    private static final int SPORT = 4444;

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(SPORT)) {
            System.out.println("Server in ascolto sulla porta: " + SPORT);
            while (true) {
                Socket clientSocket = server.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public double generatePrice() {
        Random random = new Random();
        return Math.round(random.nextDouble(10, 100) * 100.0) / 100.0;
    }

    public void checkPrice(double serverPrice, double clientPrice) {
        if (serverPrice <= clientPrice) {
            try {
                os.writeObject(new PurchaseRequests());
                os.flush();
                System.out.println(serverPrice);
                System.out.println(clientPrice);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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
            is = new ObjectInputStream(clientSocket.getInputStream()); // Ora dopo os
            os = new ObjectOutputStream(clientSocket.getOutputStream());
            sendMessage(new GreetingMessage("Hello!"));
            double price = generatePrice();
            while (true) {

                sendMessage(new Price(price));
                Message message = (Message) is.readObject();
                if (message instanceof Price) {
                    checkPrice(price, ((Price) message).getPrice());
                    // }
                    System.out.println("Client price: " + ((Price) message).getPrice());
                }
                if (message instanceof StateMessage) {
                    System.out.println("Server received: " + ((StateMessage) message).getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}