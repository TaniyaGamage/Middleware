import java.io.*;
import java.net.Socket;

public class Client {
    private Socket socket;
    private BufferedReader input;
    private BufferedWriter output;

    public Client(Socket socket){
        this.socket = socket;
        this.input = new BufferedReader(new BufferedReader(new InputStreamReader(System.in)));
        try {
            this.output = new BufferedWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void startClient() {
        try {
            String userInput;
            while(true){
                if ((userInput = input.readLine()) != null){
                    if(userInput.equalsIgnoreCase("terminate")) {
                        break;
                    }
                    output.write(userInput+"\n");
                    output.flush();
                }
            }
            System.out.println("Client terminated.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        if(args.length != 2){
            System.out.println("Usage : java client <hostname> <port>");
            return;
        }

        String hostName = args[0];
        int port = Integer.parseInt(args[1]);

        Socket socket = new Socket(hostName, port);
        Client client = new Client(socket);
        client.startClient();
    }
}
