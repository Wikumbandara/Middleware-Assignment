import java.io.*;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Usage: java Client <server ip> <port> <PUBLISHER/SUBSCRIBER> <topic>");
            System.exit(1);
        }

        String serverIp = args[0];
        int port = Integer.parseInt(args[1]);
        String clientType = args[2];
        String topic = args[3];

        try (Socket socket = new Socket(serverIp, port);
             BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send client type and topic to the server
            output.println(clientType);
            output.println(topic);

            if ("PUBLISHER".equalsIgnoreCase(clientType)) {
                String userInput;
                while ((userInput = input.readLine()) != null) {
                    output.println(userInput);
                    if ("terminate".equalsIgnoreCase(userInput)) {
                        break;
                    }
                }
            } else if ("SUBSCRIBER".equalsIgnoreCase(clientType)) {
                String serverMessage;
                while ((serverMessage = serverInput.readLine()) != null) {
                    System.out.println(serverMessage);
                }
            }

        } catch (IOException ex) {
            System.out.println("Client exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
