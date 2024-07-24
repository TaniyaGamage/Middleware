import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
   private ServerSocket serverSocket;

   public  Server(ServerSocket serverSocket){
       this.serverSocket = serverSocket;
   }

    private void startServer() {
       while (!serverSocket.isClosed()) {
           try {
               Socket socket = serverSocket.accept();
               System.out.println("A new client connected");

               BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
               String clientInput;

               while ((clientInput = input.readLine()) != null) {
                   System.out.println(clientInput);
               }
               input.close();
               socket.close();
               System.out.println("Client disconnected");

           } catch (IOException e) {
               System.out.println("An error occurred");
           }
       }


        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

   public static void main(String[] args) throws IOException {
       if(args.length != 1){
           System.out.println("Usage : java Server <port>");
           return;
       }

       int port = Integer.parseInt(args[0]);
       ServerSocket serverSocket = new ServerSocket(port);
       Server server = new Server(serverSocket);
       server.startServer();
   }
}
