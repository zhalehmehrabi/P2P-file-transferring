import java.io.IOException;
import java.net.*;
import java.util.LinkedList;

public class Server extends Thread {
    public int port = 8888;

    DatagramSocket socket;
    byte[] receivedBytes = new byte[65535];
    DatagramPacket receivedPacket;
    LinkedList<String> fileNames, filePaths;
    ServerMulticast msServer;

    public Server() {
        fileNames = new LinkedList<>();
        filePaths = new LinkedList<>();
        try {
            socket = new DatagramSocket(this.port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        try {
            msServer = new ServerMulticast(InetAddress.getLocalHost(), fileNames);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    public void addFile(String fileName, String filePath) {
        fileNames.add(fileName);
        filePaths.add(filePath);
        msServer.setFileNames(fileNames);
    }

    @Override
    public void run() {
        try {
            this.listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen() throws IOException {
        msServer.start();
        while (true) {
            receivedBytes = new byte[65535];
            receivedPacket = new DatagramPacket(receivedBytes, receivedBytes.length);
            socket.receive(receivedPacket);
//            now if requests the file directly (not broadcast), create a fileserver object and continue listening
            if (data(receivedBytes).startsWith("gimme")) {
                String fileName = data(receivedBytes).replace("gimme ", "");
                String filePath = "";
                for (int i = 0; i < fileNames.size(); i++) {
                    if (fileNames.get(i).equals(fileName)) {
                        filePath = filePaths.get(i);
                        break;
                    }
                }
                System.out.println("creating fileserver");
                new FileServer(fileName, filePath, receivedPacket.getAddress(), receivedPacket.getPort()).start();
            }
        }
    }


    public static String data(byte[] a) {
        if (a == null)
            return null;
        String ret = "";
        int i = 0;
        while (a[i] != 0)
        {
            ret += (char) a[i];
            i++;
        }
        return ret;
    }


}
