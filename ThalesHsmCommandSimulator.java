import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ThalesHsmCommandSimulator {


    public static void main(String[] args) {

        String hsmServerIp = "127.0.0.1";// replace with appropriate hsm server IP
        int port = 9990; // replace with appropriate hsm server port
        String command = "0000CAU.........."; // replace with appropriate hsm command, first 4 digits is the header-length
        ThalesHsmCommandSimulator simulator = new ThalesHsmCommandSimulator();
        simulator.processCommand(hsmServerIp, port, command);
        //Connected to HSM server: 127.0.0.1
        //Request sent to HSM: 0000CA........
        //Response received from HSM: 0000CB00..........


    }

    private void processCommand(String hsmServerIp, int hsmServerPort, String command) {

        DataInputStream serverIn = null;
        DataOutputStream serverOut = null;
        Socket socket = null;

        try {
            socket = new Socket(hsmServerIp, hsmServerPort);
            socket.setSoTimeout(900000);
            serverIn = new DataInputStream(
                    new BufferedInputStream(socket.getInputStream())
            );
            serverOut = new DataOutputStream(
                    new BufferedOutputStream(socket.getOutputStream(), 4096)
            );
            System.out.println("Connected to HSM server: " + hsmServerIp);

            byte[] request = command.getBytes(StandardCharsets.UTF_8);
            int len = request.length;
            serverOut.write(len >> 8);
            serverOut.write(len);
            serverOut.write(request);
            serverOut.flush();
            System.out.println("Request sent to HSM: " + new String(request));

            byte[] b = new byte[2];
            serverIn.readFully(b, 0, 2);
            int len2 = ((int) b[0] & 0xFF) << 8 |
                    (int) b[1] & 0xFF;

            byte[] response = new byte[len2];
            serverIn.readFully(response, 0, response.length);

            // Receiving response
            System.out.println("Response received from HSM: " + new String(response));

        } catch (ConnectException connectException) {
            System.out.println("ConnectException: " + connectException.getMessage());
        } catch (EOFException eOFException) {
            System.out.println("EOFException: " + eOFException.getMessage());
        } catch (IOException ioException) {
            System.out.println("IOException: " + ioException.getMessage());
        } catch (Throwable throwable) {
            System.out.println("Throwable: " + throwable.getMessage());
        } finally {
            this.close(serverIn);
            this.close(serverOut);
            this.close(socket);

        }
    }

    private void close(InputStream inputStream) {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException iOException) {
            System.out.println("IOException: " + iOException.getMessage());
        }
    }


    private void close(OutputStream outputStream) {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException iOException) {
            System.out.println("IOException: " + iOException.getMessage());
        }
    }

    private void close(Socket socket) {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException iOException) {
            System.out.println("IOException: " + iOException.getMessage());
        }
    }

}
