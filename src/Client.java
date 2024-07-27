import java.io.*;
import java.net.Socket;

public class Client {
    private Socket socket;
    private String role;
    private BufferedReader input;
    private BufferedWriter output;

    public Client(Socket socket, String role) {
        this.socket = socket;
        this.role = role;
        this.input = new BufferedReader(new InputStreamReader(System.in));
        try {
            this.output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            output.write(role + "\n");
            output.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void startClient() {
        Thread readThread = new Thread(this::readMessages);
        readThread.start();

        try {
            String userInput;
            while (true) {
                if ((userInput = input.readLine()) != null) {
                    if (userInput.equalsIgnoreCase("terminate")) {
                        break;
                    }
                    output.write(userInput + "\n");
                    output.flush();
                }
            }
            System.out.println("Client terminated.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                output.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        } 
        catch (IOException e) {
            // System.out.println("An error occurred while reading messages from the server.");
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Usage: java Client <hostname> <port> <role>");
            return;
        }

        String hostName = args[0];
        int port = Integer.parseInt(args[1]);
        String role = args[2];

        Socket socket = new Socket(hostName, port);
        Client client = new Client(socket, role);
        client.startClient();
    }
}
