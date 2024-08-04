import java.io.*;
import java.net.Socket;

public class Client {
    private final Socket socket;
    private final String role;
    private final String topic;
    private final BufferedReader consoleInput;
    private BufferedWriter output;

    public Client(Socket socket, String role, String topic) {
        this.socket = socket;
        this.role = role;
        this.topic = topic;
        this.consoleInput = new BufferedReader(new InputStreamReader(System.in));
        try {
            this.output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            output.write(role + "\n");
            output.write(topic + "\n");
            output.flush();
        } catch (IOException e) {
            System.err.println("Error initializing client: " + e.getMessage());
            closeResources();
        }
    }

    private void startClient() {
        Thread readThread = new Thread(this::readMessages);
        readThread.start();

        try {
            String userInput;
            while (true) {
                if ((userInput = consoleInput.readLine()) != null) {
                    if (userInput.equalsIgnoreCase("terminate")) {
                        break;
                    }
                    if (role.equalsIgnoreCase("PUBLISHER")) {
                        output.write(userInput + "\n");
                        output.flush();
                    }
                }
            }
            System.out.println("Client terminated.");
        } catch (IOException e) {
            System.err.println("Error during communication: " + e.getMessage());
        } finally {
            closeResources();
        }
    }

    private void readMessages() {
        try (BufferedReader serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String serverMessage;
            while ((serverMessage = serverInput.readLine()) != null) {
                if (role.equalsIgnoreCase("SUBSCRIBER")) {
                    System.out.println("Message from Publisher: " + serverMessage);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading messages from server: " + e.getMessage());
        }
    }

    private void closeResources() {
        try {
            if (output != null) output.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Usage: java Client <hostname> <port> <role> <topic>");
            return;
        }

        String hostName = args[0];
        int port = Integer.parseInt(args[1]);
        String role = args[2];
        String topic = args[3];

        try {
            Socket socket = new Socket(hostName, port);
            Client client = new Client(socket, role, topic);
            client.startClient();
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }
}