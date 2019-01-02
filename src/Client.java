import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import java.util.Scanner;

public class Client {
    int port = 8877;
    int msPort = 8866;
    int serverPort = 8888;
    InetAddress msIp;
    DatagramSocket socket;
    InetAddress currentServerIP;
    String currentFileName;

    public Client() {
        try {
            msIp = InetAddress.getByName("230.0.0.0");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }

    public void run() throws IOException {
        while (true) {
            System.out.println("------------------------------------------------");
            boolean broadcastMade = this.sendBroadcast();
            if (broadcastMade) {
                boolean foundAPeer = this.waitForPeers();
                if (!foundAPeer) {
                    System.out.println("Timeout... maybe next time :)");
                    continue;
                }
                System.out.println("Found a peer... requesting for file...");
                this.requestForFile();
                this.receiveFile();
            }
            else {
                System.out.println("Something is wrong with your request");
            }



        }
    }

    private boolean sendBroadcast() throws IOException {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        if (input.startsWith("p2p -receive")) {
            String[] parsed = input.split(" ");
            DatagramPacket sendPacket;
            String request = parsed[2];
            currentFileName = request;
            socket.close();
            MulticastSocket msSocket = new MulticastSocket(port);
            sendPacket = new DatagramPacket(request.getBytes(), request.getBytes().length, msIp, msPort);
//            socket.send(sendPacket);
            msSocket.send(sendPacket);
            msSocket.close();
            socket = new DatagramSocket(port);
            System.out.println("sent the broadcast");
            return true;
        }
        return false;
    }

    private boolean waitForPeers() throws SocketException {
        System.out.println("Waiting 15 seconds for a response from a peer...");
        try {
            socket.setSoTimeout(15000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        byte[] receivedBytes = new byte[65535];
        DatagramPacket receivedPacket = new DatagramPacket(receivedBytes, receivedBytes.length);
        try {
            socket.receive(receivedPacket);
            socket.close();
        } catch (IOException e) {
            // timeout
            return false;
        }
        String rec = data(receivedBytes);
        rec = rec.replace("Got the file ", "");
        try {
            currentServerIP = InetAddress.getByName(rec);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        socket = new DatagramSocket(port);
        return true;
    }

    private void requestForFile() throws IOException {
        String request = "gimme " + currentFileName;
        DatagramPacket sendPacket = new DatagramPacket(request.getBytes(), request.getBytes().length, currentServerIP, serverPort);
        socket.send(sendPacket);
    }

    private void receiveFile() throws IOException {
        // receive number of packets
        byte[] receivedBytes = new byte[65535];
        DatagramPacket receivePacket = new DatagramPacket(receivedBytes, receivedBytes.length);
        socket.receive(receivePacket);
        String response = data(receivedBytes);
        int packets = Integer.valueOf(response.split(" ")[0]);
        int size = Integer.valueOf(response.split(" ")[1]);
        int fileLength = Integer.valueOf(response.split(" ")[2]);
        socket.setReceiveBufferSize(10 * size);
        byte[] fin = new byte[fileLength];
        byte[][] finFile = new byte[packets][size-4];
        LinkedList<byte[]> allBytes = new LinkedList<>();

        int k = 0;
        while (true) {
            receivedBytes = new byte[size];
            receivePacket = new DatagramPacket(receivedBytes, size);
            socket.receive(receivePacket);
            allBytes.add(receivePacket.getData());
            k++;
            if (k == packets)
                break;
        }
        System.out.println("all packets received");

        for (int i = 0; i < allBytes.size(); i++) {
            byte[] currentBytes = allBytes.get(i);
            byte[] seq = new byte[] {currentBytes[0], currentBytes[1], currentBytes[2], currentBytes[3]};
            int seqnum = byteArrayToInt(seq);

            System.out.printf("received packet with seqnum %d\n", seqnum);
            for (int j = 0; j < size-4; j++) {
                finFile[seqnum][j] = currentBytes[j+4];
            }
        }

        k = 0;
        for (int i = 0; i < packets; i++) {
            for (int j = 0; j < size-4; j++) {
                if (k < fileLength) {
                    fin[k] = finFile[i][j];
                    k++;
                }
                else
                    break;
            }
        }

        System.out.println("writing fin to file");
        FileOutputStream fos = new FileOutputStream(new File(currentFileName));
        fos.write(fin);
        fos.close();
        System.out.println("closed fos");

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

    public static int byteArrayToInt(byte[] b) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i] & 0x000000FF) << shift;
        }
        return value;
    }
}

