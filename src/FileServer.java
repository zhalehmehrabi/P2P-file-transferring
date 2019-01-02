import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.io.File;
import java.util.Arrays;

public class FileServer extends Thread {
    DatagramSocket socket;
    String fileName, filePath;
    InetAddress receiverIP;
    int receiverPort;
    int size = 60000;

    public FileServer(String fileName, String filePath, InetAddress receiverIP, int receiverPort) throws SocketException {
        this.fileName = fileName;
        this.filePath = filePath;
        this.receiverIP = receiverIP;
        this.receiverPort = receiverPort;
        socket = new DatagramSocket();
    }

    @Override
    public void run() {
        try {

            File file = new File(filePath);
            byte[] fileBytes = readContentIntoByteArray(file);
            int fileLength = fileBytes.length;
            int packets = 1 + (fileLength / (size - 4));
            System.out.printf("packets no: %d\n", packets);


            String response = Integer.toString(packets) + " " + Integer.toString(size) + " " + Integer.toString(fileLength);
            DatagramPacket sendPacket = new DatagramPacket(response.getBytes(), response.getBytes().length, receiverIP, receiverPort);
            socket.send(sendPacket);


            byte[][] finFile = new byte[packets][size-4];
            int start = 0;
            for (int i = 0; i < packets; i++) {
                finFile[i] = Arrays.copyOfRange(fileBytes, start, start + size-4);
                start = start + size-4;
            }


            for (int i = 0; i < packets; i++) {
                byte[] currentPacket = new byte[size];
                byte[] seqnum = intToByteArray(i);
                currentPacket[0] = seqnum[0];
                currentPacket[1] = seqnum[1];
                currentPacket[2] = seqnum[2];
                currentPacket[3] = seqnum[3];

                for (int j = 0; j < size-4; j++) {
                    currentPacket[j+4] = finFile[i][j];
                }

                sendPacket = new DatagramPacket(currentPacket, currentPacket.length, receiverIP, receiverPort);
                socket.send(sendPacket);
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("closing socket");
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] readContentIntoByteArray(File file) {
        FileInputStream fileInputStream = null;
        byte[] bFile = new byte[(int) file.length()];
        try
        {
            //convert file into array of bytes
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bFile);
            fileInputStream.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return bFile;
    }

    public static byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }
}
