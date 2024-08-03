import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private ServerSocket serverSocket;
    private final Map<String, Set<BufferedWriter>> topicSubscribers = new ConcurrentHashMap<>();

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
        private String topic;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                role = input.readLine();
                topic = input.readLine();
                if (role.equalsIgnoreCase("SUBSCRIBER")) {
                    synchronized (topicSubscribers) {
                        topicSubscribers.computeIfAbsent(topic, t -> ConcurrentHashMap.newKeySet()).add(output);
                    }
                }

                String clientInput;
                while ((clientInput = input.readLine()) != null) {
                    System.out.println("Received: " + clientInput);
                    if (role.equalsIgnoreCase("PUBLISHER")) {
                        synchronized (topicSubscribers) {
                            Set<BufferedWriter> subscribers = topicSubscribers.get(topic);
                            if (subscribers != null) {
                                for (BufferedWriter subscriber : subscribers) {
                                    subscriber.write(clientInput + "\n");
                                    subscriber.flush();
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("An error occurred while communicating with the client.");
            } finally {
                if (role.equalsIgnoreCase("SUBSCRIBER")) {
                    synchronized (topicSubscribers) {
                        Set<BufferedWriter> subscribers = topicSubscribers.get(topic);
                        if (subscribers != null) {
                            subscribers.remove(output);
                            if (subscribers.isEmpty()) {
                                topicSubscribers.remove(topic);
                            }
                        }
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
