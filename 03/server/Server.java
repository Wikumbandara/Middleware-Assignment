import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {
    // Map to store topics and their associated subscribers
    private static final Map<String, List<PrintWriter>> topicSubscribers = new HashMap<>();

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Server <port>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                new ServerThread(socket).start();
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Thread class to handle each client connection
    static class ServerThread extends Thread {
        private Socket socket;

        public ServerThread(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {

                String clientType = input.readLine();
                String topic = input.readLine();

                if ("SUBSCRIBER".equalsIgnoreCase(clientType)) {
                    // Add subscriber to the topic list
                    topicSubscribers.computeIfAbsent(topic, k -> new ArrayList<>()).add(output);
                    System.out.println("Subscriber added to topic: " + topic);

                    // Keep the subscriber connection alive
                    while (true) {
                        // Keep the connection alive until client disconnects
                        // No need to read messages, just hold the connection
                    }

                } else if ("PUBLISHER".equalsIgnoreCase(clientType)) {
                    System.out.println("Publisher for topic: " + topic);

                    String message;
                    while ((message = input.readLine()) != null) {
                        System.out.println("Received from publisher: " + message);
                        // Send the message to all subscribers of the topic
                        List<PrintWriter> subscribers = topicSubscribers.get(topic);
                        if (subscribers != null) {
                            for (PrintWriter subscriber : subscribers) {
                                subscriber.println("Message on " + topic + ": " + message);
                            }
                        }

                        if ("terminate".equalsIgnoreCase(message)) {
                            System.out.println("Publisher terminated the connection");
                            break;
                        }
                    }
                }

            } catch (IOException ex) {
                System.out.println("Server exception: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}
