import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final List<PrintWriter> subscribers = new ArrayList<>();

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

    static class ServerThread extends Thread {
        private Socket socket;
        private PrintWriter output;

        public ServerThread(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String role = input.readLine(); // Read the role (PUBLISHER or SUBSCRIBER)
                System.out.println("Client role: " + role);

                if ("SUBSCRIBER".equalsIgnoreCase(role)) {
                    synchronized (subscribers) {
                        output = new PrintWriter(socket.getOutputStream(), true);
                        subscribers.add(output);
                    }
                }

                String clientMessage;
                while ((clientMessage = input.readLine()) != null) {
                    System.out.println("Received: " + clientMessage);
                    if ("terminate".equalsIgnoreCase(clientMessage)) {
                        System.out.println("Client terminated the connection");
                        break;
                    }

                    if ("PUBLISHER".equalsIgnoreCase(role)) {
                        // Echo the message to all subscribers
                        synchronized (subscribers) {
                            for (PrintWriter subscriber : subscribers) {
                                subscriber.println("Message from publisher: " + clientMessage);
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                System.out.println("Server exception: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                if (output != null) {
                    synchronized (subscribers) {
                        subscribers.remove(output);
                    }
                }
                try {
                    socket.close();
                } catch (IOException ex) {
                    System.out.println("Failed to close socket: " + ex.getMessage());
                }
            }
        }
    }
}
