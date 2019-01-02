import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.LinkedList;

public class ServerMulticast extends Thread {
    InetAddress serverIp;
    int multicastPort = 8866;
    MulticastSocket msSocket;
    LinkedList<String> fileNames;

    public ServerMulticast(InetAddress serverIp, LinkedList<String> fileNames) {
        this.fileNames = fileNames;
        this.serverIp = serverIp;
        try {
            InetAddress msIp = InetAddress.getByName("230.0.0.0");
            msSocket = new MulticastSocket(multicastPort);
            msSocket.joinGroup(msIp);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setFileNames(LinkedList<String> fileNames) {
        this.fileNames = fileNames;
    }

    @Override
    public void run() {
        DatagramPacket receivedPacket;
        System.out.println("multicast started listening");
        while (true) {
            byte[] receivedBytes = new byte[65535];
            receivedPacket = new DatagramPacket(receivedBytes, receivedBytes.length);
            try {
                msSocket.receive(receivedPacket);
                System.out.println("got sth");
            } catch (IOException e) {
                e.printStackTrace();
            }
            // if received a broadcast message, check if file is ready to send, then inform the client
            if (fileNames.contains(data(receivedBytes))) {
                InetAddress receiverIP = receivedPacket.getAddress();
                int receiverPort = receivedPacket.getPort();
                String response = "Got the file " + serverIp.getHostAddress();
                DatagramPacket sendPacket = new DatagramPacket(response.getBytes(), response.getBytes().length, receiverIP, receiverPort);
                try {
                    Thread.sleep(3);
                    msSocket.send(sendPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
