import java.io.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class QuoteOfTheDayService {
    private static final int PORT = 9090;
    private static final String[] QUOTES = {
            "Be the change that you wish to see in the world. - Mahatma Gandhi",
            "The only way to do great work is to love what you do. - Steve Jobs",
            "Be brave. Take risks. Nothing can substitute experience. - Paulo Coelho",
            "Life is either a daring adventure or nothing at all. - Helen Keller",
            "Learning without thought is labor lost; thought without learning is perilous. - Confucius"
    };

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Quote of the Day Service running on port " + PORT);

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    int quoteIndex = new Random().nextInt(QUOTES.length);
                    String selectedQuote = QUOTES[quoteIndex];

                    // Formiranje JSON odgovora
                    String jsonResponse = String.format("{\"quote\":\"%s\"}", selectedQuote.replace("\"", "\\\""));
                    out.println(jsonResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}