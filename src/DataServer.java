import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import messages.*;

public class DataServer {
    private static final int SPORT = 4444;
    private static final AtomicInteger sessionId = new AtomicInteger(1000);
    public static final int NTHREADS = 10;
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
                i++;
            }
            executor.close();
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

    public double generatePrice() throws InterruptedException {
        Thread.sleep(1000);
        Random random = new Random();
        return Math.round(random.nextDouble(10, 100) * 100.0) / 100.0;
    }

    public void checkPrice(double serverPrice, double clientPrice) throws IOException, InterruptedException {
        if (serverPrice <= clientPrice) {
            sendMessage(new PurchaseRequests());
        }
    }

    public void sendMessage(Message message) throws IOException {
        if (!(clientSocket.isClosed())) {
            os.writeObject(message);
            os.flush();
        }
        else {
            is.close();
            os.close();
        }
    }


    @Override
    public void run() {
        try {
            os = new ObjectOutputStream(clientSocket.getOutputStream());
            os.flush();
            is = new ObjectInputStream(clientSocket.getInputStream());

            GreetingMessage greetingMessage = new GreetingMessage("Hello!");
            greetingMessage.setSessionId(sessionId);
            sendMessage(greetingMessage);

            Message message = (Message) is.readObject();
            if (message instanceof GreetingMessage) {
                System.out.println("Connection established with client["+ sessionId +"]: " + ((GreetingMessage) message).getMessage());

                boolean running = true;

                ExecutorService executor = Executors.newSingleThreadExecutor();
                Callable<Double> generateNumberCallable = () -> {
                    Thread.sleep(1000);
                    Random random = new Random();
                    return Math.round(random.nextDouble(10, 100) * 100.0) / 100.0;
                };

//                double serverPrice = 100;

                while (running) {
                    double serverPrice = generatePrice();
                    sendMessage(new Price(serverPrice));
                    // Future<Double> future = executor.submit(generateNumberCallable);
                    message = (Message) is.readObject();
                    switch (message) {
                        case Price clientPrice -> checkPrice(serverPrice, clientPrice.getPrice());
                        case StateMessage stateMessage -> System.out.println("Server received: " + stateMessage.getMessage());
                        case PurchaseCompleted purchaseCompleted -> {
                            sendMessage(new GoodbyeMessage());
                            System.out.println(purchaseCompleted.getMessage());
                            running = false;
                        }
                        case null, default -> System.out.println("Unknown message type");
                    }
                    //                    serverPrice = future.get();
                }
                is.close();
                os.close();
                clientSocket.close();
                DataServer.clientsCompleted.getAndIncrement();
            }
            else {
                sendMessage(greetingMessage);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}


