import java.io.*;
import java.net.*;
import java.util.*;

public class HelperService {
    private static final int PORT = 9090;
    private static final String[] QUOTES = {
        "The only way to do great work is to love what you do. - Steve Jobs",
        "Innovation distinguishes between a leader and a follower. - Steve Jobs",
        "Life is what happens when you're busy making other plans. - John Lennon",
        "The only impossible journey is the one you never begin. - Tony Robbins",
        "The greatest glory in living lies not in never falling, but in rising every time we fall. - Nelson Mandela"
    };

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Helper service is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New request from main service: " + clientSocket);

                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                // Send a random quote from the QUOTES array
                sendRandomQuote(out);

                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendRandomQuote(PrintWriter out) {
        // Choose a random quote from the array of quotes
        Random random = new Random();
        String randomQuote = QUOTES[random.nextInt(QUOTES.length)];

        // Send the random quote as JSON response
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: application/json");
        out.println();
        out.println("{ \"quote\": \"" + randomQuote + "\" }");
    }
}
