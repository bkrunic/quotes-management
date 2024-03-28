import java.io.*;
import java.net.*;
import java.util.*;

public class MainService {
    private static final int PORT = 8080;
    private static final String HELPER_SERVICE_ADDRESS = "localhost";
    private static final int HELPER_SERVICE_PORT = 9090;

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Main service is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                String requestLine = in.readLine();
                String[] requestParts = requestLine.split("\\s+");

                if (requestParts.length >= 2) {
                    String method = requestParts[0];
                    String path = requestParts[1];

                    if (method.equals("GET") && path.equals("/quotes")) {
                        handleQuotesRequest(in, out);
                    } else if (method.equals("POST") && path.equals("/save-quote")) {
                        // Extract the request body for POST requests
                        StringBuilder requestBody = new StringBuilder();
                        String line;
                        while ((line = in.readLine()) != null && !line.isEmpty()) {
                            requestBody.append(line).append("\r\n");
                        }
                        // Extract quote and author from the request body
                        String[] formData = requestBody.toString().split("&");
                        String quote = null;
                        String author = null;
                        for (String data : formData) {
                            String[] keyValue = data.split("=");
                            if (keyValue.length == 2) {
                                String key = URLDecoder.decode(keyValue[0], "UTF-8");
                                String value = URLDecoder.decode(keyValue[1], "UTF-8");
                                if (key.equals("quote")) {
                                    quote = value;
                                } else if (key.equals("author")) {
                                    author = value;
                                }
                            }
                        }

                        // Handle saving the quote
                        handleSaveQuoteRequest(in, out, quote, author);
                    }
                }

                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleQuotesRequest(BufferedReader in, PrintWriter out) {
        try {
            // Read the request body line by line
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                requestBody.append(line).append("\r\n");
            }

            // Parse the form data to extract the quote and author
            String[] lines = requestBody.toString().split("\r\n");
            String quote = null;
            String author = null;
            for (String data : lines) {
                if (data.startsWith("quote=")) {
                    quote = data.substring(6); // Remove "quote=" prefix
                } else if (data.startsWith("author=")) {
                    author = data.substring(7); // Remove "author=" prefix
                }
            }

            // Trim leading/trailing whitespaces
            if (quote != null) {
                quote = quote.trim();
            }
            if (author != null) {
                author = author.trim();
            }

            // Generate the HTML response with the quote and author values
            String htmlResponse = "<html>\n" +
                    "<head>\n" +
                    "<title>Form for entering a new quote</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<h1>Form for entering a new quote</h1>\n" +
                    "<form action=\"/save-quote\" method=\"post\">\n" +
                    "<label for=\"quote\">Quote:</label><br>\n" +
                    "<textarea id=\"quote\" name=\"quote\" rows=\"4\" cols=\"50\">" + (quote != null ? quote : "")
                    + "</textarea><br><br>\n" +
                    "<label for=\"author\">Author:</label><br>\n" +
                    "<input type=\"text\" id=\"author\" name=\"author\" value=\"" + (author != null ? author : "")
                    + "\"><br><br>\n" +
                    "<input type=\"submit\" value=\"Save Quote\">\n" +
                    "</form>\n" +
                    "<hr>\n" +
                    "<p>Quote of the day: <i>Citat dana</i></p>\n" +
                    "<p>List of saved quotes:</p>\n" +
                    "<ul>\n" +
                    "<li>Quote 1</li>\n" +
                    "<li>Quote 2</li>\n" +
                    "</ul>\n" +
                    "</body>\n" +
                    "</html>";

            // Send the HTTP response
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/html");
            out.println();
            out.println(htmlResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleSaveQuoteRequest(BufferedReader in, PrintWriter out, String quote, String author) {
        try {
            // Save the new quote and author
            System.out.println("New quote: " + quote);
            System.out.println("Author: " + author);

            // Send a response back to the client indicating successful saving of the quote
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/html");
            out.println();
            out.println("<html><body><h1>Quote saved successfully!</h1></body></html>");
        } catch (Exception e) {
            e.printStackTrace();
            // Send an error response back to the client if there is an exception
            out.println("HTTP/1.1 500 Internal Server Error");
            out.println("Content-Type: text/html");
            out.println();
            out.println("<html><body><h1>Error saving quote!</h1></body></html>");
        }
    }

}
