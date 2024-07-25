import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private ServerSocket serverSocket;
    private final Set<BufferedWriter> subscribers = ConcurrentHashMap.newKeySet();

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    private void startServer() {
        System.out.println("Server started.");
        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("A new client connected");
                new ClientHandler(socket).start();
            } catch (IOException e) {
                System.out.println("An error occurred while accepting a client connection.");
            }
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler extends Thread {
        private Socket socket;
        private BufferedReader input;
        private BufferedWriter output;
        private String role;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                // Read role from the client
                role = input.readLine();
                if (role.equalsIgnoreCase("SUBSCRIBER")) {
                    synchronized (subscribers) {
                        subscribers.add(output);
                    }
                }

                String clientInput;
                while ((clientInput = input.readLine()) != null) {
                    System.out.println("Received: " + clientInput);
                    if (role.equalsIgnoreCase("PUBLISHER")) {
                        synchronized (subscribers) {
                            for (BufferedWriter subscriber : subscribers) {
                                subscriber.write(clientInput + "\n");
                                subscriber.flush();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("An error occurred while communicating with the client.");
            } finally {
                if (role.equalsIgnoreCase("SUBSCRIBER")) {
                    synchronized (subscribers) {
                        subscribers.remove(output);
                    }
                }
                try {
                    input.close();
                    output.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Client disconnected");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: java Server <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        ServerSocket serverSocket = new ServerSocket(port);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}
