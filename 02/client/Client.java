import java.io.*;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java Client <server ip> <port> <PUBLISHER/SUBSCRIBER>");
            System.exit(1);
        }

        String serverIp = args[0];
        int port = Integer.parseInt(args[1]);
        String role = args[2].toUpperCase();

        try (Socket socket = new Socket(serverIp, port);
             BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send role to server
            output.println(role);

            if ("PUBLISHER".equals(role)) {
                // Publisher mode
                String userInput;
                while ((userInput = input.readLine()) != null) {
                    output.println(userInput);
                    if ("terminate".equalsIgnoreCase(userInput)) {
                        break;
                    }
                }
            } else if ("SUBSCRIBER".equals(role)) {
                // Subscriber mode
                String serverMessage;
                while ((serverMessage = serverInput.readLine()) != null) {
                    System.out.println(serverMessage);
                }
            } else {
                System.out.println("Invalid role. Please specify PUBLISHER or SUBSCRIBER.");
            }

        } catch (IOException ex) {
            System.out.println("Client exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
