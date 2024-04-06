import java.io.*;
import java.net.ConnectException;
import java.net.Socket;

public class ASyncTCPClient {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 7777;
    private static final int RECONNECT_DELAY_MS = 5000;

    public static void main(String[] args) {
        ASyncTCPClient posClient = new ASyncTCPClient();
        posClient.start();
    }

    public void start() {
        while (true) {
            try {
                Socket socket = connect();
                if (socket != null) {
                    String message = "08008238000000000000040000000000000003121741001741001741000312301";
                    Thread readThread = new Thread(new Reader(socket));
                    Thread writerThread = new Thread(new Writer(socket, message));
                    readThread.start();
                    writerThread.start();
                    readThread.join();
                    writerThread.join();

                }
            } catch (IOException | InterruptedException e) {
                System.out.println("Connection lost, retrying in 5 seconds...");
                try {
                    Thread.sleep(RECONNECT_DELAY_MS);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private Socket connect() throws IOException {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            System.out.println("Connected ..."+SERVER_ADDRESS  +":"+SERVER_PORT + socket.getLocalSocketAddress() );
            return socket;
        } catch (ConnectException e) {
            throw new IOException("Connection failed", e);
        }
    }
}





class Reader implements Runnable {
    private final Socket socket;

    public Reader(Socket socket) {
        this.socket = socket;
    }

    private String processRequest(String request){
        String response = request; // process the request and return response
        return response;
    }

    @Override
    public void run() {
        try {
            DataInputStream dIn = new DataInputStream(socket.getInputStream());
            while (true) {
                byte[] buffer = new byte[1024];
                int bytesRead = dIn.read(buffer);
                if (bytesRead == -1) {
                    System.out.println("Disconnected ....");
                    ASyncTCPClient posClient = new ASyncTCPClient();
                    posClient.start();
                }
                String request = new String(buffer, 0, bytesRead);
                System.out.println("Request Received: "+ request);
                String response = processRequest(request);
                Thread threadResponse = new Thread(new Writer(socket, response));
                threadResponse.start();
            }
        } catch (IOException e) {
            System.out.println("Error in reading: " + e.getMessage());
            try {
                socket.close();
            } catch (IOException ex) {
                System.out.println("Error closing socket: " + ex.getMessage());
            }
        }
    }
}




class Writer implements Runnable {
    private final Socket socket;
    private final String message;

    public Writer(Socket socket, String message) {
        this.socket = socket;
        this.message = message;
    }

    @Override
    public void run() {
        try {
            DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
            dOut.writeUTF(message);
            dOut.flush();
            System.out.println("Response sent :" + message);

        } catch (IOException e) {
            System.out.println("Error in writing: " + e.getMessage());
            try {
                socket.close();
            } catch (IOException ex) {
                System.out.println("Error closing socket: " + ex.getMessage());
            }

            ASyncTCPClient posClient = new ASyncTCPClient();
            posClient.start();
        }
    }

}






