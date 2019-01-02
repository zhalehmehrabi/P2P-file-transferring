import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Main {
    static Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) throws IOException {
        System.out.println("Request or Serve? (R/S)");
        String input = scanner.nextLine();
        if (input.equalsIgnoreCase("R") || input.equalsIgnoreCase("Request")) {
            System.out.println("Enter your commands...");
            runClient();
        }

        else if (input.equalsIgnoreCase("S") || input.equalsIgnoreCase("Serve")) {
            System.out.println("Enter your commands...");
            runServer();
        }
    }

    public static void runServer() {
        Server server = new Server();
        server.start();
        while (true) {
            String input = scanner.nextLine();
            if (input.startsWith("p2p -serve -name") && input.contains("-path")) {
                String[] parsed = input.split(" ");
                server.addFile(parsed[3], parsed[5]);
            }
        }
    }

    public static void runClient() throws IOException {
        Client client = new Client();
        client.run();
    }
}
