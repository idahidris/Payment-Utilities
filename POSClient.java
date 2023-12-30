import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.security.cert.X509Certificate;

public class POSClient {

    public static void main(String[] args) {
        String isoMessageNetworkRequest = "080022380000008000009A0000080413303013303013303008042105F597";//replace with your request
        String isoMessagePurchaseRequest = "0200F23C44D129E09000000000000000002216418742210024332100000000000065650003130048250002070048250313210659990510012C0000000006111129374187422100243321D2106226124306470000020031300482522620390006203900000000006Citi"; //replace with your request
        String serverAddress = "127.0.0.1"; //replace with your server ip
        int serverPort = 9999; //replace with your server port

        POSClient posClient = new POSClient();
        String isoMessageNetworkResponse = posClient.processMessage(serverAddress, serverPort, isoMessageNetworkRequest);
        System.out.println("isoMessageNetworkResponse received: " + isoMessageNetworkResponse);
        //    0810023800000280000008041330301330301330300804252105F597

        String isoMessagePurchaseResponse = posClient.processMessage(serverAddress, serverPort, isoMessagePurchaseRequest);
        System.out.println("isoMessagePurchaseResponse received: " + isoMessagePurchaseResponse);

    }

    public String processMessage(String serverAddress, int serverPort, String isoMessage) {
        Socket socket = null;
        DataOutputStream dOut = null;
        DataInputStream dIn = null;

        try {
            // Load the truststore
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(
                                X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                X509Certificate[] certs, String authType) {
                        }
                    }};
            SSLContext sc = SSLContext.getInstance("SSL");

            sc.init(null, trustAllCerts, new java.security.SecureRandom());


            // Connect to the server using SSL
            SSLSocketFactory sslSocketFactory = sc.getSocketFactory();
            socket = sslSocketFactory.createSocket(serverAddress, serverPort);
            socket.setSoTimeout(60000);

            dOut = new DataOutputStream(socket.getOutputStream());
            dIn = new DataInputStream(socket.getInputStream());
            System.out.println("sending ...." + isoMessage);
            dOut.writeUTF(isoMessage);

            byte[] buffer = new byte[1024];
            int bytesRead = dIn.read(buffer);
            String response = new String(buffer, 0, bytesRead);
            // response = dIn.readUTF();

            // printHexDump(response.getBytes());
            return response;


        } catch (ConnectException connectException) {
            System.out.println("ConnectException: " + connectException.getMessage());
        } catch (EOFException eOFException) {
            System.out.println("EOFException: " + eOFException.getMessage());
        } catch (IOException ioException) {
            System.out.println("IOException: " + ioException.getMessage());
        } catch (Throwable throwable) {
            System.out.println("Throwable: " + throwable.getMessage());
        } finally {
            this.close(dOut);
            this.close(dIn);
            this.close(socket);
        }

        return "";
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
